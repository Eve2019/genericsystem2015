package org.genericsystem.cv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Zones {
	private final List<Zone> zones;
	private static final ObjectMapper mapper = new ObjectMapper();

	public static Zones get(Img img, double minArea) {
		return new Zones(img.channels() == 1 ? img : img.gray(), minArea);
	}

	public static Zones get(Img img, double minArea, int RETR) {
		return new Zones(img.channels() == 1 ? img : img.gray(), minArea, RETR);
	}

	public static Zones get(Img img, double minArea, double dx, double dy) {
		return new Zones(img.channels() == 1 ? img : img.gray(), minArea).adjust(dx, dy, img.width(), img.height());
	}

	private Zones adjust(double dx, double dy, int width, int height) {
		return new Zones(zones.stream().map(zone -> zone.adjustRect(dx, dy, width, height)).collect(Collectors.toList()));
	}

	public Zones() {
		zones = new ArrayList<>();
	}

	public Zones(List<Zone> zonesList) {
		this.zones = zonesList;
	}

	public Zones(Img gray, double minArea) {
		this(gray, minArea, Imgproc.RETR_EXTERNAL);
	}

	public Zones(Img gray, double minArea, int RETR) {
		this.zones = new ArrayList<>();
		List<MatOfPoint> contours = gray.findContours(new Img[1], RETR, Imgproc.CHAIN_APPROX_SIMPLE);
		int num = 0;
		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint contour = contours.get(i);
			double contourarea = Imgproc.contourArea(contour);
			if (contourarea > minArea) {
				Rect rect = Imgproc.boundingRect(contour);
				if (rect.width >= rect.height) {
					zones.add(new Zone(num, rect));
					num++;
				}
			}
		}
	}

	public List<Zone> get() {
		return zones;
	}

	public void draw(Img img, Scalar scalar, int thickness) {
		zones.forEach(adjusted -> adjusted.draw(img, scalar, thickness));
	}

	public void writeNum(Img img, Scalar scalar, int thickness) {
		zones.forEach(adjusted -> adjusted.write(img, String.valueOf(adjusted.getNum()), 3, scalar, thickness));
	}

	public List<Zone> getZones() {
		return zones;
	}

	public void save(File file) {
		file.getParentFile().mkdirs();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			mapper.writeValue(file, this);
			System.out.println("Zones saved in " + file.getAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void save(String imgClassDirectory) {
		this.save(new File(imgClassDirectory + "/zones/zones.json"));
	}

	public static Zones load(File file) {
		try {
			return mapper.readValue(file, Zones.class);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static Zones load(String imgClassDirectory) {
		return load(new File(imgClassDirectory + "/zones/zones.json"));
	}

}