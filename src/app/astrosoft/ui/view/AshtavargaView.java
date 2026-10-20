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
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;

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

		JLabel label = new JLabel("Ashtakavarga Chart:");
		label.setFont(UIUtil.getFont(Font.BOLD, 12));
		label.setForeground(UIConsts.DARK_BLUE);
		headerPanel.add(label);

		final JComboBox<AshtavargaItem> combo = new JComboBox<AshtavargaItem>(items);
		combo.setFont(UIUtil.getFont(Font.PLAIN, 12));
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

				g2.setColor(new Color(210, 180, 120)); // Warm gold border
				g2.setStroke(new BasicStroke(1.5f));
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

	/**
	 * Deprecated alias for backwards compatibility.
	 */
	public static javax.swing.JComponent createTabbedPane(Ashtavarga ashtavarga) {
		return createAshtavargaPanel(ashtavarga);
	}

}
