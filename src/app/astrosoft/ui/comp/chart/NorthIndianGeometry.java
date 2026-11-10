/**
 * NorthIndianGeometry.java
 *
 * Pure Java 2D geometry for the traditional North Indian (diamond-and-triangle) chart.
 * House 1 (Lagna) is always the top center diamond, and houses 1..12 proceed
 * counter-clockwise. Has zero Swing dependencies so it can be unit-tested cleanly.
 */

package app.astrosoft.ui.comp.chart;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import app.astrosoft.consts.Rasi;

public final class NorthIndianGeometry {

	public static final int HOUSE_COUNT = 12;

	private final double originX;
	private final double originY;
	private final double width;
	private final double height;

	private final Path2D.Double[] houseShapes = new Path2D.Double[HOUSE_COUNT];
	private final Point2D.Double[] houseCentroids = new Point2D.Double[HOUSE_COUNT];
	private final Point2D.Double[] signAnchors = new Point2D.Double[HOUSE_COUNT];
	private final Point2D.Double[] contentAnchors = new Point2D.Double[HOUSE_COUNT];

	public NorthIndianGeometry(double originX, double originY, double size) {
		this(originX, originY, size, size);
	}

	public NorthIndianGeometry(double originX, double originY, double width, double height) {
		this.originX = originX;
		this.originY = originY;
		this.width = Math.max(1.0, width);
		this.height = Math.max(1.0, height);
		buildHouses();
	}

	private Point2D.Double pt(double fx, double fy) {
		return new Point2D.Double(originX + fx * width, originY + fy * height);
	}

	private void buildHouses() {
		Point2D.Double TL = pt(0.0, 0.0);
		Point2D.Double T  = pt(0.5, 0.0);
		Point2D.Double TR = pt(1.0, 0.0);
		Point2D.Double L  = pt(0.0, 0.5);
		Point2D.Double C  = pt(0.5, 0.5);
		Point2D.Double R  = pt(1.0, 0.5);
		Point2D.Double BL = pt(0.0, 1.0);
		Point2D.Double B  = pt(0.5, 1.0);
		Point2D.Double BR = pt(1.0, 1.0);

		Point2D.Double a = pt(0.25, 0.25);
		Point2D.Double b = pt(0.75, 0.25);
		Point2D.Double c = pt(0.75, 0.75);
		Point2D.Double d = pt(0.25, 0.75);

		// Houses 1..12 counter-clockwise from top center diamond (H1)
		defineDiamond(1,  T,  b,  C,  a, C);
		defineTriangle(2, TL, T,  a,     a);
		defineTriangle(3, TL, a,  L,     a);
		defineDiamond(4,  L,  a,  C,  d, C);
		defineTriangle(5, L,  d,  BL,    d);
		defineTriangle(6, BL, B,  d,     d);
		defineDiamond(7,  B,  d,  C,  c, C);
		defineTriangle(8, B,  BR, c,     c);
		defineTriangle(9, BR, R,  c,     c);
		defineDiamond(10, R,  c,  C,  b, C);
		defineTriangle(11, R, TR, b,     b);
		defineTriangle(12, TR, T, b,     b);
	}

	private void defineDiamond(int house1Based,
			Point2D.Double p0, Point2D.Double p1, Point2D.Double p2, Point2D.Double p3,
			Point2D.Double innerVertex) {
		int idx = house1Based - 1;
		Path2D.Double path = new Path2D.Double();
		path.moveTo(p0.x, p0.y);
		path.lineTo(p1.x, p1.y);
		path.lineTo(p2.x, p2.y);
		path.lineTo(p3.x, p3.y);
		path.closePath();
		houseShapes[idx] = path;

		Point2D.Double centroid = new Point2D.Double(
				(p0.x + p1.x + p2.x + p3.x) / 4.0,
				(p0.y + p1.y + p2.y + p3.y) / 4.0);
		houseCentroids[idx] = centroid;
		signAnchors[idx] = pullToward(innerVertex, centroid, 0.17, 10.0, 0.38);
		// Slight pull away from inner vertex C so planet stack stays clear of sign number
		contentAnchors[idx] = new Point2D.Double(
				centroid.x + (centroid.x - innerVertex.x) * 0.08,
				centroid.y + (centroid.y - innerVertex.y) * 0.08);
	}

