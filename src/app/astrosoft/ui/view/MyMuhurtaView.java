package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
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
import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.ui.dlg.MuhurtaProfileDialog;
import app.astrosoft.consts.Varga;
import java.util.EnumMap;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;

public class MyMuhurtaView extends AstrosoftView {

    private static final Dimension viewSize = new Dimension(1500, 700);
    private static final Dimension chartSize = new Dimension(348, 296);
    private static final Dimension compactChartSize = new Dimension(272, 200);
    private static final int MIN_CHART_W = 256;
    private static final int MIN_CHART_H = 188;
    
    private static final int KeyColWidth = 140;
    private static final int ValueColWidth = 260;
    private static final int RowHeight = 28;
    private static final int AusRowHeight = 70;
    private static final Dimension headerSize = new Dimension(KeyColWidth + ValueColWidth, RowHeight);

    private static final java.awt.Color SCREEN_BG = new java.awt.Color(240, 242, 245);
    private static final java.awt.Color PANEL_BORDER_CLR = new java.awt.Color(176, 196, 222);
    private static final java.awt.Color SUB_BORDER_CLR = new java.awt.Color(208, 219, 229);
    private static final java.awt.Color KEY_CELL_BG = new java.awt.Color(247, 249, 252);
    private static final java.awt.Color KEY_TEXT_CLR = new java.awt.Color(10, 61, 98);
    private static final java.awt.Color VAL_TEXT_CLR = new java.awt.Color(17, 17, 17);
    private static final java.awt.Color BOTTOM_BAR_BG = new java.awt.Color(245, 247, 250);
    private static final java.awt.Color BTN_BG = new java.awt.Color(232, 237, 245);
    private static final java.awt.Color BTN_ARMED_BG = new java.awt.Color(212, 222, 238);
    private static final java.awt.Color BTN_BORDER_CLR = new java.awt.Color(122, 155, 194);
    private static final java.awt.Color BTN_TEXT_CLR = new java.awt.Color(26, 59, 92);
    private static final java.awt.Color SPLIT_BG_CLR = new java.awt.Color(219, 227, 237);
    private static final java.awt.Color SPLIT_DOT_CLR = new java.awt.Color(112, 136, 168);
    
    private java.util.Date selectedDate = new java.util.Date();
    private java.util.Date selectedTime = new java.util.Date();
    private app.astrosoft.beans.Place selectedPlace = app.astrosoft.ui.AstroSoft.getPreferences().getPlace();
    private MuhurtaProfile person1Profile;
    private MuhurtaProfile person2Profile;
    private javax.swing.JButton person1Btn, person2Btn;
    private javax.swing.JScrollPane chartScrollPane;
    private Dimension currentChartDim = chartSize;
    
    private AstrosoftTableModel tableModel;
    private AstrosoftTable table;
    private JPanel chartsPanel;
    private JPanel chartControlStrip;
    private JPanel chartControls;
    private JPanel chartContainer;
    private JPanel flyoutHost;
    private javax.swing.JSplitPane centerSplitPane;
    private int savedSplitLocation = -1;
    private boolean updatingSplitLayout = false;
    private app.astrosoft.core.MyMuhurta lastMuhurta;
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
        balamFlyout = new app.astrosoft.ui.comp.BalamCalendarPanel(getOrCreateMuhurta(), (startInstant, endInstant) -> applyCalendarDate(startInstant, endInstant), person1Profile, person2Profile);
        balamFlyout.setPeople(person1Profile, person2Profile);
        balamBtn.setText("Hide Balam Calendar");

