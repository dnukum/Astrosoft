package app.astrosoft.ui.comp;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import app.astrosoft.core.MyMuhurta;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.DefaultTable;
import app.astrosoft.ui.table.TableData;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.beans.TaraBalamResult;
import app.astrosoft.beans.ChandraBalamResult;
import app.astrosoft.beans.BalamDateCell;
import app.astrosoft.core.Horoscope;
import app.astrosoft.util.FileOps;
import app.astrosoft.util.AstrosoftFileFilter;
import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.ui.table.DefaultColumnMetaData;

public class BalamCalendarPanel extends JPanel {
    private final java.util.function.BiConsumer<java.util.Date, java.util.Date> updateAction;
    private JButton updateButton;
    private JButton filterButton;
    private boolean isFiltered = false;
    private MyMuhurta muhurta;
    private int currentYear;
    private int currentMonth;
    
    private AstrosoftTable table;
    private AstrosoftTableModel tableModel;
    private JLabel monthLabel;
    private JComboBox<String> monthChoice;
    private JComboBox<Integer> yearChoice;
    private boolean syncingMonthChoices;
    private app.astrosoft.beans.MuhurtaProfile partnerProfile;
    private app.astrosoft.beans.MuhurtaProfile primaryProfile;
    private String primaryLabel = "Person";
    private String partnerLabel = "Person";
    public static class HoraItem {
        private final String label;
        private final long targetTime;

        public HoraItem(String label, long targetTime) {
            this.label = label;
            this.targetTime = targetTime;
        }

        public long getTargetTime() { return targetTime; }
        public String getLabel() { return label; }

        @Override
        public String toString() { return label; }
    }

    private JComboBox<HoraItem> horaCombo;
    private JEditorPane leftDetailsPane;
    private JEditorPane rightDetailsPane;
    private JEditorPane detailsPane;
    private JSplitPane splitPane;
    private SwingWorker<app.astrosoft.beans.BalamWindowDetails, Void> detailsWorker;
    private long detailsRequest;
    private java.util.Date selectedWindowStart;
    private java.util.Date selectedWindowEnd;

    public JComboBox<HoraItem> getHoraCombo() {
        return horaCombo;
    }

    private JScrollPane scrollPane;

    private void cancelDetailsRequest() {
        detailsRequest++;
        if (detailsWorker != null) detailsWorker.cancel(true);
        detailsWorker = null;
    }

    @Override
    public void removeNotify() {
        cancelDetailsRequest();
        super.removeNotify();
    }


    public BalamCalendarPanel(MyMuhurta muhurta, java.util.function.BiConsumer<java.util.Date, java.util.Date> updateAction) {
        this(muhurta, updateAction, (app.astrosoft.beans.MuhurtaProfile) null, (app.astrosoft.beans.MuhurtaProfile) null);
    }

    public BalamCalendarPanel(MyMuhurta muhurta, java.util.function.BiConsumer<java.util.Date, java.util.Date> updateAction, Horoscope person1, Horoscope person2) {
        this(muhurta, updateAction, person1 != null ? new app.astrosoft.beans.MuhurtaProfile(person1) : null, person2 != null ? new app.astrosoft.beans.MuhurtaProfile(person2) : null);
    }

    public BalamCalendarPanel(MyMuhurta muhurta, java.util.function.BiConsumer<java.util.Date, java.util.Date> updateAction, app.astrosoft.beans.MuhurtaProfile person1, app.astrosoft.beans.MuhurtaProfile person2) {
        super();
        this.updateAction = updateAction;
        initComponents();
        setPeople(person1, person2);
        setContext(muhurta);

        setPreferredSize(new Dimension(800, 360));
        setMinimumSize(new Dimension(400, 200));
    }
    
