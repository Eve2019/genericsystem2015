package org.genericsystem.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.genericsystem.cv.Img;
import org.genericsystem.cv.Ocr;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class Layout {

	private double x1;
	private double x2;
	private double y1;
	private double y2;

	private Map<String, Integer> labels = new HashMap<>();
	private List<Layout> children = new ArrayList<>();
	private Layout parent = null;

	public Layout(Layout parent, double x1, double x2, double y1, double y2) {
		this.parent = parent;
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}

	public Img getRoi(Img img) {
		return new Img(img, this);
	}

	public Rect getRect(Img imgRoot) {
		Rect parentRect = getParent() != null ? getParent().getRect(imgRoot) : new Rect(0, 0, imgRoot.width(), imgRoot.height());
		return new Rect(new Point(parentRect.tl().x + parentRect.width * getX1(), parentRect.tl().y + parentRect.height * getY1()), new Point(parentRect.tl().x + parentRect.width * getX2(), parentRect.tl().y + parentRect.height * getY2()));
	}

	public double getSurfaceInPercent(Img imgRoot) {
		double result = 0;
		if (this.getChildren().isEmpty()) {
			result += getRect(imgRoot).area() / (imgRoot.height() * imgRoot.width());
		} else {
			for (Layout child : getChildren()) {
				result += child.getSurfaceInPercent(imgRoot);
			}
		}
		return result;
	}

	public Rect getLargeRect(Img imgRoot, int delta) {
		Rect rect = getRect(imgRoot);
		return new Rect(new Point(rect.tl().x - delta >= 0 ? rect.tl().x : 0, rect.tl().y - delta >= 0 ? rect.tl().y : 0),
				new Point(rect.br().x + delta <= imgRoot.width() ? rect.br().x + delta : imgRoot.width(), rect.br().y + delta <= imgRoot.height() ? rect.br().y + delta : imgRoot.height()));
	}

	public Layout traverse(Img img, BiConsumer<Img, Layout> visitor) {
		for (Layout shard : getChildren())
			shard.traverse(shard.getRoi(img), visitor);
		visitor.accept(img, this);
		return this;
	}

	public void draw(Img img, Scalar color, int thickness) {
		traverse(getRoi(img), (roi, shard) -> {
			if (shard.getChildren().isEmpty())
				Imgproc.rectangle(roi.getSrc(), new Point(0, 0), new Point(roi.width() - 1, roi.height() - 1), color, thickness);
			else
				Imgproc.rectangle(roi.getSrc(), new Point(0, 0), new Point(roi.width() - 1, roi.height() - 1), new Scalar(0, 0, 255), thickness);
		});

	}

	public void drawPerspective(Img img, Mat homography, Scalar color, int thickness) {
		traverse(getRoi(img), (roi, shard) -> {
			if (shard.getChildren().isEmpty()) {
				MatOfPoint2f results = new MatOfPoint2f();
				Rect rect = shard.getRect(img);
				List<Point> points = Arrays.asList(new Point(rect.tl().x, rect.tl().y), new Point(rect.tl().x + roi.width() - 1, rect.tl().y), new Point(rect.tl().x + roi.width() - 1, rect.tl().y + roi.height() - 1),
						new Point(rect.tl().x, rect.tl().y + roi.height() - 1));
				Mat pts = Converters.vector_Point2f_to_Mat(points);
				Core.perspectiveTransform(pts, results, homography);
				Point[] targets = results.toArray();
				Imgproc.line(img.getSrc(), targets[0], targets[1], color, thickness);
				Imgproc.line(img.getSrc(), targets[1], targets[2], color, thickness);
				Imgproc.line(img.getSrc(), targets[2], targets[3], color, thickness);
				Imgproc.line(img.getSrc(), targets[3], targets[0], color, thickness);
			}
		});
	}

	public void ocrTree(Img rootImg, int delta) {
		traverse(rootImg, (root, layout) -> {
			// if (layout.getChildren().isEmpty()) {
			String ocr = Ocr.doWork(new Mat(rootImg.getSrc(), layout.getLargeRect(rootImg, delta)));
			if (!"".equals(ocr)) {
				Integer count = layout.getLabels().get(ocr);
				layout.getLabels().put(ocr, 1 + (count != null ? count : 0));
				// int all = layout.getLabels().values().stream().reduce(0, (i, j) -> i + j);
				// layout.getLabels().entrySet().forEach(entry -> {
				// if (entry.getValue() > all / 10)
				// Imgproc.putText(rootImg.getSrc(), entry.getKey(), layout.getRect(rootImg).tl(), Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 1);
				// });
				// Imgproc.putText(rootImg.getSrc(), layout.getBestLabel(), layout.getRect(rootImg).tl(), Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 1);
				// System.out.println(layout.getBestLabel());
				// }
			}
		});
	}

	private String getBestLabel() {
		String result = "";
		int occurence = 0;
		for (String key : getLabels().keySet()) {
			if (getLabels().get(key) > occurence) {
				result = key;
				occurence = getLabels().get(key);
			}
		}
		return result;
	}

	public void addChild(Layout child) {
		if (!children.contains(child))
			children.add(child);
	}

	public void removeChild(Layout child) {
		if (children.contains(child))
			children.remove(child);
	}

	public boolean equiv(Layout s, double xTolerance, double yTolerance) {

		if (Math.abs(s.x1 - x1) <= xTolerance && Math.abs(s.x2 - x2) <= xTolerance && Math.abs(s.y1 - y1) <= yTolerance && Math.abs(s.y2 - y2) <= yTolerance)
			return true;

		return false;
	}

	public List<Layout> getChildren() {
		return children;
	}

	public double getX1() {
		return x1;
	}

	public void setX1(double x1) {
		this.x1 = x1;
	}

	public double getX2() {
		return x2;
	}

	public void setX2(double x2) {
		this.x2 = x2;
	}

	public double getY1() {
		return y1;
	}

	public void setY1(double y1) {
		this.y1 = y1;
	}

	public double getY2() {
		return y2;
	}

	public void setY2(double y2) {
		this.y2 = y2;
	}

	public Layout getParent() {
		return parent;
	}

	public void setParent(Layout parent) {
		this.parent = parent;
	}

	public Map<String, Integer> getLabels() {
		return labels;
	}

	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	public String recursivToString() {
		StringBuilder sb = new StringBuilder();
		recursivToString(this, sb, 0);
		sb.append("\n");
		return sb.toString();
	}

	private void recursivToString(Layout shard, StringBuilder sb, int depth) {
		sb.append("depth : " + depth + " : ");
		sb.append("((" + shard.x1 + "-" + shard.y1 + "),(" + shard.x2 + "-" + shard.y2 + "))".toString());
		if (!shard.hasChildren())
			sb.append(" : Label : " + shard.labels);
		else {
			depth++;
			for (Layout s : shard.getChildren()) {
				sb.append("\n");
				for (int i = 0; i < depth; i++)
					sb.append("    ");
				recursivToString(s, sb, depth);
			}
		}
	}

	@Override
	public String toString() {
		return "tl : (" + this.x1 + "," + this.y1 + "), br :(" + this.x2 + "," + this.y2 + ")";
	}

	public Layout tighten(Img binary) {

		List<Float> Vhist = new ArrayList<>();
		List<Float> Hhist = new ArrayList<>();

		Converters.Mat_to_vector_float(binary.projectVertically().getSrc(), Vhist);
		Converters.Mat_to_vector_float(binary.projectHorizontally().transpose().getSrc(), Hhist);

		for (int i = 0; i < binary.getSrc().rows(); i++) {
			for (int j = 0; j < binary.getSrc().cols(); j++) {
				double value = binary.getSrc().get(i, j)[0];
				if (value > 255.0)
					System.out.println(value);
			}
		}
		// Vhist = smoothHisto(concentration, Vhist, true, binary);
		// Hhist = smoothHisto(concentration, Hhist, false, binary);

		double[] x = getHistoLimits(Hhist);
		double[] y = getHistoLimits(Vhist);

		if (x[0] <= x[1] || y[0] <= y[1]) {
			return new Layout(this.getParent(), getX1() + x[0] * (getX2() - getX1()), getX1() + x[1] * (getX2() - getX1()), getY1() + y[0] * (getY2() - getY1()), getY1() + y[1] * (getY2() - getY1()));
		} else {
			return new Layout(this.getParent(), 0, 0, 0, 0);
		}

	}

	public static double[] getHistoLimits(List<Float> hist) {
		int start = 0;
		int end = hist.size() - 1;
		while (start < hist.size() && hist.get(start) >= 255.0)
			start++;
		while (end >= 0 && hist.get(end) >= 255.0)
			end--;
		return new double[] { Integer.valueOf(start).doubleValue() / hist.size(), Integer.valueOf(end + 1).doubleValue() / hist.size() };
	}

	public List<Layout> split(Size morph, Img binary) {
		List<Float> histoVertical = new ArrayList<>();
		List<Float> histoHorizontal = new ArrayList<>();

		Converters.Mat_to_vector_float(binary.projectVertically().getSrc(), histoVertical);
		Converters.Mat_to_vector_float((binary.projectHorizontally().transpose()).getSrc(), histoHorizontal);

		// histoVertical = smoothHisto(concentration, histoVertical, true, binary);
		// histoHorizontal = smoothHisto(concentration, histoHorizontal, false, binary);

		int kV = new Double(Math.floor(morph.height * histoVertical.size())).intValue();
		int kH = new Double(Math.floor(morph.width * histoHorizontal.size())).intValue();
		boolean[] closedV = getClosedFromHisto(kV, histoVertical);
		boolean[] closedH = getClosedFromHisto(kH, histoHorizontal);
		return extractZones(closedV, closedH, binary);
	}

	// private static List<Float> smoothHisto(float concentration, List<Float> histo, boolean vertical, Img binary) {
	// float min = concentration * 255;
	// float max = (1 - concentration) * 255;
	// for (int i = 0; i < histo.size(); i++) {
	// float value = histo.get(i);
	// if (histo.size() > 180)
	// if (value <= min || value >= max) {
	// histo.set(i, 255f);
	// // System.out.println("mask black " + (vertical ? "line" : "column") + " " + i);
	// if (vertical)
	// Imgproc.line(binary.getSrc(), new Point(0, i), new Point(binary.getSrc().width() - 1, i), new Scalar(255));
	// else
	// Imgproc.line(binary.getSrc(), new Point(i, 0), new Point(i, binary.getSrc().rows() - 1), new Scalar(255));
	// }
	// }
	// return histo;
	// }

	private static boolean[] getClosedFromHisto(int k, List<Float> histo) {

		boolean[] closed = new boolean[histo.size()];
		Function<Integer, Boolean> isWhite = i -> histo.get(i) == 255;
		for (int i = 0; i < histo.size() - 1; i++)
			if (!isWhite.apply(i) && isWhite.apply(i + 1)) {
				for (int j = k + 1; j > 0; j--)
					if (i + j < histo.size()) {
						if (!isWhite.apply(i + j)) {
							Arrays.fill(closed, i, i + j + 1, true);
							i += j - 1;
							break;
						}
						closed[i] = !isWhite.apply(i);
					}
			} else
				closed[i] = !isWhite.apply(i);
		if (!closed[histo.size() - 1])
			closed[histo.size() - 1] = !isWhite.apply(histo.size() - 1);
		return closed;
	}

	private List<Layout> extractZones(boolean[] resultV, boolean[] resultH, Img binary) {

		List<Layout> shardsV = getShards(resultV, true);
		List<Layout> shardsH = getShards(resultH, false);
		List<Layout> shards = new ArrayList<>();
		for (Layout shardv : shardsV)
			for (Layout shardh : shardsH) {
				Layout target = new Layout(this, shardh.x1, shardh.x2, shardv.y1, shardv.y2);
				Img roi = target.getRoi(binary);
				if (roi.rows() != 0 && roi.cols() != 0)
					shards.add(target.tighten(roi));
			}
		return shards;
	}

	private List<Layout> getShards(boolean[] result, boolean vertical) {
		List<Layout> shards = new ArrayList<>();
		Integer start = result[0] ? 0 : null;
		assert result.length >= 1;
		for (int i = 0; i < result.length - 1; i++)
			if (!result[i] && result[i + 1])
				start = i + 1;
			else if (result[i] && !result[i + 1]) {
				shards.add(vertical ? new Layout(this, 0, 1, Integer.valueOf(start).doubleValue() / result.length, (Integer.valueOf(i).doubleValue() + 1) / result.length)
						: new Layout(this, Integer.valueOf(start).doubleValue() / result.length, (Integer.valueOf(i).doubleValue() + 1) / result.length, 0, 1));
				start = null;
			}
		if (result[result.length - 1]) {
			shards.add(vertical ? new Layout(this, 0, 1, Integer.valueOf(start).doubleValue() / result.length, Integer.valueOf(result.length).doubleValue() / result.length)
					: new Layout(this, Integer.valueOf(start).doubleValue() / result.length, Integer.valueOf(result.length).doubleValue() / result.length, 0, 1));
			start = null;
		}
		return shards;
	}

	public Layout recursivSplit(Size morph, int level, Img img, Img binary) {
		assert img.size().equals(binary.size());
		if (level <= 0)
			return this;

		List<Layout> shards = split(morph, binary);
		shards.removeIf(shard -> ((shard.getY2() - shard.getY1()) * img.size().height) < 4 || ((shard.getX2() - shard.getX1()) * img.size().width) < 4);
		if (shards.isEmpty())
			return this;

		if (shards.size() == 1)
			return this;

		for (Layout shard : shards) {
			shard.recursivSplit(morph, level - 1, shard.getRoi(img), shard.getRoi(binary));
			this.addChild(shard);
		}
		return this;
	}
}