        int targetLoc = savedSplitLocation > 0 ? savedSplitLocation : getPreferredTopSplitHeight(compactChartSize.height);
        updatingSplitLayout = true;
        try {
            flyoutHost.removeAll();
            refreshCharts(getOrCreateMuhurta());
            centerSplitPane = createCenterSplitPane(chartContainer, balamFlyout);
            flyoutHost.add(centerSplitPane, BorderLayout.CENTER);
            centerSplitPane.setDividerLocation(targetLoc);
        } finally {
            updatingSplitLayout = false;
        }
        flyoutHost.revalidate();
        flyoutHost.repaint();
    }

    private void hideBalamFlyout() {
        if (balamFlyout != null) {
            if (centerSplitPane != null && centerSplitPane.getDividerLocation() > 0) {
                savedSplitLocation = centerSplitPane.getDividerLocation();
            }
            flyoutHost.removeAll();
            balamFlyout = null;
            centerSplitPane = null;
            flyoutHost.add(chartContainer, BorderLayout.NORTH);
            balamBtn.setText("Show Balam Calendar");
            refreshCharts(getOrCreateMuhurta());
            flyoutHost.revalidate();
            flyoutHost.repaint();
        }
    }

    private int getPreferredTopSplitHeight(int targetChartH) {
        int toolbarH = 46;
        int controlH = chartControlStrip != null && chartControlStrip.isVisible() ? 32 : 4;
        return toolbarH + targetChartH + 18 + controlH;
    }

    private javax.swing.JSplitPane createCenterSplitPane(JPanel topComp, JPanel bottomComp) {
        topComp.setMinimumSize(new Dimension(MIN_CHART_W + 20, getPreferredTopSplitHeight(MIN_CHART_H)));
        bottomComp.setMinimumSize(new Dimension(320, 180));

        javax.swing.JSplitPane sp = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT, true, topComp, bottomComp);
        sp.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            protected Component createDefaultNonContinuousLayoutDivider() {
                JPanel p = new JPanel();
                p.setOpaque(false);
                return p;
            }
            @Override
            public void finishedPaintingChildren(javax.swing.JSplitPane jc, java.awt.Graphics g) {
                // Never paint the dark non-continuous ghost divider bar
            }
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(java.awt.Graphics g) {
                        int w = getWidth();
                        int h = getHeight();
                        g.setColor(SPLIT_BG_CLR);
                        g.fillRect(0, 0, w, h);
                        g.setColor(PANEL_BORDER_CLR);
                        g.drawLine(0, 0, w, 0);
                        g.drawLine(0, h - 1, w, h - 1);
                        g.setColor(SPLIT_DOT_CLR);
                        int cy = h / 2;
                        int cx = w / 2;
                        for (int dx = -12; dx <= 12; dx += 6) {
                            g.fillOval(cx + dx - 1, cy - 1, 3, 3);
                        }
                    }
                };
            }
        });
        sp.setContinuousLayout(true);
        sp.setOneTouchExpandable(false);
        sp.setResizeWeight(0.0);
        sp.setDividerSize(8);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(SCREEN_BG);
        sp.setToolTipText("Drag divider to adjust Charts and Balam Calendar height");

        sp.addPropertyChangeListener(javax.swing.JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            if (updatingSplitLayout || balamFlyout == null) return;
            int loc = sp.getDividerLocation();
            if (loc > 0) {
                savedSplitLocation = loc;
                Dimension desired = computeEffectiveChartSize(loc);
                if (Math.abs(desired.width - currentChartDim.width) >= 8 || Math.abs(desired.height - currentChartDim.height) >= 8) {
                    updatingSplitLayout = true;
                    try {
                        refreshCharts(getOrCreateMuhurta());
                    } finally {
                        updatingSplitLayout = false;
                    }
                } else {
                    resizeChartViewport();
                }
            }
        });
        return sp;
    }

    public MyMuhurtaView(Point loc, Horoscope userHoroscope) {
        this(loc, userHoroscope != null ? new MuhurtaProfile(userHoroscope) : null);
    }

    public MyMuhurtaView(Point loc, MuhurtaProfile userProfile) {
        super(viewSize, loc);
        // Person selection is always explicit, independent of the main-screen chart.
        this.setBackground(SCREEN_BG);

        final JPanel mainPanel = new JPanel(new BorderLayout(14, 10));
        mainPanel.setBackground(SCREEN_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        
        chartsPanel = new JPanel(new java.awt.GridLayout(1, 0, 10, 8));
        chartsPanel.setBackground(java.awt.Color.WHITE);
        
        mainPanel.add(createTablePanel(), BorderLayout.WEST);
        chartsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));
        chartScrollPane = new javax.swing.JScrollPane(chartsPanel);
        chartScrollPane.setBackground(java.awt.Color.WHITE);
        chartScrollPane.getViewport().setBackground(java.awt.Color.WHITE);
        chartScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(SUB_BORDER_CLR, 1));
        
        final JPanel scrollWrapper = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        scrollWrapper.setOpaque(false);
        scrollWrapper.add(chartScrollPane);
        JPanel chartArea = new JPanel(new BorderLayout(0, 3));
        chartArea.setOpaque(false);
        JPanel toolbarHost = new JPanel(new BorderLayout());
        toolbarHost.setOpaque(false);
        toolbarHost.add(createPeopleToolbar(), BorderLayout.WEST);
        chartArea.add(toolbarHost, BorderLayout.NORTH);
        chartArea.add(scrollWrapper, BorderLayout.CENTER);
        // Outside the chart viewport border, aligned with the first (Muhurta) chart.
        chartControlStrip = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        chartControlStrip.setOpaque(false);
        chartControlStrip.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        chartControls = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 1));
        chartControls.setOpaque(false);
        chartControls.setPreferredSize(new Dimension(chartSize.width, 30));
        chartControls.add(balamBtn);
        chartArea.add(chartControlStrip, BorderLayout.SOUTH);
        chartControlStrip.add(chartControls);

        flyoutHost = new JPanel(new BorderLayout(0, 8));
        flyoutHost.setOpaque(false);
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setOpaque(false);
        chartContainer.add(chartArea, BorderLayout.CENTER);
        flyoutHost.add(chartContainer, BorderLayout.NORTH);
        mainPanel.add(flyoutHost, BorderLayout.CENTER);
        
        java.awt.event.ComponentAdapter resizeHandler = new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension desired = computeEffectiveChartSize(centerSplitPane != null ? centerSplitPane.getDividerLocation() : -1);
                if (!desired.equals(currentChartDim)) {
                    refreshCharts(getOrCreateMuhurta());
                } else {
                    resizeChartViewport();
                }
                scrollWrapper.revalidate();
            }
        };
        MyMuhurtaView.this.addComponentListener(resizeHandler);
        flyoutHost.addComponentListener(resizeHandler);
        
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
        lastMuhurta = new app.astrosoft.core.MyMuhurta(selectedDate, selectedTime, selectedPlace, person1Profile != null ? person1Profile : person2Profile);
        return lastMuhurta;
    }

    private app.astrosoft.core.MyMuhurta getOrCreateMuhurta() {
        return lastMuhurta != null ? lastMuhurta : createMuhurta();
    }

    private void applyCalendarDate(java.util.Date chosenInstant, java.util.Date endInstant) {
        selectedDate = new java.util.Date(chosenInstant.getTime());
        selectedTime = selectedDate;
        updateView(true);
    }

    private void updateView() {
        updateView(true);
    }

    private void updateView(boolean refreshCalendar) {
        app.astrosoft.core.MyMuhurta muhurta = createMuhurta();
        
        tableModel.updateData(muhurta.getTableData());
        boolean hasPeople = person1Profile != null || person2Profile != null;
        balamBtn.setEnabled(hasPeople);
        balamBtn.setVisible(hasPeople);
        chartControlStrip.setVisible(hasPeople);
        balamBtn.setToolTipText(hasPeople ? "Show personal Balam calendar" : "Select Person 1 or Person 2 to view personal Balam");
        if (!hasPeople) hideBalamFlyout();
        updatePeopleButtons();
        applyRowHeights();

        refreshCharts(muhurta);
        if (balamFlyout != null && refreshCalendar) {
            balamFlyout.setPeople(person1Profile, person2Profile);
            balamFlyout.setContext(muhurta);
        }
    }

    private void applyRowHeights() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            TableCellRenderer renderer = table.getCellRenderer(i, 1);
            Component c = table.prepareRenderer(renderer, i, 1);
            int prefH = c.getPreferredSize().height;
            int lines = Math.max(1, Math.round((float) prefH / 15f));
            int calculatedHeight = Math.max(25, Math.max(lines * 16 + 7, prefH + 7));
            table.setRowHeight(i, calculatedHeight);
        }
    }

    public static String formatMultiLineValue(String key, String value, int width) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String s = value.trim();
        if (s.startsWith("<html>")) {
            return s;
        }

        if (key != null && key.equals("Time")) {
            s = s.replace(",", "<br>");
        } else if (key != null && (key.contains(DisplayStrings.AUS_TIME_STR.toString())
                || key.contains("Auspicious Time") || key.contains("Subha Hora"))) {
            s = s.replace(", ", "<br>").replace(",", "<br>");
        } else {
            if (s.contains(" → ")) {
                s = s.replace(" → ", " →<br>");
            } else if (s.contains(" -> ")) {
                s = s.replace(" -> ", " →<br>");
            }

            if (s.contains(" – ")) {
                s = s.replace(" – ", " –<br>");
            } else if (s.contains(" - ") && key != null && (key.contains("Muhurta") || key.contains("Brahma") || key.contains("Abhijit"))) {
                s = s.replace(" - ", " –<br>");
            }

            if (key != null && key.equals("Location")) {
                if (s.length() > 25 && s.contains(",")) {
                    String[] parts = s.split(",\\s*");
                    if (parts.length == 2) {
                        s = parts[0] + ",<br>" + parts[1];
                    } else if (parts.length >= 3) {
                        if ((parts[1] + ", " + parts[2]).length() <= 28) {
                            s = parts[0] + ",<br>" + parts[1] + ", " + parts[2];
                        } else {
                            s = parts[0] + ",<br>" + parts[1] + ",<br>" + parts[2];
                        }
                    }
                }
            }
        }

        if (s.contains("<br>") || s.length() > 28) {
            return "<html><table cellpadding=\"0\" cellspacing=\"0\" width=\"" + width + "\"><tr><td>" + s + "</td></tr></table></html>";
        }
        return value;
    }

    private Dimension computeEffectiveChartSize(int splitDividerLoc) {
        int chartCount = 1 + (person1Profile != null && person1Profile.getHoroscope() != null ? 1 : 0)
                           + (person2Profile != null && person2Profile.getHoroscope() != null ? 1 : 0);
        int availW = flyoutHost != null && flyoutHost.getWidth() > 0
            ? flyoutHost.getWidth()
            : Math.max(360, getWidth() - KeyColWidth - ValueColWidth - 45);

        int targetW = balamFlyout != null ? compactChartSize.width : chartSize.width;
        int targetH = balamFlyout != null ? compactChartSize.height : chartSize.height;

        if (balamFlyout != null && splitDividerLoc > 0) {
            int overheadH = 46 + 18 + (chartControlStrip != null && chartControlStrip.isVisible() ? 32 : 4);
            int maxFromSplitH = Math.max(MIN_CHART_H, Math.min(chartSize.height, splitDividerLoc - overheadH));
            targetH = maxFromSplitH;
            targetW = Math.max(MIN_CHART_W, Math.min(chartSize.width, (int) Math.round(targetH * (double) compactChartSize.width / compactChartSize.height)));
        }

        if (chartCount >= 2 && availW > 0) {
            int maxPerChartW = (availW - 18 - (chartCount - 1) * 10) / chartCount;
            if (maxPerChartW >= MIN_CHART_W && maxPerChartW < targetW) {
                targetW = maxPerChartW;
            }
        }

        // Snap to multiples of 4 so all 4x4 house cells align cleanly without rounding gaps or clipping.
        int snappedW = (Math.max(MIN_CHART_W, Math.min(chartSize.width, targetW)) / 4) * 4;
        int snappedH = (Math.max(MIN_CHART_H, Math.min(chartSize.height, targetH)) / 4) * 4;
        return new Dimension(snappedW, snappedH);
    }

    private void refreshCharts(app.astrosoft.core.MyMuhurta muhurta) {
        chartsPanel.removeAll();
        currentChartDim = computeEffectiveChartSize(centerSplitPane != null ? centerSplitPane.getDividerLocation() : -1);

        Chart muhurtaRasiChart = new Chart(new app.astrosoft.beans.PlanetChartData(
            app.astrosoft.consts.Varga.Rasi,
            muhurta.getEventHoroscope().getPlanetaryInfo()) {
            public String getChartName() { return "<html><center>Muhurta<br>Rasi</center></html>"; }
        }, currentChartDim);
        chartsPanel.add(muhurtaRasiChart);

        addPersonChart("Person 1", person1Profile != null ? person1Profile.getHoroscope() : null);
        addPersonChart("Person 2", person2Profile != null ? person2Profile.getHoroscope() : null);
        if (chartControls != null) {
            chartControls.setPreferredSize(new Dimension(currentChartDim.width, 30));
        }
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
        int w = currentChartDim.width;
        int fit = 1;
        for (int k = count; k >= 1; k--) {
            int requiredW = k * w + (k - 1) * 10 + 18;
            if (requiredW <= available) {
                fit = k;
                break;
            }
        }
        int gridW = fit * w + (fit - 1) * 10 + 18;
        int width = Math.min(available, gridW);
        int scrollHeight = count > fit || width < gridW
            ? chartScrollPane.getHorizontalScrollBar().getPreferredSize().height : 0;
        chartScrollPane.setPreferredSize(new Dimension(width, currentChartDim.height + 12 + scrollHeight));
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
        }, currentChartDim));
    }

    private JPanel createPeopleToolbar() {
        JPanel bar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 3));
        bar.setBackground(java.awt.Color.WHITE);
        javax.swing.border.TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SUB_BORDER_CLR, 1),
            "Personal horoscopes"
        );
        tb.setTitleFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
        tb.setTitleColor(KEY_TEXT_CLR);
        bar.setBorder(tb);
        person1Btn = createRoundedButton("Person 1: Select...", java.awt.Font.BOLD, new Dimension(190, 26));
        person2Btn = createRoundedButton("Person 2: Select...", java.awt.Font.PLAIN, new Dimension(190, 26));
        person1Btn.addActionListener(e -> selectPerson(true));
        person2Btn.addActionListener(e -> selectPerson(false));
        bar.add(person1Btn);
        bar.add(person2Btn);
        return bar;
    }

    private void selectPerson(boolean person1) {
        MuhurtaProfile current = person1 ? person1Profile : person2Profile;
        String label = person1 ? "Person 1" : "Person 2";
        MuhurtaProfileDialog dlg = new MuhurtaProfileDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this), label, current);
        dlg.setVisible(true);
        if (dlg.isApplied()) {
            if (person1) {
                person1Profile = dlg.getSelectedProfile();
            } else {
                person2Profile = dlg.getSelectedProfile();
            }
            updateView();
        }
    }

    private void updatePeopleButtons() {
        person1Btn.setText(getButtonLabel("Person 1", person1Profile));
        person2Btn.setText(getButtonLabel("Person 2", person2Profile));
        person1Btn.setToolTipText(getButtonTooltip("Person 1", person1Profile));
        person2Btn.setToolTipText(getButtonTooltip("Person 2", person2Profile));
    }

    private String getButtonLabel(String role, MuhurtaProfile profile) {
        if (profile == null) {
            return role + ": Select...";
        }
        String name = profile.getName();
        if (name == null || name.trim().isEmpty()) {
            name = role;
        }
        return role + ": " + name;
    }

    private String getButtonTooltip(String role, MuhurtaProfile profile) {
        if (profile == null) {
            return role + ": Select... — click to select full horoscope or quick entry";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(profile.getName());
        if (profile.getRasi() != null && profile.getNakshathra() != null) {
            sb.append(" (").append(profile.getRasi()).append(" / ").append(profile.getNakshathra()).append(")");
        }
        if (profile.hasHoroscope()) {
            sb.append(" [Full Horoscope]");
        } else {
            sb.append(" [Quick Entry]");
        }
        sb.append(" — click to change or clear");
        return sb.toString();
    }

    private JPanel createTablePanel() {
        tableModel = new AstrosoftTableModel(new app.astrosoft.ui.table.DefaultTable(
            TableDataFactory.getTableData(new ArrayList<MapTableRow>()),
            new DefaultColumnMetaData(AstrosoftTableColumn.keyvalCols())
        ));
        
        final java.awt.Font keyFont = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 11);
        final java.awt.Font valFont = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 11);

        table = new AstrosoftTable(tableModel, TableStyle.GRID) {
            TableCellRenderer multiLineRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setFont(valFont);
                    Object keyVal = table.getValueAt(row, 0);
                    String key = keyVal != null ? keyVal.toString() : "";
                    String valStr = value != null ? value.toString() : "";
                    String formatted = formatMultiLineValue(key, valStr, ValueColWidth - 18);
                    if (!valFont.canDisplay('\u2192') && formatted.indexOf('\u2192') >= 0) {
                        formatted = formatted.replace("\u2192", "->");
                    }
                    setText(formatted);
                    return this;
                }
            };

            @Override
            public TableCellRenderer getCellRenderer(int row, int col) {
                if (col == 1) {
                    return multiLineRenderer;
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
                if (column == 0) {
                    c.setFont(keyFont);
                    c.setBackground(KEY_CELL_BG);
                    c.setForeground(KEY_TEXT_CLR);
                } else {
                    c.setFont(valFont);
                    c.setBackground(java.awt.Color.WHITE);
                    c.setForeground(VAL_TEXT_CLR);
                }
                if (c instanceof javax.swing.JComponent) {
                    ((javax.swing.JComponent) c).setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 4));
                }
                return c;
            }
        };
        
        table.setGridColor(SUB_BORDER_CLR);
        table.setColumnWidth(ValueColWidth, AstrosoftTableColumn.Value);
        table.setColumnWidth(KeyColWidth, AstrosoftTableColumn.Key);
        table.setRowHeight(RowHeight);
        
        javax.swing.JScrollPane tableScrollPane = new javax.swing.JScrollPane(table);
        tableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(java.awt.Color.WHITE);
        tableScrollPane.getVerticalScrollBar().setUnitIncrement(18);
        tableScrollPane.setPreferredSize(new Dimension(KeyColWidth + ValueColWidth + 16, 560));

        JPanel tableColumn = new JPanel(new BorderLayout(0, 0));
        tableColumn.setBackground(java.awt.Color.WHITE);
        tableColumn.setBorder(BorderFactory.createLineBorder(PANEL_BORDER_CLR, 1));
        tableColumn.add(tableScrollPane, BorderLayout.CENTER);
        tableColumn.add(createBottomPanel(), BorderLayout.SOUTH);
        return tableColumn;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 6));
        bottomPanel.setBackground(BOTTOM_BAR_BG);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, PANEL_BORDER_CLR));

        balamBtn = createRoundedButton("Show Balam Calendar", java.awt.Font.BOLD, new java.awt.Dimension(168, 28));
        balamBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                toggleBalamFlyout();
            }
        });

        javax.swing.JButton editBtn = createRoundedButton("Edit Date/Time", java.awt.Font.BOLD, new java.awt.Dimension(124, 28));
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


        javax.swing.JButton summarizeBtn = createRoundedButton("Summarize", java.awt.Font.BOLD, new java.awt.Dimension(104, 28));
        summarizeBtn.setToolTipText("Open plain-text summary of Muhurta and planetary positions");
        summarizeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showSummaryDialog();
            }
        });

        bottomPanel.add(summarizeBtn);
        bottomPanel.add(editBtn);
        return bottomPanel;
    }

    private void showSummaryDialog() {
        app.astrosoft.core.MyMuhurta muhurta = createMuhurta();
        app.astrosoft.ui.dlg.MuhurtaSummaryDialog dlg = new app.astrosoft.ui.dlg.MuhurtaSummaryDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this),
            muhurta,
            selectedPlace,
            person1Profile,
            person2Profile
        );
        dlg.setVisible(true);
    }

    private javax.swing.JButton createRoundedButton(String text, int fontStyle, java.awt.Dimension preferredSize) {
        javax.swing.JButton button = new javax.swing.JButton(text) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed() || getModel().isPressed()) {
                    g2.setColor(BTN_ARMED_BG);
                } else {
                    g2.setColor(BTN_BG);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.setColor(BTN_BORDER_CLR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setBackground(BTN_BG);
        button.setForeground(BTN_TEXT_CLR);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        button.setMargin(new java.awt.Insets(2, 6, 2, 6));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setFont(new java.awt.Font("SansSerif", fontStyle, 11));
        button.setPreferredSize(preferredSize);
        return button;
    }
}
