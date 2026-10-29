package app.astrosoft.ui.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.DefaultColumnMetaData;
import app.astrosoft.ui.table.ListTableData;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.util.AstroUtil;

public class RelativePlanetView extends JPanel {

	private final JLabel title;
	private final JPanel gridPanel;

	public RelativePlanetView(PlanetaryInfo planetaryInfo) {
		
		setLayout(new BorderLayout());
		setOpaque(false);
		
		EnumMap<Planet, Rasi> planetRasi = planetaryInfo.getPlanetRasi();
		final EnumMap<Planet, Double> planetPos = planetaryInfo.getPlanetPosition();
		
		Rasi ascRasi = planetRasi.get(Planet.Ascendant);
		int ascIndex = ascRasi.ordinal();
		
		List<Planet> sortedPlanets = new ArrayList<Planet>();
		
		for (int i = 0; i < 12; i++) {
			int rasiIndex = (ascIndex + i) % 12;
			Rasi currentRasi = Rasi.values()[rasiIndex];
			
			List<Planet> planetsInSign = new ArrayList<Planet>();
			for (Planet p : Planet.values()) {
				if (planetRasi.get(p) == currentRasi) {
					planetsInSign.add(p);
				}
			}
			
			Collections.sort(planetsInSign, new Comparator<Planet>() {
				public int compare(Planet p1, Planet p2) {
					Double pos1 = planetPos.get(p1) % 30.0;
					Double pos2 = planetPos.get(p2) % 30.0;
					return pos1.compareTo(pos2);
				}
			});
			
			sortedPlanets.addAll(planetsInSign);
		}
		
		EnumMap<Planet, Boolean> planetDir = planetaryInfo.getPlanetDirection();

		List<MapTableRow> rows = new ArrayList<MapTableRow>();
		for (Planet p : sortedPlanets) {
			MapTableRow row = new MapTableRow();
			String name = p.name();
			boolean isRetro = planetDir.containsKey(p) && planetDir.get(p);
			boolean isCombust = planetaryInfo.getPlanetCombustion() != null && planetaryInfo.getPlanetCombustion().containsKey(p) && planetaryInfo.getPlanetCombustion().get(p);
			
			if (isRetro && isCombust) {
				name += " (R, C)";
			} else if (isRetro) {
				name += " (R)";
			} else if (isCombust) {
				name += " (C)";
			}
			row.addColumn(AstrosoftTableColumn.Planet, name);
			
			double offset = planetPos.get(p) % 30.0;
			int[] deg_min_sec = AstroUtil.int_dms(offset);
			String dms = AstroUtil.twoDigit(deg_min_sec[0]) + ":" + AstroUtil.twoDigit(deg_min_sec[1]);
			dms += " (" + planetRasi.get(p).toString() + ")";
			row.addColumn(AstrosoftTableColumn.Longitude, dms);
			rows.add(row);
		}
		ListTableData<MapTableRow> tableData = new ListTableData<MapTableRow>(rows);

		DefaultColumnMetaData colMetaData = new DefaultColumnMetaData(AstrosoftTableColumn.Planet, AstrosoftTableColumn.Longitude);
		
		final int cardWidth = 530;
		final int cardHeight = 184;
		final Dimension panelSize = new Dimension(cardWidth, cardHeight);

		setLayout(null);
		setPreferredSize(panelSize);
		setMinimumSize(panelSize);
		setMaximumSize(panelSize);

		title = new JLabel("Planetary Positions", SwingConstants.CENTER);
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		title.setForeground(UIConsts.TEXT_DARK_NAVY);
		title.setBounds(0, 0, cardWidth, 20);
		add(title);
		
		gridPanel = new JPanel(new GridLayout(1, 2, 16, 0));
		gridPanel.setOpaque(false);
		List<ListTableData<MapTableRow>> splittedData = TableDataFactory.splitTableData(tableData, 5);
		
		for(ListTableData<MapTableRow> data : splittedData){
			AstrosoftTable table = new AstrosoftTable(new AstrosoftTableModel(data, colMetaData), TableStyle.NONE);
			table.setOpaque(false);
			javax.swing.table.TableColumn pCol = table.getColumnModel().getColumn(0);
			javax.swing.table.TableColumn lCol = table.getColumnModel().getColumn(1);
			pCol.setPreferredWidth(90);
			pCol.setMaxWidth(100);
			lCol.setPreferredWidth(145);

			pCol.setCellRenderer(new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
					Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
					comp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
					String text = (val != null) ? val.toString() : "";
					if (text.startsWith("Ascendant")) {
						comp.setForeground(UIConsts.getChartAscendantColor());
					} else if (text.startsWith("Moon")) {
						comp.setForeground(UIConsts.getChartMoonColor());
					} else {
						comp.setForeground(UIConsts.getChartPlanetForeground());
					}
					setOpaque(false);
					return comp;
				}
			});

			lCol.setCellRenderer(new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
					Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
					comp.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
					comp.setForeground(UIConsts.TEXT_DARK_NAVY);
					setOpaque(false);
					return comp;
				}
			});

			gridPanel.add(table);
		}
		
		gridPanel.setBounds(20, 36, 490, 114);
		add(gridPanel);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int arc = 12;
		int gap = 16;

		int boxLeft = 2;
		int boxRight = 528;
		int boxTop = 10;
		int boxBottom = 180;

		// Theme card background
		g2.setColor(UIConsts.getChartBackground());
		g2.fillRoundRect(boxLeft, boxTop, boxRight - boxLeft, boxBottom - boxTop, arc, arc);

		title.setForeground(UIConsts.getChartPlanetForeground());

		FontMetrics fm = title.getFontMetrics(title.getFont());
		int textW = fm.stringWidth(title.getText());
		int textX = (getWidth() - textW) / 2;

		Path2D p = new Path2D.Double();
		// Top-left line to title cutout
		p.moveTo(boxLeft + arc, boxTop);
		p.lineTo(Math.max(boxLeft + arc, textX - gap), boxTop);

		// Top-right line from title cutout to right corner
		p.moveTo(Math.min(boxRight - arc, textX + textW + gap), boxTop);
		p.lineTo(boxRight - arc, boxTop);

		// Top-right rounded corner
		p.quadTo(boxRight, boxTop, boxRight, boxTop + arc);

		// Right vertical border (exactly 30px away from table: 458 - 428 = 30)
		p.lineTo(boxRight, boxBottom - arc);

		// Bottom-right rounded corner
		p.quadTo(boxRight, boxBottom, boxRight - arc, boxBottom);

		// Bottom horizontal border (exactly 30px away from table: 180 - 150 = 30)
		p.lineTo(boxLeft + arc, boxBottom);

		// Bottom-left rounded corner
		p.quadTo(boxLeft, boxBottom, boxLeft, boxBottom - arc);

		// Left vertical border (exactly 30px away from table: 32 - 2 = 30)
		p.lineTo(boxLeft, boxTop + arc);

		// Top-left rounded corner
		p.quadTo(boxLeft, boxTop, boxLeft + arc, boxTop);

		g2.setColor(UIConsts.getChartBorderColor()); // Theme border matching charts
		g2.setStroke(new BasicStroke(1.5f));
		g2.draw(p);
		g2.dispose();
	}
}
