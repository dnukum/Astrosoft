/**
 * NorthIndianChartCanvas.java
 *
 * Custom Java 2D canvas rendering a chart in the traditional North Indian
 * diamond-and-triangle layout, supporting both planet charts and Ashtavarga bindu charts.
 */

package app.astrosoft.ui.comp.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.chart.HouseContentModel.HouseEntry;
import app.astrosoft.ui.comp.chart.HouseContentModel.PlanetItem;
import app.astrosoft.ui.util.UIConsts;

public class NorthIndianChartCanvas extends JComponent {

	private static final int CANVAS_PAD = 2;
	private static final double MAX_ASPECT_RATIO = 1.65;

	private static final class HitTarget {
		final Shape bounds;
		final Planet planet;
		final String customTip;

		HitTarget(Shape bounds, Planet planet, String customTip) {
			this.bounds = bounds;
			this.planet = planet;
			this.customTip = customTip;
		}
	}

	private static final class PlacedItem {
		final PlanetItem item;
		final Rectangle2D.Double box;
		final double labelX;
		final double labelBaselineY;
		final double labelWidth;

		PlacedItem(PlanetItem item, Rectangle2D.Double box,
				double labelX, double labelBaselineY, double labelWidth) {
			this.item = item;
			this.box = box;
			this.labelX = labelX;
			this.labelBaselineY = labelBaselineY;
			this.labelWidth = labelWidth;
		}
	}

	private static final class HouseLayout {
		final int house;
		final Rasi rasi;
		final Rectangle2D.Double signBounds;
		final double signBaselineX;
		final double signBaselineY;
		final Font planetFont;
		final List<PlacedItem> items;
		final boolean allFitInside;

		HouseLayout(int house, Rasi rasi, Rectangle2D.Double signBounds,
				double signBaselineX, double signBaselineY,
				Font planetFont, List<PlacedItem> items, boolean allFitInside) {
			this.house = house;
			this.rasi = rasi;
			this.signBounds = signBounds;
			this.signBaselineX = signBaselineX;
			this.signBaselineY = signBaselineY;
			this.planetFont = planetFont;
			this.items = items;
			this.allFitInside = allFitInside;
		}
	}

	private HouseContentModel model;
	private boolean showHouseNumber;
	private final EnumMap<Rasi, Color> houseBackgrounds = new EnumMap<>(Rasi.class);
	private final EnumMap<Rasi, String> houseToolTips = new EnumMap<>(Rasi.class);

	private NorthIndianGeometry lastGeometry;
	private final List<HouseLayout> lastHouseLayouts = new ArrayList<>(NorthIndianGeometry.HOUSE_COUNT);
	private final List<HitTarget> hitTargets = new ArrayList<>();
	private int laidOutWidth = -1;
	private int laidOutHeight = -1;
	private int laidOutFontOffset = Integer.MIN_VALUE;

