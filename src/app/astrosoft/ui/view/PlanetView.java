/**
 * PlanetPanel.java
 *
 * Created on January 10, 2003, 3:45 PM
 * @author  E. Rajasekar
 */
package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import java.util.EnumMap;
import java.util.Map;

import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.PlanetStatusCalculator;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.ui.comp.TitleLabel;
import app.astrosoft.ui.comp.TitledTable;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.ColumnMetaData;
import app.astrosoft.ui.table.PlanetCellRenderer;


public class PlanetView extends AstrosoftView {

	PlanetaryInfo planetaryInfo;

	/** Creates a new instance of PlanetPanel 
     */
    public PlanetView(String title, PlanetaryInfo planetaryInfo) {

    	super(title, new Dimension(720, 295));
    	this.planetaryInfo = planetaryInfo;

    	ColumnMetaData colMetaData = planetaryInfo.getPlanateryInfoColumnMetaData();
    	AstrosoftTable planetTable = new AstrosoftTable(new AstrosoftTableModel(
				planetaryInfo.getPlanateryInfoTableData(), colMetaData), TableStyle.STANDARD);
		planetTable.setColumnWidth(170, AstrosoftTableColumn.Longitude);
		planetTable.setColumnWidth(160, AstrosoftTableColumn.NakshathraPada);
		planetTable.setColumnWidth(150, AstrosoftTableColumn.Planet);



    	
    	PlanetCellRenderer planetRenderer = new PlanetCellRenderer(planetTable.getCellRenderer(AstrosoftTableColumn.Planet), DisplayFormat.FULL_NAME, true, true, planetaryInfo.getPlanetDirection());
		planetRenderer.setPlanetStatus(planetStatuses(planetaryInfo));
		planetTable.setCellRenderer(planetRenderer, AstrosoftTableColumn.Planet);
		TitleLabel label = new TitleLabel(DisplayStrings.PLANET_POS_STR);

		TitledTable tablePanel = new TitledTable(label, planetTable, viewSize);
		
		add(tablePanel,BorderLayout.CENTER);
		
		this.setVisible(true);

    }

	/** Dignity and Vargottama for display only: Rasi from D1, Navamsa from the divisional chart (not getPlanetRasi(Varga)). */
	private static Map<Planet, PlanetStatus> planetStatuses(PlanetaryInfo info) {
		EnumMap<Planet, Rasi> d9 = new EnumMap<Planet, Rasi>(Planet.class);
		EnumMap<Planet, Integer> d9Numbers = info.getDivChart(Varga.Navamsa);
		if (d9Numbers != null) {
			try {
				d9 = PlanetStatusCalculator.signsOf(d9Numbers);
			} catch (IllegalArgumentException e) {
				// bad Navamsa data: show dignity only, never break the view
			}
		}
		return PlanetStatusCalculator.statusMap(info.getPlanetRasi(), d9);
	}
}
