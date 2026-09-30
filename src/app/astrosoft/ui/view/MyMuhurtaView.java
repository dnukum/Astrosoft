package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableCellRenderer;

import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.Panchang;
import app.astrosoft.core.MyMuhurta;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.CalendarChooser;
import app.astrosoft.ui.comp.CalendarSpinner;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.DateListener;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.DefaultColumnMetaData;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.MapTableRowHelper;
import app.astrosoft.ui.table.MultiLineCellRenderer;
import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Varga;
import java.util.EnumMap;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;

public class MyMuhurtaView extends AstrosoftView {

    private static final Dimension viewSize = new Dimension(1500, 700);
    private static final Dimension chartSize = new Dimension(350, 300);
    
    private static final int KeyColWidth = 140;
    private static final int ValueColWidth = 260;
    private static final int RowHeight = 30;
    private static final int AusRowHeight = 70;
    private static final Dimension headerSize = new Dimension(KeyColWidth + ValueColWidth, RowHeight);
    
    private java.util.Date selectedDate = new java.util.Date();
    private java.util.Date selectedTime = new java.util.Date();
    private app.astrosoft.beans.Place selectedPlace = app.astrosoft.ui.AstroSoft.getPreferences().getPlace();
    private javax.swing.JTextArea currentConfigLabel;
    private Horoscope userHoroscope;
    
    private AstrosoftTableModel tableModel;
    private AstrosoftTable table;
    private JPanel chartsPanel;
    
