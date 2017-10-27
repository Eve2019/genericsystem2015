package org.genericsystem.cv.classifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.genericsystem.cv.Img;
import org.genericsystem.cv.utils.ParallelTasks;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.utils.Converters;

public class Fields extends AbstractFields<Field> {

	private Mat perspectiveHomographyInv;
	private Mat homographyFromStabilized;

	private static ThreadLocalRandom rand = ThreadLocalRandom.current();
	private static final int MAX_DELETE_UNMERGED = 2;
	private static final int OCR_TIMEOUT = 100;

	public void draw(Img stabilized) {
		draw(stabilized, getFullHomography());
	}

	public void draw(Img stabilized, Mat homography) {
		// List<Field> oldFields = restabilizeFields(homography);
		fields.forEach(f -> f.draw(stabilized, new Scalar(0, 0, 255)));
		fields.forEach(f -> f.draw(stabilized, new Scalar(0, 255, 0)));

		if (homographyFromStabilized != null) {
			Iterator<Field> it = fields.iterator();
			while (it.hasNext()) {
				Field currentOldField = it.next();
				// List<Field> matches = (List) findMatchingFieldsWithConfidence(currentOldField, 0.7);
				List<Field> matches = findClusteredFields(currentOldField, 0.1);
				matches.forEach(f -> f.draw(stabilized, new Scalar(255, 0, 0)));
			}
		}
	}

	public void merge(List<Rect> newRects) {
		List<Field> oldFields = restabilizeFields(getFullHomography());
		System.out.println("oldFields transformed (" + oldFields.size() + ")");
		fields = newRects.stream().map(Field::new).collect(Collectors.toList());

		if (homographyFromStabilized != null) {
			Iterator<Field> it = oldFields.iterator();
			while (it.hasNext()) {
				Field currentOldField = it.next();
				// List<Field> matches = (List) findMatchingFieldsWithConfidence(currentOldField, 0.7);
				List<Field> matches = findClusteredFields(currentOldField, 0.1);

				if (!matches.isEmpty()) {
					currentOldField.getConsolidated().ifPresent(s -> System.out.println("Merged: " + s));
					matches.forEach(f -> {
						f.merge(currentOldField);
						f.resetDeadCounter();
					});
					it.remove();
				} else {
					System.out.print(".");
				}
			}
			// Increment the deadCounter in old fields that were not merged
			oldFields.forEach(f -> f.incrementDeadCounter());
			oldFields.removeIf(f -> f.deadCounter >= MAX_DELETE_UNMERGED);
			// At this stage, add all the remaining fields still in oldFields
			fields.addAll(oldFields);
		}
	}

	private List<Field> restabilizeFields(Mat homography) {
		// Apply the homography to the oldFields
		List<Rect> virtualRects = fields.stream().map(AbstractField::getRect).map(rect -> findNewRect(rect, homography)).collect(Collectors.toList());
		return IntStream.range(0, fields.size()).mapToObj(i -> {
			Field f = new Field(virtualRects.get(i));
			f.merge(fields.get(i));
			return f;
		}).collect(Collectors.toList());
	}

	private Rect findNewRect(Rect rect, Mat homography) {
		List<Point> points = restabilize(Arrays.asList(rect.tl(), rect.br()), homography);
		return new Rect(points.get(0), points.get(1));
	}

	private List<Point> restabilize(List<Point> originals, Mat homography) {
		Mat original = Converters.vector_Point2f_to_Mat(originals);
		MatOfPoint2f results = new MatOfPoint2f();
		Core.perspectiveTransform(original, results, homography);
		List<Point> res = results.toList();
		return res;
	}

	public void storePerspectiveHomographyInv(Mat perspectiveHomographyInv) {
		this.perspectiveHomographyInv = perspectiveHomographyInv;
	}

	public void storeHomographyFromStabilized(Mat homographyFromStabilized) {
		this.homographyFromStabilized = homographyFromStabilized;
	}

	public void updateFieldsWithHomography(Mat homography) {
		fields = restabilizeFields(homography);
	}

	public Mat getFullHomography() {
		Mat fullHomography = new Mat();
		Core.gemm(homographyFromStabilized, perspectiveHomographyInv, 1, new Mat(), 0, fullHomography);
		return fullHomography;
	}

	@Override
	public void consolidateOcr(Img rootImg) {
		long TS = System.currentTimeMillis();
		while (System.currentTimeMillis() - TS <= OCR_TIMEOUT) {
			ParallelTasks tasks = new ParallelTasks();
			Set<Integer> indexes = new HashSet<>(tasks.getCounter());
			while (indexes.size() < tasks.getCounter()) {
				int idx = rand.nextInt(size());
				if (indexes.add(idx))
					tasks.add(() -> fields.get(idx).ocr(rootImg));
			}
			try {
				tasks.run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}