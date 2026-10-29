/**
 * AshtavargaView.java
 * Created On 2006, Apr 1, 2006 7:59:50 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import app.astrosoft.beans.AshtaVargaChartData;
import app.astrosoft.consts.AshtavargaName;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Ashtavarga;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;

public class AshtavargaView extends AstrosoftView {
	
	private static final Dimension chartSize = new Dimension(360, 320);
	private static final Dimension panelSize = new Dimension(424, 400);
	private static final Dimension viewSize = new Dimension(panelSize.width + 30, panelSize.height + 40);

	public static class AshtavargaItem {
		private final String displayName;
		private final String chartCode;
		private final AshtavargaName vargaName;

		public AshtavargaItem(String displayName, String chartCode, AshtavargaName vargaName) {
			this.displayName = displayName;
			this.chartCode = chartCode;
			this.vargaName = vargaName;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getChartCode() {
			return chartCode;
		}

		public AshtavargaName getVargaName() {
			return vargaName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	public AshtavargaView(String title, Ashtavarga ashtavarga, Point loc) {
		this(title, ashtavarga, null, null, loc);
	}

	public AshtavargaView(String title, Ashtavarga ashtavarga, Rasi ascendant, Rasi moonRasi, Point loc) {
		super(title, viewSize, loc);
		add(createAshtavargaPanel(ashtavarga, ascendant, moonRasi), BorderLayout.CENTER);
	}

	public static JPanel createAshtavargaPanel(final Ashtavarga ashtavarga) {
		return createAshtavargaPanel(ashtavarga, null, null);
	}

	public static JPanel createAshtavargaPanel(final Ashtavarga ashtavarga, final Rasi ascendant, final Rasi moonRasi) {
		final AshtavargaItem[] items = new AshtavargaItem[] {
			new AshtavargaItem("SAV (Sarva Ashtakavarga)", "SAV", AshtavargaName.SarvaAshtavarga),
			new AshtavargaItem("Sun (Su)", "Su", AshtavargaName.Sun),
			new AshtavargaItem("Moon (Mo)", "Mo", AshtavargaName.Moon),
			new AshtavargaItem("Mars (Ma)", "Ma", AshtavargaName.Mars),
			new AshtavargaItem("Mercury (Me)", "Me", AshtavargaName.Mercury),
			new AshtavargaItem("Jupiter (Ju)", "Ju", AshtavargaName.Jupiter),
			new AshtavargaItem("Venus (Ve)", "Ve", AshtavargaName.Venus),
			new AshtavargaItem("Saturn (Sa)", "Sa", AshtavargaName.Saturn)
		};

		final Font titleFont = UIUtil.getFont(Font.BOLD, 16);
		final Chart[] charts = new Chart[items.length];
		for (int i = 0; i < items.length; i++) {
			charts[i] = new Chart(new AshtaVargaChartData(items[i].getChartCode(), ashtavarga.getAshtavarga(items[i].getVargaName()), ascendant, moonRasi), chartSize);
			charts[i].setTitleFont(titleFont);
		}

		final JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
		headerPanel.setOpaque(false);
		headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel label = new JLabel("Ashtavarga");
		label.setFont(new Font("Verdana", Font.BOLD, 12));
		label.setForeground(UIConsts.DARK_BLUE);
		headerPanel.add(label);

		final JComboBox<AshtavargaItem> combo = new JComboBox<AshtavargaItem>(items);
		combo.setFont(new Font("Verdana", Font.PLAIN, 12));
		headerPanel.add(combo);

		final int cardWidth = 424;
		final int cardHeight = 400;
		final Dimension panelSize = new Dimension(cardWidth, cardHeight);

		final JPanel chartContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		chartContainer.setOpaque(false);
		chartContainer.setPreferredSize(chartSize);
		chartContainer.add(charts[0]);

		final JPanel mainPanel = new JPanel(null) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				int arc = 12;
				int gap = 8;

				int boxLeft = 2;
				int boxRight = 422;
				int boxTop = 14;
				int boxBottom = 394;

				int headerX = headerPanel.getX();
				int headerW = headerPanel.getWidth();

				Path2D p = new Path2D.Double();
				// Top-left line running snug to header
				p.moveTo(boxLeft + arc, boxTop);
				p.lineTo(Math.max(boxLeft + arc, headerX - gap), boxTop);

				// Top-right line from header end to right corner
				p.moveTo(Math.min(boxRight - arc, headerX + headerW + gap), boxTop);
				p.lineTo(boxRight - arc, boxTop);

				// Top-right rounded corner
				p.quadTo(boxRight, boxTop, boxRight, boxTop + arc);

				// Right vertical border (exactly 30px away from chart: 422 - 392 = 30)
				p.lineTo(boxRight, boxBottom - arc);

				// Bottom-right rounded corner
				p.quadTo(boxRight, boxBottom, boxRight - arc, boxBottom);

				// Bottom horizontal border (exactly 30px away from chart: 394 - 364 = 30)
				p.lineTo(boxLeft + arc, boxBottom);

				// Bottom-left rounded corner
				p.quadTo(boxLeft, boxBottom, boxLeft, boxBottom - arc);

				// Left vertical border (exactly 30px away from chart: 32 - 2 = 30)
				p.lineTo(boxLeft, boxTop + arc);

				// Top-left rounded corner
				p.quadTo(boxLeft, boxTop, boxLeft + arc, boxTop);

				g2.setColor(Color.decode("#d0a65c")); // Warm gold-ochre border
				g2.setStroke(new BasicStroke(1.8f));
				g2.draw(p);
				g2.dispose();
			}
		};

		mainPanel.setOpaque(false);
		mainPanel.setPreferredSize(panelSize);
		mainPanel.setMinimumSize(panelSize);
		mainPanel.setMaximumSize(panelSize);

		Dimension hd = headerPanel.getPreferredSize();
		int headerX = (cardWidth - hd.width) / 2;
		headerPanel.setBounds(headerX, 0, hd.width, 28);
		chartContainer.setBounds(32, 44, 360, 320);

		mainPanel.add(headerPanel);
		mainPanel.add(chartContainer);

		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = combo.getSelectedIndex();
				if (idx >= 0 && idx < charts.length) {
					chartContainer.removeAll();
					chartContainer.add(charts[idx]);
					chartContainer.revalidate();
					chartContainer.repaint();
				}
			}
		});

		return mainPanel;
	}

	private static final Dimension miniChartSize = new Dimension(196, 162);
	public static final Dimension gridPanelSize = new Dimension(858, 380);

	public static JPanel createAshtavargaGridPanel(final Ashtavarga ashtavarga) {
		return createAshtavargaGridPanel(ashtavarga, null, null);
	}

	public static JPanel createAshtavargaGridPanel(final Ashtavarga ashtavarga, final Rasi ascendant, final Rasi moonRasi) {
		final AshtavargaItem[] items = new AshtavargaItem[] {
			new AshtavargaItem("SAV", "SAV", AshtavargaName.SarvaAshtavarga),
			new AshtavargaItem("Su", "Su", AshtavargaName.Sun),
			new AshtavargaItem("Mo", "Mo", AshtavargaName.Moon),
			new AshtavargaItem("Ma", "Ma", AshtavargaName.Mars),
			new AshtavargaItem("Me", "Me", AshtavargaName.Mercury),
			new AshtavargaItem("Ju", "Ju", AshtavargaName.Jupiter),
			new AshtavargaItem("Ve", "Ve", AshtavargaName.Venus),
			new AshtavargaItem("Sa", "Sa", AshtavargaName.Saturn)
		};

		final String[] fullNames = new String[] {
			"SAV (Sarva Ashtakavarga)",
			"Sun (Surya)",
			"Moon (Chandra)",
			"Mars (Mangala)",
			"Mercury (Budha)",
			"Jupiter (Guru)",
			"Venus (Shukra)",
			"Saturn (Shani)"
		};

		final Chart[] charts = new Chart[items.length];
		final Font titleFont = UIUtil.getFont(Font.BOLD, 13);
		final Color badgeBg = new Color(254, 243, 199);
		final Color badgeBorder = new Color(212, 163, 75);
		final Color badgeText = new Color(26, 54, 93);

		for (int i = 0; i < items.length; i++) {
			charts[i] = new Chart(new AshtaVargaChartData(items[i].getChartCode(), ashtavarga.getAshtavarga(items[i].getVargaName()), ascendant, moonRasi, items[i].getVargaName()), miniChartSize);
			charts[i].setTitleFont(titleFont);
			charts[i].setToolTipText(fullNames[i]);
			charts[i].setTitleBadge(badgeBg, badgeBorder, badgeText);
		}

		final JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
		headerPanel.setOpaque(false);
		headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel label = new JLabel("Ashtavarga");
		label.setFont(new Font("Verdana", Font.BOLD, 12));
		label.setForeground(UIConsts.DARK_BLUE);
		headerPanel.add(label);

		final int cardWidth = gridPanelSize.width;
		final int cardHeight = gridPanelSize.height;
		final Dimension panelSize = gridPanelSize;

		final int hgap = 14;
		final int vgap = 12;

		final int gridW = 4 * miniChartSize.width + 3 * hgap;
		final int gridH = 2 * miniChartSize.height + vgap;
		final int gridX = (cardWidth - gridW) / 2;
		final int gridY = 28;

		final JPanel gridContainer = new JPanel(new GridLayout(2, 4, hgap, vgap));
		gridContainer.setOpaque(false);
		for (Chart c : charts) {
			gridContainer.add(c);
		}

		final JPanel mainPanel = new JPanel(null) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				int arc = 12;
				int gap = 8;

				int boxLeft = 2;
				int boxRight = cardWidth - 2;
				int boxTop = 14;
				int boxBottom = cardHeight - 4;

				int headerX = headerPanel.getX();
				int headerW = headerPanel.getWidth();

				// Grid delineators (warm gold-ochre dividers across charts)
				int x1 = gridX + miniChartSize.width + hgap / 2;
				int x2 = gridX + 2 * miniChartSize.width + hgap + hgap / 2;
				int x3 = gridX + 3 * miniChartSize.width + 2 * hgap + hgap / 2;
				int yDiv = gridY + miniChartSize.height + vgap / 2;

				final Color dividerColor = UIConsts.ASHTAVARGA_DIVIDER_CLR;
				g2.setColor(dividerColor);
				g2.setStroke(new BasicStroke(2.0f));

				// Horizontal divider dividing Row 1 and Row 2
				g2.drawLine(boxLeft, yDiv, boxRight, yDiv);

				// 3 vertical dividers dividing the 4 columns
				g2.drawLine(x1, boxTop, x1, boxBottom);
				g2.drawLine(x3, boxTop, x3, boxBottom);
				int headerBottom = Math.max(boxTop, headerPanel.getY() + headerPanel.getHeight() - 4);
				g2.drawLine(x2, headerBottom, x2, boxBottom);

				Path2D p = new Path2D.Double();
				// Top-left line running snug to header
				p.moveTo(boxLeft + arc, boxTop);
				p.lineTo(Math.max(boxLeft + arc, headerX - gap), boxTop);

				// Top-right line from header end to right corner
				p.moveTo(Math.min(boxRight - arc, headerX + headerW + gap), boxTop);
				p.lineTo(boxRight - arc, boxTop);

				// Top-right rounded corner
				p.quadTo(boxRight, boxTop, boxRight, boxTop + arc);

				// Right vertical border
				p.lineTo(boxRight, boxBottom - arc);

				// Bottom-right rounded corner
				p.quadTo(boxRight, boxBottom, boxRight - arc, boxBottom);

				// Bottom horizontal border
				p.lineTo(boxLeft + arc, boxBottom);

				// Bottom-left rounded corner
				p.quadTo(boxLeft, boxBottom, boxLeft, boxBottom - arc);

				// Left vertical border
				p.lineTo(boxLeft, boxTop + arc);

				// Top-left rounded corner
				p.quadTo(boxLeft, boxTop, boxLeft + arc, boxTop);

				g2.setColor(Color.decode("#d0a65c")); // Warm gold-ochre border
				g2.setStroke(new BasicStroke(1.8f));
				g2.draw(p);
				g2.dispose();
			}
		};

		mainPanel.setOpaque(false);
		mainPanel.setPreferredSize(panelSize);
		mainPanel.setMinimumSize(panelSize);
		mainPanel.setMaximumSize(panelSize);

		Dimension hd = headerPanel.getPreferredSize();
		int headerX = (cardWidth - hd.width) / 2;
		headerPanel.setBounds(headerX, 0, hd.width, 28);

		gridContainer.setBounds(gridX, gridY, gridW, gridH);

		mainPanel.add(headerPanel);
		mainPanel.add(gridContainer);

		return mainPanel;
	}

	/**
	 * Deprecated alias for backwards compatibility.
	 */
	public static javax.swing.JComponent createTabbedPane(Ashtavarga ashtavarga) {
		return createAshtavargaPanel(ashtavarga);
	}

}
