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
        
        javax.swing.JPanel centerWrapper = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 20));
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
            tzStr = selectedPlace.astrosoftTimeZone().toString();
        }
        
        rows.add(helper.createRow(app.astrosoft.consts.DisplayStrings.DATE_STR, dateStr));
        rows.add(helper.createRow("Time", timeStr + "," + tzStr));
        rows.add(helper.createRow("Location", placeStr));
        
        for (int i=0; i<pan.getPanchangTableData().getRowCount(); i++) {
            app.astrosoft.ui.table.MapTableRow row = pan.getPanchangTableData().getRow(i);
            if (row.getColumnData(app.astrosoft.consts.AstrosoftTableColumn.Key).toString().equals(app.astrosoft.consts.DisplayStrings.DATE_STR.toString())) continue;
            rows.add(row);
        }
        
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
            } else if (val != null && val.toString().equals("Time")) {
                table.setRowHeight(i, RowHeight * 2);
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
                if (keyVal != null && col == 1) {
                    if (keyVal.toString().contains(app.astrosoft.consts.DisplayStrings.AUS_TIME_STR.toString()) || keyVal.toString().equals("Time")) {
                        return multiLineRenderer;
                    }
                }
                return super.getCellRenderer(row, col);
            }
            
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof javax.swing.JComponent) {
                    ((javax.swing.JComponent) c).setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 0));
                }
                return c;
            }
        };
        
        table.setColumnWidth(ValueColWidth, AstrosoftTableColumn.Value);
        table.setColumnWidth(KeyColWidth, AstrosoftTableColumn.Key);
        table.setRowHeight(RowHeight);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createBottomPanel(), BorderLayout.SOUTH);
        tablePanel.add(table, BorderLayout.CENTER);
        return tablePanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 10));
        bottomPanel.setOpaque(false);
        
        javax.swing.JButton editBtn = new javax.swing.JButton("Edit") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setColor(java.awt.Color.LIGHT_GRAY);
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
            @Override
            protected void paintBorder(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(java.awt.Color.GRAY);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        editBtn.setContentAreaFilled(false);
        editBtn.setFocusPainted(false);
        editBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        editBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        editBtn.setPreferredSize(new java.awt.Dimension(80, 30));
        
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
                            updateView();
                        }
                    }
                );
                dlg.showDialog();
            }
        });
        
        bottomPanel.add(editBtn);
        return bottomPanel;
    }
}