	public NorthIndianChartCanvas(HouseContentModel model, boolean showHouseNumber) {
		this.model = model;
		this.showHouseNumber = showHouseNumber;
		setOpaque(false);
		ToolTipManager mgr = ToolTipManager.sharedInstance();
		mgr.setInitialDelay(200);
		mgr.setReshowDelay(100);
		mgr.setDismissDelay(15000);
		mgr.registerComponent(this);
	}

	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		return new Dimension(360, 360);
	}

	public void setModel(HouseContentModel model, boolean showHouseNumber) {
		this.model = model;
		this.showHouseNumber = showHouseNumber;
		invalidateLayoutCache();
		repaint();
	}

	public void setHouseBackground(Rasi rasi, Color bg) {
		if (rasi != null) {
			if (bg == null) {
				houseBackgrounds.remove(rasi);
			} else {
				houseBackgrounds.put(rasi, bg);
			}
			repaint();
		}
	}

	public void setHouseToolTip(Rasi rasi, String toolTip) {
		if (rasi != null) {
			if (toolTip == null) {
				houseToolTips.remove(rasi);
			} else {
				houseToolTips.put(rasi, toolTip);
			}
		}
	}

	public void invalidateLayoutCache() {
		laidOutWidth = -1;
		laidOutHeight = -1;
	}

	public NorthIndianGeometry getGeometry() {
		ensureCurrentLayout();
		return lastGeometry;
	}

	/** Testing helper: returns true when every placed planet label fits inside its house without clipping. */
	public boolean allItemsFitInsideHouses() {
		ensureCurrentLayout();
		for (HouseLayout hl : lastHouseLayouts) {
			if (!hl.allFitInside) {
				return false;
			}
		}
		return true;
	}

	private static final class MarkerPlacement {
		final Font font;
		final List<Rectangle2D.Double> boxes;
		final boolean allFit;

		MarkerPlacement(Font font, List<Rectangle2D.Double> boxes, boolean allFit) {
			this.font = font;
			this.boxes = boxes;
			this.allFit = allFit;
		}
	}

	private MarkerPlacement resolveAshtavargaMarkers(HouseLayout hl, HouseEntry entry,
			double s, boolean compact, Rectangle2D.Double scoreZone) {
		int targetSize = ashtavargaMarkerFontSize(s, compact);
		Shape shape = lastGeometry.getHouseShape(hl.house);
		MarkerPlacement fallback = null;
		for (int pt = targetSize; pt >= 5; pt--) {
			Font candidateFont = app.astrosoft.ui.util.UIUtil.getFont(Font.BOLD, pt);
			FontMetrics mfm = getFontMetrics(candidateFont);
			List<Rectangle2D.Double> boxes = computeMarkerBoxes(hl, entry, s, compact, mfm, scoreZone);
			boolean fits = markersFitInHouse(shape, hl.signBounds, scoreZone, boxes);
			MarkerPlacement placement = new MarkerPlacement(candidateFont, boxes, fits);
			if (fits) {
				return placement;
			}
			if (fallback == null) {
				fallback = placement;
			}
		}
		return fallback;
	}

	private static boolean markersFitInHouse(Shape shape, Rectangle2D.Double signBounds,
			Rectangle2D.Double scoreZone, List<Rectangle2D.Double> boxes) {
		for (Rectangle2D.Double box : boxes) {
			if (!shape.contains(box) || box.intersects(signBounds) || box.intersects(scoreZone)) {
				return false;
			}
		}
		return boxes.size() < 2 || !boxes.get(0).intersects(boxes.get(1));
	}

	/**
	 * Testing helper: returns true when every Ashtavarga natal highlight ring and Asc/Mo
	 * marker stays inside its house shape without overlapping sign numbers or score/ring bounds.
	 */
	public boolean allAshtavargaMarkersFit() {
		ensureCurrentLayout();
		if (model == null || !model.isAshtavarga() || lastGeometry == null) {
			return true;
		}
		double s = lastGeometry.getSize();
		boolean compact = (s < 220.0);

		for (HouseLayout hl : lastHouseLayouts) {
			HouseEntry entry = model.getEntry(hl.rasi);
			if (entry == null) {
				continue;
			}
			Shape shape = lastGeometry.getHouseShape(hl.house);
			Rectangle2D.Double scoreZone = computeAshtavargaScoreZone(hl, entry, s, compact);
			if (entry.isNatalHighlight() && entry.getBinduScore() != null) {
				if (!shape.contains(scoreZone) || scoreZone.intersects(hl.signBounds)) {
					return false;
				}
			}
			if (!entry.isAscMarker() && !entry.isMoonMarker()) {
				continue;
			}
			MarkerPlacement placement = resolveAshtavargaMarkers(hl, entry, s, compact, scoreZone);
			if (placement == null || !placement.allFit) {
				return false;
			}
		}
		return true;
	}

	/** Testing helper: returns the screen-space center of a placed planet label, or null if absent. */
	public Point2D.Double getPlanetHitCenter(Planet planet) {
		ensureCurrentLayout();
		for (HitTarget t : hitTargets) {
			if (t.planet == planet) {
				Rectangle2D r = t.bounds.getBounds2D();
				return new Point2D.Double(r.getCenterX(), r.getCenterY());
			}
		}
		return null;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (event == null || model == null) {
			return null;
		}
		ensureCurrentLayout();
		Point2D pt = event.getPoint();

		for (HitTarget target : hitTargets) {
			if (target.bounds.contains(pt)) {
				if (target.customTip != null) {
					return target.customTip;
				}
				if (target.planet != null) {
					String tip = ChartTooltips.planetToolTip(model.getChartData(), target.planet);
					if (tip != null) {
						return tip;
					}
				}
			}
		}

		if (lastGeometry != null) {
			int house = lastGeometry.houseAt(pt);
			if (house >= 1) {
				Rasi rasi = NorthIndianGeometry.signForHouse(model.getAscendant(), house);
				String override = houseToolTips.get(rasi);
				if (override != null) {
					return override;
				}
				if (model.isPlanetChart()) {
					return ChartTooltips.houseToolTip(model.getChartData(), rasi, showHouseNumber);
				}
			}
		}
		return null;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (model == null) {
			return;
		}
		Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

			ensureLayout(getWidth(), getHeight());
			if (lastGeometry == null) {
				return;
			}

			paintHouseFills(g2);
			paintGridLines(g2);
			paintHouseContents(g2);
		} finally {
			g2.dispose();
		}
	}

	private void paintHouseFills(Graphics2D g2) {
		Color chartBg = UIConsts.getChartBackground();
		Color diamondTint = UIConsts.getNorthIndianDiamondTint();
		Rectangle2D.Double outer = lastGeometry.getOuterBounds();
		g2.setColor(chartBg);
		g2.fill(outer);

		for (int h = 1; h <= NorthIndianGeometry.HOUSE_COUNT; h++) {
			Rasi rasi = NorthIndianGeometry.signForHouse(model.getAscendant(), h);
			Shape shape = lastGeometry.getHouseShape(h);
			Color override = houseBackgrounds.get(rasi);
			if (override != null) {
				g2.setColor(override);
				g2.fill(shape);
			} else if (model.isAshtavarga()) {
				HouseEntry entry = model.getEntry(rasi);
				Color avColor = UIConsts.getAshtavargaColor(entry != null ? entry.getStrength() : null);
				if (!avColor.equals(chartBg)) {
					g2.setColor(avColor);
					g2.fill(shape);
				}
			} else if (NorthIndianGeometry.isDiamondHouse(h)) {
				g2.setColor(diamondTint);
				g2.fill(shape);
			}
		}
	}

	private void paintGridLines(Graphics2D g2) {
		double ox = lastGeometry.getOriginX();
		double oy = lastGeometry.getOriginY();
		double w = lastGeometry.getWidth();
		double h = lastGeometry.getHeight();
		double midX = ox + w * 0.5;
		double midY = oy + h * 0.5;

		g2.setColor(UIConsts.getChartBorderColor());
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		g2.draw(new Rectangle2D.Double(ox, oy, w, h));
		g2.draw(new Line2D.Double(ox, oy, ox + w, oy + h));
		g2.draw(new Line2D.Double(ox + w, oy, ox, oy + h));
		g2.draw(new Line2D.Double(midX, oy, ox + w, midY));
		g2.draw(new Line2D.Double(ox + w, midY, midX, oy + h));
		g2.draw(new Line2D.Double(midX, oy + h, ox, midY));
		g2.draw(new Line2D.Double(ox, midY, midX, oy));

		g2.setStroke(oldStroke);
	}

	private void paintHouseContents(Graphics2D g2) {
		Font signFont = createSignFont(lastGeometry.getSize());
		Color signColor = UIConsts.getChartLabelColor();

		for (HouseLayout hl : lastHouseLayouts) {
			g2.setFont(signFont);
			g2.setColor(signColor);
			g2.drawString(String.valueOf(hl.rasi.ordinal() + 1),
					(float) hl.signBaselineX, (float) hl.signBaselineY);

			if (model.isAshtavarga()) {
				paintAshtavargaHouse(g2, hl);
			} else {
				paintPlanetHouse(g2, hl);
			}
		}
	}

	private static Font createSignFont(double side) {
		int signFontSize = Math.max(8, Math.min(11, (int) Math.round(side * 0.032)));
		return new Font(Font.SANS_SERIF, Font.PLAIN, signFontSize);
	}

	private void paintPlanetHouse(Graphics2D g2, HouseLayout hl) {
		if (hl.items.isEmpty()) {
			return;
		}
		for (PlacedItem pi : hl.items) {
			g2.setFont(hl.planetFont);
			g2.setColor(pi.item.getColor());
			g2.drawString(pi.item.getLabel(), (float) pi.labelX, (float) pi.labelBaselineY);

			if (pi.item.isRetrograde()) {
				Stroke prev = g2.getStroke();
				g2.setStroke(new BasicStroke(Math.max(1.0f, hl.planetFont.getSize() / 11.0f)));
				double ulY = pi.labelBaselineY + 1.5;
				g2.draw(new Line2D.Double(pi.labelX, ulY, pi.labelX + pi.labelWidth, ulY));
				g2.setStroke(prev);
			}
		}
	}

	private Point2D.Double ashtavargaScoreCenter(int house, double s) {
		if (NorthIndianGeometry.isDiamondHouse(house)) {
			return lastGeometry.getContentAnchor(house);
		}
		if (s < 145.0) {
			// Nudge slightly toward outer triangle base so natal ring clears inner sign label at compact heights
			Point2D.Double c = lastGeometry.getHouseCentroid(house);
			double shiftX = 0.008 * lastGeometry.getWidth();
			double shiftY = 0.008 * lastGeometry.getHeight();
			switch (house) {
				case 2: case 12: return new Point2D.Double(c.x, c.y - shiftY);
				case 6: case 8:  return new Point2D.Double(c.x, c.y + shiftY);
				case 3: case 5:  return new Point2D.Double(c.x - shiftX, c.y);
				default:         return new Point2D.Double(c.x + shiftX, c.y);
			}
		}
		return lastGeometry.getHouseCentroid(house);
	}

	private static int ashtavargaScoreFontSize(double s, boolean compact) {
		if (compact) {
			return 11;
		}
		return Math.max(15, Math.min(22, (int) Math.round(s * 0.068)));
	}

	private static int ashtavargaMarkerFontSize(double s, boolean compact) {
		if (compact) {
			return 7;
		}
		return Math.max(10, Math.min(14, (int) Math.round(s * 0.045)));
	}

	private double ashtavargaRingDiameter(double s, boolean compact) {
		if (s < 145.0) {
			return 12.0;
		}
		if (s < 160.0) {
			return 14.0;
		}
		if (compact) {
			return 18.0;
		}
		return Math.max(26.0, Math.min(36.0, s * 0.115));
	}

	private Rectangle2D.Double computeAshtavargaScoreZone(HouseLayout hl, HouseEntry entry, double s, boolean compact) {
		Point2D.Double anchor = ashtavargaScoreCenter(hl.house, s);
		double diameter = ashtavargaRingDiameter(s, compact);
		if (entry.isNatalHighlight() && entry.getBinduScore() != null) {
			return new Rectangle2D.Double(anchor.x - diameter * 0.5, anchor.y - diameter * 0.5, diameter, diameter);
		}
		Font scoreFont = new Font(Font.SANS_SERIF, Font.BOLD, ashtavargaScoreFontSize(s, compact));
		FontMetrics sfm = getFontMetrics(scoreFont);
		String valStr = (entry.getBinduScore() != null) ? entry.getBinduScore().toString() : "";
		double w = Math.max(8.0, sfm.stringWidth(valStr) + 2.0);
		double h = Math.max(10.0, sfm.getAscent() + 1.0);
		return new Rectangle2D.Double(anchor.x - w * 0.5, anchor.y - h * 0.5, w, h);
	}

	private void paintAshtavargaHouse(Graphics2D g2, HouseLayout hl) {
		HouseEntry entry = model.getEntry(hl.rasi);
		if (entry == null) {
			return;
		}
		double s = lastGeometry.getSize();
		boolean compact = (s < 220.0);

		Point2D.Double anchor = ashtavargaScoreCenter(hl.house, s);
		Integer score = entry.getBinduScore();
		String valStr = (score != null) ? score.toString() : "";

		int scoreSize = ashtavargaScoreFontSize(s, compact);
		Font scoreFont = new Font(Font.SANS_SERIF, Font.BOLD, scoreSize);
		g2.setFont(scoreFont);
		FontMetrics sfm = g2.getFontMetrics();
		int sw = sfm.stringWidth(valStr);
		int sh = sfm.getAscent();
		float sx = (float) (anchor.x - sw / 2.0);
		float sy = (float) (anchor.y + sh / 2.0 - sfm.getDescent() * 0.35);

		if (entry.isNatalHighlight() && !valStr.isEmpty()) {
			double diameter = ashtavargaRingDiameter(s, compact);
			double rx = anchor.x - diameter / 2.0;
			double ry = anchor.y - diameter / 2.0;
			Stroke prev = g2.getStroke();
			g2.setColor(UIConsts.getChartNatalHighlightColor());
			g2.setStroke(new BasicStroke(1.6f));
			g2.draw(new Ellipse2D.Double(rx, ry, diameter, diameter));
			g2.setStroke(prev);
		}

		if (!valStr.isEmpty()) {
			g2.setColor(UIConsts.getChartScoreColor());
			g2.drawString(valStr, sx, sy);
		}

		if (entry.isAscMarker() || entry.isMoonMarker()) {
			Rectangle2D.Double scoreZone = computeAshtavargaScoreZone(hl, entry, s, compact);
			MarkerPlacement placement = resolveAshtavargaMarkers(
					hl, entry, s, compact, scoreZone);
			g2.setFont(placement.font);
			FontMetrics mfm = g2.getFontMetrics();
			List<Rectangle2D.Double> boxes = placement.boxes;
			if (entry.isAscMarker() && entry.isMoonMarker()) {
				Rectangle2D.Double ascBox = boxes.get(0);
				Rectangle2D.Double moBox = boxes.get(1);
				g2.setColor(UIConsts.getChartAscendantColor());
				g2.drawString("Asc", (float) ascBox.x, (float) (ascBox.y + mfm.getAscent()));
				g2.setColor(UIConsts.getChartMoonColor());
				g2.drawString("Mo", (float) moBox.x, (float) (moBox.y + mfm.getAscent()));
			} else {
				Rectangle2D.Double box = boxes.get(0);
				String mStr = entry.isAscMarker() ? "Asc" : "Mo";
				g2.setColor(entry.isAscMarker() ? UIConsts.getChartAscendantColor() : UIConsts.getChartMoonColor());
				g2.drawString(mStr, (float) box.x, (float) (box.y + mfm.getAscent()));
			}
		}
	}

	/**
	 * Places Asc and/or Mo markers strictly inside {@code houseShape}, clear of
	 * {@code signBounds} and {@code scoreZone} (including any natal highlight ring).
	 */
	private List<Rectangle2D.Double> computeMarkerBoxes(HouseLayout hl, HouseEntry entry,
			double s, boolean compact, FontMetrics mfm, Rectangle2D.Double scoreZone) {
		double markerH = mfm.getAscent() + mfm.getDescent() * 0.4;
		double ascW = mfm.stringWidth("Asc");
		double moW = mfm.stringWidth("Mo");
		Shape houseShape = lastGeometry.getHouseShape(hl.house);
		Rectangle2D sb = houseShape.getBounds2D();
		double ox = lastGeometry.getOriginX();
		double oy = lastGeometry.getOriginY();

		double gw = lastGeometry.getWidth();
		double gh = lastGeometry.getHeight();

		if (entry.isAscMarker() && entry.isMoonMarker()) {
			// In diamonds, prefer side-by-side in outer tip if it clears scoreZone; otherwise split pockets
			if (NorthIndianGeometry.isDiamondHouse(hl.house)) {
				int gap = compact ? 2 : 5;
				Rectangle2D.Double combined = placeDiamondMarker(hl.house, gw, gh, ox, oy,
						ascW + gap + moW, markerH, houseShape, hl.signBounds, scoreZone);
				if (combined != null) {
					Rectangle2D.Double ascBox = new Rectangle2D.Double(combined.x, combined.y, ascW, markerH);
					Rectangle2D.Double moBox = new Rectangle2D.Double(combined.x + ascW + gap, combined.y, moW, markerH);
					List<Rectangle2D.Double> res = new ArrayList<>(2);
					res.add(ascBox);
					res.add(moBox);
					return res;
				}
			}
			Rectangle2D.Double ascBox = placeTriangleOrSplitMarker(
					hl.house, true, ascW, markerH, houseShape, sb, hl.signBounds, scoreZone);
			Rectangle2D.Double moBox = placeTriangleOrSplitMarker(
					hl.house, false, moW, markerH, houseShape, sb, hl.signBounds, scoreZone);
			List<Rectangle2D.Double> res = new ArrayList<>(2);
			res.add(ascBox);
			res.add(moBox);
			return res;
		}

		double w = entry.isAscMarker() ? ascW : moW;
		if (NorthIndianGeometry.isDiamondHouse(hl.house)) {
			Rectangle2D.Double box = placeDiamondMarker(hl.house, gw, gh, ox, oy,
					w, markerH, houseShape, hl.signBounds, scoreZone);
			if (box != null) {
				return Collections.singletonList(box);
			}
		}
		return Collections.singletonList(placeTriangleOrSplitMarker(
				hl.house, entry.isAscMarker(), w, markerH, houseShape, sb, hl.signBounds, scoreZone));
	}

	private Rectangle2D.Double placeDiamondMarker(int house, double gw, double gh, double ox, double oy,
			double w, double h, Shape houseShape, Rectangle2D.Double signBounds, Rectangle2D.Double scoreZone) {
		for (double frac = 0.06; frac <= 0.22; frac += 0.015) {
			double x;
			double y;
			switch (house) {
				case 1:
					x = ox + 0.5 * gw - w * 0.5;
					y = oy + frac * gh;
					break;
				case 7:
					x = ox + 0.5 * gw - w * 0.5;
					y = oy + (1.0 - frac) * gh - h;
					break;
				case 4:
					x = ox + frac * gw;
					y = oy + 0.5 * gh - h * 0.5;
					break;
				default: // 10
					x = ox + (1.0 - frac) * gw - w;
					y = oy + 0.5 * gh - h * 0.5;
					break;
			}
			Rectangle2D.Double cand = new Rectangle2D.Double(x, y, w, h);
			if (houseShape.contains(cand) && !cand.intersects(signBounds) && !cand.intersects(scoreZone)) {
				return cand;
			}
		}
		return null;
	}

	private Rectangle2D.Double placeTriangleOrSplitMarker(int house, boolean firstPocket,
			double w, double h, Shape houseShape, Rectangle2D sb,
			Rectangle2D.Double signBounds, Rectangle2D.Double scoreZone) {
		if (house == 2 || house == 12 || house == 6 || house == 8 || NorthIndianGeometry.isDiamondHouse(house)) {
			boolean top = (house == 2 || house == 12 || house == 1);
			double yStart = (house == 1) ? (sb.getMinY() + 6.0)
					: (house == 7) ? (sb.getMaxY() - 6.0 - h)
					: top ? (sb.getMinY() + 2.0) : (sb.getMaxY() - 2.0 - h);
			double yStep = top ? 1.5 : -1.5;
			for (int step = 0; step < 12; step++) {
				double y = yStart + step * yStep;
				double[] span = scanSpan(houseShape, sb, y, h, true);
				if (span == null) continue;
				double minX = span[0] + 1.0;
				double maxX = span[1] - 1.0 - w;
				if (maxX < minX) continue;
				// Try left pocket first (for Asc) or right pocket first (for Mo)
				double primaryX = firstPocket ? Math.min(maxX, Math.max(minX, scoreZone.getMinX() - 1.5 - w))
						: Math.max(minX, Math.min(maxX, scoreZone.getMaxX() + 1.5));
				double secondaryX = firstPocket ? maxX : minX;
				double[] xs = { primaryX, minX, maxX, secondaryX };
				for (double x : xs) {
					Rectangle2D.Double cand = new Rectangle2D.Double(x, y, w, h);
					if (houseShape.contains(cand) && !cand.intersects(signBounds) && !cand.intersects(scoreZone)) {
						return cand;
					}
				}
			}
		} else {
			// Left/Right triangles (3, 5, 9, 11): upper pocket vs lower pocket along outer vertical edge
			boolean left = (house == 3 || house == 5);
			double xStart = left ? (sb.getMinX() + 2.0) : (sb.getMaxX() - 2.0 - w);
			double xStep = left ? 1.5 : -1.5;
			for (int step = 0; step < 12; step++) {
				double x = xStart + step * xStep;
				double[] vSpan = scanSpan(houseShape, sb, x, w, false);
				if (vSpan == null) continue;
				double minY = vSpan[0] + 1.0;
				double maxY = vSpan[1] - 1.0 - h;
				if (maxY < minY) continue;
				double upperY = Math.min(maxY, Math.max(minY, scoreZone.getMinY() - 1.0 - h));
				double lowerY = Math.max(minY, Math.min(maxY, scoreZone.getMaxY() + 1.0));
				double[] ys = firstPocket ? new double[] { upperY, minY, lowerY, maxY }
						: new double[] { lowerY, maxY, upperY, minY };
				for (double y : ys) {
					Rectangle2D.Double cand = new Rectangle2D.Double(x, y, w, h);
					if (houseShape.contains(cand) && !cand.intersects(signBounds) && !cand.intersects(scoreZone)) {
						return cand;
					}
				}
			}
		}
		Point2D.Double c = lastGeometry.getHouseCentroid(house);
		return new Rectangle2D.Double(c.x - w * 0.5, c.y - h * 0.5, w, h);
	}

	private void ensureCurrentLayout() {
		int w = getWidth();
		int h = getHeight();
		if (w <= 0 || h <= 0) {
			Dimension pref = getPreferredSize();
			if (pref != null && pref.width > 0 && pref.height > 0) {
				w = pref.width;
				h = pref.height;
			} else {
				w = 360;
				h = 360;
			}
		}
		ensureLayout(w, h);
	}

	private void ensureLayout(int w, int h) {
		int offset = Chart.getFontOffset();
		if (w == laidOutWidth && h == laidOutHeight && offset == laidOutFontOffset && lastGeometry != null) {
			return;
		}
		laidOutWidth = w;
		laidOutHeight = h;
		laidOutFontOffset = offset;

		double availW = Math.max(40.0, w - 2.0 * CANVAS_PAD);
		double availH = Math.max(40.0, h - 2.0 * CANVAS_PAD);
		double chartW = Math.min(availW, availH * MAX_ASPECT_RATIO);
		double chartH = Math.min(availH, availW * MAX_ASPECT_RATIO);
		double ox = (w - chartW) / 2.0;
		double oy = (h - chartH) / 2.0;
		lastGeometry = new NorthIndianGeometry(ox, oy, chartW, chartH);
		lastHouseLayouts.clear();
		hitTargets.clear();

		double side = lastGeometry.getSize();
		Font signFont = createSignFont(side);
		FontMetrics signFm = getFontMetrics(signFont);
		int baseFontSize = computeBaseFontSize(side, model.getMaxPlanetsInAnyHouse(), offset);

		for (int house = 1; house <= NorthIndianGeometry.HOUSE_COUNT; house++) {
			Rasi rasi = NorthIndianGeometry.signForHouse(model.getAscendant(), house);
			HouseEntry entry = model.getEntry(rasi);
			String signText = String.valueOf(rasi.ordinal() + 1);

			Point2D.Double sa = lastGeometry.getSignAnchor(house);
			int sw = signFm.stringWidth(signText);
			int sh = signFm.getAscent();
			double signBaselineX = sa.x - sw / 2.0;
			double signBaselineY = sa.y + sh / 2.0 - signFm.getDescent() * 0.3;
			Rectangle2D.Double signBounds = new Rectangle2D.Double(
					signBaselineX - 1.5, signBaselineY - sh - 1.0, sw + 3.0, sh + signFm.getDescent() + 2.0);

			if (model.isAshtavarga()) {
				lastHouseLayouts.add(new HouseLayout(house, rasi, signBounds,
						signBaselineX, signBaselineY, signFont,
						Collections.<PlacedItem>emptyList(), true));
				if (entry != null && entry.isNatalHighlight() && entry.getNatalToolTip() != null) {
					double d = ashtavargaRingDiameter(side, side < 220.0) + 2.0;
					Point2D.Double ca = ashtavargaScoreCenter(house, side);
					hitTargets.add(new HitTarget(
							new Ellipse2D.Double(ca.x - d / 2.0, ca.y - d / 2.0, d, d),
							null, entry.getNatalToolTip()));
				}
				continue;
			}

			List<PlanetItem> planets = (entry != null) ? entry.getPlanets() : Collections.<PlanetItem>emptyList();
			HouseLayout hl = fitPlanetHouse(house, rasi, planets, signBounds, signBaselineX, signBaselineY, baseFontSize);
			lastHouseLayouts.add(hl);
			for (PlacedItem pi : hl.items) {
				hitTargets.add(new HitTarget(pi.box, pi.item.getPlanet(), null));
			}
		}
	}

	private static int computeBaseFontSize(double side, int maxPlanets, int fontOffset) {
		double cellScale = side / 4.0;
		if (cellScale < 68.0) {
			int baseCompact = (cellScale < 52.0) ? 9 : 10;
			return Math.max(8, baseCompact + fontOffset);
		} else if (cellScale < 80.0) {
			int baseMedium = (maxPlanets <= 2) ? 14 : ((maxPlanets <= 4) ? 12 : 11);
			return Math.max(9, baseMedium + fontOffset);
		} else {
			int baseLarge = (maxPlanets <= 2) ? 15 : ((maxPlanets <= 4) ? 14 : 12);
			return Math.max(9, baseLarge + fontOffset);
		}
	}

	private HouseLayout fitPlanetHouse(int house, Rasi rasi, List<PlanetItem> planets,
			Rectangle2D.Double signBounds, double signBaselineX, double signBaselineY, int baseFontSize) {
		if (planets == null || planets.isEmpty()) {
			Font f = new Font(Font.SANS_SERIF, Font.BOLD, baseFontSize);
			return new HouseLayout(house, rasi, signBounds, signBaselineX, signBaselineY,
					f, Collections.<PlacedItem>emptyList(), true);
		}

		int maxCols = (planets.size() >= 5) ? 3 : 2;
		for (int sz = baseFontSize; sz >= 8; sz--) {
			for (int cols = 1; cols <= maxCols; cols++) {
				HouseLayout hl = tryPlacePlanets(house, rasi, planets, cols, sz,
						signBounds, signBaselineX, signBaselineY);
				if (hl.allFitInside) {
					return hl;
				}
			}
		}

		if (!NorthIndianGeometry.isDiamondHouse(house)) {
			for (int sz = Math.min(baseFontSize, 9); sz >= 6; sz--) {
				HouseLayout hl = tryTriangleTapered(house, rasi, planets, sz,
						signBounds, signBaselineX, signBaselineY);
				if (hl != null && hl.allFitInside) {
					return hl;
				}
			}
		}

		for (int sz = 7; sz >= 6; sz--) {
			HouseLayout hl = tryPlacePlanets(house, rasi, planets, 4, sz,
					signBounds, signBaselineX, signBaselineY);
			if (hl.allFitInside) {
				return hl;
			}
		}

		return tryPlacePlanets(house, rasi, planets, 3, 6,
				signBounds, signBaselineX, signBaselineY);
	}

	private HouseLayout tryPlacePlanets(int house, Rasi rasi, List<PlanetItem> planets,
			int cols, int fontSize,
			Rectangle2D.Double signBounds, double signBaselineX, double signBaselineY) {
		Font planetFont = app.astrosoft.ui.util.UIUtil.getFont(Font.SANS_SERIF, Font.BOLD, fontSize);
		FontMetrics pfm = getFontMetrics(planetFont);

		int n = planets.size();
		int effectiveCols = Math.min(cols, n);
		int rows = (n + effectiveCols - 1) / effectiveCols;

		double[] labelWidths = new double[n];
		double[] colWidths = new double[effectiveCols];
		for (int i = 0; i < n; i++) {
			double lw = pfm.stringWidth(planets.get(i).getLabel());
			labelWidths[i] = lw;
			int col = i % effectiveCols;
			if (lw > colWidths[col]) {
				colWidths[col] = lw;
			}
		}

		double colGap = Math.max(2.5, fontSize * 0.30);
		double rowHeight = pfm.getAscent() + Math.max(2.0, pfm.getDescent() * 0.6);
		double rowGap = (rows >= 4) ? 0.5 : 1.5;
		double blockH = rows * rowHeight + Math.max(0, rows - 1) * rowGap;

		Shape houseShape = lastGeometry.getHouseShape(house);
		Point2D.Double anchor = lastGeometry.getContentAnchor(house);
		double[] nudge = outwardNudgeUnit(house);
		double side = lastGeometry.getSize();
		double[] nudgeSteps = { 0.0, 0.03 * side, 0.06 * side, 0.09 * side };

		List<PlacedItem> bestItems = null;
		boolean bestFits = false;

		for (double step : nudgeSteps) {
			double cx = anchor.x + nudge[0] * step;
			double cy = anchor.y + nudge[1] * step;
			double startY = cy - blockH / 2.0;

			List<PlacedItem> candidate = new ArrayList<>(n);
			boolean allFit = true;

			for (int i = 0; i < n; i++) {
				int r = i / effectiveCols;
				int c = i % effectiveCols;
				int itemsInRow = (r == rows - 1) ? (n - r * effectiveCols) : effectiveCols;
				double rowW = 0.0;
				for (int rc = 0; rc < itemsInRow; rc++) {
					rowW += colWidths[rc];
					if (rc > 0) rowW += colGap;
				}
				double cellX = cx - rowW / 2.0;
				for (int rc = 0; rc < c; rc++) {
					cellX += colWidths[rc] + colGap;
				}
				double itemX = cellX + (colWidths[c] - labelWidths[i]) / 2.0;
				double itemTopY = startY + r * (rowHeight + rowGap);
				double baselineY = itemTopY + pfm.getAscent();

				Rectangle2D.Double box = new Rectangle2D.Double(
						itemX - 0.5, itemTopY, labelWidths[i] + 1.0, rowHeight);

				if (!houseShape.contains(box) || box.intersects(signBounds)) {
					allFit = false;
				}
				candidate.add(new PlacedItem(planets.get(i), box, itemX, baselineY, labelWidths[i]));
			}

			if (allFit) {
				bestItems = candidate;
				bestFits = true;
				break;
			}
			if (bestItems == null) {
				bestItems = candidate;
			}
		}

		return new HouseLayout(house, rasi, signBounds, signBaselineX, signBaselineY,
				planetFont, bestItems, bestFits);
	}

	/**
	 * Packs planets from the wide outer base of a triangle toward its inner apex,
	 * steering around {@code signBounds} so crowded mini triangles fit cleanly.
	 */
	private HouseLayout tryTriangleTapered(int house, Rasi rasi, List<PlanetItem> planets,
			int fontSize, Rectangle2D.Double signBounds, double signBaselineX, double signBaselineY) {
		Font planetFont = app.astrosoft.ui.util.UIUtil.getFont(Font.SANS_SERIF, Font.BOLD, fontSize);
		FontMetrics pfm = getFontMetrics(planetFont);
		Shape houseShape = lastGeometry.getHouseShape(house);
		Rectangle2D sb = houseShape.getBounds2D();

		double rowH = pfm.getAscent() + Math.max(2.0, pfm.getDescent() * 0.5);
		double hGap = Math.max(2.0, fontSize * 0.25);
		double vGap = 0.5;
		double margin = 1.5;

		boolean horizontal = (house == 2 || house == 12 || house == 6 || house == 8);
		boolean fromStart = (house == 2 || house == 12 || house == 3 || house == 5);

		List<PlacedItem> placed = horizontal
				? packHorizontalSlices(planets, pfm, houseShape, sb, signBounds, fromStart, rowH, hGap, vGap, margin)
				: packVerticalSlices(planets, pfm, houseShape, sb, signBounds, fromStart, rowH, hGap, vGap, margin);

		if (placed == null) {
			return null;
		}
		return new HouseLayout(house, rasi, signBounds, signBaselineX, signBaselineY,
				planetFont, placed, true);
	}

	private static List<PlacedItem> packHorizontalSlices(List<PlanetItem> planets, FontMetrics pfm,
			Shape houseShape, Rectangle2D sb, Rectangle2D.Double signBounds,
			boolean fromStart, double rowH, double hGap, double vGap, double margin) {
		List<PlacedItem> placed = new ArrayList<>(planets.size());
		int idx = 0;
		double y = fromStart ? (sb.getMinY() + margin) : (sb.getMaxY() - margin - rowH);
		double yLimit = fromStart ? (sb.getMaxY() - rowH) : sb.getMinY();

		while (idx < planets.size() && (fromStart ? y <= yLimit : y >= yLimit)) {
			double[] span = scanSpan(houseShape, sb, y, rowH, true);
			if (span != null) {
				// Filter x positions on this row that clear signBounds
				double rowW = 0.0;
				List<Double> validXs = new ArrayList<>();
				int startIdx = idx;
				double curScanX = span[0] + 0.5;
				while (idx < planets.size()) {
					double lw = pfm.stringWidth(planets.get(idx).getLabel());
					double testX = (idx == startIdx) ? curScanX : (curScanX + hGap);
					Rectangle2D.Double probe = new Rectangle2D.Double(testX - 0.5, y, lw + 1.0, rowH);
					if (probe.intersects(signBounds)) {
						testX = signBounds.getMaxX() + 1.0;
						probe = new Rectangle2D.Double(testX - 0.5, y, lw + 1.0, rowH);
					}
					if (testX + lw + 0.5 > span[1] || !houseShape.contains(probe) || probe.intersects(signBounds)) {
						break;
					}
					validXs.add(testX);
					curScanX = testX + lw;
					rowW = curScanX - validXs.get(0);
					idx++;
				}
				if (idx > startIdx) {
					// Re-center row if the whole packed block doesn't touch signBounds
					double shift = (span[0] + span[1] - rowW) * 0.5 - validXs.get(0);
					boolean canCenter = true;
					for (int k = 0; k < validXs.size(); k++) {
						double lw = pfm.stringWidth(planets.get(startIdx + k).getLabel());
						Rectangle2D.Double shifted = new Rectangle2D.Double(
								validXs.get(k) + shift - 0.5, y, lw + 1.0, rowH);
						if (!houseShape.contains(shifted) || shifted.intersects(signBounds)) {
							canCenter = false;
							break;
						}
					}
					double xOffset = canCenter ? shift : 0.0;
					double baselineY = y + pfm.getAscent();
					for (int k = 0; k < validXs.size(); k++) {
						PlanetItem pi = planets.get(startIdx + k);
						double lw = pfm.stringWidth(pi.getLabel());
						double itemX = validXs.get(k) + xOffset;
						Rectangle2D.Double box = new Rectangle2D.Double(itemX - 0.5, y, lw + 1.0, rowH);
						placed.add(new PlacedItem(pi, box, itemX, baselineY, lw));
					}
				}
			}
			y += fromStart ? (rowH + vGap) : -(rowH + vGap);
		}
		return (idx == planets.size()) ? placed : null;
	}

	private static List<PlacedItem> packVerticalSlices(List<PlanetItem> planets, FontMetrics pfm,
			Shape houseShape, Rectangle2D sb, Rectangle2D.Double signBounds,
			boolean fromStart, double rowH, double hGap, double vGap, double margin) {
		List<PlacedItem> placed = new ArrayList<>(planets.size());
		int idx = 0;
		double x = fromStart ? (sb.getMinX() + margin) : (sb.getMaxX() - margin);
		double xLimit = fromStart ? sb.getMaxX() : sb.getMinX();

		while (idx < planets.size() && (fromStart ? x < xLimit : x > xLimit)) {
			double firstW = pfm.stringWidth(planets.get(idx).getLabel());
			double probeX = fromStart ? x : (x - firstW);
			double[] probeSpan = scanSpan(houseShape, sb, probeX - 0.5, firstW + 1.0, false);
			if (probeSpan == null) {
				break;
			}
			int estRows = Math.max(1, (int) Math.floor((probeSpan[1] - probeSpan[0] + vGap) / (rowH + vGap)));
			double colW = firstW;
			for (int k = idx + 1; k < Math.min(planets.size(), idx + estRows); k++) {
				colW = Math.max(colW, pfm.stringWidth(planets.get(k).getLabel()));
			}

			double colX = fromStart ? x : (x - colW);
			double[] exactSpan = scanSpan(houseShape, sb, colX - 0.5, colW + 1.0, false);
			if (exactSpan == null) {
				break;
			}

			// Collect candidate y slots inside exactSpan that clear signBounds
			List<Double> validYs = new ArrayList<>();
			for (double sy = exactSpan[0]; sy + rowH <= exactSpan[1]; sy += rowH + vGap) {
				Rectangle2D.Double slot = new Rectangle2D.Double(colX - 0.5, sy, colW + 1.0, rowH);
				if (houseShape.contains(slot) && !slot.intersects(signBounds)) {
					validYs.add(sy);
				}
			}
			if (validYs.isEmpty()) {
				break;
			}

			int count = Math.min(validYs.size(), planets.size() - idx);
			int startSlot = (validYs.size() - count) / 2;
			for (int r = 0; r < count; r++) {
				PlanetItem pi = planets.get(idx++);
				double lw = pfm.stringWidth(pi.getLabel());
				double itemX = colX + (colW - lw) * 0.5;
				double topY = validYs.get(startSlot + r);
				double baselineY = topY + pfm.getAscent();
				Rectangle2D.Double box = new Rectangle2D.Double(itemX - 0.5, topY, lw + 1.0, rowH);
				placed.add(new PlacedItem(pi, box, itemX, baselineY, lw));
			}
			x += fromStart ? (colW + hGap) : -(colW + hGap);
		}
		return (idx == planets.size()) ? placed : null;
	}

	/** Scans the contiguous span inside {@code shape} along x (if horizontal) or y (if vertical). */
	private static double[] scanSpan(Shape shape, Rectangle2D bounds, double pos, double thickness, boolean horizontal) {
		double start = horizontal ? (bounds.getMinX() + 1.0) : (bounds.getMinY() + 1.0);
		double end = horizontal ? (bounds.getMaxX() - 1.0) : (bounds.getMaxY() - 1.0);
		double minV = Double.NaN;
		double maxV = Double.NaN;
		for (double v = start; v <= end; v += 1.0) {
			boolean inside = horizontal
					? shape.contains(v, pos, 1.0, thickness)
					: shape.contains(pos, v, thickness, 1.0);
			if (inside) {
				if (Double.isNaN(minV)) minV = v;
				maxV = v + 1.0;
			}
		}
		return (Double.isNaN(minV) || maxV - minV < 6.0) ? null : new double[] { minV, maxV };
	}

	private static double[] outwardNudgeUnit(int house) {
		switch (house) {
			case 1:  return new double[] { 0.0, -1.0 };
			case 2:  return new double[] { 0.0, -1.0 };
			case 3:  return new double[] { -1.0, 0.0 };
			case 4:  return new double[] { -1.0, 0.0 };
			case 5:  return new double[] { -1.0, 0.0 };
			case 6:  return new double[] { 0.0,  1.0 };
			case 7:  return new double[] { 0.0,  1.0 };
			case 8:  return new double[] { 0.0,  1.0 };
			case 9:  return new double[] { 1.0,  0.0 };
			case 10: return new double[] { 1.0,  0.0 };
			case 11: return new double[] { 1.0,  0.0 };
			case 12: return new double[] { 0.0, -1.0 };
			default: return new double[] { 0.0,  0.0 };
		}
	}
}