    public MyMuhurtaView(Point loc, Horoscope userHoroscope) {
        super(viewSize, loc);
        this.userHoroscope = userHoroscope;
        
        
        final JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        chartsPanel = new JPanel(new java.awt.GridLayout(1, 0, 10, 10));
        
        mainPanel.add(createTablePanel(), BorderLayout.WEST);
        chartsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        final javax.swing.JScrollPane chartScrollPane = new javax.swing.JScrollPane(chartsPanel);
        chartScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY, 1));
        
        final JPanel scrollWrapper = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        scrollWrapper.add(chartScrollPane);
        mainPanel.add(scrollWrapper, BorderLayout.CENTER);
        
        MyMuhurtaView.this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int chartW = chartSize.width + 10;
                // Measure the total screen width dynamically since FlowLayout doesn't shrink
                int totalAvailable = MyMuhurtaView.this.getWidth() - 400 - 40; // 400 for table, 40 for margins
                if (totalAvailable <= 0) return;
                
                int fit = totalAvailable / chartW;
                if (fit < 1) fit = 1;
                
                int chartCount = chartsPanel.getComponentCount();
                if (fit > chartCount) fit = chartCount;
                
                int scrollBarHeight = chartScrollPane.getHorizontalScrollBar().getPreferredSize().height;
                chartScrollPane.setPreferredSize(new Dimension(fit * chartW + 20, chartSize.height + scrollBarHeight + 25));
                scrollWrapper.revalidate();
            }
        });
        
        selectedDate = new java.util.Date();
        selectedTime = new java.util.Date();
        selectedPlace = AstroSoft.getPreferences().getPlace();
        
                // Ensure the content inside mainPanel stays left-aligned relative to itself,
        // but the mainPanel as a whole gets perfectly centered!
        this.removeAll();
        this.setLayout(new java.awt.BorderLayout());
        
        javax.swing.JPanel centerWrapper = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 20));
        centerWrapper.add(mainPanel);
        
        this.add(centerWrapper, java.awt.BorderLayout.CENTER);
        
        // Initial setup
        updateView();
    }
    
    private void updateView() {
        java.util.TimeZone tz = java.util.TimeZone.getDefault();
        if (selectedPlace != null) {
            tz = selectedPlace.astrosoftTimeZone().getTimeZone();
            
        }
        
        Calendar cal = Calendar.getInstance(tz);
        Calendar systemCal = Calendar.getInstance();
        systemCal.setTime(selectedDate);
        cal.set(Calendar.YEAR, systemCal.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, systemCal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, systemCal.get(Calendar.DAY_OF_MONTH));
        
        Calendar tCal = Calendar.getInstance();
        tCal.setTime(selectedTime);
        cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, 0);
        
        Panchang pan;
        if (selectedPlace != null) {
            pan = new Panchang(cal.getTime(), selectedPlace);
        } else {
            pan = new Panchang(cal.getTime());
        }
        
        // Construct the combined table
        List<MapTableRow> rows = new ArrayList<MapTableRow>();
        DefaultColumnMetaData meta = new DefaultColumnMetaData(AstrosoftTableColumn.keyvalCols());
        meta.localizeColumns();
        MapTableRowHelper helper = new MapTableRowHelper(meta);
        
        if (userHoroscope != null && userHoroscope.getPersonName() != null && !userHoroscope.getPersonName().isEmpty()) {
            rows.add(helper.createRow(app.astrosoft.consts.DisplayStrings.NAME_STR, userHoroscope.getPersonName()));
        }
        for (int i=0; i<pan.getPanchangTableData().getRowCount(); i++) rows.add(pan.getPanchangTableData().getRow(i));
        
        if (userHoroscope != null) {
            app.astrosoft.consts.Nakshathra birthNak = userHoroscope.getNakshathra().getNak();
            app.astrosoft.consts.Nakshathra transitNak = pan.getNakshathra();
            
            app.astrosoft.core.BalamRank taraBalam = MyMuhurta.evalTaraBalam(birthNak, transitNak);
            int taraIndex = MyMuhurta.calcTaraIndex(birthNak, transitNak);
            
            rows.add(helper.createRow("Tara Balam", taraIndex + " (" + taraBalam.name() + ")"));
            
            app.astrosoft.consts.Rasi birthRasi = userHoroscope.getRasi();
            app.astrosoft.consts.Rasi transitRasi = pan.getRasi();
            
            app.astrosoft.core.BalamRank chandraBalam = MyMuhurta.evalChandraBalam(birthRasi, transitRasi);
            int house = MyMuhurta.calcChandraHouse(birthRasi, transitRasi);
            
            rows.add(helper.createRow("Chandra Balam", house + " (" + chandraBalam.name() + ")"));
        }
        
        tableModel.updateData(TableDataFactory.getTableData(rows));
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object val = tableModel.getValueAt(i, 0);
            if (val != null && val.toString().contains(app.astrosoft.consts.DisplayStrings.AUS_TIME_STR.toString())) {
                table.setRowHeight(i, 100); // 100 allows 4-5 lines of text to breathe
            } else {
                table.setRowHeight(i, RowHeight);
            }
        }
        
        // Charts
        chartsPanel.removeAll();
        app.astrosoft.beans.ChartData muhurtaData = new app.astrosoft.beans.PlanetChartData(app.astrosoft.consts.Varga.Rasi, pan.getPlanetPositions(), pan.getPlanetDirection()) {
            public String getChartName() { return "<html><center>Muhurta<br>Rasi</center></html>"; }
        };
        chartsPanel.add(new Chart(muhurtaData, chartSize));
        
        if (userHoroscope != null) {
            app.astrosoft.beans.ChartData d1Data = new app.astrosoft.beans.PlanetChartData(app.astrosoft.consts.Varga.Rasi, userHoroscope.getPlanetaryInfo()) {
                public String getChartName() { return "<html><center>Natal<br>Rasi</center></html>"; }
            };
            chartsPanel.add(new Chart(d1Data, chartSize));
            
            
            app.astrosoft.beans.ChartData d9Data = new app.astrosoft.beans.PlanetChartData(app.astrosoft.consts.Varga.Navamsa, userHoroscope.getPlanetaryInfo()) {
                public String getChartName() { return "<html><center>Natal<br>Navamsa</center></html>"; }
            };
            chartsPanel.add(new Chart(d9Data, chartSize));
        }
        
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
    
    private JPanel createTablePanel() {
        tableModel = new AstrosoftTableModel(new app.astrosoft.ui.table.DefaultTable(
            TableDataFactory.getTableData(new ArrayList<MapTableRow>()),
            new DefaultColumnMetaData(AstrosoftTableColumn.keyvalCols())
        ));
        
        table = new AstrosoftTable(tableModel, TableStyle.GRID) {
            TableCellRenderer multiLineRenderer = new MultiLineCellRenderer(",");
            public TableCellRenderer getCellRenderer(int row, int col) {
                Object keyVal = getValueAt(row, 0);
                if (keyVal != null && keyVal.toString().contains(app.astrosoft.consts.DisplayStrings.AUS_TIME_STR.toString()) && col == 1) {
                    return multiLineRenderer;
                }
                return super.getCellRenderer(row, col);
            }
        };
        
        table.setColumnWidth(ValueColWidth, AstrosoftTableColumn.Value);
        table.setColumnWidth(KeyColWidth, AstrosoftTableColumn.Key);
        table.setRowHeight(RowHeight);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createTableHeader(), BorderLayout.PAGE_START);
        tablePanel.add(table, BorderLayout.CENTER);
        return tablePanel;
    }
    
    private JPanel createTableHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        currentConfigLabel = new javax.swing.JTextArea();
        currentConfigLabel.setFont(UIUtil.getFont());
        currentConfigLabel.setEditable(false);
        currentConfigLabel.setOpaque(false);
        currentConfigLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        updateConfigLabel();
        
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(currentConfigLabel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        javax.swing.JButton editBtn = new javax.swing.JButton("Change");
        editBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                app.astrosoft.ui.dlg.MuhurtaConfigDialog dlg = new app.astrosoft.ui.dlg.MuhurtaConfigDialog(
                    null, 
                    selectedDate, 
                    selectedTime, 
                    selectedPlace, 
                    new app.astrosoft.ui.dlg.MuhurtaConfigDialog.ConfigListener() {
                        public void configChanged(java.util.Date date, java.util.Date time, app.astrosoft.beans.Place place) {
                            selectedDate = date;
                            selectedTime = time;
                            selectedPlace = place;
                            updateConfigLabel();
                            updateView();
                        }
                    }
                );
                dlg.showDialog();
            }
        });
        
        headerPanel.add(scrollPane, BorderLayout.CENTER);
        headerPanel.add(editBtn, BorderLayout.EAST);
        
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        headerPanel.setPreferredSize(new Dimension(KeyColWidth + ValueColWidth, 60));
        
        headerPanel.setBackground(UIConsts.TABLE_HEADER_BACKGROUND);
        currentConfigLabel.setForeground(UIConsts.TABLE_HEADER_FOREGROUND);
        
        return headerPanel;
    }
    
    private void updateConfigLabel() {
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MMMM d, yyyy");
        java.text.SimpleDateFormat tf = new java.text.SimpleDateFormat("hh:mm:ss a");
        String dateStr = df.format(selectedDate);
        String timeStr = tf.format(selectedTime);
        
        String placeStr = "Default Place";
        String tzStr = "";
        if (selectedPlace != null) {
            placeStr = selectedPlace.city().trim();
            if (selectedPlace.state() != null && !selectedPlace.state().trim().isEmpty()) placeStr += ", " + selectedPlace.state().trim();
            if (selectedPlace.country() != null && !selectedPlace.country().trim().isEmpty()) placeStr += ", " + selectedPlace.country().trim();
            
            app.astrosoft.util.AstrosoftTimeZone tz = selectedPlace.astrosoftTimeZone();
            tzStr = tz.toString();
        }
        currentConfigLabel.setText("Date: " + dateStr + "\nTime: " + timeStr + ", " + tzStr + "\nLocation: " + placeStr);
    }
}
