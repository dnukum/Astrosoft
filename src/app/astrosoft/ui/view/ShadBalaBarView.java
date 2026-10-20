package app.astrosoft.ui.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Roman;
import app.astrosoft.core.ShadBala;
import app.astrosoft.ui.util.UIConsts;

public class ShadBalaBarView extends JPanel {

	private final JLabel title;
	private final List<PlanetBalaItem> items = new ArrayList<PlanetBalaItem>();
	private double scaleMax = 100.0;
	private int hoveredIndex = -1;

	// Dimensions & Layout constants
	private static final int CARD_WIDTH = 530;
	private static final int CARD_HEIGHT = 218;
	private static final int BOX_LEFT = 2;
	private static final int BOX_RIGHT = 528;
	private static final int BOX_TOP = 10;
	private static final int BOX_BOTTOM = 210;

	private static final int TRACK_X = 105;
	private static final int TRACK_WIDTH = 340;
	private static final int START_Y = 38;
	private static final int ROW_HEIGHT = 23;
	private static final int BAR_HEIGHT = 15;

	private static final DecimalFormat PCT_FMT = new DecimalFormat("0.0'%'");
	private static final DecimalFormat VAL_FMT = new DecimalFormat("0.00");
	private static final DecimalFormat RUPA_FMT = new DecimalFormat("0.00");

	public static class PlanetBalaItem {
		public final Planet planet;
		public final double balaPercent;
		public final double virupas;
		public final double rupa;
		public final int rank;
		public final double minVirupas;

		public PlanetBalaItem(Planet planet, double balaPercent, double virupas, double rupa, int rank, double minVirupas) {
			this.planet = planet;
			this.balaPercent = balaPercent;
			this.virupas = virupas;
			this.rupa = rupa;
			this.rank = rank;
			this.minVirupas = minVirupas;
		}
	}

	public ShadBalaBarView(ShadBala shadBala) {
		this(extractItems(shadBala));
	}

	public ShadBalaBarView(ShadBala shadBala, String customTitle) {
		this(shadBala, customTitle, null);
	}

	public ShadBalaBarView(ShadBala shadBala, String customTitle, Dimension customSize) {
		this(extractItems(shadBala));
		if (customTitle != null) {
			title.setText(customTitle);
		}
		if (customSize != null) {
			setPreferredSize(customSize);
			setMinimumSize(customSize);
			setMaximumSize(customSize);
			setSize(customSize);
		}
	}

	public void setTitle(String text) {
		title.setText(text);
	}

	public static List<PlanetBalaItem> extractItems(ShadBala shadBala) {
		List<PlanetBalaItem> list = new ArrayList<PlanetBalaItem>();
		if (shadBala == null) return list;

		Map<Planet, Double> strengthPer = shadBala.getStrengthPer();
		for (Planet p : Planet.majorPlanets()) {
			Double pct = strengthPer != null ? strengthPer.get(p) : null;
			if (pct == null) pct = 0.0;
			Double virupas = shadBala.getShadBala(p);
			if (virupas == null) virupas = 0.0;
			Double rupa = shadBala.getRupa(p);
			if (rupa == null) rupa = 0.0;
			Integer rank = shadBala.getShadBalaRank(p);
			if (rank == null) rank = 0;
			double minVirupas = ShadBala.getMinStrength(p);

			list.add(new PlanetBalaItem(p, pct, virupas, rupa, rank, minVirupas));
		}
		return list;
	}

