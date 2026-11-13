/**
 * AshtavargaView.java
 * Created On 2006, Apr 1, 2006 7:59:50 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import app.astrosoft.beans.AshtaVargaChartData;
import app.astrosoft.consts.AshtavargaName;
import app.astrosoft.consts.AshtavargaStrength;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.core.Ashtavarga;
import app.astrosoft.ui.comp.AstrosoftTabbedPane;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;

public class AshtavargaView extends AstrosoftView {
	
	private static final Dimension tabbedPaneSize = new Dimension(750, 380);
	private static final Dimension viewSize = new Dimension(
			tabbedPaneSize.width, tabbedPaneSize.height + 50);
	
	private static final Dimension tabChartSize = new Dimension((int)(viewSize.width * 0.32), (int)(viewSize.height * 0.55));
	private static final Dimension gunaSize = new Dimension((int)(viewSize.width * 0.32), (int)(viewSize.height * 0.20));

	private static final Dimension chartSize = new Dimension(360, 320);
	private static final Dimension panelSize = new Dimension(424, 400);

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
		this(title, ashtavarga, null, null, null, loc);
	}

	public AshtavargaView(String title, Ashtavarga ashtavarga, Rasi ascendant, Rasi moonRasi, Point loc) {
		this(title, ashtavarga, ascendant, moonRasi, null, loc);
	}

	public AshtavargaView(String title, Ashtavarga ashtavarga, Rasi ascendant, Rasi moonRasi, EnumMap<Planet, Rasi> planetRasis, Point loc) {
		super(title, viewSize, loc);
		add(createTabbedPane(ashtavarga, ascendant, moonRasi, planetRasis), BorderLayout.CENTER);
	}

	private static final Dimension miniChartSize = new Dimension(196, 162);
	public static final Dimension gridPanelSize = new Dimension(858, 380);

	public static final String TAB_CHARTS = "CHARTS";
	public static final String TAB_PRASTHARA = "PRASTHARA";

	public static JPanel createAshtavargaGridPanel(final Ashtavarga ashtavarga) {
		return createAshtavargaGridPanel(ashtavarga, null, null, null);
	}

	public static JPanel createAshtavargaGridPanel(final Ashtavarga ashtavarga, final Rasi ascendant, final Rasi moonRasi) {
		return createAshtavargaGridPanel(ashtavarga, ascendant, moonRasi, null);
	}

	public static JPanel createAshtavargaGridPanel(final Ashtavarga ashtavarga, final Rasi ascendant, final Rasi moonRasi, final EnumMap<Planet, Rasi> planetRasis) {
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
		final Color badgeText = UIConsts.TEXT_DARK_NAVY;

		for (int i = 0; i < items.length; i++) {
			AshtavargaName vName = items[i].getVargaName();
			Planet p = vName != null ? vName.toPlanet() : null;
			Rasi natalRasi = (p != null && planetRasis != null) ? planetRasis.get(p) : (p == Planet.Moon ? moonRasi : null);
			charts[i] = new Chart(new AshtaVargaChartData(items[i].getChartCode(), ashtavarga != null ? ashtavarga.getAshtavarga(items[i].getVargaName()) : null, ascendant, moonRasi, items[i].getVargaName(), natalRasi), miniChartSize);
			charts[i].setTitleFont(titleFont);
			charts[i].setToolTipText(fullNames[i]);
			charts[i].setTitleBadge(badgeBg, badgeBorder, badgeText);
		}

		final int cardWidth = gridPanelSize.width;
		final int cardHeight = gridPanelSize.height;
		final Dimension panelSize = gridPanelSize;

		final int hgap = 14;
		final int vgap = 12;

		final int gridW = 4 * miniChartSize.width + 3 * hgap;
		final int gridH = 2 * miniChartSize.height + vgap;

		final JPanel gridContainer = new JPanel(new GridLayout(2, 4, hgap, vgap));
		gridContainer.setOpaque(false);
		for (Chart c : charts) {
			gridContainer.add(c);
		}

		// Card 1: 8-Chart Grid View (stretches dynamically with container size)
		final JPanel chartsCard = new JPanel(null) {
			@Override
			public void doLayout() {
				int w = getWidth();
				int h = getHeight();
				int padX = 12;
				int padY = 8;
				int availW = Math.max(gridW, w - 2 * padX);
				int availH = Math.max(gridH, h - 2 * padY);
				int gx = Math.max(0, (w - availW) / 2);
				int gy = Math.max(0, (h - availH) / 2);
				gridContainer.setBounds(gx, gy, availW, availH);
				gridContainer.doLayout();
			}
		};
		chartsCard.setName("ChartsCard");
		chartsCard.setOpaque(false);
		chartsCard.add(gridContainer);

		// Card 2: 96-Cell Prasthara Chakra View (with Su..Sa + Asc target strip)
		final PrastharaCardPanel prastharaCard = new PrastharaCardPanel(ashtavarga, ascendant, moonRasi, planetRasis);
		prastharaCard.setName("PrastharaCard");

		final CardLayout contentCardLayout = new CardLayout();
		final JPanel contentCards = new JPanel(contentCardLayout);
		contentCards.setName("AshtavargaContentCards");
		contentCards.setOpaque(false);
		contentCards.add(chartsCard, TAB_CHARTS);
		contentCards.add(prastharaCard, TAB_PRASTHARA);

		final boolean[] showingCharts = new boolean[] { true };

		final PillTabBar pillBar = new PillTabBar(new String[] { "SAV/BAV", "Prasthara" }, idx -> {
			showingCharts[0] = (idx == 0);
			if (idx == 0) {
				contentCardLayout.show(contentCards, TAB_CHARTS);
			} else {
				contentCardLayout.show(contentCards, TAB_PRASTHARA);
			}
			contentCards.revalidate();
			if (contentCards.getParent() != null) {
				contentCards.getParent().repaint();
			}
		});
		pillBar.setName("AshtavargaPillTabBar");

		final JComponent infoButton = createAshtavargaInfoButton(pillBar);

		final JPanel mainPanel = new JPanel(null) {
			@Override
			public void doLayout() {
				int w = getWidth();
				int h = getHeight();

				Dimension pd = pillBar.getPreferredSize();
				int pillW = pd.width;
				int pillH = pd.height;
				int pillX = w - pillW - 18;
				pillBar.setBounds(pillX, 2, pillW, pillH);

				int infoSize = 18;
				int infoGap = 8;
				int infoX = pillX - infoSize - infoGap;
				int infoY = 2 + Math.max(0, (pillH - infoSize) / 2);
				infoButton.setBounds(infoX, infoY, infoSize, infoSize);

				int contentX = 8;
				int contentY = 28;
				int contentW = Math.max(1, w - 16);
				int contentH = Math.max(1, h - 36);
				contentCards.setBounds(contentX, contentY, contentW, contentH);
				contentCards.doLayout();
				chartsCard.doLayout();
				prastharaCard.doLayout();
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				int arc = 12;
				int gap = 8;

				int curWidth = getWidth();
				int curHeight = getHeight();
				int boxLeft = 2;
				int boxRight = curWidth - 2;
				int boxTop = 14;
				int boxBottom = curHeight - 4;

				// Clean white card background
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(boxLeft, boxTop, boxRight - boxLeft, boxBottom - boxTop, arc, arc);

				if (showingCharts[0]) {
					int curGridX = contentCards.getX() + gridContainer.getX();
					int curGridY = contentCards.getY() + gridContainer.getY();
					int curGridW = gridContainer.getWidth();
					int curGridH = gridContainer.getHeight();
					int cellW = (curGridW - 3 * hgap) / 4;
					int cellH = (curGridH - vgap) / 2;

					// Grid delineators (warm gold-ochre dividers across charts)
					int x1 = curGridX + cellW + hgap / 2;
					int x2 = curGridX + 2 * cellW + hgap + hgap / 2;
					int x3 = curGridX + 3 * cellW + 2 * hgap + hgap / 2;
					int yDiv = curGridY + cellH + vgap / 2;

					final Color dividerColor = UIConsts.ASHTAVARGA_DIVIDER_CLR;
					g2.setColor(dividerColor);
					g2.setStroke(new BasicStroke(2.0f));

					// Horizontal divider dividing Row 1 and Row 2
					g2.drawLine(boxLeft, yDiv, boxRight, yDiv);

					// 3 vertical dividers dividing the 4 columns
					g2.drawLine(x1, boxTop, x1, boxBottom);
					g2.drawLine(x2, boxTop, x2, boxBottom);
					g2.drawLine(x3, boxTop, x3, boxBottom);
				}

				int controlsStart = infoButton.getX();
				int controlsEnd = pillBar.getX() + pillBar.getWidth();

				Path2D p = new Path2D.Double();
				// Smooth continuous top border from top-left corner all the way to controls!
				p.moveTo(boxLeft + arc, boxTop);
				p.lineTo(Math.max(boxLeft + arc, controlsStart - gap), boxTop);

				// Top-right line from controlsEnd to right corner
				int rightStart = Math.min(boxRight - arc, controlsEnd + gap);
				p.moveTo(rightStart, boxTop);
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

		Dimension pd = pillBar.getPreferredSize();
		int pillW = pd.width;
		int pillH = pd.height;
		int pillX = cardWidth - pillW - 18;
		pillBar.setBounds(pillX, 2, pillW, pillH);

		int infoSize = 18;
		int infoGap = 8;
		int infoX = pillX - infoSize - infoGap;
		int infoY = 2 + Math.max(0, (pillH - infoSize) / 2);
		infoButton.setBounds(infoX, infoY, infoSize, infoSize);

		contentCards.setBounds(6, 26, cardWidth - 12, cardHeight - 32);

		mainPanel.add(infoButton);
		mainPanel.add(pillBar);
		mainPanel.add(contentCards);

		return mainPanel;
	}

	/**
	 * Rounded 2-pill tab selector ("Charts" | "Prasthara") matching RelativePlanetView's PillTabBar.
	 */
	public static class PillTabBar extends JPanel {
		private int selectedTab = 0;
		private final List<JButton> buttons = new ArrayList<JButton>();
		private final Consumer<Integer> onTabSelected;

		public PillTabBar(String[] tabs, Consumer<Integer> onTabSelected) {
			super(new FlowLayout(FlowLayout.RIGHT, 4, 0));
			this.onTabSelected = onTabSelected;
			setOpaque(true);
			setBackground(Color.WHITE);

			for (int i = 0; i < tabs.length; i++) {
				final int idx = i;
				final JButton btn = new JButton(tabs[i]) {
					@Override
					protected void paintComponent(Graphics g) {
						Graphics2D g2 = (Graphics2D) g.create();
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

						boolean isSelected = (selectedTab == idx);
						boolean isHover = getModel().isRollover();

						int w = getWidth();
						int h = getHeight();
						int arc = 12;

						if (isSelected) {
							g2.setColor(new Color(44, 82, 130));
							g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
							g2.setColor(new Color(30, 60, 95));
							g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
							setForeground(Color.WHITE);
						} else if (isHover) {
							g2.setColor(new Color(228, 238, 250));
							g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
							g2.setColor(new Color(110, 140, 175));
							g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
							setForeground(new Color(25, 45, 75));
						} else {
							g2.setColor(Color.WHITE);
							g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
							g2.setColor(new Color(145, 165, 188));
							g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
							setForeground(new Color(30, 50, 80));
						}

						FontMetrics fm = g2.getFontMetrics(getFont());
						int textW = fm.stringWidth(getText());
						int textH = fm.getAscent();
						int textX = (w - textW) / 2;
						int textY = (h + textH) / 2 - 2;

						g2.setColor(getForeground());
						g2.setFont(getFont());
						g2.drawString(getText(), textX, textY);
						g2.dispose();
					}
				};

				btn.setName("PillTab_" + tabs[i]);
				btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
				btn.setMargin(new java.awt.Insets(0, 0, 0, 0));
				btn.setFocusPainted(false);
				btn.setBorderPainted(false);
				btn.setContentAreaFilled(false);
				btn.setOpaque(false);
				btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				FontMetrics fm = btn.getFontMetrics(btn.getFont());
				int textW = fm.stringWidth(tabs[i]);
				btn.setPreferredSize(new Dimension(textW + 18, 22));

				btn.addActionListener(e -> selectTab(idx));
				buttons.add(btn);
				add(btn);
			}
		}

		public void selectTab(int idx) {
			if (idx >= 0 && idx < buttons.size()) {
				selectedTab = idx;
				for (JButton b : buttons) {
					b.repaint();
				}
				if (onTabSelected != null) {
					onTabSelected.accept(idx);
				}
			}
		}

		public int getSelectedTab() {
			return selectedTab;
		}

		public List<JButton> getButtons() {
			return buttons;
		}
	}

	/**
	 * Dedicated 96-cell Prasthara Chakra panel with an 8-target selector strip
	 * (Su, Mo, Ma, Me, Ju, Ve, Sa, Asc) and B.V. Raman's 9x13 grid layout.
	 */
	public static class PrastharaCardPanel extends JPanel {

		private static final AshtavargaName[] TARGETS = {
			AshtavargaName.Sun,
			AshtavargaName.Moon,
			AshtavargaName.Mars,
			AshtavargaName.Mercury,
			AshtavargaName.Jupiter,
			AshtavargaName.Venus,
			AshtavargaName.Saturn,
			AshtavargaName.Ascendant
		};

		private static final String[] TARGET_CODES = {
			"Su", "Mo", "Ma", "Me", "Ju", "Ve", "Sa", "Asc"
		};

		private static final String[] TARGET_FULL_NAMES = {
			"Sun (Surya)",
			"Moon (Chandra)",
			"Mars (Mangala)",
			"Mercury (Budha)",
			"Jupiter (Guru)",
			"Venus (Shukra)",
			"Saturn (Shani)",
			"Ascendant (Lagna)"
		};

		private final Ashtavarga ashtavarga;
		private final Rasi ascendant;
		private final Rasi moonRasi;
		private final EnumMap<Planet, Rasi> planetRasis;

		private int selectedTargetIndex = 0;
		private final List<JButton> targetButtons = new ArrayList<JButton>(8);
		private final JLabel summaryBadgeLabel;
		private final PrastharaGridCanvas gridCanvas;

		public PrastharaCardPanel(Ashtavarga ashtavarga, Rasi ascendant, Rasi moonRasi, EnumMap<Planet, Rasi> planetRasis) {
			super(new BorderLayout(0, 6));
			this.ashtavarga = ashtavarga;
			this.ascendant = ascendant;
			this.moonRasi = moonRasi;
			this.planetRasis = planetRasis;

			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

			// Top bar: 8-target selector strip on WEST, active planet summary badge on EAST
			JPanel topBar = new JPanel(new BorderLayout(10, 0));
			topBar.setOpaque(false);

			JPanel stripPanel = new JPanel(new GridLayout(1, 8, 5, 0));
			stripPanel.setOpaque(false);
			stripPanel.setPreferredSize(new Dimension(400, 24));

			for (int i = 0; i < TARGETS.length; i++) {
				final int idx = i;
				JButton btn = new JButton(TARGET_CODES[i]) {
					@Override
					protected void paintComponent(Graphics g) {
						Graphics2D g2 = (Graphics2D) g.create();
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						boolean isSelected = (idx == selectedTargetIndex);
						boolean isHover = getModel().isRollover();

						int w = getWidth();
						int h = getHeight();
						int arc = 10;

						if (isSelected) {
							g2.setColor(new Color(254, 243, 199)); // Warm gold badge matching mini-charts
							g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
							g2.setColor(new Color(212, 163, 75));
							g2.setStroke(new BasicStroke(1.5f));
							g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
							setForeground(UIConsts.TEXT_DARK_NAVY);
						} else if (isHover) {
							g2.setColor(new Color(228, 238, 250));
							g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
							g2.setColor(new Color(110, 140, 175));
							g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
							setForeground(new Color(25, 45, 75));
						} else {
							g2.setColor(new Color(248, 250, 252));
							g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
							g2.setColor(new Color(203, 213, 224));
							g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
							setForeground(new Color(45, 55, 72));
						}

						FontMetrics fm = g2.getFontMetrics(getFont());
						int tw = fm.stringWidth(getText());
						int th = fm.getAscent();
						g2.setColor(getForeground());
						g2.setFont(getFont());
						g2.drawString(getText(), (w - tw) / 2, (h + th) / 2 - 2);
						g2.dispose();
					}
				};

				btn.setName("PrastharaTarget_" + TARGET_CODES[i]);
				btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
				btn.setFocusPainted(false);
				btn.setBorderPainted(false);
				btn.setContentAreaFilled(false);
				btn.setOpaque(false);
				btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				btn.setToolTipText("View 96-cell Prasthara Chakra for " + TARGET_FULL_NAMES[i]);
				btn.addActionListener(e -> selectTarget(idx));

				targetButtons.add(btn);
				stripPanel.add(btn);
			}

			summaryBadgeLabel = new JLabel();
			summaryBadgeLabel.setName("PrastharaSummaryBadge");
			summaryBadgeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
			summaryBadgeLabel.setForeground(UIConsts.TEXT_DARK_NAVY);

			topBar.add(stripPanel, BorderLayout.WEST);
			topBar.add(summaryBadgeLabel, BorderLayout.EAST);
			add(topBar, BorderLayout.NORTH);

			gridCanvas = new PrastharaGridCanvas();
			gridCanvas.setName("PrastharaGridCanvas");
			add(gridCanvas, BorderLayout.CENTER);

			updateSummaryBadge();
		}

		public void selectTarget(int idx) {
			if (idx >= 0 && idx < TARGETS.length) {
				selectedTargetIndex = idx;
				for (JButton b : targetButtons) {
					b.repaint();
				}
				updateSummaryBadge();
				gridCanvas.repaint();
			}
		}

		public void selectTarget(AshtavargaName target) {
			for (int i = 0; i < TARGETS.length; i++) {
				if (TARGETS[i] == target) {
					selectTarget(i);
					return;
				}
			}
		}

		public AshtavargaName getSelectedTarget() {
			return TARGETS[selectedTargetIndex];
		}

		public List<JButton> getTargetButtons() {
			return targetButtons;
		}

		public JLabel getSummaryBadgeLabel() {
			return summaryBadgeLabel;
		}

		private Rasi resolveNatalRasi(Planet p) {
			if (p == null) {
				return null;
			}
			if (p == Planet.Ascendant) {
				return ascendant != null ? ascendant : (planetRasis != null ? planetRasis.get(Planet.Ascendant) : null);
			}
			if (planetRasis != null && planetRasis.get(p) != null) {
				return planetRasis.get(p);
			}
			if (p == Planet.Moon) {
				return moonRasi;
			}
			return null;
		}

		private void updateSummaryBadge() {
			AshtavargaName target = TARGETS[selectedTargetIndex];
			Planet targetPlanet = target.toPlanet();
			Rasi natal = resolveNatalRasi(targetPlanet);
			int totalCount = Ashtavarga.getCount(target);
			String natalStr = natal != null ? ("  •  Natal: " + natal.name() + " (" + natal.sym() + ")") : "";
			String countStr = totalCount > 0 ? ("  •  Total Bindus: " + totalCount) : "";
			summaryBadgeLabel.setText(TARGET_FULL_NAMES[selectedTargetIndex] + natalStr + countStr);
		}

		/**
		 * Custom-rendered 96-cell Prasthara Chakra grid (8 orbital contributors x 12 Rasis + Header & BAV Total rows).
		 */
		private class PrastharaGridCanvas extends JComponent {

			private final List<Planet> contributors = Ashtavarga.getPrastharaContributors();
			private final Rasi[] rasis = Rasi.values();

			PrastharaGridCanvas() {
				setOpaque(false);
				setToolTipText("");
				addMouseMotionListener(new MouseMotionAdapter() {
					@Override
					public void mouseMoved(MouseEvent e) {
						updateTooltipForPoint(e.getPoint());
					}
				});
			}

			private void updateTooltipForPoint(Point pt) {
				if (ashtavarga == null) {
					setToolTipText(null);
					return;
				}
				int w = getWidth();
				int h = getHeight();
				if (w <= 0 || h <= 0) {
					return;
				}
				int contributorColW = 126;
				int totalColW = 52;
				int rasiColW = (w - contributorColW - totalColW) / 12;
				int rowH = h / 10;

				int row = pt.y / Math.max(1, rowH);
				if (row >= 1 && row <= 8 && pt.x >= contributorColW && pt.x < contributorColW + 12 * rasiColW) {
					int col = (pt.x - contributorColW) / Math.max(1, rasiColW);
					if (col >= 0 && col < 12) {
						Planet contributor = contributors.get(row - 1);
						Rasi rasi = rasis[col];
						AshtavargaName target = TARGETS[selectedTargetIndex];
						int bindu = ashtavarga.getPrastharaBindu(target, contributor, rasi);
						Rasi contribNatal = resolveNatalRasi(contributor);
						String houseOffsetStr = "";
						if (contribNatal != null) {
							int houseNum = ((rasi.ordinal() - contribNatal.ordinal() + 12) % 12) + 1;
							houseOffsetStr = " (" + houseNum + ordinalSuffix(houseNum) + " from " + contributor.name() + " in " + contribNatal.sym() + ")";
						}
						setToolTipText(contributor.name() + " → " + target.name() + " in " + rasi.name() + " (" + rasi.sym() + "): "
								+ bindu + (bindu == 1 ? " bindu" : " bindus") + houseOffsetStr);
						return;
					}
				} else if (row == 9 && pt.x >= contributorColW && pt.x < contributorColW + 12 * rasiColW) {
					int col = (pt.x - contributorColW) / Math.max(1, rasiColW);
					if (col >= 0 && col < 12) {
						Rasi rasi = rasis[col];
						AshtavargaName target = TARGETS[selectedTargetIndex];
						EnumMap<Rasi, Integer> bav = ashtavarga.getAshtavarga(target);
						int val = (bav != null && bav.get(rasi) != null) ? bav.get(rasi) : 0;
						AshtavargaStrength strength = AshtavargaStrength.of(false, val);
						setToolTipText(target.name() + " BAV Total in " + rasi.name() + " (" + rasi.sym() + "): " + val + " bindus (" + strength.name() + ")");
						return;
					}
				}
				setToolTipText(null);
			}

			private String ordinalSuffix(int n) {
				if (n >= 11 && n <= 13) return "th";
				switch (n % 10) {
					case 1: return "st";
					case 2: return "nd";
					case 3: return "rd";
					default: return "th";
				}
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				int w = getWidth();
				int h = getHeight();
				if (w <= 0 || h <= 0) {
					g2.dispose();
					return;
				}

				int contributorColW = 126;
				int totalColW = 52;
				int rasiColW = (w - contributorColW - totalColW) / 12;
				int gridW = contributorColW + 12 * rasiColW + totalColW;
				int rowH = h / 10;
				int gridH = 10 * rowH;

				AshtavargaName target = TARGETS[selectedTargetIndex];
				Planet targetPlanet = target.toPlanet();
				Rasi targetNatalRasi = resolveNatalRasi(targetPlanet);
				EnumMap<Rasi, Integer> bavMap = (ashtavarga != null) ? ashtavarga.getAshtavarga(target) : null;

				Font headerFont = new Font(Font.SANS_SERIF, Font.BOLD, 11);
				Font markerFont = new Font(Font.SANS_SERIF, Font.BOLD, 8);
				Font contributorFont = new Font(Font.SANS_SERIF, Font.BOLD, 11);
				Font contributorSubFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
				Font binduOneFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
				Font binduZeroFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
				Font totalRowFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);

				// 1. Paint Row 0: Header Row
				g2.setColor(new Color(241, 245, 249));
				g2.fillRect(0, 0, gridW, rowH);

				g2.setFont(headerFont);
				g2.setColor(UIConsts.TEXT_DARK_NAVY);
				drawCenteredString(g2, "Contributor", 0, 0, contributorColW, rowH);

				for (int c = 0; c < 12; c++) {
					Rasi r = rasis[c];
					int cx = contributorColW + c * rasiColW;
					boolean isTargetNatal = (targetNatalRasi == r);
					if (isTargetNatal) {
						g2.setColor(new Color(254, 243, 199));
						g2.fillRect(cx, 0, rasiColW, rowH);
					}

					g2.setFont(headerFont);
					g2.setColor(isTargetNatal ? UIConsts.getChartNatalHighlightColor() : UIConsts.TEXT_DARK_NAVY);
					drawCenteredString(g2, r.sym(), cx, 2, rasiColW, rowH - 2);

					// Asc and Moon micro-markers in top corners of sign column header
					boolean isAsc = (ascendant == r);
					boolean isMoon = (moonRasi == r);
					if (isAsc) {
						g2.setFont(markerFont);
						g2.setColor(UIConsts.getChartAscendantColor());
						g2.drawString("Asc", cx + 3, 9);
					}
					if (isMoon) {
						g2.setFont(markerFont);
						g2.setColor(UIConsts.getChartMoonColor());
						FontMetrics mfm = g2.getFontMetrics();
						g2.drawString("Mo", cx + rasiColW - mfm.stringWidth("Mo") - 3, 9);
					}
				}

				int totalColX = contributorColW + 12 * rasiColW;
				g2.setFont(headerFont);
				g2.setColor(UIConsts.TEXT_DARK_NAVY);
				drawCenteredString(g2, "Total", totalColX, 0, totalColW, rowH);

				// 2. Paint Rows 1..8: The 8 Contributors in B.V. Raman's descending orbital order
				int grandTotal = 0;
				for (int rIdx = 0; rIdx < 8; rIdx++) {
					Planet contributor = contributors.get(rIdx);
					int ry = (rIdx + 1) * rowH;

					// Alternating subtle row background
					Color rowBg = (rIdx % 2 == 0) ? Color.WHITE : new Color(250, 251, 253);
					g2.setColor(rowBg);
					g2.fillRect(0, ry, gridW, rowH);

					// Contributor label + natal sign tag in Col 0
					g2.setFont(contributorFont);
					g2.setColor(UIConsts.TEXT_DARK_NAVY);
					String cLabel = contributor == Planet.Ascendant ? "Ascendant" : contributor.name();
					FontMetrics cfm = g2.getFontMetrics();
					int textY = ry + (rowH + cfm.getAscent() - cfm.getDescent()) / 2;
					g2.drawString(cLabel, 8, textY);

					Rasi contribNatal = resolveNatalRasi(contributor);
					if (contribNatal != null) {
						g2.setFont(contributorSubFont);
						g2.setColor(UIConsts.LABEL_MUTED_COLOR);
						String symTag = "(" + contribNatal.sym() + ")";
						FontMetrics sfm = g2.getFontMetrics();
						g2.drawString(symTag, contributorColW - sfm.stringWidth(symTag) - 6, textY);
					}

					int rowSum = 0;
					for (int c = 0; c < 12; c++) {
						Rasi rasi = rasis[c];
						int cx = contributorColW + c * rasiColW;
						int bindu = (ashtavarga != null) ? ashtavarga.getPrastharaBindu(target, contributor, rasi) : 0;
						rowSum += bindu;

						if (bindu == 1) {
							g2.setColor(new Color(238, 246, 240)); // Subtle sage-tint for active bindu
							g2.fillRect(cx + 1, ry + 1, rasiColW - 1, rowH - 1);
							g2.setFont(binduOneFont);
							g2.setColor(UIConsts.TEXT_DARK_NAVY);
							drawCenteredString(g2, "1", cx, ry, rasiColW, rowH);
						} else {
							g2.setFont(binduZeroFont);
							g2.setColor(new Color(180, 190, 205));
							drawCenteredString(g2, "0", cx, ry, rasiColW, rowH);
						}
					}

					grandTotal += rowSum;
					// Row total cell
					g2.setColor(new Color(248, 250, 252));
					g2.fillRect(totalColX + 1, ry + 1, totalColW - 1, rowH - 1);
					g2.setFont(contributorFont);
					g2.setColor(new Color(45, 65, 95));
					drawCenteredString(g2, String.valueOf(rowSum), totalColX, ry, totalColW, rowH);
				}

				// 3. Paint Row 9: BAV Summary Footer Row
				int totalRowY = 9 * rowH;
				g2.setColor(new Color(241, 245, 249));
				g2.fillRect(0, totalRowY, contributorColW, rowH);
				g2.setFont(totalRowFont);
				g2.setColor(UIConsts.TEXT_DARK_NAVY);
				drawCenteredString(g2, "Total (BAV)", 0, totalRowY, contributorColW, rowH);

				for (int c = 0; c < 12; c++) {
					Rasi rasi = rasis[c];
					int cx = contributorColW + c * rasiColW;
					int colVal = (bavMap != null && bavMap.get(rasi) != null) ? bavMap.get(rasi) : 0;
					AshtavargaStrength strength = AshtavargaStrength.of(false, colVal);
					Color cellBg = UIConsts.getAshtavargaColor(strength);
					g2.setColor(cellBg);
					g2.fillRect(cx + 1, totalRowY + 1, rasiColW - 1, rowH - 1);

					g2.setFont(totalRowFont);
					boolean isTargetNatal = (targetNatalRasi == rasi);
					g2.setColor(isTargetNatal ? UIConsts.getChartNatalHighlightColor() : UIConsts.TEXT_DARK_NAVY);
					drawCenteredString(g2, String.valueOf(colVal), cx, totalRowY, rasiColW, rowH);
				}

				// Grand total cell (bottom-right)
				g2.setColor(new Color(254, 243, 199));
				g2.fillRect(totalColX + 1, totalRowY + 1, totalColW - 1, rowH - 1);
				g2.setFont(totalRowFont);
				g2.setColor(UIConsts.TEXT_DARK_NAVY);
				drawCenteredString(g2, String.valueOf(grandTotal), totalColX, totalRowY, totalColW, rowH);

				// 4. Grid Lines (9 horizontal + 13 vertical internal lines + outer border)
				Color gridLineClr = new Color(208, 166, 92, 140);
				g2.setColor(gridLineClr);
				g2.setStroke(new BasicStroke(1.0f));

				for (int r = 1; r < 10; r++) {
					int y = r * rowH;
					if (r == 1 || r == 9) {
						g2.setColor(UIConsts.ASHTAVARGA_DIVIDER_CLR);
						g2.setStroke(new BasicStroke(1.5f));
						g2.drawLine(0, y, gridW, y);
						g2.setColor(gridLineClr);
						g2.setStroke(new BasicStroke(1.0f));
					} else {
						g2.drawLine(0, y, gridW, y);
					}
				}

				// Vertical lines
				g2.setColor(UIConsts.ASHTAVARGA_DIVIDER_CLR);
				g2.setStroke(new BasicStroke(1.4f));
				g2.drawLine(contributorColW, 0, contributorColW, gridH);
				g2.drawLine(totalColX, 0, totalColX, gridH);

				g2.setColor(gridLineClr);
				g2.setStroke(new BasicStroke(1.0f));
				for (int c = 1; c < 12; c++) {
					int x = contributorColW + c * rasiColW;
					g2.drawLine(x, 0, x, gridH);
				}

				// Highlight target planet's Natal Rasi column border if present
				if (targetNatalRasi != null) {
					int natalColX = contributorColW + targetNatalRasi.ordinal() * rasiColW;
					g2.setColor(new Color(212, 163, 75));
					g2.setStroke(new BasicStroke(1.6f));
					g2.drawRect(natalColX, 0, rasiColW, gridH - 1);
				}

				// Outer border
				g2.setColor(UIConsts.ASHTAVARGA_DIVIDER_CLR);
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(0, 0, gridW - 1, gridH - 1, 6, 6);

				g2.dispose();
			}

			private void drawCenteredString(Graphics2D g2, String text, int x, int y, int w, int h) {
				FontMetrics fm = g2.getFontMetrics();
				int tx = x + (w - fm.stringWidth(text)) / 2;
				int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
				g2.drawString(text, tx, ty);
			}
		}
	}

	public static JComponent createAshtavargaInfoButton(final Component parent) {
		final JComponent btn = new JComponent() {
			private boolean hovered = false;
			{
				setToolTipText("Ashtavarga Strength Guide");
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				Dimension d = new Dimension(18, 18);
				setPreferredSize(d);
				setMinimumSize(d);
				setMaximumSize(d);
				setSize(d);
				setFocusable(true);

				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						hovered = true;
						repaint();
					}

					@Override
					public void mouseExited(MouseEvent e) {
						hovered = false;
						repaint();
					}

					@Override
					public void mouseClicked(MouseEvent e) {
						app.astrosoft.ui.dlg.AshtavargaGuideDialog.showDialog(parent);
					}
				});

				addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
							app.astrosoft.ui.dlg.AshtavargaGuideDialog.showDialog(parent);
						}
					}
				});
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				int w = getWidth();
				int h = getHeight();
				int size = Math.min(w, h) - 2;
				int x = (w - size) / 2;
				int y = (h - size) / 2;

				// Background circle fill
				Color bg = hovered ? new Color(254, 235, 175) : new Color(254, 243, 199);
				g2.setColor(bg);
				g2.fillOval(x, y, size, size);

				// Border
				Color border = UIConsts.getChartBorderColor();
				g2.setColor(border);
				g2.setStroke(new BasicStroke(1.4f));
				g2.drawOval(x, y, size, size);

				// Centered italic serif "i"
				g2.setFont(new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 11));
				g2.setColor(UIConsts.TEXT_DARK_NAVY);
				FontMetrics fm = g2.getFontMetrics();
				int textW = fm.stringWidth("i");
				int textH = fm.getAscent() - fm.getDescent();
				g2.drawString("i", x + (size - textW) / 2, y + (size + textH) / 2);

				g2.dispose();
			}
		};
		return btn;
	}

	public static AstrosoftTabbedPane createTabbedPane(Ashtavarga ashtavarga) {
		return createTabbedPane(ashtavarga, null, null, null);
	}

	public static AstrosoftTabbedPane createTabbedPane(Ashtavarga ashtavarga, Rasi ascendant, Rasi moonRasi, EnumMap<Planet, Rasi> planetRasis) {
		
		AstrosoftTabbedPane tabbedPane = new AstrosoftTabbedPane(tabbedPaneSize);
		Font font = UIUtil.getFont(Font.BOLD, 12, Font.BOLD, 10);
		
		for (AshtavargaName name : AshtavargaName.ashtavargas()) {
		
			JPanel panel = new JPanel(new BorderLayout());
			
			JPanel vargaPanel = new JPanel();

			Planet p = name.toPlanet();
			Rasi natalRasi = (p != null && planetRasis != null) ? planetRasis.get(p) : (p == Planet.Moon ? moonRasi : null);

			int count = Ashtavarga.getCount(name);
			String displayName = name.toString(Language.ENGLISH);
			if (count != -1) {
				displayName = displayName + " ( " + count + " )";
			}

			AshtaVargaChartData ashtavargaData = new AshtaVargaChartData(
				displayName,
				ashtavarga.getAshtavarga(name),
				ascendant,
				moonRasi,
				name,
				natalRasi
			);
			Chart ashtavargaChart = new Chart(ashtavargaData, tabChartSize);
			ashtavargaChart.setTitleFont(font);
			
			AshtaVargaChartData trikonaData = new AshtaVargaChartData(AshtavargaName.Trikona, ashtavarga.getTrikona(name));
			trikonaData.setOrientationAscendant(ascendant);
			Chart trikonaChart = new Chart(trikonaData, tabChartSize);
			trikonaChart.setTitleFont(font);
			
			AshtaVargaChartData ekathipathyaData = new AshtaVargaChartData(AshtavargaName.Ekathipathya, ashtavarga.getEkathipathiya(name));
			ekathipathyaData.setOrientationAscendant(ascendant);
			Chart ekathipathyaChart = new Chart(ekathipathyaData, tabChartSize);
			ekathipathyaChart.setTitleFont(font);
			
			vargaPanel.add(ashtavargaChart);
			vargaPanel.add(trikonaChart);
			vargaPanel.add(ekathipathyaChart);
			
			JPanel gunaPanel = new JPanel();
			
			if (name != AshtavargaName.SarvaAshtavarga) {
				AstrosoftTable gunaTable = new AstrosoftTable(new AstrosoftTableModel(ashtavarga.getGunaTable(name)), TableStyle.NONE);
				gunaPanel.add(gunaTable);
				gunaTable.setPreferredSize(gunaSize);
			}
			panel.add(vargaPanel, BorderLayout.CENTER);
			panel.add(gunaPanel, BorderLayout.PAGE_END);
			
			tabbedPane.addTab(name.toString(Language.ENGLISH), panel);
		}
		
		return tabbedPane;
	}

}
