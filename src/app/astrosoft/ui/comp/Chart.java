/**
 * Chart.java
 * Created On 2006, Mar 31, 2006 4:56:17 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.comp;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumnModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import app.astrosoft.beans.ChartData;
import app.astrosoft.beans.PlanetCharacter;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.consts.Varga;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.PlanetCellRenderer;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.comp.chart.ChartTooltips;
import app.astrosoft.ui.comp.chart.HouseContentModel;
import app.astrosoft.ui.comp.chart.NorthIndianChartCanvas;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.ui.window.AstrosoftWindow;
import app.astrosoft.ui.window.DefaultWindowLabelModel;
import app.astrosoft.ui.window.WindowLabelModel;

public class Chart extends JPanel{

	public static final Color ASHTAVARGA_WEAK_BG = UIConsts.ASHTAVARGA_WEAK_BG;
	public static final Color ASHTAVARGA_STRONG_BG = UIConsts.ASHTAVARGA_STRONG_BG;
	public static final Color ASHTAVARGA_NEUTRAL_BG = UIConsts.ASHTAVARGA_NEUTRAL_BG;

	private ChartData chartData;
	
	private List<JPanel> housePanel;
	
	private Dimension houseSize;
	
	private Dimension tableSize;
	
	private int rowHeight;
	
	private JLabel title;
	private String subtitle;
	private boolean showHouseNumber = true;

	private boolean showTitleBadge = false;
	private Color badgeBgColor;
	private Color badgeBorderColor;
	private Color badgeTextColor;

	private final Dimension baseChartSize;
	private Dimension lastSouthLayoutSize;
	private NorthIndianChartCanvas northIndianCanvas;
	private JLabel northHeaderTitleLabel;
	private boolean popupRegistered = false;

	private static int fontOffset = 0;
	private static int chartZoomPercent = 100;

	public static int getFontOffset() {
		return fontOffset;
	}

	public static void adjustFontOffset(int delta) {
		fontOffset = Math.max(-4, Math.min(8, fontOffset + delta));
		chartZoomPercent = Math.max(70, Math.min(160, 100 + fontOffset * 10));
	}

	public static void resetFontOffset() {
		fontOffset = 0;
		chartZoomPercent = 100;
	}

	public static int getChartZoomPercent() {
		return chartZoomPercent;
	}

	public static void setChartZoomPercent(int percent) {
		chartZoomPercent = Math.max(70, Math.min(160, percent));
		fontOffset = Math.max(-4, Math.min(8, Math.round((chartZoomPercent - 100) / 10.0f)));
	}

	public void rebuild() {
		this.removeAll();
		if (getBorder() != null) {
			setBorder(UIConsts.getChartBorder());
		}
		addHouseTables();
		revalidate();
		repaint();
	}
	
	public Chart(ChartData chartData, Dimension chartSize) {
		this(chartData, chartSize, null, true);
	}

	public Chart(ChartData chartData, Dimension chartSize, String subtitle) {
		this(chartData, chartSize, subtitle, true);
	}

	public Chart(ChartData chartData, Dimension chartSize, String subtitle, boolean showHouseNumber) {
		this.chartData = chartData;
		this.subtitle = subtitle;
		this.showHouseNumber = showHouseNumber;
		this.baseChartSize = new Dimension(chartSize);
		
		houseSize = new Dimension(chartSize.width / 4, chartSize.height / 4);
		
		tableSize = new Dimension(Math.max(1, houseSize.width - 6), Math.max(3, houseSize.height - 4));
		
		rowHeight = Math.max(14, tableSize.height / 3);
		
		//Thirteen-th fills center
		housePanel = new ArrayList<JPanel>();
		
		title = new TitleLabel(normalizeChartName(chartData.getChartName()));
		
		setLayout(new GridBagLayout());
		//setPreferredSize(new Dimension(chartSize.width + (3 * 4), chartSize.height + (3 * 4)));
		setPreferredSize(chartSize);
		setMinimumSize(chartSize);
		addHouseTables();
		if (chartData instanceof app.astrosoft.beans.AshtaVargaChartData) {
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					if (UIConsts.getChartStyle() == UIConsts.ChartStyle.NORTH_INDIAN) {
						return;
					}
					int cw = getWidth();
					int ch = getHeight();
					if (cw <= 0 || ch <= 0) {
						return;
					}
					if (lastSouthLayoutSize == null
							|| Math.abs(cw - lastSouthLayoutSize.width) > 10
							|| Math.abs(ch - lastSouthLayoutSize.height) > 10) {
						SwingUtilities.invokeLater(() -> {
							if (UIConsts.getChartStyle() != UIConsts.ChartStyle.NORTH_INDIAN
									&& getWidth() > 0 && getHeight() > 0) {
								rebuild();
							}
						});
					}
				}
			});
		}
		setVisible(true);
	}
	
    /** Display absolute sidereal degrees and whole arcminutes; omit seconds without rounding. */
    public static String formatAbsoluteLongitude(Double longitude) {
        return ChartTooltips.formatAbsoluteLongitude(longitude);
    }

    /** Format sign-relative degrees and whole arcminutes (00:00 to 29:59) as offset from start of rasi. */
    public static String formatSignRelativeOrb(Double longitude) {
        return ChartTooltips.formatSignRelativeOrb(longitude);
    }

	private void addHouseTables() {
		if (UIConsts.getChartStyle() == UIConsts.ChartStyle.NORTH_INDIAN) {
			buildNorthIndian();
		} else {
			northIndianCanvas = null;
			northHeaderTitleLabel = null;
			buildSouthIndian();
		}
		if (!(chartData instanceof app.astrosoft.beans.AshtaVargaChartData)) {
			createPopups();
			propagateInheritsPopupMenu(this);
		}
	}

	/** North Indian diamond-and-triangle layout with header strip above the chart square. */
	private void buildNorthIndian() {
		if (housePanel != null) {
			housePanel.clear();
		}
		setLayout(new BorderLayout());
		setBackground(chartData instanceof app.astrosoft.beans.AshtaVargaChartData
				? ASHTAVARGA_NEUTRAL_BG
				: UIConsts.getChartBackground());

		JPanel header = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 6, 2));
		header.setOpaque(false);
		header.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		String rawTitle = title.getText();
		if (showTitleBadge && badgeBgColor != null) {
			northHeaderTitleLabel = null;
			header.add(createTitleBadgePanel());
		} else if (rawTitle != null && rawTitle.toLowerCase(java.util.Locale.ROOT).contains("<html")) {
			northHeaderTitleLabel = new JLabel(stripHtmlTitle(rawTitle), javax.swing.SwingConstants.CENTER);
			northHeaderTitleLabel.setFont(title.getFont());
			northHeaderTitleLabel.setForeground(UIConsts.getChartScoreColor());
			header.add(northHeaderTitleLabel);
		} else {
			northHeaderTitleLabel = null;
			title.setForeground(UIConsts.getChartScoreColor());
			header.add(title);
		}
		if (subtitle != null && !subtitle.trim().isEmpty()) {
			header.add(createSubtitleLabel());
		}
		header.setToolTipText(buildCenterTitleToolTip());
		ToolTipManager.sharedInstance().registerComponent(header);
		add(header, BorderLayout.NORTH);

		northIndianCanvas = new NorthIndianChartCanvas(HouseContentModel.from(chartData), showHouseNumber);
		createPopups();
		add(northIndianCanvas, BorderLayout.CENTER);
	}

	private JLabel createSubtitleLabel() {
		JLabel subTitleLabel = new JLabel(subtitle, javax.swing.SwingConstants.CENTER);
		subTitleLabel.setFont(UIUtil.getFont(Font.BOLD, 11));
		subTitleLabel.setForeground(UIConsts.getChartScoreColor());
		return subTitleLabel;
	}

	private String buildCenterTitleToolTip() {
		String tip = stripHtmlTitle(title.getText()) + " Chart";
		if (subtitle != null && !subtitle.trim().isEmpty()) {
			tip += " " + subtitle;
		}
		return tip;
	}

	private static String stripHtmlTitle(String text) {
		if (text == null) {
			return "";
		}
		return text.replaceAll("(?i)<br\\s*/?>", " ")
				.replaceAll("<[^>]+>", "")
				.replaceAll("\\s+", " ")
				.trim();
	}

	private static String normalizeChartName(String name) {
		if ("Navamsa".equalsIgnoreCase(name)) {
			return "Navamsha";
		}
		return name;
	}

	private JPanel createTitleBadgePanel() {
		JPanel badge = new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int arc = getHeight();
				g2.setColor(badgeBgColor);
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
				if (badgeBorderColor != null) {
					g2.setColor(badgeBorderColor);
					g2.setStroke(new BasicStroke(1.2f));
					g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
				}
				g2.dispose();
			}
		};
		badge.setOpaque(false);
		badge.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
		title.setFont(UIUtil.getFont(Font.BOLD, 12));
		title.setForeground(badgeTextColor != null ? badgeTextColor : Color.WHITE);
		badge.add(title);
		return badge;
	}

	public NorthIndianChartCanvas getNorthIndianCanvas() {
		return northIndianCanvas;
	}

	/** Classic South Indian layout: fixed signs on a 4x4 grid, Pisces top-left, clockwise. */
	private void buildSouthIndian() {
		setLayout(new GridBagLayout());
		if (housePanel != null) {
			housePanel.clear();
		}
		
		Set<Rasi> dataHouses = chartData.getHouses();
		boolean isPlanetChart = (chartData instanceof PlanetChartData);
		int houseNo;
		String toolTip;
		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
		toolTipManager.setInitialDelay(200);
		toolTipManager.setReshowDelay(100);
		toolTipManager.setDismissDelay(15000);
		Rasi ascendant = chartData.getAscendant();
		
		int vGap = Math.max(1, (houseSize.height - (rowHeight * 3) - 2) / 2);
		int hGap = Math.max(1, (houseSize.width - tableSize.width - 2) / 2);

		int maxPlanets = 1;
		if (chartData instanceof PlanetChartData) {
			PlanetChartData pcd = (PlanetChartData) chartData;
			if (pcd.getPlanetsInRasi() != null) {
				for (List<Planet> pList : pcd.getPlanetsInRasi().values()) {
					if (pList != null && pList.size() > maxPlanets) {
						maxPlanets = pList.size();
					}
				}
			}
		}

		int uniformFontSize;
		if (houseSize.height < 68) {
			int baseCompact = (houseSize.height < 52) ? 9 : 10;
			uniformFontSize = Math.max(8, baseCompact + fontOffset);
		} else if (houseSize.height < 80) {
			int baseMedium = (maxPlanets <= 2) ? 14 : ((maxPlanets <= 4) ? 12 : 11);
			uniformFontSize = Math.max(9, baseMedium + fontOffset);
		} else {
			int baseLarge = (maxPlanets <= 2) ? 15 : ((maxPlanets <= 4) ? 14 : 12);
			uniformFontSize = Math.max(9, baseLarge + fontOffset);
		}

		int effW = (chartData instanceof app.astrosoft.beans.AshtaVargaChartData && getWidth() > 0)
				? getWidth() : baseChartSize.width;
		int effH = (chartData instanceof app.astrosoft.beans.AshtaVargaChartData && getHeight() > 0)
				? getHeight() : baseChartSize.height;
		Dimension avHouseSize = new Dimension(Math.max(1, effW / 4), Math.max(1, effH / 4));
		if (chartData instanceof app.astrosoft.beans.AshtaVargaChartData) {
			lastSouthLayoutSize = new Dimension(effW, effH);
		}

		for(Rasi house : Rasi.values()){
			
			houseNo = house.ordinal();
			
			JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, hGap, vGap));
			
			if (chartData instanceof app.astrosoft.beans.AshtaVargaChartData) {
				app.astrosoft.beans.AshtaVargaChartData avData = (app.astrosoft.beans.AshtaVargaChartData) chartData;
				panel.setLayout(new BorderLayout());
				panel.setPreferredSize(avHouseSize);
				panel.setBorder(UIConsts.getChartBorder());

				Rasi asc = avData.getAscendant();
				Rasi moon = avData.getMoonRasi();
				boolean isAsc = (asc == house);
				boolean isMoon = (moon == house);

				boolean isCompact = (avHouseSize.width < 60 || avHouseSize.height < 50);

				JPanel topRow = new JPanel(new BorderLayout());
				topRow.setOpaque(false);
				topRow.setBorder(BorderFactory.createEmptyBorder(isCompact ? 1 : 5, isCompact ? 2 : 7, 0, isCompact ? 2 : 7));

				int markerPt = isCompact
						? (isAsc && isMoon ? 8 : 9)
						: Math.max(11, Math.min(16, (int) Math.round(avHouseSize.height * 0.21)));
				Font markerFont = new Font(Font.SANS_SERIF, Font.BOLD, markerPt);

				if (isAsc && isMoon) {
					JLabel ascLbl = new JLabel("Asc");
					ascLbl.setFont(markerFont);
					ascLbl.setForeground(UIConsts.getChartAscendantColor());
					topRow.add(ascLbl, BorderLayout.WEST);

					JLabel moonLbl = new JLabel("Mo");
					moonLbl.setFont(markerFont);
					moonLbl.setForeground(UIConsts.getChartMoonColor());
					topRow.add(moonLbl, BorderLayout.EAST);
				} else if (isAsc) {
					JLabel ascLbl = new JLabel("Asc");
					ascLbl.setFont(markerFont);
					ascLbl.setForeground(UIConsts.getChartAscendantColor());
					topRow.add(ascLbl, BorderLayout.WEST);
				} else if (isMoon) {
					JLabel moonLbl = new JLabel("Mo");
					moonLbl.setFont(markerFont);
					moonLbl.setForeground(UIConsts.getChartMoonColor());
					topRow.add(moonLbl, BorderLayout.WEST);
				}

				panel.add(topRow, BorderLayout.NORTH);

				Integer val = avData.getVarga().get(house);
				String valStr = (val != null) ? val.toString() : "";

				final boolean isNatal = (avData.getNatalRasi() == house);
				JLabel numLbl;
				int scorePt = isCompact
						? 12
						: Math.max(14, Math.min(24, (int) Math.round(avHouseSize.height * 0.30)));
				Font scoreFont = new Font(Font.SANS_SERIF, Font.BOLD, scorePt);
				if (isNatal) {
					final Color ringColor = UIConsts.getChartNatalHighlightColor();
					numLbl = new JLabel(valStr, javax.swing.SwingConstants.CENTER) {
						@Override
						protected void paintComponent(Graphics g) {
							Graphics2D g2 = (Graphics2D) g.create();
							g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							int w = getWidth();
							int h = getHeight();
							int diameter = Math.min(w, h) - 2;
							if (isCompact) {
								diameter = Math.min(diameter, 22);
							} else {
								diameter = Math.min(diameter, Math.max(30, (int) Math.round(Math.min(w, h) * 0.72)));
							}
							int x = (w - diameter) / 2;
							int y = (h - diameter) / 2;

							// Highlight ring stroke around the score without altering the cell background
							g2.setColor(ringColor);
							g2.setStroke(new BasicStroke(1.8f));
							g2.drawOval(x, y, diameter, diameter);
							g2.dispose();

							super.paintComponent(g);
						}
					};
					numLbl.setFont(scoreFont);
					numLbl.setForeground(UIConsts.getChartScoreColor());
					String tip = "Natal " + (avData.getVargaName() != null ? avData.getVargaName().toString() : "Planet") 
							+ " placement in " + house.toString(Language.ENGLISH) + " (" + valStr + " bindus)";
					numLbl.setToolTipText(tip);
					toolTipManager.registerComponent(numLbl);
				} else {
					numLbl = new JLabel(valStr, javax.swing.SwingConstants.CENTER);
					numLbl.setFont(scoreFont);
					numLbl.setForeground(UIConsts.getChartScoreColor());
				}
				panel.add(numLbl, BorderLayout.CENTER);

				panel.add(Box.createVerticalStrut(isCompact ? 2 : 16), BorderLayout.SOUTH);

				app.astrosoft.consts.AshtavargaStrength strength = avData.getStrength(house);
				panel.setBackground(UIConsts.getAshtavargaColor(strength));

				add(panel, getConstrains(houseNo));
				housePanel.add(houseNo, panel);
				continue;
			}

			if(dataHouses.contains(house)){

				final Rasi curHouse = house;
				Table table = chartData.getChartHouseTable(house);
				AstrosoftTable houseTable = new AstrosoftTable(new AstrosoftTableModel(table), TableStyle.NONE) {
                    @Override
                    public String getToolTipText(java.awt.event.MouseEvent event) {
                        int row = rowAtPoint(event.getPoint());
                        int col = columnAtPoint(event.getPoint());
                        if (row >= 0 && col >= 0 && chartData instanceof PlanetChartData) {
                            Object value = getValueAt(row, col);
                            if (value instanceof Planet) {
                                return ChartTooltips.planetToolTip(chartData, (Planet) value);
                            }
                        }
                        // Keep the existing house tooltip outside occupied planet cells.
                        return super.getToolTipText(event);
                    }
                };
				panel.add(houseTable, getConstrains(houseNo));

				houseTable.setFont(UIUtil.getFont(Font.BOLD, uniformFontSize));
				if (houseSize.height < 68) {
					houseTable.setRowMargin(1);
				}

				int pCount = 0;
				if (chartData instanceof PlanetChartData) {
					PlanetChartData pcd = (PlanetChartData) chartData;
					if (pcd.getPlanetsInRasi() != null && pcd.getPlanetsInRasi().containsKey(house)) {
						List<Planet> pList = pcd.getPlanetsInRasi().get(house);
						if (pList != null) {
							pCount = pList.size();
						}
					}
				}

				if (pCount == 1) {
					TableColumnModel cm = houseTable.getColumnModel();
					if (cm.getColumnCount() >= 3) {
						cm.getColumn(0).setMinWidth(0);
						cm.getColumn(0).setMaxWidth(0);
						cm.getColumn(0).setPreferredWidth(0);
						cm.getColumn(0).setWidth(0);

						cm.getColumn(1).setPreferredWidth(tableSize.width);
						cm.getColumn(1).setWidth(tableSize.width);

						cm.getColumn(2).setMinWidth(0);
						cm.getColumn(2).setMaxWidth(0);
						cm.getColumn(2).setPreferredWidth(0);
						cm.getColumn(2).setWidth(0);
					}
				} else if (pCount == 2) {
					TableColumnModel cm = houseTable.getColumnModel();
					if (cm.getColumnCount() >= 3) {
						int w0 = tableSize.width / 2;
						int w2 = tableSize.width - w0;
						cm.getColumn(0).setPreferredWidth(w0);
						cm.getColumn(0).setWidth(w0);

						cm.getColumn(1).setMinWidth(0);
						cm.getColumn(1).setMaxWidth(0);
						cm.getColumn(1).setPreferredWidth(0);
						cm.getColumn(1).setWidth(0);

						cm.getColumn(2).setPreferredWidth(w2);
						cm.getColumn(2).setWidth(w2);
					}
				}
				houseTable.setRowHeight(rowHeight);
				houseTable.setPreferredSize(tableSize);
				houseTable.setBackground(UIConsts.getChartBackground());
	
				if (isPlanetChart){
					decoratePlanetChart(table, houseTable);
					toolTip = buildHouseToolTip(house);
					houseTable.setToolTipText(toolTip);
					toolTipManager.registerComponent(houseTable);
				}
				//houseTable.addMouseListener(new ChartHouseMouseListener());
			}
			panel.setPreferredSize(houseSize);
			panel.setMinimumSize(houseSize);
			panel.setBackground(UIConsts.getChartBackground());
			
			panel.setBorder(UIConsts.getChartBorder());
			add(panel, getConstrains(houseNo));
			
			if (isPlanetChart){
				toolTip = buildHouseToolTip(house);
				panel.setToolTipText(toolTip);
				toolTipManager.registerComponent(panel);
			}
			
			housePanel.add(houseNo, panel);
			
			//panel.addMouseListener(new ChartHouseMouseListener());
		}
		
		// Add ChartName and optional Subtitle at Center - Vertically and Horizontally Centered in Middle
		houseNo = 12;
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(chartData instanceof app.astrosoft.beans.AshtaVargaChartData ? ASHTAVARGA_NEUTRAL_BG : UIConsts.getChartBackground());
		Dimension centerSize = (chartData instanceof app.astrosoft.beans.AshtaVargaChartData)
				? new Dimension(avHouseSize.width * 2, avHouseSize.height * 2)
				: new Dimension(houseSize.width * 2, houseSize.height * 2);
		panel.setPreferredSize(centerSize);
		if (!(chartData instanceof app.astrosoft.beans.AshtaVargaChartData)) {
			panel.setMinimumSize(centerSize);
		}
		
		title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		
		GridBagConstraints cGbc = new GridBagConstraints();
		cGbc.gridx = 0;
		cGbc.gridy = 0;
		cGbc.anchor = GridBagConstraints.CENTER;

		if (showTitleBadge && badgeBgColor != null) {
			panel.add(createTitleBadgePanel(), cGbc);
		} else {
			title.setForeground(UIConsts.ROSE);
			panel.add(title, cGbc);
		}
		
		if (subtitle != null && !subtitle.trim().isEmpty()) {
			cGbc.gridy = 1;
			cGbc.insets = new java.awt.Insets(2, 0, 0, 0);
			panel.add(createSubtitleLabel(), cGbc);
		}
		
		panel.setToolTipText(buildCenterTitleToolTip());
		toolTipManager.registerComponent(panel);
		add(panel, getConstrains(houseNo));
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
		this.removeAll();
		addHouseTables();
		revalidate();
		repaint();
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setTitleFont(Font font){
		title.setFont(font);
		if (northHeaderTitleLabel != null) {
			northHeaderTitleLabel.setFont(font);
		}
	}

	public void setTitleBadge(Color bgColor, Color borderColor, Color textColor) {
		this.showTitleBadge = true;
		this.badgeBgColor = bgColor;
		this.badgeBorderColor = borderColor;
		this.badgeTextColor = textColor;
		this.removeAll();
		addHouseTables();
		revalidate();
		repaint();
	}

	public void setHouseBackground(Rasi house, Color bg) {
		if (northIndianCanvas != null) {
			northIndianCanvas.setHouseBackground(house, bg);
			return;
		}
		int houseNo = house.ordinal();
		if (houseNo < housePanel.size()) {
			JPanel panel = housePanel.get(houseNo);
			if (panel != null) {
				panel.setBackground(bg);
				for (Component comp : panel.getComponents()) {
					comp.setBackground(bg);
					if (comp instanceof JTable) {
						((JTable) comp).setBackground(bg);
					}
				}
			}
		}
	}

	private String buildHouseToolTip(Rasi house) {
		return ChartTooltips.houseToolTip(chartData, house, showHouseNumber);
	}

	public void setHouseToolTip(Rasi house, String toolTip) {
		if (northIndianCanvas != null) {
			northIndianCanvas.setHouseToolTip(house, toolTip);
			return;
		}
		int houseNo = house.ordinal();
		if (houseNo < housePanel.size()) {
			JPanel panel = housePanel.get(houseNo);
			if (panel != null) {
				panel.setToolTipText(toolTip);
				ToolTipManager.sharedInstance().registerComponent(panel);
				for (Component comp : panel.getComponents()) {
					if (comp instanceof JComponent) {
						((JComponent) comp).setToolTipText(toolTip);
						ToolTipManager.sharedInstance().registerComponent((JComponent) comp);
					}
				}
			}
		}
	}
	
	private void decoratePlanetChart(Table table, AstrosoftTable houseTable) {
		
		PlanetChartData planetChartData = (PlanetChartData) chartData;
		
		boolean highlightRetrogrades = planetChartData.highlightRetrogrades();
		EnumMap<Planet, Boolean> planetDir = planetChartData.getPlanetDir();
		
		for(AstrosoftTableColumn col : table.getColumnMetaData().getVisibleColumns()){
			houseTable.setCellRenderer(new PlanetCellRenderer(houseTable.getCellRenderer(col), highlightRetrogrades, false, planetDir ), col);
		}
		
		createPopups();
	}

	private void createPopups() {
		if (popupRegistered || (chartData instanceof app.astrosoft.beans.AshtaVargaChartData)) {
			return;
		}
		popupRegistered = true;
		final JPopupMenu popup = new JPopupMenu();
		final JMenuItem statusItem = new JMenuItem("Chart Text Zoom: " + chartZoomPercent + "%");
		statusItem.setEnabled(false);
		final JMenuItem zoomInItem = new JMenuItem("Zoom In (+10%)");
		final JMenuItem zoomOutItem = new JMenuItem("Zoom Out (-10%)");
		final JMenuItem resetItem = new JMenuItem("Reset Zoom (100%)");

		zoomInItem.addActionListener(e -> applyPopupZoomChange(chartZoomPercent + 10));
		zoomOutItem.addActionListener(e -> applyPopupZoomChange(chartZoomPercent - 10));
		resetItem.addActionListener(e -> applyPopupZoomChange(100));

		popup.add(statusItem);
		popup.addSeparator();
		popup.add(zoomInItem);
		popup.add(zoomOutItem);
		popup.add(resetItem);

		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				statusItem.setText("Chart Text Zoom: " + chartZoomPercent + "%");
				zoomInItem.setEnabled(chartZoomPercent < 160);
				zoomOutItem.setEnabled(chartZoomPercent > 70);
				resetItem.setEnabled(chartZoomPercent != 100);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

		setComponentPopupMenu(popup);
	}

	private void applyPopupZoomChange(int targetPercent) {
		setChartZoomPercent(targetPercent);
		if (app.astrosoft.ui.AstroSoft.getPreferences() != null) {
			app.astrosoft.ui.AstroSoft.getPreferences().setChartZoom(chartZoomPercent);
		}
		app.astrosoft.ui.AstroSoft appInstance = app.astrosoft.ui.AstroSoft.getInstance();
		if (appInstance != null) {
			appInstance.refreshAllChartsAndCachedViews();
			return;
		}
		boolean rebuiltWindow = false;
		for (Window win : Window.getWindows()) {
			if (win != null && win.isDisplayable()) {
				rebuildFixedChartsInTree(win);
				win.revalidate();
				win.repaint();
				rebuiltWindow = true;
			}
		}
		if (!rebuiltWindow) {
			Container root = SwingUtilities.getAncestorOfClass(Container.class, this);
			if (root != null) {
				rebuildFixedChartsInTree(root);
				root.revalidate();
				root.repaint();
			} else {
				rebuild();
			}
		}
	}

	private static void rebuildFixedChartsInTree(Component comp) {
		if (comp instanceof Chart) {
			Chart c = (Chart) comp;
			if (!(c.getChartData() instanceof app.astrosoft.beans.AshtaVargaChartData)) {
				c.rebuild();
			}
		} else if (comp instanceof Container) {
			for (Component child : ((Container) comp).getComponents()) {
				rebuildFixedChartsInTree(child);
			}
		}
	}

	private static void propagateInheritsPopupMenu(Component comp) {
		if (comp instanceof Container) {
			for (Component child : ((Container) comp).getComponents()) {
				if (child instanceof JComponent) {
					((JComponent) child).setInheritsPopupMenu(true);
				}
				propagateInheritsPopupMenu(child);
			}
		}
	}

	/**
	 * Wraps a primary Rasi + Navamsha chart pair in a unified rounded card matching
	 * the Horoscope Dashboard's card visual language.
	 */
	public static JPanel createDualChartsCard(final Chart rasi, final Chart navamsa) {
		rasi.setBorder(null);
		navamsa.setBorder(null);

		final JLabel headerLabel = new JLabel("Charts", SwingConstants.LEFT);
		headerLabel.setFont(UIUtil.getFont(Font.BOLD, 12));
		headerLabel.setToolTipText("Right-click anywhere on a chart to zoom text size");
		headerLabel.setInheritsPopupMenu(true);

		final JPanel chartsRow = new JPanel(new GridLayout(1, 2, 22, 0));
		chartsRow.setOpaque(false);
		chartsRow.setInheritsPopupMenu(true);
		chartsRow.add(rasi);
		chartsRow.add(navamsa);

		final int cardW = 858;
		final int cardH = 402;

		final JPanel card = new JPanel(null) {
			@Override
			public void doLayout() {
				int w = getWidth() > 0 ? getWidth() : cardW;
				int h = getHeight() > 0 ? getHeight() : cardH;
				FontMetrics fm = headerLabel.getFontMetrics(headerLabel.getFont());
				int labelW = fm.stringWidth(headerLabel.getText()) + 4;
				headerLabel.setBounds(18, 2, labelW, 20);
				chartsRow.setBounds(10, 20, w - 20, h - 36);
				chartsRow.doLayout();
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				int w = getWidth() > 0 ? getWidth() : cardW;
				int h = getHeight() > 0 ? getHeight() : cardH;
				int arc = 12;
				int gap = 8;
				int boxLeft = 2;
				int boxRight = w - 2;
				int boxTop = 12;
				int boxBottom = h - 4;

				g2.setColor(UIConsts.getChartBackground());
				g2.fillRoundRect(boxLeft, boxTop, boxRight - boxLeft, boxBottom - boxTop, arc, arc);

				headerLabel.setForeground(UIConsts.getChartPlanetForeground());
				int textX = headerLabel.getX() > 0 ? headerLabel.getX() : 18;
				FontMetrics fm = headerLabel.getFontMetrics(headerLabel.getFont());
				int textW = fm.stringWidth(headerLabel.getText());

				Path2D p = new Path2D.Double();
				p.moveTo(boxLeft + arc, boxTop);
				p.lineTo(Math.max(boxLeft + arc, textX - gap), boxTop);
				p.moveTo(textX + textW + gap, boxTop);
				p.lineTo(boxRight - arc, boxTop);
				p.quadTo(boxRight, boxTop, boxRight, boxTop + arc);
				p.lineTo(boxRight, boxBottom - arc);
				p.quadTo(boxRight, boxBottom, boxRight - arc, boxBottom);
				p.lineTo(boxLeft + arc, boxBottom);
				p.quadTo(boxLeft, boxBottom, boxLeft, boxBottom - arc);
				p.lineTo(boxLeft, boxTop + arc);
				p.quadTo(boxLeft, boxTop, boxLeft + arc, boxTop);

				g2.setColor(UIConsts.getChartBorderColor());
				g2.setStroke(new BasicStroke(1.5f));
				g2.draw(p);

				g2.dispose();
			}
		};
		card.setName("ChartsCard");
		card.setOpaque(false);
		card.setPreferredSize(new Dimension(cardW, cardH));
		card.setMinimumSize(new Dimension(cardW, cardH));
		card.setMaximumSize(new Dimension(cardW, cardH));
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.add(headerLabel);
		card.add(chartsRow);
		if (rasi.getComponentPopupMenu() != null) {
			card.setComponentPopupMenu(rasi.getComponentPopupMenu());
		}
		return card;
	}

	private class ChartHouseMouseListener extends MouseAdapter{
		
		AstrosoftWindow window;
		
		//Container previous_source;
		
		Container source;
		
		public void mouseEntered(MouseEvent e) {
			
			source = (Container) e.getSource();
			
			if (source instanceof JTable){
				source = ((JTable)source).getParent();
			}
			
			//if (previous_source != null && previous_source == source){
			//	return;
			//}
			
			Rasi ascendant = chartData.getAscendant();
			
			if (ascendant != null){
				
				int bhava = Rasi.ofIndex(housePanel.indexOf(source)).bhava(ascendant);
				
				WindowLabelModel windowModel = new DefaultWindowLabelModel(String.valueOf(bhava));
				
				window = new AstrosoftWindow(windowModel);
				window.setPreferredSize(new Dimension(30,20));
    			Point houseLoc = source.getLocationOnScreen();
    			Point loc = e.getPoint();
                
                loc.translate(houseLoc.x + 10,houseLoc.y - 10);
                window.show(loc);
			}
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			
			if (window != null){
				window.dispose();
			}
			//previous_source = source;
		}
	}
	private static GridBagConstraints getConstrains(int house){

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;

		switch(house){
			case 0: c.gridx = 1;
					c.gridy = 0;
					break;
			case 1: c.gridx = 2;
					c.gridy = 0;
					break;
			case 2: c.gridx = 3;
					c.gridy = 0;
					break;
			case 3: c.gridx = 3;
					c.gridy = 1;
					break;
			case 4: c.gridx = 3;
					c.gridy = 2;
					break;
			case 5: c.gridx = 3;
					c.gridy = 3;
					break;
			case 6: c.gridx = 2;
					c.gridy = 3;
					break;
					
			case 7: c.gridx = 1;
					c.gridy = 3;
					break;
			case 8: c.gridx = 0;
					c.gridy = 3;
					break;
			case 9: c.gridx = 0;
					c.gridy = 2;
					break;
			case 10: c.gridx = 0;
					 c.gridy = 1;
					break;
			case 11: c.gridx = 0;
					 c.gridy = 0;
					 break;
			
					 //Center Panel
			case 12: c.gridx = 1;
					 c.gridy = 1;
					 c.gridwidth = 2;
					 c.gridheight = 2;
					 c.weightx = 2.0;
					 c.weighty = 2.0;
					 break;
			default : throw new IllegalArgumentException("house > 12"); 
		}
		
		/*c.ipadx = 1;
		c.ipady = 1;
		c.insets = new Insets(1,1,1,1);*/
		return c;
	}

	public ChartData getChartData() {
		return chartData;
	}

	public void updateChartData(ChartData chartData){
		updateChartData(chartData, this.subtitle);
	}

	public void updateChartData(ChartData chartData, String subtitle){
		this.subtitle = subtitle;
		this.chartData = chartData;
		title.setText(normalizeChartName(chartData.getChartName()));
		this.removeAll();
		addHouseTables();
		revalidate();
		repaint();
	}
}
