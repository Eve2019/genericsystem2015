package org.genericsystem.cv.retriever;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.genericsystem.cv.AbstractApp;
import org.genericsystem.cv.Calibrated.AngleCalibrated;
import org.genericsystem.cv.Img;
import org.genericsystem.cv.lm.LevenbergImpl;
import org.genericsystem.cv.utils.Line;
import org.genericsystem.cv.utils.NativeLibraryLoader;
import org.genericsystem.cv.utils.Tools;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

@SuppressWarnings({ "resource" })
public class CamLiveRetriever extends AbstractApp {

	public static enum DeperspectivationMode {
		NONE, ROTATION, FULL
	}

	static {
		NativeLibraryLoader.load();
	}

	static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static long counter = 0;

	private static final int STABILIZATION_DELAY = 500;
	private static final int FRAME_DELAY = 100;

	private final ScheduledExecutorService timerFields = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardPolicy());
	private final Fields fields = new Fields();
	private int recoveringCounter = 0;

	private ImgDescriptor stabilizedImgDescriptor;
	private ImgDescriptor deperspectivedImgDescriptor;
	private final VideoCapture capture = new VideoCapture(0);
	private Mat frame = new Mat();
	private boolean stabilizationHasChanged = true;
	private int stabilizationErrors = 0;
	// private double[] vp1 = new double[]{5000, 0,1};
	// private AngleCalibrated calibrated;
	private AngleCalibrated calibrated0;

	private DescriptorManager descriptorManager = new DescriptorManager();
	private Mat deperspectiveHomography = new Mat();

	private final double f = 6.053 / 0.009;
	private boolean stabilizedMode = false;
	private boolean textsEnabledMode = false;
	private Lines lines;
	private Img display;
	private Img savedDisplay = null;
	protected DeperspectivationMode mode = DeperspectivationMode.FULL;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		timerFields.shutdown();
		timerFields.awaitTermination(5, TimeUnit.SECONDS);
	}

	@Override
	protected void fillGrid(GridPane mainGrid) {

		capture.read(frame);

		double[] pp = new double[] { frame.width() / 2, frame.height() / 2 };
		calibrated0 = new AngleCalibrated(0, Math.PI / 2);

		ImageView src0 = new ImageView(Tools.mat2jfxImage(frame));
		mainGrid.add(src0, 0, 0);

		ImageView src1 = new ImageView(Tools.mat2jfxImage(frame));
		mainGrid.add(src1, 1, 0);

		ImageView src2 = new ImageView(Tools.mat2jfxImage(frame));
		mainGrid.add(src2, 1, 1);

		timerFields.scheduleAtFixedRate(() -> onSpace(), 0, STABILIZATION_DELAY, TimeUnit.MILLISECONDS);

		Img display = new Img(frame, false);
		timerFields.scheduleAtFixedRate(() -> {
			try {
				Stats.beginTask("frame");
				capture.read(frame);
				if (frame == null) {
					logger.warn("No frame !");
					return;
				}

				Stats.beginTask("deperspectivation");
				Mat deperspectivGraphy = computeDeperspectivedHomography(frame, pp, f, mode);
				Stats.endTask("deperspectivation");
				if (deperspectivGraphy != null) {
					descriptorManager.setFrame(frame);
					deperspectiveHomography = deperspectivGraphy;
					if (stabilizedImgDescriptor == null) {
						stabilizedImgDescriptor = new ImgDescriptor(frame, deperspectivGraphy);
						return;
					}
					if (stabilizationHasChanged && stabilizationErrors > 20) {
						fields.reset();
						stabilizationErrors = 0;
						stabilizedImgDescriptor = new ImgDescriptor(frame, deperspectivGraphy);
						// deperspectivedImgDescriptor = stabilizedImgDescriptor;
						return;
					}

					Stats.beginTask("get img descriptors");
					ImgDescriptor newImgDescriptor = new ImgDescriptor(frame, deperspectivGraphy);

					deperspectivedImgDescriptor = newImgDescriptor;

					Stats.endTask("get img descriptors");
					Stats.beginTask("stabilization homography");
					Mat betweenStabilizedHomography = stabilizedImgDescriptor.computeStabilizationGraphy(newImgDescriptor);

					// displayMat(betweenStabilizedHomography);
					Stats.endTask("stabilization homography");
					if (betweenStabilizedHomography != null) {
						stabilizationErrors = 0;

						// computeDistanceBetweenStabilized(betweenStabilizedHomography);

						Mat stabilizationHomographyFromFrame = new Mat();
						Core.gemm(betweenStabilizedHomography.inv(), deperspectivGraphy, 1, new Mat(), 0, stabilizationHomographyFromFrame);
						Img stabilized = warpPerspective(frame, stabilizationHomographyFromFrame);
						Img stabilizedDisplay = new Img(stabilized.getSrc(), true);
						if (stabilizationHasChanged && recoveringCounter == 0) {
							Stats.beginTask("stabilizationHasChanged");
							stabilized = newImgDescriptor.getDeperspectivedImg();
							stabilizedDisplay = new Img(stabilized.getSrc(), true);
							Stats.beginTask("restabilizeFields");
							fields.restabilizeFields(betweenStabilizedHomography);
							Stats.endTask("restabilizeFields");
							stabilizedImgDescriptor = newImgDescriptor;
							stabilizationHomographyFromFrame = deperspectivGraphy;
							stabilizationHasChanged = false;
							Stats.endTask("stabilizationHasChanged");
						}
						Stats.beginTask("consolidate fields");
						fields.consolidate(stabilizedDisplay);
						Stats.endTask("consolidate fields");
						Stats.beginTask("performOcr");
						fields.performOcr(stabilized);
						Stats.endTask("performOcr");
						Img stabilizedDebug = new Img(stabilizedDisplay.getSrc(), true);
						Stats.beginTask("draw");
						fields.drawFieldsOnStabilizedDebug(stabilizedDebug);
						fields.drawOcrPerspectiveInverse(display, stabilizationHomographyFromFrame.inv(), 1);
						fields.drawFieldsOnStabilized(stabilizedDisplay);
						Stats.endTask("draw");

						Image stabilizedDisplayImage = stabilizedDisplay.toJfxImage();
						if (savedDisplay == null)
							savedDisplay = stabilizedDisplay;
						Platform.runLater(() -> src2.setImage(savedDisplay.toJfxImage()));
						Platform.runLater(() -> src1.setImage(stabilizedDisplayImage));
						if (++counter % 20 == 0) {
							System.out.println(Stats.getStatsAndReset());
							counter = 0;
						}
					} else {
						stabilizationErrors++;
						logger.warn("Unable to compute a valid stabilization ({} times)", stabilizationErrors);
					}
				}

				Image displayImage = display.toJfxImage();

				Platform.runLater(() -> src0.setImage(displayImage));

			} catch (Throwable e) {
				logger.warn("Exception while computing layout.", e);
			} finally {
				Stats.endTask("frame");
			}
		}, 100, FRAME_DELAY, TimeUnit.MILLISECONDS);
	}

	private Mat computeDeperspectivedHomography(Mat frame, double[] pp, double f, DeperspectivationMode mode) {
		if (!stabilizedMode) {
			capture.read(frame);
		}
		if (DeperspectivationMode.NONE == mode)
			return Mat.eye(3, 3, CvType.CV_64FC1);
		display = new Img(frame, true);
		List<Line> addedLines = null;
		if (textsEnabledMode) {
			Mat diffFrame = getDiffFrame(frame);
			List<Circle> circles = detectCircles(frame, diffFrame, 30, 100);
			Collection<Circle> selectedCircles = selectRandomCirles(circles, 20);
			addedLines = new ArrayList<>();
			for (Circle circle : selectedCircles) {
				Img circledImg = getCircledImg(frame, (int) circle.radius, circle.center);
				double angle = getBestAngle(circledImg, 42, 12, 5, 180, null) / 180 * Math.PI;
				addedLines.add(buildLine(frame, circle.center, angle, circle.radius));
				Imgproc.circle(display.getSrc(), circle.center, (int) circle.radius, new Scalar(0, 255, 0), 1);
			}
		}
		Mat diffFrame = new Mat();
		Core.absdiff(frame, new Scalar(255), diffFrame);
		Img grad = new Img(diffFrame, false).adaptativeGaussianInvThreshold(5, 3).morphologyEx(Imgproc.MORPH_CLOSE, Imgproc.MORPH_ELLIPSE, new Size(10, 10)).morphologyEx(Imgproc.MORPH_GRADIENT, Imgproc.MORPH_ELLIPSE, new Size(3, 3));
		lines = new Lines(grad.houghLinesP(1, Math.PI / 180, 10, 40, 10));
		if (addedLines != null)
			lines.lines.addAll(addedLines);
		if (lines.size() > 4) {
			double[] thetaPhi = new LevenbergImpl<>((line, params) -> distance(new AngleCalibrated(params).uncalibrate(pp, f), line), lines.lines, calibrated0.getThetaPhi()).getParams();
			calibrated0 = calibrated0.dumpThetaPhi(thetaPhi, 1);
			AngleCalibrated[] result = findOtherVps(calibrated0, lines, pp, f);
			return findHomography(frame.size(), result, pp, f);
		} else {
			System.out.println("Not enough lines : " + lines.size());
			return null;
		}
	}

	public static AngleCalibrated[] findOtherVps(AngleCalibrated calibrated0, Lines lines, double[] pp, double f) {
		AngleCalibrated[] result = new AngleCalibrated[] { null, null, null };
		double bestError = Double.MAX_VALUE;
		for (double angle = 0; angle < 360 / 180 * Math.PI; angle += 1 * Math.PI / 180) {
			double error = 0;
			AngleCalibrated calibratexy = calibrated0.getOrthoFromAngle(angle);
			AngleCalibrated calibratez = calibrated0.getOrthoFromVps(calibratexy);
			if (calibratexy.getPhi() < calibratez.getPhi()) {
				AngleCalibrated tmp = calibratexy;
				calibratexy = calibratez;
				calibratez = tmp;
			}
			double[] uncalibrate = calibratexy.uncalibrate(pp, f);
			for (Line line : lines.lines)
				error += distance(uncalibrate, line);
			if (error < bestError) {
				bestError = error;
				result[0] = calibrated0;
				result[1] = calibratexy;
				result[2] = calibratez;
			}
		}

		double theta0 = Math.abs(result[0].getTheta()) % Math.PI;
		theta0 = Math.min(Math.PI - theta0, theta0);

		double theta1 = Math.abs(result[1].getTheta()) % Math.PI;
		theta1 = Math.min(Math.PI - theta1, theta1);

		if (theta0 > theta1) {
			AngleCalibrated tmp = result[0];
			result[0] = result[1];
			result[1] = tmp;
		}
		return result;
	}

	private static double distance(double[] vp, Line line) {
		double dy = line.y1 - line.y2;
		double dx = line.x2 - line.x1;
		double dz = line.y1 * line.x2 - line.x1 * line.y2;
		double norm = Math.sqrt(dy * dy + dx * dx + dz * dz);
		double n0 = -dx / norm;
		double n1 = dy / norm;
		double nNorm = Math.sqrt(n0 * n0 + n1 * n1);
		double[] midPoint = new double[] { (line.x1 + line.x2) / 2, (line.y1 + line.y2) / 2, 1d };
		double r0 = vp[1] * midPoint[2] - midPoint[1];
		double r1 = midPoint[0] - vp[0] * midPoint[2];
		double rNorm = Math.sqrt(r0 * r0 + r1 * r1);
		double num = r0 * n0 + r1 * n1;
		if (num < 0)
			num = -num;
		double d = 0;
		if (nNorm != 0 && rNorm != 0)
			d = num / (nNorm * rNorm);
		return d < 0.4 ? d : 0.4;
	}

	public static Mat findHomography(Size size, AngleCalibrated[] calibrateds, double[] pp, double f) {

		double[][] vps = new double[][] { calibrateds[0].getCalibratexyz(), calibrateds[1].getCalibratexyz(), calibrateds[2].getCalibratexyz() };

		double[][] vps2D = getVp2DFromVps(vps, pp, f);
		// System.out.println("vps2D : " + Arrays.deepToString(vps2D));
		//
		// System.out.println("vp1 " + calibrateds[0]);
		// System.out.println("vp2 " + calibrateds[1]);
		// System.out.println("vp3 " + calibrateds[2]);

		double theta = calibrateds[0].getTheta();
		double theta2 = calibrateds[1].getTheta();
		double x = size.width / 6;

		double[] A = new double[] { size.width / 2, size.height / 2, 1 };
		double[] B = new double[] { size.width / 2 + (Math.cos(theta) < 0 ? -x : x), size.height / 2 };
		double[] D = new double[] { size.width / 2, size.height / 2 + (Math.sin(theta2) < 0 ? -x : x), 1 };
		double[] C = new double[] { size.width / 2 + (Math.cos(theta) < 0 ? -x : x), size.height / 2 + (Math.sin(theta2) < 0 ? -x : x) };

		double[] A_ = A;
		double[] B_ = new double[] { size.width / 2 + x * vps[0][0], size.height / 2 + x * vps[0][1], 1 };
		double[] D_ = new double[] { size.width / 2 + x * vps[1][0], size.height / 2 + x * vps[1][1], 1 };
		double[] C_ = cross2D(cross(B_, vps2D[1]), cross(D_, vps2D[0]));

		return Imgproc.getPerspectiveTransform(new MatOfPoint2f(new Point(A_), new Point(B_), new Point(C_), new Point(D_)), new MatOfPoint2f(new Point(A), new Point(B), new Point(C), new Point(D)));
	}

	private Mat getDiffFrame(Mat frame) {
		Mat result = new Mat();
		Imgproc.cvtColor(frame, result, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(result, result, new Size(3, 3), 0);
		Mat diffFrame = new Mat();
		Core.absdiff(result, new Scalar(255), diffFrame);
		Imgproc.adaptiveThreshold(diffFrame, diffFrame, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 7, 3);
		return diffFrame;
	}

	private Collection<Circle> selectRandomCirles(List<Circle> circles, int circlesNumber) {
		if (circles.size() <= circlesNumber)
			return circles;
		Set<Circle> result = new HashSet<>();
		while (result.size() < circlesNumber)
			result.add(circles.get((int) (Math.random() * circles.size())));
		return result;
	}

	private List<Circle> detectCircles(Mat frame, Mat diffFrame, int minRadius, int maxRadius) {
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(diffFrame, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		List<Circle> circles = new ArrayList<>();
		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint contour = contours.get(i);
			double contourarea = Imgproc.contourArea(contour);
			if (contourarea > 50) {
				float[] radius = new float[1];
				Point center = new Point();
				MatOfPoint2f contour2F = new MatOfPoint2f(contour.toArray());
				Imgproc.minEnclosingCircle(contour2F, center, radius);
				if (radius[0] > minRadius && radius[0] < maxRadius && center.x > radius[0] && center.y > radius[0] && ((center.x + radius[0]) < frame.width()) && ((center.y + radius[0]) < frame.height())) {
					circles.add(new Circle(center, radius[0]));
					// Imgproc.circle(frame, center, (int) radius[0], new Scalar(0, 0, 255));
				}
				// Imgproc.drawContours(frame, Arrays.asList(contour), 0, new Scalar(0, 255, 0), 1);
			}
		}
		return circles;
	}

	private static class Circle {
		public Circle(Point center, float radius) {
			this.center = center;
			this.radius = radius;
		}

		Point center;
		float radius;
	}

	public Img getCircledImg(Mat frame, int radius, Point center) {
		Mat mask = new Mat(new Size(radius * 2, radius * 2), CvType.CV_8UC1, new Scalar(0));
		Imgproc.circle(mask, new Point(radius, radius), radius, new Scalar(255), -1);
		Rect rect = new Rect(new Point(center.x - radius, center.y - radius), new Point(center.x + radius, center.y + radius));
		Mat roi = new Img(new Mat(frame, rect), true).bilateralFilter().adaptativeGaussianInvThreshold(3, 3).getSrc();
		Mat circled = new Mat();
		roi.copyTo(circled, mask);
		Img circledImg = new Img(circled, false);
		return circledImg;
	}

	public Line buildLine(Mat mat, Point center, double angle, double size) {
		double x1 = center.x - Math.sin(angle) * size;
		double y1 = center.y + Math.cos(angle) * size;
		double x2 = center.x + Math.sin(angle) * size;
		double y2 = center.y - Math.cos(angle) * size;
		return new Line(new Point(x1, y1), new Point(x2, y2));
	}

	public double getBestAngle(Img circledImg, int absMinMax, double step, int filterSize, double threshold, Img[] binarized) {
		double maxScore = 0;
		double bestAngle = -1;
		if (binarized != null)
			binarized[0] = new Img(new Mat(new Size(2 * absMinMax * 10, 200), CvType.CV_8UC1, new Scalar(0)), false);
		List<double[]> results = new ArrayList<>();
		for (double angle = -absMinMax; angle <= absMinMax; angle += step) {
			double score = score(circledImg, angle, filterSize, threshold);
			if (angle != 0 && score > maxScore) {
				maxScore = score;
				bestAngle = angle;
			}
			if (angle != 0)
				results.add(new double[] { angle, score });
			// System.out.println(score);
			if (binarized != null)
				new Line((absMinMax + angle) * 10, 0, (absMinMax + angle) * 10, score / 1000).draw(binarized[0].getSrc(), new Scalar(255, 0, 0), 1);
		}
		BiFunction<Double, double[], Double> f = (x, params) -> params[0] * x * x * x * x + params[1] * x * x * x + params[2] * x * x + params[3] * x + params[4];
		BiFunction<double[], double[], Double> e = (xy, params) -> f.apply(xy[0], params) - xy[1];
		double[] result = new LevenbergImpl<>(e, results, new double[] { 1, 1, 1, 1, 1 }).getParams();
		Point point = null;
		double polynomAngle = 0.0;
		double max = 0.0;
		for (double angle = -absMinMax; angle <= absMinMax; angle++) {
			Point oldPoint = point;
			double score = f.apply(angle, result);
			point = new Point((absMinMax + angle) * 10, score / 1000);
			if (score > max) {
				max = score;
				polynomAngle = angle;
			}
			if (binarized != null && oldPoint != null)
				new Line(oldPoint, point).draw(binarized[0].getSrc(), new Scalar(255, 0, 0), 1);
		}
		if (binarized != null) {
			Imgproc.circle(binarized[0].getSrc(), new Point((absMinMax + polynomAngle) * 10, max / 1000), 10, new Scalar(255, 255, 0), 3);
			// new Line(new Point((absMinMax + bestAngle) * 10, maxScore / 1000), new Point((absMinMax + bestAngle) * 10, 0)).draw(binarized[0].getSrc(), new Scalar(255, 255, 0), 3);
		}
		// System.out.println(Arrays.toString(result));

		return polynomAngle;
	}

	public double score(Img circled, double angle, int filterSize, double threshold) {
		Mat M = Imgproc.getRotationMatrix2D(new Point(circled.width() / 2, circled.width() / 2), angle, 1);
		Mat rotated = new Mat();
		Imgproc.warpAffine(circled.getSrc(), rotated, M, new Size(circled.width(), circled.width()));
		Img binarized = new Img(rotated, false).directionalFilter(filterSize).thresHold(threshold, 255, Imgproc.THRESH_BINARY);
		Mat result = new Mat();
		Core.reduce(binarized.getSrc(), result, 1, Core.REDUCE_SUM, CvType.CV_64F);
		Core.reduce(result, result, 0, Core.REDUCE_SUM, CvType.CV_64F);
		return result.get(0, 0)[0];
	}

	@Override
	protected void onSpace() {
		stabilizationHasChanged = true;
	}

	@Override
	protected void onR() {
		fields.reset();
	}

	static Img warpPerspective(Mat frame, Mat homography) {
		Mat dePerspectived = new Mat(frame.size(), CvType.CV_64F, Scalar.all(255));
		Imgproc.warpPerspective(frame, dePerspectived, homography, frame.size(), Imgproc.INTER_LINEAR, Core.BORDER_REPLICATE, Scalar.all(255));
		return new Img(dePerspectived, false);
	}

	static double[] getVpFromVp2D(double[] vpImg, double[] pp, double f) {
		double[] vp = new double[] { vpImg[0] / vpImg[2] - pp[0], vpImg[1] / vpImg[2] - pp[1], f };
		if (vp[2] == 0)
			vp[2] = 0.0011;
		double N = Math.sqrt(vp[0] * vp[0] + vp[1] * vp[1] + vp[2] * vp[2]);
		vp[0] *= 1.0 / N;
		vp[1] *= 1.0 / N;
		vp[2] *= 1.0 / N;
		return vp;
	}

	public static double[][] getVp2DFromVps(double vps[][], double[] pp, double f) {
		double[][] result = new double[2][3];
		for (int i = 0; i < 2; i++) {
			result[i][0] = vps[i][0] * f / vps[i][2] + pp[0];
			result[i][1] = vps[i][1] * f / vps[i][2] + pp[1];
			result[i][2] = 1.0;
		}
		return result;
	}

	static double[] cross(double[] a, double b[]) {
		return new double[] { a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0] };
	}

	static double det(double[] u, double v[], double w[]) {
		return u[0] * v[1] * w[2] + u[2] * v[0] * w[1] + u[1] * v[2] * w[0] - u[2] * v[1] * w[0] - u[1] * v[0] * w[2] - u[0] * v[2] * w[1];
	}

	static double[] cross2D(double[] a, double b[]) {
		return uncalibrate(cross(a, b));
	}

	static double[] uncalibrate(double[] a) {
		return new double[] { a[0] / a[2], a[1] / a[2], 1 };
	}

	public static class Lines extends org.genericsystem.cv.utils.Lines {

		public Lines(Mat src) {
			super(src);
		}

		public Lines(Collection<Line> lines) {
			super(lines);
		}

		public Lines filter(Predicate<Line> predicate) {
			return new Lines(lines.stream().filter(predicate).collect(Collectors.toList()));
		}

		public Lines reduce(int max) {
			if (lines.size() <= max)
				return this;
			Set<Line> newLines = new HashSet<>();
			while (newLines.size() < max)
				newLines.add(lines.get((int) (Math.random() * size())));
			return new Lines(newLines);
		}
	}

	@Override
	protected void onT() {
		textsEnabledMode = !textsEnabledMode;
	}

	@Override
	protected void onS() {
		Img image = descriptorManager.add(deperspectivedImgDescriptor, deperspectiveHomography);
		savedDisplay = image != null ? image : savedDisplay;
	}

}