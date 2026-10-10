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
    private Horoscope person1Horoscope;
    private Horoscope person2Horoscope;
    private javax.swing.JButton person1Btn, person2Btn;
    private javax.swing.JScrollPane chartScrollPane;
    
    private AstrosoftTableModel tableModel;
    private AstrosoftTable table;
    private JPanel chartsPanel;
    private JPanel chartControlStrip;
    private JPanel flyoutHost;
    private app.astrosoft.ui.comp.BalamCalendarPanel balamFlyout;
    private javax.swing.JButton balamBtn;
    
    private void toggleBalamFlyout() {
        if (balamFlyout == null) {
            showBalamFlyout();
        } else {
            hideBalamFlyout();
        }
    }

    private void showBalamFlyout() {
        if (balamFlyout != null) {
            return;
        }
        balamFlyout = new app.astrosoft.ui.comp.BalamCalendarPanel(createMuhurta(), (startInstant, endInstant) -> applyCalendarDate(startInstant, endInstant), person1Horoscope, person2Horoscope);
        balamFlyout.setPeople(person1Horoscope, person2Horoscope);
        flyoutHost.add(balamFlyout, BorderLayout.CENTER);
        balamBtn.setText("Hide Balam Calendar");
        flyoutHost.revalidate();
        flyoutHost.repaint();
    }

    private void hideBalamFlyout() {
        if (balamFlyout != null) {
            flyoutHost.remove(balamFlyout);
            balamFlyout = null;
            balamBtn.setText("Show Balam Calendar");
            flyoutHost.revalidate();
            flyoutHost.repaint();
        }
    }

    public MyMuhurtaView(Point loc, Horoscope userHoroscope) {
        super(viewSize, loc);
        // Person selection is always explicit, independent of the main-screen chart.

        final JPanel mainPanel = new JPanel(new BorderLayout(15, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 20));
        
        chartsPanel = new JPanel(new java.awt.GridLayout(1, 0, 10, 10));
        
        mainPanel.add(createTablePanel(), BorderLayout.WEST);
        chartsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartScrollPane = new javax.swing.JScrollPane(chartsPanel);
        chartScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY, 1));
        
        final JPanel scrollWrapper = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        scrollWrapper.add(chartScrollPane);
        JPanel chartArea = new JPanel(new BorderLayout(0, 8));
        JPanel toolbarHost = new JPanel(new BorderLayout());
        toolbarHost.add(createPeopleToolbar(), BorderLayout.WEST);
        chartArea.add(toolbarHost, BorderLayout.NORTH);
        chartArea.add(scrollWrapper, BorderLayout.CENTER);
        // Outside the chart viewport border, aligned with the first (Muhurta) chart.
        chartControlStrip = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        chartControlStrip.setBorder(BorderFactory.createEmptyBorder(0, 11, 0, 0));
        JPanel chartControls = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 6));
        chartControls.setPreferredSize(new Dimension(chartSize.width, 42));
        chartControls.add(balamBtn);
        chartArea.add(chartControlStrip, BorderLayout.SOUTH);
        chartControlStrip.add(chartControls);

        flyoutHost = new JPanel(new BorderLayout(0, 10) {
            @Override
            public void layoutContainer(java.awt.Container parent) {
                super.layoutContainer(parent);
                java.awt.Component calendar = getLayoutComponent(BorderLayout.CENTER);
                if (calendar != null) {
                    Dimension size = app.astrosoft.ui.comp.CalendarResizeHandles.fit(
                        (JPanel) calendar, calendar.getWidth(), calendar.getHeight());
                    calendar.setSize(size);
                }
            }
        });
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.add(chartArea, BorderLayout.CENTER);
        flyoutHost.add(chartContainer, BorderLayout.NORTH);
        mainPanel.add(flyoutHost, BorderLayout.CENTER);
        
        MyMuhurtaView.this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                resizeChartViewport();
                scrollWrapper.revalidate();
            }
        });
        
        selectedDate = new java.util.Date();
        selectedTime = selectedDate;
        selectedPlace = AstroSoft.getPreferences().getPlace();
        
                // Ensure the content inside mainPanel stays left-aligned relative to itself,
        // but the mainPanel as a whole gets perfectly centered!
        this.removeAll();
        this.setLayout(new java.awt.BorderLayout());

        this.add(mainPanel, BorderLayout.CENTER);
        
        // Initial setup
        updateView();
    }
    
    private app.astrosoft.core.MyMuhurta createMuhurta() {
        return new app.astrosoft.core.MyMuhurta(selectedDate, selectedTime, selectedPlace, person1Horoscope != null ? person1Horoscope : person2Horoscope);
    }

    private void applyCalendarDate(java.util.Date startInstant, java.util.Date endInstant) {
        selectedDate = new java.util.Date(startInstant.getTime());
        selectedTime = selectedDate;
        
        try {
            app.astrosoft.beans.Place placeToUse = selectedPlace != null ? selectedPlace : app.astrosoft.ui.AstroSoft.getPreferences().getPlace();
            java.util.Calendar cal = java.util.Calendar.getInstance(placeToUse.astrosoftTimeZone().getTimeZone());
            cal.setTime(startInstant);
            java.util.List<app.astrosoft.core.MuhurtaWindowsCalculator.Window> timings = 
                app.astrosoft.core.MuhurtaWindowsCalculator.forDay(cal, placeToUse);
            
            Long firstHoraStart = null;
            long selectedEpochStart = startInstant.getTime();
            long selectedEpochEnd = endInstant.getTime();
            
            for (app.astrosoft.core.MuhurtaWindowsCalculator.Window w : timings) {
                if ("Subha Hora".equals(w.kind)) {
                    // Check if Subha Hora OVERLAPS the selected window (or starts within it)
                    // The simplest is to find the first Subha Hora that starts *before* the end of the window
                    // AND ends *after* the start of the window.
                    if (w.start < selectedEpochEnd && w.end > selectedEpochStart) {
                        if (firstHoraStart == null || w.start < firstHoraStart) {
                            firstHoraStart = w.start;
                        }
                    }
                }
            }
            
            // If no Subha Hora is available inside the window, the user said:
            // "If no Subha Hora is availabel, it will pick start time of widnow in the selction."
            // So we DO NOT fall back to the first Subha Hora of the day! We leave selectedTime as selectedDate (start time of window).
            
            if (firstHoraStart != null) {
                // If the Subha Hora starts BEFORE the window starts, we should snap to the window start
                // OR we can just snap to the Subha Hora start. The user said: "For time, is should the first Subha Hora hour."
                // I will use the maximum of the Subha Hora start and the Window start so it's strictly inside the window,
                // OR just the Subha Hora start. The user's screenshot showed exactly the Subha Hora start time in their own example (8:46).
                // Actually, I'll just use the Subha Hora start, since that's what makes sense astrologically.
                long snappedTime = Math.max(firstHoraStart, selectedEpochStart);
                selectedTime = new java.util.Date(snappedTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        updateView(false);
    }

    private void updateView() {
        updateView(true);
    }

    private void updateView(boolean refreshCalendar) {
        app.astrosoft.core.MyMuhurta muhurta = createMuhurta();
        
        tableModel.updateData(new MyMuhurta(selectedDate, selectedTime, selectedPlace, null).getTableData());
        boolean hasPeople = person1Horoscope != null || person2Horoscope != null;
        balamBtn.setEnabled(hasPeople);
        balamBtn.setVisible(hasPeople);
        chartControlStrip.setVisible(hasPeople);
        balamBtn.setToolTipText(hasPeople ? "Show personal Balam calendar" : "Select Person 1 or Person 2 to view personal Balam");
        if (!hasPeople) hideBalamFlyout();
        updatePeopleButtons();
        applyRowHeights();

        refreshCharts(muhurta);
        if (balamFlyout != null && refreshCalendar) {
            balamFlyout.setPeople(person1Horoscope, person2Horoscope);
            balamFlyout.setContext(muhurta);
        }
    }

    private void applyRowHeights() {
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
    }

    private void refreshCharts(app.astrosoft.core.MyMuhurta muhurta) {
        chartsPanel.removeAll();

        Chart muhurtaRasiChart = new Chart(new app.astrosoft.beans.PlanetChartData(
            app.astrosoft.consts.Varga.Rasi,
            muhurta.getEventHoroscope().getPlanetaryInfo()) {
            public String getChartName() { return "<html><center>Muhurta<br>Rasi</center></html>"; }
        }, chartSize);
        chartsPanel.add(muhurtaRasiChart);

        addPersonChart("Person 1", person1Horoscope);
        addPersonChart("Person 2", person2Horoscope);
        resizeChartViewport();
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
    
    /** Fit the chart strip to the actual padded right column, including its border. */
    private void resizeChartViewport() {
        if (chartScrollPane == null || flyoutHost == null) return;
        int available = flyoutHost.getWidth();
        if (available <= 0) available = Math.max(100, getWidth() - KeyColWidth - ValueColWidth - 50);
        int count = Math.max(1, chartsPanel.getComponentCount());
        int chartW = chartSize.width + 10;
        int fit = Math.max(1, Math.min(count, (available - 25) / chartW));
        int width = Math.min(available, fit * chartW + 25);
        int scrollHeight = count > fit || width < chartW + 25
            ? chartScrollPane.getHorizontalScrollBar().getPreferredSize().height : 0;
        chartScrollPane.setPreferredSize(new Dimension(width, chartSize.height + 35 + scrollHeight));
        chartScrollPane.revalidate();
    }

    private static String htmlEscape(String text) {
        return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void addPersonChart(final String role, final Horoscope person) {
        if (person == null) return;
        chartsPanel.add(new Chart(new PlanetChartData(Varga.Rasi, person.getPlanetaryInfo()) {
            public String getChartName() {
                return "<html><center>" + role + "<br>Rasi</center></html>";
            }
        }, chartSize));
    }

    private JPanel createPeopleToolbar() {
        JPanel bar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));
        bar.setBorder(BorderFactory.createTitledBorder("Personal horoscopes"));
        person1Btn = createRoundedButton("Person 1: Select...", java.awt.Font.PLAIN, new Dimension(190, 30));
        person2Btn = createRoundedButton("Person 2: Select...", java.awt.Font.PLAIN, new Dimension(190, 30));
        person1Btn.addActionListener(e -> selectPerson(true));
        person2Btn.addActionListener(e -> selectPerson(false));
        bar.add(person1Btn);
        bar.add(person2Btn);
        return bar;
    }

    private void selectPerson(boolean person1) {
        String file = app.astrosoft.util.FileOps.openFileDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this), app.astrosoft.util.FileOps.FileDialogMode.OPEN,
            app.astrosoft.util.AstrosoftFileFilter.HOROSCOPE_EXTN);
        if (file == null) return;
        Horoscope person = Horoscope.createFromFile(file);
        if (person == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Unable to load horoscope file.",
                "Load horoscope", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (person1) person1Horoscope = person; else person2Horoscope = person;
        updateView();
    }

    private void updatePeopleButtons() {
        person1Btn.setText(person1Horoscope == null ? "Person 1: Select..." : "Person 1: " + person1Horoscope.getPersonName());
        person2Btn.setText(person2Horoscope == null ? "Person 2: Select..." : "Person 2: " + person2Horoscope.getPersonName());
        person1Btn.setToolTipText(person1Btn.getText() + " — click to load or replace");
        person2Btn.setToolTipText(person2Btn.getText() + " — click to load or replace");
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
            public String getToolTipText(java.awt.event.MouseEvent event) {
                int row=rowAtPoint(event.getPoint());
                return row<0?null:app.astrosoft.core.PanchangTransitionHelp.forRow(String.valueOf(getValueAt(row,0)));
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
        // WEST stretches vertically: anchor the content, not its buttons, at the top.
        JPanel tableColumn = new JPanel(new BorderLayout());
        tableColumn.add(tablePanel, BorderLayout.NORTH);
        return tableColumn;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 10));
        bottomPanel.setOpaque(false);

        balamBtn = createRoundedButton("Show Balam Calendar", java.awt.Font.BOLD, new java.awt.Dimension(160, 30));
        balamBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                toggleBalamFlyout();
            }
        });

        javax.swing.JButton editBtn = createRoundedButton("Edit Date/Time", java.awt.Font.PLAIN, new java.awt.Dimension(120, 30));
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

    private javax.swing.JButton createRoundedButton(String text, int fontStyle, java.awt.Dimension preferredSize) {
        javax.swing.JButton button = new javax.swing.JButton(text) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setColor(java.awt.Color.LIGHT_GRAY);
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setFont(new java.awt.Font("SansSerif", fontStyle, 12));
        button.setPreferredSize(preferredSize);
        return button;
    }
}
