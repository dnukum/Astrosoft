/**
 * InfoView.java
 * Created On 2006, Mar 18, 2006 3:39:42 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.List;

import javax.swing.JPanel;

import app.astrosoft.consts.TableStyle;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.ColumnMetaData;
import app.astrosoft.ui.table.ListTableData;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.table.TableData;
import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.ui.table.TableRowData;

public class InfoView extends AstrosoftView {

	/** Creates a new instance of PlanetPanel 
     */
    public InfoView(Table horoscopeInfo, Point loc) {

    	super(new Dimension(800,150), loc);
    	
    	ListTableData<MapTableRow> allData = (ListTableData<MapTableRow>) horoscopeInfo.getTableData();
    	ColumnMetaData infoColMetaData = horoscopeInfo.getColumnMetaData();
    	
    	JPanel infoPanel = new JPanel(new GridLayout(1, 3 , 20, 20));
    	int splitSize = (allData.getRowCount() + 1) / 2;
    	List<ListTableData<MapTableRow>> splittedData = TableDataFactory.splitTableData(allData, splitSize);
    	
    	for(ListTableData<MapTableRow> data : splittedData){
    		AstrosoftTable infoTable = new AstrosoftTable(new AstrosoftTableModel(data, infoColMetaData), TableStyle.NONE );
    		infoPanel.add(infoTable);
    	}
    	
    	add(infoPanel,BorderLayout.CENTER);
    }
}