	public ShadBalaBarView(List<PlanetBalaItem> planetItems) {
		setLayout(null);
		setOpaque(false);

		final Dimension panelSize = new Dimension(CARD_WIDTH, CARD_HEIGHT);
		setPreferredSize(panelSize);
		setMinimumSize(panelSize);
		setMaximumSize(panelSize);

		title = new JLabel("Planetary Strength (Shadbala)", SwingConstants.CENTER);
		title.setFont(new Font("Verdana", Font.BOLD, 12));
		title.setForeground(UIConsts.DARK_BLUE);
		title.setBounds(0, 0, CARD_WIDTH, 20);
		add(title);

		double maxP = 0.0;
		if (planetItems != null) {
			for (PlanetBalaItem item : planetItems) {
				items.add(item);
				if (item.balaPercent > maxP) {
					maxP = item.balaPercent;
				}
			}
		}

		// Sort descending by Bala %
		Collections.sort(items, new Comparator<PlanetBalaItem>() {
			public int compare(PlanetBalaItem o1, PlanetBalaItem o2) {
				return Double.compare(o2.balaPercent, o1.balaPercent);
			}
		});

		// Longest bar has fixed maximum length (equal to TRACK_WIDTH)
		// scaleMax ensures 100% threshold line remains visible even if maxP < 100
		scaleMax = Math.max(100.0, maxP);

		// Register for tooltips
		ToolTipManager.sharedInstance().registerComponent(this);

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int prevIndex = hoveredIndex;
				hoveredIndex = getRowAt(e.getPoint());
				if (prevIndex != hoveredIndex) {
					repaint();
				}
			}
		});
	}

	private int getRowAt(Point pt) {
		if (pt.y >= START_Y && pt.y < START_Y + items.size() * ROW_HEIGHT) {
			return (pt.y - START_Y) / ROW_HEIGHT;
		}
		return -1;
	}

	public static Color getVedicPlanetColor(Planet p) {
		switch (p) {
			case Sun:
				return new Color(225, 110, 30);  // Solar Warm Amber / Orange
			case Moon:
				return new Color(90, 150, 205);  // Pearl Silver-Blue
			case Mars:
				return new Color(215, 60, 50);   // Coral Red
			case Mercury:
				return new Color(40, 155, 75);   // Emerald Green
			case Jupiter:
				return new Color(225, 160, 20);  // Royal Saffron Gold
			case Venus:
				return new Color(195, 85, 140);  // Rose Pink / Brilliant Luster
			case Saturn:
				return new Color(60, 75, 135);   // Cosmic Indigo / Slate Blue
			default:
				return UIConsts.DARK_BLUE;
		}
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		int index = getRowAt(e.getPoint());
		if (index >= 0 && index < items.size()) {
			PlanetBalaItem item = items.get(index);
			String rankStr = item.rank > 0 ? Roman.of(item.rank).toString() : "-";
			String minRupaStr = RUPA_FMT.format(item.minVirupas / 60.0);
			String statusColor = item.balaPercent >= 100.0 ? "#006400" : "#B22222";

			StringBuilder sb = new StringBuilder();
			sb.append("<html><div style='padding:4px; font-family:Verdana,sans-serif; font-size:11px;'>");
			sb.append("<b style='color:#00008C; font-size:12px;'>").append(item.planet.name()).append("</b>");
			sb.append("&nbsp;<span style='color:#800000;'>(").append(rankStr).append(")</span>");
			sb.append("<hr style='border:0; border-top:1px solid #CCC; margin:4px 0;'>");
			sb.append("<table cellpadding='1' cellspacing='0'>");
			sb.append("<tr><td><b>Shadbala:</b>&nbsp;</td><td>").append(VAL_FMT.format(item.virupas)).append(" Virupas</td></tr>");
			sb.append("<tr><td><b>Rupa:</b>&nbsp;</td><td>").append(RUPA_FMT.format(item.rupa)).append("</td></tr>");
			sb.append("<tr><td><b>Bala %:</b>&nbsp;</td><td><b style='color:").append(statusColor).append(";'>")
			  .append(PCT_FMT.format(item.balaPercent)).append("</b></td></tr>");
			sb.append("<tr><td><b>Required:</b>&nbsp;</td><td>").append(VAL_FMT.format(item.minVirupas)).append(" Virupas (")
			  .append(minRupaStr).append(" Rupa)</td></tr>");
			sb.append("</table></div></html>");
			return sb.toString();
		}
		return null;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int currentW = getWidth();
		if (currentW <= 0) currentW = getPreferredSize() != null ? getPreferredSize().width : CARD_WIDTH;
		int currentH = getHeight();
		if (currentH <= 0) currentH = getPreferredSize() != null ? getPreferredSize().height : CARD_HEIGHT;

		int boxLeft = BOX_LEFT;
		int boxRight = currentW - 3;
		int boxTop = BOX_TOP;
		int boxBottom = currentH - 8;

		title.setBounds(0, 0, currentW, 20);

		// Layout geometry
		int trackX = Math.min(85, Math.max(70, currentW / 5));
		int pctWidth = 52;
		int trackWidth = Math.max(80, boxRight - trackX - pctWidth);

		// Draw bounded border matching RelativePlanetView
		drawCardBorder(g2, boxLeft, boxRight, boxTop, boxBottom);

		// Calculate 100% reference line X
		int x100 = trackX + (int) Math.round((100.0 / scaleMax) * trackWidth);

		// Draw 100% reference dashed line & label
		int barTopY = START_Y + (ROW_HEIGHT - BAR_HEIGHT) / 2;
		int barBottomY = START_Y + (items.size() - 1) * ROW_HEIGHT + (ROW_HEIGHT - BAR_HEIGHT) / 2 + BAR_HEIGHT;
		int lineTop = barTopY - 2;
		int lineBottom = barBottomY + 2;

		Stroke origStroke = g2.getStroke();
		Stroke dashed = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f);
		g2.setStroke(dashed);
		g2.setColor(new Color(185, 170, 150));
		g2.drawLine(x100, lineTop, x100, lineBottom);
		g2.setStroke(origStroke);

		// 100% marker label
		g2.setFont(new Font("Verdana", Font.PLAIN, 9));
		g2.setColor(new Color(140, 120, 100));
		String markStr = "100% Min";
		FontMetrics fmMark = g2.getFontMetrics();
		g2.drawString(markStr, x100 - fmMark.stringWidth(markStr) / 2, lineTop - 3);

		// Fonts
		Font labelFont = new Font("Verdana", Font.BOLD, 11);
		Font pctFont = new Font("Verdana", Font.BOLD, 10);
		FontMetrics fmLabel = g2.getFontMetrics(labelFont);
		FontMetrics fmPct = g2.getFontMetrics(pctFont);

		// Draw each planet row
		for (int i = 0; i < items.size(); i++) {
			PlanetBalaItem item = items.get(i);
			int rowY = START_Y + i * ROW_HEIGHT;
			int barY = rowY + (ROW_HEIGHT - BAR_HEIGHT) / 2;

			// 1. Planet Name (Right-aligned at trackX - 8)
			g2.setFont(labelFont);
			g2.setColor(UIConsts.DARK_BLUE);
			String pName = item.planet.name();
			int nameW = fmLabel.stringWidth(pName);
			g2.drawString(pName, trackX - 8 - nameW, barY + BAR_HEIGHT - 3);

			// 2. Background Track
			g2.setColor(new Color(243, 240, 235));
			g2.fillRoundRect(trackX, barY, trackWidth, BAR_HEIGHT, 6, 6);

			// 3. Colored Bar
			int barWidth = (int) Math.round((item.balaPercent / scaleMax) * trackWidth);
			barWidth = Math.min(barWidth, trackWidth);
			Color vedicClr = getVedicPlanetColor(item.planet);

			g2.setColor(vedicClr);
			g2.fillRoundRect(trackX, barY, barWidth, BAR_HEIGHT, 6, 6);

			// Hover highlight
			if (i == hoveredIndex) {
				g2.setColor(UIConsts.DARK_BLUE);
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(trackX, barY, Math.max(barWidth, 6), BAR_HEIGHT, 6, 6);
				g2.setStroke(origStroke);
			}

			// 4. Bala % Label (at trackX + trackWidth + 6)
			g2.setFont(pctFont);
			g2.setColor(item.balaPercent >= 100.0 ? new Color(0, 100, 0) : new Color(175, 40, 40));
			String pctStr = PCT_FMT.format(item.balaPercent);
			g2.drawString(pctStr, trackX + trackWidth + 6, barY + BAR_HEIGHT - 3);
		}

		g2.dispose();
	}

	private void drawCardBorder(Graphics2D g2, int boxLeft, int boxRight, int boxTop, int boxBottom) {
		int arc = 12;
		int gap = 8;

		FontMetrics fm = title.getFontMetrics(title.getFont());
		int textW = fm.stringWidth(title.getText());
		int textX = (getWidth() - textW) / 2;

		Path2D p = new Path2D.Double();
		p.moveTo(boxLeft + arc, boxTop);
		p.lineTo(Math.max(boxLeft + arc, textX - gap), boxTop);

		p.moveTo(Math.min(boxRight - arc, textX + textW + gap), boxTop);
		p.lineTo(boxRight - arc, boxTop);

		p.quadTo(boxRight, boxTop, boxRight, boxTop + arc);
		p.lineTo(boxRight, boxBottom - arc);

		p.quadTo(boxRight, boxBottom, boxRight - arc, boxBottom);
		p.lineTo(boxLeft + arc, boxBottom);

		p.quadTo(boxLeft, boxBottom, boxLeft, boxBottom - arc);
		p.lineTo(boxLeft, boxTop + arc);

		p.quadTo(boxLeft, boxTop, boxLeft + arc, boxTop);

		g2.setColor(new Color(210, 180, 120)); // Warm gold border matching Ashtakavarga & Offsets
		g2.setStroke(new BasicStroke(1.5f));
		g2.draw(p);
	}
}
