package org.genericsystem.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.genericsystem.cv.Img;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class Layout {

	private double x1;
	private double x2;
	private double y1;
	private double y2;

	private String label;
	private List<Layout> children = new ArrayList<Layout>();
	private Layout parent = null;

	public Layout(double x1, double x2, double y1, double y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}

	public Img getRoi(Img img) {
		return new Img(img, this);
	}

	// public void draw(Img img, Scalar color, int thickness) {
	// Imgproc.rectangle(img.getSrc(), new Point(x1 * img.width(), y1 * img.height()), new Point(x2 * img.width(), y2 * img.height()), color, thickness);// rect.tl(), rect.br(), color, thickness);
	// }

	public void draw(Img img, Scalar color, int thickness) {
		traverse(img, (roi, shard) -> Imgproc.rectangle(roi.getSrc(), new Point(0, 0), new Point(roi.width() - 1, roi.height() - 1), color, thickness));
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Layout getParent() {
		return parent;
	}

	public void setParent(Layout parent) {
		this.parent = parent;
	}

	public boolean hasChildren() {

		if (children != null && children.size() >= 1)
			return true;

		return false;
	}

	public String recursivToString() {
		StringBuilder sb = new StringBuilder();
		recursivToString(this, sb, 0);
		return sb.toString();
	}

	private void recursivToString(Layout shard, StringBuilder sb, int depth) {
		sb.append("depth : " + depth + " : ");
		sb.append("((" + shard.x1 + "-" + shard.y1 + "),(" + shard.x2 + "-" + shard.y2 + "))".toString());

		if (shard.hasChildren()) {
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

	public static List<Layout> split(double morph, boolean vertical, float concentration, Img binary) {
		// assert 1 / (vertical ? binary.rows() : binary.cols()) < concentration;
		// if ((vertical ? binary.rows() : binary.cols()) <= 4)
		// System.out.println("size too low : " + (vertical ? binary.rows() : binary.cols()));
		List<Float> histo = new ArrayList<>();
		Converters.Mat_to_vector_float((vertical ? binary.projectVertically() : binary.projectHorizontally().transpose()).getSrc(), histo);
		float min = concentration * 255;
		float max = (1 - concentration) * 255;
		for (int i = 1; i < histo.size() - 1; i++) {
			float value = histo.get(i);
			if (value <= min && histo.size() > 32) {
				histo.set(i, 0f);
				// if (vertical)
				// Imgproc.line(src, new Point(0, i), new Point(src.cols(), i), new Scalar(255));
				// else
				// Imgproc.line(src, new Point(i, 0), new Point(i, src.rows()), new Scalar(255));
			}
			if (value >= max && histo.size() > 32) {
				histo.set(i, 0f);
				// if (vertical)
				// Imgproc.line(getSrc(), new Point(0, i), new Point(src.cols(), i), new Scalar(255));
				// else
				// Imgproc.line(getSrc(), new Point(i, 0), new Point(i, src.rows()), new Scalar(255));
			}
		}
		return split(histo, morph, vertical ? binary.cols() : binary.rows(), vertical, concentration);
	}

	private static List<Layout> split(List<Float> histo, double morph, int matSize, boolean vertical, float concentration) {
		int k = new Double(Math.floor(morph * histo.size())).intValue();
		boolean[] closed = new boolean[histo.size()];
		Function<Integer, Boolean> isBlack = i -> histo.get(i) == 0;
		for (int i = 0; i < histo.size() - 1; i++)
			if (!isBlack.apply(i) && isBlack.apply(i + 1)) {
				for (int j = k + 1; j > 0; j--)
					if (i + j < histo.size()) {
						if (!isBlack.apply(i + j)) {
							Arrays.fill(closed, i, i + j + 1, true);
							i += j - 1;
							break;
						}
						closed[i] = !isBlack.apply(i);
					}
			} else
				closed[i] = !isBlack.apply(i);
		if (!closed[histo.size() - 1])
			closed[histo.size() - 1] = !isBlack.apply(histo.size() - 1);
		return extractZones(closed, vertical);
	}

	private static List<Layout> extractZones(boolean[] result, boolean vertical) {
		List<Layout> shards = new ArrayList<>();
		Integer start = result[0] ? 0 : null;
		assert result.length >= 1;
		for (int i = 0; i < result.length - 1; i++)
			if (!result[i] && result[i + 1])
				start = i + 1;
			else if (result[i] && !result[i + 1]) {
				shards.add(vertical ? new Layout(0, 1, Integer.valueOf(start).doubleValue() / result.length, (Integer.valueOf(i).doubleValue() + 1) / result.length)
						: new Layout(Integer.valueOf(start).doubleValue() / result.length, (Integer.valueOf(i).doubleValue() + 1) / result.length, 0, 1));
				start = null;
			}
		if (result[result.length - 1]) {
			shards.add(vertical ? new Layout(0, 1, Integer.valueOf(start).doubleValue() / result.length, Integer.valueOf(result.length).doubleValue() / result.length)
					: new Layout(Integer.valueOf(start).doubleValue() / result.length, Integer.valueOf(result.length).doubleValue() / result.length, 0, 1));
			start = null;
		}
		return shards;
	}

	public Layout traverse(Img img, BiConsumer<Img, Layout> visitor) {
		for (Layout shard : getChildren())
			shard.traverse(shard.getRoi(img), visitor);
		visitor.accept(img, this);
		return this;
	}

	public Layout recursivSplit(Size morph, int level, float concentration, Img img, Img binary) {
		assert img.size().equals(binary.size());
		if (level < 0) {
			Imgproc.rectangle(img.getSrc(), new Point(0, 0), new Point(img.width(), img.height()), new Scalar(255, 0, 0), -1);
			return this;
		}
		boolean vertical = img.size().height > img.size().width;
		List<Layout> shards = split(vertical ? morph.height : morph.width, vertical, concentration, binary);
		shards.removeIf(shard -> (vertical ? (shard.getY2() - shard.getY1()) * img.size().height : (shard.getX2() - shard.getX1()) * img.size().width) < 4);
		if (shards.isEmpty()) {
			Imgproc.rectangle(img.getSrc(), new Point(0, 0), new Point(img.width(), img.height()), new Scalar(0, 0, 255), -1);
			return this;
		}
		if (shards.size() == 1) {
			Layout subShard = shards.iterator().next();
			if (subShard.equiv(this, 0, 0)) {
				shards = split(!vertical ? morph.height : morph.width, !vertical, concentration, binary);
				shards.removeIf(shard -> (!vertical ? (shard.getY2() - shard.getY1()) * img.size().height : (shard.getX2() - shard.getX1()) * img.size().width) < 4);
				if (shards.isEmpty()) {
					Imgproc.rectangle(img.getSrc(), new Point(0, 0), new Point(img.width(), img.height()), new Scalar(0, 0, 255), -1);
					return this;
				}
				if (shards.size() == 1) {
					subShard = shards.iterator().next();
					if (subShard.equiv(new Layout(0, 1, 0, 1), 0, 0)) {
						return this;
					}
				}
			}
		}
		for (Layout shard : shards) {
			shard.recursivSplit(morph, level - 1, concentration, shard.getRoi(img), shard.getRoi(binary));
			this.addChild(shard);
		}
		return this;
	}
}