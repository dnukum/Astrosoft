package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

import javax.swing.JPanel;

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
import app.astrosoft.util.AstroUtil;

public class RelativePlanetView extends JPanel {

	public RelativePlanetView(PlanetaryInfo planetaryInfo) {
		
		setLayout(new BorderLayout());
		
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
			if (planetDir.containsKey(p) && planetDir.get(p)) {
				name += " (R)";
			}
			row.addColumn(AstrosoftTableColumn.Planet, name);
			
			double offset = planetPos.get(p) % 30.0;
			String dms = AstroUtil.dms(offset).replace(" : ", ":");
			int lastColon = dms.lastIndexOf(":");
			if (lastColon != -1) {
				dms = dms.substring(0, lastColon);
			}
			row.addColumn(AstrosoftTableColumn.Longitude, dms);
			rows.add(row);
		}
		ListTableData<MapTableRow> tableData = new ListTableData<MapTableRow>(rows);

		
		DefaultColumnMetaData colMetaData = new DefaultColumnMetaData(AstrosoftTableColumn.Planet, AstrosoftTableColumn.Longitude);
		
		
		javax.swing.JLabel title = new javax.swing.JLabel("Rasi Chart Planet Offsets", javax.swing.SwingConstants.CENTER);
		title.setFont(new java.awt.Font("Verdana", java.awt.Font.BOLD, 12));
		title.setForeground(app.astrosoft.ui.util.UIConsts.DARK_BLUE);
		add(title, BorderLayout.NORTH);
		
		JPanel gridPanel = new JPanel(new GridLayout(1, 2, 20, 20));
		List<ListTableData<MapTableRow>> splittedData = TableDataFactory.splitTableData(tableData, 5);
		
		for(ListTableData<MapTableRow> data : splittedData){
			AstrosoftTable table = new AstrosoftTable(new AstrosoftTableModel(data, colMetaData), TableStyle.NONE);
			gridPanel.add(table);
		}
		
		add(gridPanel, BorderLayout.CENTER);
	}
}
