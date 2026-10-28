/**
 * Chart.java
 * Created On 2006, Mar 31, 2006 4:56:17 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import app.astrosoft.beans.ChartData;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.PlanetCellRenderer;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.window.AstrosoftWindow;
import app.astrosoft.ui.window.DefaultWindowLabelModel;
import app.astrosoft.ui.window.WindowLabelModel;

public class Chart extends JPanel{

	private ChartData chartData;
	
	private List<JPanel> housePanel;
	
	private Dimension houseSize;
	
	private Dimension tableSize;
	
	private int rowHeight;
	
	private JLabel title;
	private String subtitle;
	private boolean showHouseNumber = true;
	
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
		
		houseSize = new Dimension(chartSize.width / 4, chartSize.height / 4);
		
		if (houseSize.width < 85 || houseSize.height < 72) {
			tableSize = new Dimension(Math.max(1, houseSize.width - 6), Math.max(3, houseSize.height - 4));
		} else {
			tableSize = new Dimension((int)(houseSize.width * 0.82), (int)(houseSize.height * 0.80));
		}
		
		rowHeight = Math.max(14, tableSize.height / 3);
		
		//Thirteen-th fills center
		housePanel = new ArrayList<JPanel>();
		
		String name = chartData.getChartName();
		if ("Navamsa".equalsIgnoreCase(name)) {
			name = "Navamsha";
		}
		title = new TitleLabel(name);
		
		setLayout(new GridBagLayout());
		//setPreferredSize(new Dimension(chartSize.width + (3 * 4), chartSize.height + (3 * 4)));
		setPreferredSize(chartSize);
		setMinimumSize(chartSize);
		addHouseTables();
		setVisible(true);
	}
	
    /** Display absolute sidereal degrees and whole arcminutes; omit seconds without rounding. */
    public static String formatAbsoluteLongitude(Double longitude) {
        if (longitude == null || !Double.isFinite(longitude)) return null;
        double normalized = (longitude % 360 + 360) % 360;
        int totalMinutes = (int) Math.floor(normalized * 60);
        return String.format(java.util.Locale.ROOT, "%03d:%02d", totalMinutes / 60, totalMinutes % 60);
    }

	private void addHouseTables() {
		
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
		for(Rasi house : Rasi.values()){
			
			houseNo = house.ordinal();
			
			JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, hGap, vGap));
			
			if (chartData instanceof app.astrosoft.beans.AshtaVargaChartData) {
				app.astrosoft.beans.AshtaVargaChartData avData = (app.astrosoft.beans.AshtaVargaChartData) chartData;
				panel.setLayout(new BorderLayout());
				panel.setPreferredSize(houseSize);
				panel.setBackground(UIConsts.getChartBackground());
				panel.setBorder(UIConsts.getChartBorder());

				Rasi asc = avData.getAscendant();
				Rasi moon = avData.getMoonRasi();
				boolean isAsc = (asc == house);
				boolean isMoon = (moon == house);

				JPanel topRow = new JPanel(new BorderLayout());
				topRow.setOpaque(false);
				topRow.setBorder(BorderFactory.createEmptyBorder(5, 7, 0, 7));

				if (isAsc && isMoon) {
					JLabel ascLbl = new JLabel("Asc");
					ascLbl.setFont(app.astrosoft.ui.util.UIUtil.getFont(Font.BOLD, 12));
					ascLbl.setForeground(Color.RED);
					topRow.add(ascLbl, BorderLayout.WEST);

					JLabel moonLbl = new JLabel("Mo");
					moonLbl.setFont(app.astrosoft.ui.util.UIUtil.getFont(Font.BOLD, 12));
					moonLbl.setForeground(Color.MAGENTA);
					topRow.add(moonLbl, BorderLayout.EAST);
				} else if (isAsc) {
					JLabel ascLbl = new JLabel("Asc");
					ascLbl.setFont(app.astrosoft.ui.util.UIUtil.getFont(Font.BOLD, 12));
					ascLbl.setForeground(Color.RED);
					topRow.add(ascLbl, BorderLayout.WEST);
				} else if (isMoon) {
					JLabel moonLbl = new JLabel("Mo");
					moonLbl.setFont(app.astrosoft.ui.util.UIUtil.getFont(Font.BOLD, 12));
					moonLbl.setForeground(Color.MAGENTA);
					topRow.add(moonLbl, BorderLayout.WEST);
				}

				panel.add(topRow, BorderLayout.NORTH);

				Integer val = avData.getVarga().get(house);
				String valStr = (val != null) ? val.toString() : "";
				JLabel numLbl = new JLabel(valStr, javax.swing.SwingConstants.CENTER);
				numLbl.setFont(app.astrosoft.ui.util.UIUtil.getFont(Font.BOLD, 15));
				numLbl.setForeground(UIConsts.DARK_BLUE);
				panel.add(numLbl, BorderLayout.CENTER);

				panel.add(Box.createVerticalStrut(16), BorderLayout.SOUTH);

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
                                Planet planet = (Planet) value;
                                PlanetChartData planetData = (PlanetChartData) chartData;
                                app.astrosoft.consts.Nakshathra nak = planetData.getPlanetNakshathra(planet);
                                String longitude = formatAbsoluteLongitude(planetData.getPlanetLongitude(planet));
                                String orb = formatAbsoluteLongitude(planetData.getPlanetOrb(planet));
                                StringBuilder text = new StringBuilder();
                                if (orb != null) {
                                    text.append("Orb ").append(orb);
                                }
                                if (longitude != null) {
                                    if (text.length() > 0) text.append(", ");
                                    text.append(longitude);
                                }
                                if (nak != null) {
                                    if (text.length() > 0) text.append(", ");
                                    text.append(nak.toString());
                                }
                                return text.length() == 0 ? null : text.toString();
                            }
                        }
                        // Keep the existing house tooltip outside occupied planet cells.
                        return super.getToolTipText(event);
                    }
                };
				panel.add(houseTable, getConstrains(houseNo));

				//houseTable.setDefaultRenderer(Planet.class, new PlanetCellRenderer());
				if (houseSize.height < 68) {
					houseTable.setFont(new Font("Verdana", Font.PLAIN, 10));
					houseTable.setRowMargin(1);
				}
				houseTable.setRowHeight(rowHeight);
				houseTable.setPreferredSize(tableSize);
				houseTable.setBackground(UIConsts.getChartBackground());
	
				if (isPlanetChart){
					decoratePlanetChart(table, houseTable);
					int startDeg = house.ordinal() * 30;
					int endDeg = startDeg + 30;
					String houseNumStr = (showHouseNumber && ascendant != null) ? " <br>House: " + house.bhava(ascendant) : "";
					toolTip = "<html><b>" + house.toString(Language.ENGLISH) + "</b> (" + startDeg + "&deg; - " + endDeg + "&deg;)" + houseNumStr + "</html>";
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
				int startDeg = house.ordinal() * 30;
				int endDeg = startDeg + 30;
				String houseNumStr = (showHouseNumber && ascendant != null) ? " <br>House: " + house.bhava(ascendant) : "";
				toolTip = "<html><b>" + house.toString(Language.ENGLISH) + "</b> (" + startDeg + "&deg; - " + endDeg + "&deg;)" + houseNumStr + "</html>";
				panel.setToolTipText(toolTip);
				toolTipManager.registerComponent(panel);
			}
			
			housePanel.add(houseNo, panel);
			
			//panel.addMouseListener(new ChartHouseMouseListener());
		}
		
		// Add ChartName and optional Subtitle at Center - Vertically and Horizontally Centered in Middle
		houseNo = 12;
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(UIConsts.getChartBackground());
		Dimension centerSize = new Dimension(houseSize.width * 2, houseSize.height * 2);
		panel.setPreferredSize(centerSize);
		panel.setMinimumSize(centerSize);
		
		title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		
		GridBagConstraints cGbc = new GridBagConstraints();
		cGbc.gridx = 0;
		cGbc.gridy = 0;
		cGbc.anchor = GridBagConstraints.CENTER;
		panel.add(title, cGbc);
		
		if (subtitle != null && !subtitle.trim().isEmpty()) {
			cGbc.gridy = 1;
			cGbc.insets = new java.awt.Insets(2, 0, 0, 0);
			JLabel subTitleLabel = new JLabel(subtitle, javax.swing.SwingConstants.CENTER);
			subTitleLabel.setFont(new Font("Verdana", Font.BOLD, 11));
			subTitleLabel.setForeground(UIConsts.DARK_BLUE);
			panel.add(subTitleLabel, cGbc);
		}
		
		String tip = title.getText() + " Chart";
		if (subtitle != null && !subtitle.trim().isEmpty()) {
			tip += " " + subtitle;
		}
		panel.setToolTipText(tip);
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
	}

	public void setHouseBackground(Rasi house, Color bg) {
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

	public void setHouseToolTip(Rasi house, String toolTip) {
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
		JPopupMenu popup = new JPopupMenu();
		popup.add("Rajasekar");
		//popup.setSelectionModel();
		
		MouseListener popupListener = new PopupListener(popup);
		this.addMouseListener(popupListener);
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
					 break;
			default : throw new IllegalArgumentException("house > 12"); 
		}
		
		/*c.ipadx = 1;
		c.ipady = 1;
		c.insets = new Insets(1,1,1,1);*/
		return c;
	}

	public void updateChartData(ChartData chartData){
		updateChartData(chartData, this.subtitle);
	}

	public void updateChartData(ChartData chartData, String subtitle){
		this.subtitle = subtitle;
		this.chartData = chartData;
		String name = chartData.getChartName();
		if ("Navamsa".equalsIgnoreCase(name)) {
			name = "Navamsha";
		}
		title.setText(name);
		this.removeAll();
		addHouseTables();
	}
}