	private void defineTriangle(int house1Based,
			Point2D.Double p0, Point2D.Double p1, Point2D.Double p2,
			Point2D.Double innerVertex) {
		int idx = house1Based - 1;
		Path2D.Double path = new Path2D.Double();
		path.moveTo(p0.x, p0.y);
		path.lineTo(p1.x, p1.y);
		path.lineTo(p2.x, p2.y);
		path.closePath();
		houseShapes[idx] = path;

		Point2D.Double centroid = new Point2D.Double(
				(p0.x + p1.x + p2.x) / 3.0,
				(p0.y + p1.y + p2.y) / 3.0);
		houseCentroids[idx] = centroid;
		signAnchors[idx] = pullToward(innerVertex, centroid, 0.24, 9.0, 0.42);
		// Shift slightly away from the acute inner vertex toward the wide outer base
		contentAnchors[idx] = new Point2D.Double(
				centroid.x + (centroid.x - innerVertex.x) * 0.14,
				centroid.y + (centroid.y - innerVertex.y) * 0.14);
	}

	private static Point2D.Double pullToward(Point2D.Double from, Point2D.Double to,
			double fraction, double minPx, double maxFraction) {
		double dx = to.x - from.x;
		double dy = to.y - from.y;
		double dist = Math.hypot(dx, dy);
		if (dist < 1e-6) {
			return new Point2D.Double(from.x, from.y);
		}
		double step = Math.min(dist * maxFraction, Math.max(minPx, dist * fraction));
		double t = step / dist;
		return new Point2D.Double(from.x + dx * t, from.y + dy * t);
	}

	public double getOriginX() {
		return originX;
	}

	public double getOriginY() {
		return originY;
	}

	public double getSize() {
		return Math.min(width, height);
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public Rectangle2D.Double getOuterBounds() {
		return new Rectangle2D.Double(originX, originY, width, height);
	}

	/** Returns the polygon shape for 1-based house {@code 1..12}. */
	public Shape getHouseShape(int house1Based) {
		validateHouse(house1Based);
		return houseShapes[house1Based - 1];
	}

	public Point2D.Double getHouseCentroid(int house1Based) {
		validateHouse(house1Based);
		Point2D.Double p = houseCentroids[house1Based - 1];
		return new Point2D.Double(p.x, p.y);
	}

	public Point2D.Double getSignAnchor(int house1Based) {
		validateHouse(house1Based);
		Point2D.Double p = signAnchors[house1Based - 1];
		return new Point2D.Double(p.x, p.y);
	}

	public Point2D.Double getContentAnchor(int house1Based) {
		validateHouse(house1Based);
		Point2D.Double p = contentAnchors[house1Based - 1];
		return new Point2D.Double(p.x, p.y);
	}

	public static boolean isDiamondHouse(int house1Based) {
		return house1Based == 1 || house1Based == 4 || house1Based == 7 || house1Based == 10;
	}

	/** Returns the 1-based house number containing {@code pt}, or -1 if outside. */
	public int houseAt(Point2D pt) {
		if (pt == null) {
			return -1;
		}
		for (int i = 0; i < HOUSE_COUNT; i++) {
			if (houseShapes[i].contains(pt)) {
				return i + 1;
			}
		}
		return -1;
	}

	/** Sign occupying 1-based house {@code house1Based} for the given ascendant (Aries fallback if null). */
	public static Rasi signForHouse(Rasi ascendant, int house1Based) {
		validateHouse(house1Based);
		int ascIdx = (ascendant != null) ? ascendant.ordinal() : 0;
		return Rasi.ofIndex(Math.floorMod(ascIdx + (house1Based - 1), HOUSE_COUNT));
	}

	/** 1-based house number (1..12) occupied by {@code rasi} for the given ascendant. */
	public static int houseForRasi(Rasi ascendant, Rasi rasi) {
		if (rasi == null) {
			throw new IllegalArgumentException("rasi cannot be null");
		}
		int ascIdx = (ascendant != null) ? ascendant.ordinal() : 0;
		return Math.floorMod(rasi.ordinal() - ascIdx, HOUSE_COUNT) + 1;
	}

	private static void validateHouse(int house1Based) {
		if (house1Based < 1 || house1Based > HOUSE_COUNT) {
			throw new IllegalArgumentException("house must be 1..12, got " + house1Based);
		}
	}
}