    /** Refresh the context month without discarding the selected partner. */
    public void setContext(MyMuhurta muhurta) {
        if (muhurta == null) {
            throw new IllegalArgumentException("Muhurta context must not be null");
        }
        this.muhurta = muhurta;
        Calendar cal = (Calendar) muhurta.getPanchang().getDate().clone();
        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH) + 1;
        updateTable();
    }

    public void setPeople(Horoscope person1, Horoscope person2) {
        setPeople(person1 != null ? new app.astrosoft.beans.MuhurtaProfile(person1) : null,
                  person2 != null ? new app.astrosoft.beans.MuhurtaProfile(person2) : null);
    }

    /** Shared selectors are owned by the main MyMuhurta screen. */
    public void setPeople(app.astrosoft.beans.MuhurtaProfile person1, app.astrosoft.beans.MuhurtaProfile person2) {
        app.astrosoft.beans.MuhurtaProfile primary = person1 != null ? person1 : person2;
        primaryProfile = primary;
        partnerProfile = person1 != null ? person2 : null;
        primaryLabel = person1 != null ? (person1.getName().isEmpty() ? "Person 1" : person1.getName()) : "Person 2";
        partnerLabel = (person2 != null && !person2.getName().isEmpty()) ? person2.getName() : "Person 2";
    }

    private static String htmlEscape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final Color HEADER_BG = new Color(245, 247, 250);
    private static final Color PANEL_BORDER_CLR = new Color(176, 196, 222);
    private static final Color SUB_BORDER_CLR = new Color(208, 219, 229);
    private static final Color TBL_HEADER_BG = new Color(226, 234, 245);
    private static final Color TBL_ALT_ROW_BG = new Color(248, 250, 253);
    private static final Color TBL_SEL_BG = new Color(217, 235, 255);
    private static final Color NAVY_TEXT = new Color(26, 59, 92);
    private static final Color BTN_BG = new Color(238, 242, 247);
    private static final Color BTN_BORDER = new Color(173, 192, 214);

    private static void styleActionBtn(JButton btn) {
        btn.setBackground(BTN_BG);
        btn.setForeground(NAVY_TEXT);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(2, 6, 2, 6));
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BTN_BORDER, 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(PANEL_BORDER_CLR, 1));
        
        JPanel topPanel = new JPanel(new BorderLayout(12, 0));
        topPanel.setBackground(HEADER_BG);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SUB_BORDER_CLR));
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        navigationPanel.setOpaque(false);
        JButton prevBtn = new JButton("< Prev Month");
        JButton nextBtn = new JButton("Next Month >");
        styleActionBtn(prevBtn);
        styleActionBtn(nextBtn);
        monthLabel = new JLabel("");
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        monthLabel.setForeground(NAVY_TEXT);
        monthLabel.setPreferredSize(new Dimension(150, 30));
        monthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        prevBtn.addActionListener(e -> {
            currentMonth--;
            if (currentMonth < 1) { currentMonth = 12; currentYear--; }
            updateTable();
        });
        
        nextBtn.addActionListener(e -> {
            currentMonth++;
            if (currentMonth > 12) { currentMonth = 1; currentYear++; }
            updateTable();
        });
        

        monthChoice = new JComboBox<String>(new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
        yearChoice = new JComboBox<Integer>();
        monthChoice.setEditable(false);
        yearChoice.setEditable(false);
        monthChoice.setToolTipText("Choose calendar month");
        yearChoice.setToolTipText("Choose calendar year");
        java.awt.event.ActionListener chooseMonth = e -> {
            if (syncingMonthChoices || muhurta == null || yearChoice.getSelectedItem() == null) return;
            currentMonth = monthChoice.getSelectedIndex() + 1;
            currentYear = (Integer) yearChoice.getSelectedItem();
            updateTable();
        };
        monthChoice.addActionListener(chooseMonth);
        yearChoice.addActionListener(chooseMonth);
        navigationPanel.add(prevBtn);
        navigationPanel.add(monthChoice);
        navigationPanel.add(yearChoice);
        navigationPanel.add(nextBtn);
        topPanel.add(navigationPanel, BorderLayout.CENTER);
        filterButton = new JButton("Filter");
        styleActionBtn(filterButton);
        filterButton.setFocusable(false);
        filterButton.setToolTipText("Filter out rows with inauspicious Tara or Chandra Balam");
        filterButton.addActionListener(e -> {
            isFiltered = !isFiltered;
            filterButton.setText(isFiltered ? "Show All" : "Filter");
            updateTable();
        });
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        filterPanel.setOpaque(false);
        filterPanel.add(filterButton);
        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SUB_BORDER_CLR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        Font detailFont = new Font("SansSerif", Font.PLAIN, 11);

        leftDetailsPane = new JEditorPane("text/html", EMPTY_DETAILS) {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        leftDetailsPane.setEditable(false);
        leftDetailsPane.setBackground(Color.WHITE);
        leftDetailsPane.setFont(detailFont);
        leftDetailsPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JPanel rightContainer = new JPanel(new BorderLayout(0, 0));
        rightContainer.setBackground(Color.WHITE);

        JPanel horaControlPanel = new JPanel(new BorderLayout(6, 0));
        horaControlPanel.setBackground(HEADER_BG);
        horaControlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, SUB_BORDER_CLR),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));

        JLabel horaLabel = new JLabel("Subha Hora:");
        horaLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        horaLabel.setForeground(NAVY_TEXT);

        horaCombo = new JComboBox<HoraItem>();
        horaCombo.setPrototypeDisplayValue(new HoraItem("00:00 AM - 00:00 AM", 0L));
        horaCombo.setFont(detailFont);
        horaCombo.setPreferredSize(new Dimension(145, 24));
        horaCombo.setFocusable(false);
        horaCombo.setEnabled(false);

        updateButton = new JButton("Update");
        styleActionBtn(updateButton);
        updateButton.setPreferredSize(new Dimension(64, 24));
        updateButton.setEnabled(false);
        updateButton.setFocusable(false);
        updateButton.setToolTipText("Apply the selected window Subha Hora / start date/time to Muhurta");
        updateButton.addActionListener(e -> {
            int row = table == null ? -1 : table.getSelectedRow();
            if (row < 0) return;
            Object cell = table.getValueAt(row, 0);
            if (cell instanceof BalamDateCell) {
                app.astrosoft.beans.BalamRowBean rowBean = ((BalamDateCell) cell).getRow();
                selectedWindowStart = new java.util.Date(rowBean.getStartTime().getTime());
                selectedWindowEnd = new java.util.Date(rowBean.getEndTime().getTime());
                long chosenTime = selectedWindowStart.getTime();
                if (horaCombo != null) {
                    Object sel = horaCombo.getSelectedItem();
                    if (sel instanceof HoraItem) {
                        chosenTime = ((HoraItem) sel).getTargetTime();
                    }
                }
                updateAction.accept(new java.util.Date(chosenTime), new java.util.Date(rowBean.getEndTime().getTime()));
                if (table != null) table.requestFocusInWindow();
            }
        });

        horaControlPanel.add(horaLabel, BorderLayout.WEST);
        horaControlPanel.add(horaCombo, BorderLayout.CENTER);
        horaControlPanel.add(updateButton, BorderLayout.EAST);

        rightDetailsPane = new JEditorPane("text/html", "") {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        rightDetailsPane.setEditable(false);
        rightDetailsPane.setBackground(Color.WHITE);
        rightDetailsPane.setFont(detailFont);
        rightDetailsPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightDetailsPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JScrollPane leftScroll = new JScrollPane(leftDetailsPane);
        leftScroll.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        leftScroll.setBackground(Color.WHITE);
        leftScroll.getViewport().setBackground(Color.WHITE);
        leftScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        leftScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftScroll.setMinimumSize(new Dimension(150, 50));

        JScrollPane rightScroll = new JScrollPane(rightDetailsPane);
        rightScroll.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        rightScroll.setBackground(Color.WHITE);
        rightScroll.getViewport().setBackground(Color.WHITE);
        rightScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        rightScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightScroll.setMinimumSize(new Dimension(150, 50));

        rightContainer.add(horaControlPanel, BorderLayout.NORTH);
        rightContainer.add(rightScroll, BorderLayout.CENTER);
        rightContainer.setBorder(BorderFactory.createEmptyBorder());
        rightContainer.setMinimumSize(new Dimension(150, 50));

        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftScroll, rightContainer);
        styleSplitPane(bottomSplit, false);
        bottomSplit.setResizeWeight(0.5);
        bottomSplit.setDividerSize(5);
        bottomSplit.setContinuousLayout(true);
        bottomSplit.setBorder(BorderFactory.createEmptyBorder());
        SwingUtilities.invokeLater(() -> bottomSplit.setDividerLocation(0.5));

        detailsPane = leftDetailsPane;

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, scrollPane, bottomSplit);
        styleSplitPane(splitPane, true);
        splitPane.setResizeWeight(0.42);
        splitPane.setDividerLocation(132);
        splitPane.setDividerSize(6);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOneTouchExpandable(false);
        add(splitPane, BorderLayout.CENTER);
        CalendarResizeHandles.install(this);
    }

    private static void styleSplitPane(final JSplitPane sp, final boolean vertical) {
        sp.setContinuousLayout(true);
        sp.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            protected Component createDefaultNonContinuousLayoutDivider() {
                JPanel p = new JPanel();
                p.setOpaque(false);
                return p;
            }
            @Override
            public void finishedPaintingChildren(JSplitPane jc, Graphics g) {
                // Never paint the dark non-continuous ghost divider bar
            }
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(HEADER_BG);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(SUB_BORDER_CLR);
                        if (vertical) {
                            g.drawLine(0, 0, getWidth(), 0);
                            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                        } else {
                            g.drawLine(0, 0, 0, getHeight());
                            g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                        }
                    }
                };
            }
        });
    }
    
    /** Synchronize controls without triggering recursive table regeneration.
     * Rolling 101-year list stays centered on the displayed year; arrow navigation remains unbounded.
     */
    private void syncMonthChoices() {
        syncingMonthChoices = true;
        try {
            monthChoice.setSelectedIndex(currentMonth - 1);
            if (yearChoice.getItemCount() == 0 || currentYear < yearChoice.getItemAt(0)
                    || currentYear > yearChoice.getItemAt(yearChoice.getItemCount() - 1)) {
                yearChoice.removeAllItems();
                for (int year = Math.max(1, currentYear - 50); year <= currentYear + 50; year++) yearChoice.addItem(year);
            }
            yearChoice.setSelectedItem(currentYear);
        } finally { syncingMonthChoices = false; }
    }

    private void updateTable() {
        cancelDetailsRequest();
        String[] months = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthLabel.setText(months[currentMonth] + " " + currentYear);
        syncMonthChoices();
        
        TableData<MapTableRow> data = muhurta.getBalamCalendarTable(currentYear, currentMonth, partnerProfile, isFiltered);
        
        DefaultColumnMetaData meta;
        if (partnerProfile == null) {
            meta = new DefaultColumnMetaData(
                app.astrosoft.consts.AstrosoftTableColumn.Date,
                app.astrosoft.consts.AstrosoftTableColumn.TaraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.ChandraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.Match
            );
        } else {
            meta = new DefaultColumnMetaData(
                app.astrosoft.consts.AstrosoftTableColumn.Date,
                app.astrosoft.consts.AstrosoftTableColumn.TaraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.ChandraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.SpouseTaraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.SpouseChandraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.Match
            );
        }
        
        tableModel = new AstrosoftTableModel(new DefaultTable(data, meta));
        
        table = new AstrosoftTable(tableModel, app.astrosoft.consts.TableStyle.GRID) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TBL_ALT_ROW_BG);
                    c.setForeground(new Color(17, 17, 17));
                } else {
                    c.setBackground(TBL_SEL_BG);
                    c.setForeground(NAVY_TEXT);
                }
                return c;
            }

            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int row = rowAtPoint(p);
                int col = columnAtPoint(p);
                if (row >= 0 && col >= 0) {
                    Object val = getValueAt(row, col);
                    if (val instanceof TaraBalamResult) {
                        TaraBalamResult tr = (TaraBalamResult) val;
                        return "<html><b>" + tr.getTara().getName() + " (" + tr.getTara().getNumber() + ")</b><br>" 
                               + "<i>" + tr.getTara().getEffect() + "</i></html>";
                    } else if (val instanceof ChandraBalamResult) {
                        ChandraBalamResult cr = (ChandraBalamResult) val;
                        return "<html><b>" + cr.getHouse().getName() + "</b><br>" 
                               + "<i>" + cr.getHouse().getEffect() + "</i></html>";
                    }
                }
                return super.getToolTipText(e);
            }
        };
        
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setGridColor(SUB_BORDER_CLR);
        table.setRowHeight(21);
        if (table.getTableHeader() != null) {
            table.getTableHeader().setBackground(TBL_HEADER_BG);
            table.getTableHeader().setForeground(NAVY_TEXT);
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        }

        // GRID style disables selection rendering; re-enable it so the clicked row is highlighted.
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(TBL_SEL_BG);
        table.setSelectionForeground(NAVY_TEXT);
        
        BalamStarRenderer starRenderer = new BalamStarRenderer();
        table.setCellRenderer(starRenderer, app.astrosoft.consts.AstrosoftTableColumn.TaraBalam);
        table.setCellRenderer(starRenderer, app.astrosoft.consts.AstrosoftTableColumn.ChandraBalam);
        if (partnerProfile != null) {
            table.setCellRenderer(starRenderer, app.astrosoft.consts.AstrosoftTableColumn.SpouseTaraBalam);
            table.setCellRenderer(starRenderer, app.astrosoft.consts.AstrosoftTableColumn.SpouseChandraBalam);
        }
        
        table.getColumnModel().getColumn(1).setHeaderValue(primaryLabel + " — Tara");
        table.getColumnModel().getColumn(2).setHeaderValue(primaryLabel + " — Chandra");
        if (partnerProfile != null) {
            table.getColumnModel().getColumn(3).setHeaderValue(partnerLabel + " — Tara");
            table.getColumnModel().getColumn(4).setHeaderValue(partnerLabel + " — Chandra");
        }
        table.getTableHeader().repaint();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                boolean valid = selectedRow >= 0 && table.getValueAt(selectedRow, 0) instanceof BalamDateCell;
                updateButton.setEnabled(valid);
                if (valid) {
                    BalamDateCell cell = (BalamDateCell) table.getValueAt(selectedRow, 0);
                    selectedWindowStart = new java.util.Date(cell.getRow().getStartTime().getTime());
                    selectedWindowEnd = new java.util.Date(cell.getRow().getEndTime().getTime());
                    requestDetails(selectedRow);
                } else {
                    requestDetails(-1);
                }
            }
        });
        
        scrollPane.setViewportView(table);
        int targetRow = findMatchingRow();
        if (targetRow >= 0) {
            final int r = targetRow;
            table.setRowSelectionInterval(r, r);
            table.scrollRectToVisible(table.getCellRect(r, 0, true));
            updateButton.setEnabled(true);
            requestDetails(r);
        } else {
            if (leftDetailsPane != null) leftDetailsPane.setText(EMPTY_DETAILS);
            if (rightDetailsPane != null) rightDetailsPane.setText("");
            if (horaCombo != null) {
                horaCombo.removeAllItems();
                horaCombo.setEnabled(false);
            }
            updateButton.setEnabled(false);
        }
    }

    private int findMatchingRow() {
        if (table == null || table.getRowCount() == 0) return -1;
        if (selectedWindowStart != null) {
            long targetStart = selectedWindowStart.getTime();
            for (int r = 0; r < table.getRowCount(); r++) {
                Object val = table.getValueAt(r, 0);
                if (val instanceof BalamDateCell) {
                    BalamDateCell cell = (BalamDateCell) val;
                    if (cell.getRow() != null && cell.getRow().getStartTime() != null) {
                        if (cell.getRow().getStartTime().getTime() == targetStart) {
                            return r;
                        }
                    }
                }
            }
        }
        if (muhurta != null && muhurta.getEventInstant() != null) {
            long eventTime = muhurta.getEventInstant().getTime();
            for (int r = 0; r < table.getRowCount(); r++) {
                Object val = table.getValueAt(r, 0);
                if (val instanceof BalamDateCell) {
                    BalamDateCell cell = (BalamDateCell) val;
                    if (cell.getRow() != null && cell.getRow().getStartTime() != null && cell.getRow().getEndTime() != null) {
                        long start = cell.getRow().getStartTime().getTime();
                        long end = cell.getRow().getEndTime().getTime();
                        if (eventTime >= start && eventTime <= end) {
                            return r;
                        }
                    }
                }
            }
        }
        return -1;
    }
    
    private static final String EMPTY_DETAILS =
        "<html><body style='font-family:sans-serif; font-size:13px; padding:10px;'>Select a row to see details...</body></html>";
    
    /** UI lifecycle only: snapshot selection, schedule Core, ignore stale completions. */
    private void requestDetails(int row) {
        cancelDetailsRequest();
        if (row < 0 || !(table.getValueAt(row, 0) instanceof BalamDateCell)) {
            if (leftDetailsPane != null) leftDetailsPane.setText(EMPTY_DETAILS);
            if (rightDetailsPane != null) rightDetailsPane.setText("");
            if (horaCombo != null) {
                horaCombo.removeAllItems();
                horaCombo.setEnabled(false);
            }
            return;
        }
        final BalamDateCell cell = (BalamDateCell) table.getValueAt(row, 0);
        final java.util.Date start = new java.util.Date(cell.getRow().getStartTime().getTime());
        final java.util.Date end = new java.util.Date(cell.getRow().getEndTime().getTime());
        final app.astrosoft.consts.Nakshathra nak = cell.getRow().getTransitNakshathra();
        final app.astrosoft.consts.Rasi rasi = cell.getRow().getTransitRasi();
        final Object matchValue = table.getValueAt(row, table.getColumnCount() - 1);
        final java.util.EnumSet<app.astrosoft.consts.Nakshathra> matches =
            matchValue instanceof app.astrosoft.beans.BalamMatchCell
                ? ((app.astrosoft.beans.BalamMatchCell) matchValue).getMatches()
                : java.util.EnumSet.noneOf(app.astrosoft.consts.Nakshathra.class);
        final MyMuhurta context = muhurta;
        final long request = detailsRequest;
        if (leftDetailsPane != null) leftDetailsPane.setText("<html><body style='font-family:sans-serif; font-size:13px; padding:10px;'>Loading window details...</body></html>");
        if (rightDetailsPane != null) rightDetailsPane.setText("");
        if (horaCombo != null) {
            horaCombo.removeAllItems();
            horaCombo.setEnabled(false);
        }
        detailsWorker = new SwingWorker<app.astrosoft.beans.BalamWindowDetails, Void>() {
            @Override
            protected app.astrosoft.beans.BalamWindowDetails doInBackground() {
                return context.getBalamWindowDetails(start, end, matches);
            }
            @Override
            protected void done() {
                if (isCancelled() || request != detailsRequest) return;
                try {
                    app.astrosoft.beans.BalamWindowDetails details = get();
                    if (leftDetailsPane != null) {
                        leftDetailsPane.setText(buildLeftDetailsHtml(details, nak, rasi));
                        leftDetailsPane.setCaretPosition(0);
                    }

                    if (horaCombo != null) {
                        horaCombo.removeAllItems();
                        java.util.List<app.astrosoft.core.MuhurtaWindowsCalculator.Window> subhaHoras = new java.util.ArrayList<>();
                        for (app.astrosoft.core.MuhurtaWindowsCalculator.Window w : details.getTimingWindows()) {
                            if ("Subha Hora".equals(w.kind)) {
                                subhaHoras.add(w);
                            }
                        }
                        java.text.SimpleDateFormat tf = new java.text.SimpleDateFormat("hh:mm a");
                        tf.setTimeZone(details.getTimeZone());
                        java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat("dd MMM, hh:mm a");
                        dtf.setTimeZone(details.getTimeZone());

                        java.util.Calendar calStart = java.util.Calendar.getInstance(details.getTimeZone());
                        calStart.setTime(start);

                        if (subhaHoras.isEmpty()) {
                            horaCombo.addItem(new HoraItem("Window Start (" + tf.format(start) + ")", start.getTime()));
                        } else {
                            for (app.astrosoft.core.MuhurtaWindowsCalculator.Window w : subhaHoras) {
                                long effStart = Math.max(w.start, start.getTime());
                                java.util.Calendar calH = java.util.Calendar.getInstance(details.getTimeZone());
                                calH.setTimeInMillis(effStart);
                                boolean sameDay = calH.get(java.util.Calendar.DAY_OF_YEAR) == calStart.get(java.util.Calendar.DAY_OF_YEAR)
                                    && calH.get(java.util.Calendar.YEAR) == calStart.get(java.util.Calendar.YEAR);
                                String label = (sameDay ? tf.format(new java.util.Date(effStart)) : dtf.format(new java.util.Date(effStart)))
                                    + " – " + tf.format(new java.util.Date(w.end));
                                horaCombo.addItem(new HoraItem(label, effStart));
                            }
                            horaCombo.addItem(new HoraItem("Window Start (" + tf.format(start) + ")", start.getTime()));
                        }
                        horaCombo.setSelectedIndex(0);
                        horaCombo.setEnabled(true);
                    }

                    if (rightDetailsPane != null) {
                        rightDetailsPane.setText(buildRightDetailsHtml(details));
                        rightDetailsPane.setCaretPosition(0);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (java.util.concurrent.CancellationException ex) {
                    // A newer selection owns the panel.
                } catch (java.util.concurrent.ExecutionException ex) {
                    if (leftDetailsPane != null) {
                        leftDetailsPane.setText("<html><body>Unable to load window details: "
                            + htmlEscape(String.valueOf(ex.getCause().getMessage())) + "</body></html>");
                    }
                }
            }
        };
        detailsWorker.execute();
    }

    /** Presentation only: formats a completed immutable Core result. */
    private static String buildDetailsHtml(app.astrosoft.beans.BalamWindowDetails details,
            app.astrosoft.consts.Nakshathra nak, app.astrosoft.consts.Rasi rasi) {
        StringBuilder sb = new StringBuilder("<html><body style='font-family:sans-serif; padding:8px;'><table width='100%' cellpadding='8'><tr><td width='57%' valign='top'>");
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a z", java.util.Locale.ENGLISH);
        dateFormat.setTimeZone(details.getTimeZone());
        sb.append("<div style='margin:0 0 6px 0;'><b>Start:</b> ")
          .append(htmlEscape(dateFormat.format(details.getStart())))
          .append("<br><b>End:</b> ").append(htmlEscape(dateFormat.format(details.getEnd())));
        if (details.getSunrises() != null && !details.getSunrises().isEmpty()) {
            java.text.SimpleDateFormat sunriseDayFormat = new java.text.SimpleDateFormat("MMM d", java.util.Locale.ENGLISH);
            sunriseDayFormat.setTimeZone(details.getTimeZone());
            java.text.SimpleDateFormat sunriseTimeFormat = new java.text.SimpleDateFormat("hh:mm a z", java.util.Locale.ENGLISH);
            sunriseTimeFormat.setTimeZone(details.getTimeZone());
            for (java.util.Date sunrise : details.getSunrises()) {
                sb.append("<br><b>Sunrise on ").append(sunriseDayFormat.format(sunrise)).append(":</b> ")
                  .append(htmlEscape(sunriseTimeFormat.format(sunrise)));
            }
        }
        sb.append("</div>");
        for (app.astrosoft.beans.BalamWindowDetails.Suitability item : details.getSuitabilities()) {
            sb.append("<b>").append(htmlEscape(item.getStar().toString())).append(":</b> ");
            java.util.EnumSet<app.astrosoft.consts.MuhurtaEvent> events = item.getEvents();
            if (events.isEmpty()) sb.append("<font color='red'>Not recommended</font>");
            else {
                boolean first = true;
                for (app.astrosoft.consts.MuhurtaEvent event : events) {
                    if (!first) sb.append(", ");
                    sb.append(htmlEscape(event.toString()));
                    first = false;
                }
            }
            sb.append("<br>");
        }
        if (!details.getSuitabilities().isEmpty()) sb.append("<br>");
        sb.append("<b>Transit Moon:</b> ").append(nak.toString()).append(" in ").append(rasi.toString())
          .append("  (").append(nak.getLord().toString()).append(")<br><br>");
        StringBuilder thithis = new StringBuilder();
        StringBuilder varams = new StringBuilder();
        StringBuilder amirthathiYogas = new StringBuilder();
        StringBuilder yogas = new StringBuilder(), karanas = new StringBuilder();
        app.astrosoft.consts.Yoga previousYoga = null;
        app.astrosoft.consts.Karana previousKarana = null;
        java.text.SimpleDateFormat transitionFormat = new java.text.SimpleDateFormat("dd MMM, hh:mm a z", java.util.Locale.ENGLISH);
        transitionFormat.setTimeZone(details.getTimeZone());
        app.astrosoft.consts.AmirthathiYoga previousAmirthathiYoga = null;
        String previousThithi = null;
        int previousVaram = -1;
        String[] weekdays = new java.text.DateFormatSymbols().getWeekdays();
        for (app.astrosoft.beans.BalamWindowDetails.Entry entry : details.getEntries()) {
            app.astrosoft.consts.AmirthathiYoga amirthathiYoga = entry.getAmirthathiYoga();
            if (amirthathiYoga != null && amirthathiYoga != previousAmirthathiYoga) {
                if (amirthathiYogas.length() > 0) amirthathiYogas.append(" &rarr; (").append(htmlEscape(transitionFormat.format(entry.getStart()))).append(") ");
                boolean isMarana = (amirthathiYoga == app.astrosoft.consts.AmirthathiYoga.Marana);
                if (isMarana) amirthathiYogas.append("<font color='red'>");
                amirthathiYogas.append(htmlEscape(amirthathiYoga.toString()));
                if (isMarana) amirthathiYogas.append("</font>");
                previousAmirthathiYoga = amirthathiYoga;
            }
            if (entry.getYoga() != null && entry.getYoga() != previousYoga) {
                if (yogas.length() > 0) yogas.append(" &rarr; (").append(htmlEscape(transitionFormat.format(entry.getStart()))).append(") ");
                yogas.append(htmlEscape(entry.getYoga().toString())); previousYoga = entry.getYoga();
            }
            if (entry.getKarana() != null && entry.getKarana() != previousKarana) {
                if (karanas.length() > 0) karanas.append(" &rarr; (").append(htmlEscape(transitionFormat.format(entry.getStart()))).append(") ");
                karanas.append(htmlEscape(entry.getKarana().toString())); previousKarana = entry.getKarana();
            }
            String thithi = htmlEscape(entry.getPaksha().toString()) + " " + htmlEscape(entry.getThithi().toString());
            if (!thithi.equals(previousThithi)) {
                if (thithis.length() > 0) thithis.append(" &rarr; (").append(htmlEscape(transitionFormat.format(entry.getStart()))).append(") ");
                boolean isWarningThithi = entry.isWarning()
                    || entry.getThithi() == app.astrosoft.consts.Thithi.Amavasya
                    || entry.getThithi() == app.astrosoft.consts.Thithi.Prathamai
                    || entry.getThithi() == app.astrosoft.consts.Thithi.Ashtami
                    || entry.getThithi() == app.astrosoft.consts.Thithi.Navami;
                if (isWarningThithi) thithis.append("<font color='red'>");
                thithis.append(thithi);
                if (isWarningThithi) thithis.append("</font>");
                previousThithi = thithi;
            }
            if (entry.getWeekday() != previousVaram) {
                if (varams.length() > 0) varams.append(" &rarr; ");
                boolean isRedVara = (entry.getWeekday() == java.util.Calendar.TUESDAY || entry.getWeekday() == java.util.Calendar.SATURDAY);
                if (isRedVara) varams.append("<font color='red'>");
                varams.append(htmlEscape(weekdays[entry.getWeekday()]));
                if (isRedVara) varams.append("</font>");
                previousVaram = entry.getWeekday();
            }
        }
        sb.append("<b>Thithi:</b> ").append(thithis).append("<br>")
          .append("<b>Varam:</b> ").append(varams).append("<br>");
        if (amirthathiYogas.length() > 0)
            sb.append("<b>Amirthathi Yoga:</b> ").append(amirthathiYogas).append("<br>");
        if (yogas.length() > 0) sb.append("<b>Yoga (Nitya Yoga):</b> ").append(yogas).append("<br>");
        if (karanas.length() > 0) sb.append("<b>Karana:</b> ").append(karanas).append("<br>");
        sb.append("</td><td width='43%' valign='top'>");
        for(String kind:new String[]{"Subha Hora","Abhijit Muhurta","Brahma Muhurta"}){
            sb.append("<b>").append(kind).append(":</b><br>");boolean found=false;
            for(app.astrosoft.core.MuhurtaWindowsCalculator.Window window:details.getTimingWindows())if(window.kind.equals(kind)){
                sb.append(htmlEscape(app.astrosoft.core.MuhurtaWindowsCalculator.format(window,details.getTimeZone()))).append("<br>");found=true;
            }
            if(!found)sb.append("Not Applicable<br>");
            sb.append("<br>");
        }
        sb.append("</td></tr></table></body></html>");
        return sb.toString();
    }

    private static String buildLeftDetailsHtml(app.astrosoft.beans.BalamWindowDetails details,
            app.astrosoft.consts.Nakshathra nak, app.astrosoft.consts.Rasi rasi) {
        StringBuilder sb = new StringBuilder("<html><body style='font-family:sans-serif; color:#1f2933; padding:2px 6px;'>");
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a z", java.util.Locale.ENGLISH);
        dateFormat.setTimeZone(details.getTimeZone());
        sb.append("<div style='margin-bottom:3px;'><b style='color:#0a3d62;'>Start:</b> ")
          .append(htmlEscape(dateFormat.format(details.getStart())))
          .append("<br><b style='color:#0a3d62;'>End:</b> ").append(htmlEscape(dateFormat.format(details.getEnd())));
        if (details.getSunrises() != null && !details.getSunrises().isEmpty()) {
            java.text.SimpleDateFormat sunriseDayFormat = new java.text.SimpleDateFormat("MMM d", java.util.Locale.ENGLISH);
            sunriseDayFormat.setTimeZone(details.getTimeZone());
            java.text.SimpleDateFormat sunriseTimeFormat = new java.text.SimpleDateFormat("hh:mm a z", java.util.Locale.ENGLISH);
            sunriseTimeFormat.setTimeZone(details.getTimeZone());
            for (java.util.Date sunrise : details.getSunrises()) {
                sb.append("<br><b style='color:#0a3d62;'>Sunrise on ").append(sunriseDayFormat.format(sunrise)).append(":</b> ")
                  .append(htmlEscape(sunriseTimeFormat.format(sunrise)));
            }
        }
        sb.append("</div>");
        for (app.astrosoft.beans.BalamWindowDetails.Suitability item : details.getSuitabilities()) {
            sb.append("<div style='margin-bottom:3px;'><b style='color:#0a3d62;'>").append(htmlEscape(item.getStar().toString())).append(":</b> ");
            java.util.EnumSet<app.astrosoft.consts.MuhurtaEvent> events = item.getEvents();
            if (events.isEmpty()) sb.append("<font color='#c0392b'><b>Not recommended</b></font>");
            else {
                boolean first = true;
                for (app.astrosoft.consts.MuhurtaEvent event : events) {
                    if (!first) sb.append(", ");
                    sb.append(htmlEscape(event.toString()));
                    first = false;
                }
            }
            sb.append("</div>");
        }
        sb.append("<div style='margin-bottom:3px;'><b style='color:#0a3d62;'>Transit Moon:</b> <b>").append(nak.toString()).append(" in ").append(rasi.toString())
          .append("</b> (").append(nak.getLord().toString()).append(")</div>");
        StringBuilder thithis = new StringBuilder();
        StringBuilder varams = new StringBuilder();
        StringBuilder amirthathiYogas = new StringBuilder();
        StringBuilder yogas = new StringBuilder(), karanas = new StringBuilder();
        app.astrosoft.consts.Yoga previousYoga = null;
        app.astrosoft.consts.Karana previousKarana = null;
        java.text.SimpleDateFormat transitionFormat = new java.text.SimpleDateFormat("dd MMM, hh:mm a z", java.util.Locale.ENGLISH);
        transitionFormat.setTimeZone(details.getTimeZone());
        app.astrosoft.consts.AmirthathiYoga previousAmirthathiYoga = null;
        String previousThithi = null;
        int previousVaram = -1;
        String[] weekdays = new java.text.DateFormatSymbols().getWeekdays();
        for (app.astrosoft.beans.BalamWindowDetails.Entry entry : details.getEntries()) {
            app.astrosoft.consts.AmirthathiYoga amirthathiYoga = entry.getAmirthathiYoga();
            if (amirthathiYoga != null && amirthathiYoga != previousAmirthathiYoga) {
                if (amirthathiYogas.length() > 0) amirthathiYogas.append(" &rarr; (").append(htmlEscape(transitionFormat.format(entry.getStart()))).append(") ");
                boolean isMarana = (amirthathiYoga == app.astrosoft.consts.AmirthathiYoga.Marana);
                if (isMarana) amirthathiYogas.append("<font color='#c0392b'><b>");
                amirthathiYogas.append(htmlEscape(amirthathiYoga.toString()));
                if (isMarana) amirthathiYogas.append("</b></font>");
                previousAmirthathiYoga = amirthathiYoga;
            }
            if (entry.getYoga() != null && entry.getYoga() != previousYoga) {
                if (yogas.length() > 0) yogas.append(" &rarr; (").append(htmlEscape(transitionFormat.format(entry.getStart()))).append(") ");
                yogas.append(htmlEscape(entry.getYoga().toString())); previousYoga = entry.getYoga();
            }
            if (entry.getKarana() != null && entry.getKarana() != previousKarana) {
                if (karanas.length() > 0) karanas.append(" &rarr; (").append(htmlEscape(transitionFormat.format(entry.getStart()))).append(") ");
                karanas.append(htmlEscape(entry.getKarana().toString())); previousKarana = entry.getKarana();
            }
            String thithi = htmlEscape(entry.getPaksha().toString()) + " " + htmlEscape(entry.getThithi().toString());
            if (!thithi.equals(previousThithi)) {
                if (thithis.length() > 0) thithis.append(" &rarr; (").append(htmlEscape(transitionFormat.format(entry.getStart()))).append(") ");
                boolean isWarningThithi = entry.isWarning()
                    || entry.getThithi() == app.astrosoft.consts.Thithi.Amavasya
                    || entry.getThithi() == app.astrosoft.consts.Thithi.Prathamai
                    || entry.getThithi() == app.astrosoft.consts.Thithi.Ashtami
                    || entry.getThithi() == app.astrosoft.consts.Thithi.Navami;
                if (isWarningThithi) thithis.append("<font color='#c0392b'>");
                thithis.append(thithi);
                if (isWarningThithi) thithis.append("</font>");
                previousThithi = thithi;
            }
            if (entry.getWeekday() != previousVaram) {
                if (varams.length() > 0) varams.append(" &rarr; ");
                boolean isRedVara = (entry.getWeekday() == java.util.Calendar.TUESDAY || entry.getWeekday() == java.util.Calendar.SATURDAY);
                if (isRedVara) varams.append("<font color='#c0392b'><b>");
                varams.append(htmlEscape(weekdays[entry.getWeekday()]));
                if (isRedVara) varams.append("</b></font>");
                previousVaram = entry.getWeekday();
            }
        }
        sb.append("<div style='line-height:1.38;'>");
        sb.append("<b style='color:#0a3d62;'>Thithi:</b> ").append(thithis).append("<br>")
          .append("<b style='color:#0a3d62;'>Varam:</b> ").append(varams).append("<br>");
        if (amirthathiYogas.length() > 0)
            sb.append("<b style='color:#0a3d62;'>Amirthathi Yoga:</b> ").append(amirthathiYogas).append("<br>");
        if (yogas.length() > 0) sb.append("<b style='color:#0a3d62;'>Yoga (Nitya Yoga):</b> ").append(yogas).append("<br>");
        if (karanas.length() > 0) sb.append("<b style='color:#0a3d62;'>Karana:</b> ").append(karanas).append("<br>");
        sb.append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String buildRightDetailsHtml(app.astrosoft.beans.BalamWindowDetails details) {
        StringBuilder sb = new StringBuilder("<html><body style='font-family:sans-serif; color:#1f2933; padding:2px 6px;'>");
        java.util.List<app.astrosoft.core.MuhurtaWindowsCalculator.Window> subhaHoras = new java.util.ArrayList<>();
        for (app.astrosoft.core.MuhurtaWindowsCalculator.Window window : details.getTimingWindows()) {
            if ("Subha Hora".equals(window.kind)) subhaHoras.add(window);
        }
        if (!subhaHoras.isEmpty()) {
            sb.append("<div style='margin-bottom:3px;'><b style='color:#0a3d62;'>Available Subha Hora Windows:</b><br>");
            for (app.astrosoft.core.MuhurtaWindowsCalculator.Window w : subhaHoras) {
                sb.append("• ").append(htmlEscape(app.astrosoft.core.MuhurtaWindowsCalculator.format(w, details.getTimeZone()))).append("<br>");
            }
            sb.append("</div>");
        } else {
            sb.append("<div style='margin-bottom:3px;'><b style='color:#0a3d62;'>Available Subha Hora Windows:</b><br>Not Applicable</div>");
        }
        for (String kind : new String[]{"Abhijit Muhurta", "Brahma Muhurta"}) {
            sb.append("<div style='margin-bottom:3px;'><b style='color:#0a3d62;'>").append(kind).append(":</b> ");
            boolean found = false;
            for (app.astrosoft.core.MuhurtaWindowsCalculator.Window window : details.getTimingWindows()) {
                if (window.kind.equals(kind)) {
                    if (found) sb.append(", ");
                    sb.append(htmlEscape(app.astrosoft.core.MuhurtaWindowsCalculator.format(window, details.getTimeZone())));
                    found = true;
                }
            }
            if (!found) sb.append("<span style='color:#555555;'>Not Applicable</span>");
            sb.append("</div>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    /** Presentation only: domain resolves candidates; UI formats returned sets. */
    private void appendCandidateSets(StringBuilder sb, String label, Horoscope person,
            Object taraVal, Object chandraVal) {
        appendCandidateSets(sb, label, person != null ? new app.astrosoft.beans.MuhurtaProfile(person) : null, taraVal, chandraVal);
    }

    private void appendCandidateSets(StringBuilder sb, String label, app.astrosoft.beans.MuhurtaProfile person,
            Object taraVal, Object chandraVal) {
        if (person == null || !(taraVal instanceof TaraBalamResult)
                || !(chandraVal instanceof ChandraBalamResult)) return;
        app.astrosoft.beans.TaraChandraMatchResult result = MyMuhurta.find_tara_chandra_match(
            person.getNakshathra(), person.getRasi(),
            ((TaraBalamResult) taraVal).getTara().getNumber(),
            ((ChandraBalamResult) chandraVal).getHouse().getHouseNumber());
        sb.append("<h4>").append(htmlEscape(label)).append(" — Candidates</h4>");
        sb.append("<table cellpadding='4' cellspacing='0' border='1' width='100%'>")
          .append("<tr><th>Tara Set</th><th>Chandra Set</th><th>Match</th></tr><tr>");
        sb.append("<td valign='top'>").append(formatNakshathras(result.getTaraSet())).append("</td>");
        sb.append("<td valign='top'>").append(formatNakshathras(result.getChandraSet())).append("</td>");
        sb.append("<td valign='top'>").append(result.getMatches().isEmpty()
            ? "Null: Astronomically impossible combination." : formatNakshathras(result.getMatches()));
        sb.append("</td></tr></table>");
    }

    private static String formatNakshathras(java.util.Set<app.astrosoft.consts.Nakshathra> stars) {
        StringBuilder text = new StringBuilder();
        for (app.astrosoft.consts.Nakshathra star : stars) {
            if (text.length() > 0) text.append("<br>");
            text.append(htmlEscape(star.toString()));
        }
        return text.toString();
    }

    private void appendPersonRow(StringBuilder sb, String label, Object taraVal, Object chandraVal) {
        sb.append("<tr><td valign='top'><b>").append(label).append("</b></td><td valign='top'>");
        if (taraVal instanceof TaraBalamResult) {
            TaraBalamResult tr = (TaraBalamResult) taraVal;
            sb.append("<b>").append(tr.getTara().getName()).append(" (").append(tr.getTara().getNumber()).append(")</b><br>")
              .append("<i>").append(tr.getTara().getEffect()).append("</i>");
        } else {
            sb.append(taraVal);
        }
        sb.append("</td><td valign='top'>");
        if (chandraVal instanceof ChandraBalamResult) {
            ChandraBalamResult cr = (ChandraBalamResult) chandraVal;
            sb.append("<b>").append(cr.getHouse().getName()).append("</b><br>")
              .append("<i>").append(cr.getHouse().getEffect()).append("</i>");
        } else {
            sb.append(chandraVal);
        }
        sb.append("</td></tr>");
    }

    /**
     * Renders Tara and Chandra cells as "N (stars)" using Muthu's thumb rules.
     * Tara:    2/6/9 = 2 green, 4/8 = 1 green, 7 = 1 red, others = 1 yellow.
     * Chandra: 3/6/10/11 = 3 green, 1/7 = 2 green, 8 = 1 red, others = 1 green.
     * Draws vector stars directly to guarantee crisp rendering across all OSes
     * without relying on system fonts or missing Unicode glyphs.
     */
    private static class BalamStarRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private static final Color GREEN = new Color(0x1E, 0x9E, 0x3A);
        private static final Color RED = new Color(0xD3, 0x2F, 0x2F);
        private static final Color YELLOW = new Color(0xE0, 0xA8, 0x00);

        private int number = 0;
        private int starCount = 0;
        private Color starColor = null;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
            setText("");
            if (value instanceof TaraBalamResult) {
                app.astrosoft.consts.Tara tara = ((TaraBalamResult) value).getTara();
                switch (tara.getThumbRuleGrade()) {
                    case BEST:  setStarData(tara.getNumber(), 2, GREEN);  break;
                    case GOOD:  setStarData(tara.getNumber(), 1, GREEN);  break;
                    case AVOID: setStarData(tara.getNumber(), 1, RED);    break;
                    default:    setStarData(tara.getNumber(), 1, YELLOW); break;
                }
            } else if (value instanceof ChandraBalamResult) {
                app.astrosoft.consts.ChandraHouse house = ((ChandraBalamResult) value).getHouse();
                switch (house.getThumbRuleGrade()) {
                    case BEST:  setStarData(house.getHouseNumber(), 3, GREEN); break;
                    case GOOD:  setStarData(house.getHouseNumber(), 2, GREEN); break;
                    case AVOID: setStarData(house.getHouseNumber(), 1, RED);   break;
                    default:    setStarData(house.getHouseNumber(), 1, GREEN); break;
                }
            } else {
                number = 0;
                starCount = 0;
                starColor = null;
                setText(value != null ? value.toString() : "-");
            }
            return this;
        }

        private void setStarData(int num, int count, Color col) {
            this.number = num;
            this.starCount = count;
            this.starColor = col;
            String hex = col == RED ? "#D32F2F" : col == GREEN ? "#1E9E3A" : "#E0A800";
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < count; i++) {
                if (i > 0) stars.append(" ");
                stars.append("\u2605");
            }
            setText("<html>" + num + " (<font color='" + hex + "'>" + stars + "</font>)</html>");
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (starCount <= 0 || starColor == null) {
                super.paintComponent(g);
                return;
            }
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();

            String prefix = number + " (";
            String suffix = ")";
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            int x = 6;
            g2.setColor(getForeground());
            g2.drawString(prefix, x, textY);
            x += fm.stringWidth(prefix);

            int starY = getHeight() / 2;
            for (int i = 0; i < starCount; i++) {
                drawStar(g2, x + 5, starY, 5, 2, starColor);
                x += 12;
            }

            g2.setColor(getForeground());
            g2.drawString(suffix, x - 1, textY);
            g2.dispose();
        }

        private void drawStar(Graphics2D g2, int cx, int cy, int outerR, int innerR, Color color) {
            int[] x = new int[10];
            int[] y = new int[10];
            for (int i = 0; i < 10; i++) {
                double r = (i % 2 == 0) ? outerR : innerR;
                double angle = i * Math.PI / 5 - Math.PI / 2;
                x[i] = (int) (cx + r * Math.cos(angle));
                y[i] = (int) (cy + r * Math.sin(angle));
            }
            g2.setColor(color);
            g2.fillPolygon(x, y, 10);
        }
    }

    public JTable getTable() {
        return table;
    }

    public JButton getUpdateButton() {
        return updateButton;
    }
}
