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
    private final java.util.function.Consumer<java.util.Date> updateAction;
    private JButton updateButton;
    private MyMuhurta muhurta;
    private int currentYear;
    private int currentMonth;
    
    private AstrosoftTable table;
    private AstrosoftTableModel tableModel;
    private JLabel monthLabel;
    private Horoscope partnerHoroscope;
    private String primaryLabel = "Person";
    private String partnerLabel = "Person";
    private JScrollPane scrollPane;
    private JEditorPane detailsPane;
    private JSplitPane splitPane;

    public BalamCalendarPanel(MyMuhurta muhurta, java.util.function.Consumer<java.util.Date> updateAction) {
        this(muhurta, updateAction, null, null);
    }

    public BalamCalendarPanel(MyMuhurta muhurta, java.util.function.Consumer<java.util.Date> updateAction, Horoscope person1, Horoscope person2) {
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

    /** Shared selectors are owned by the main MyMuhurta screen. */
    public void setPeople(Horoscope person1, Horoscope person2) {
        Horoscope primary = person1 != null ? person1 : person2;
        partnerHoroscope = person1 != null ? person2 : null;
        primaryLabel = person1 != null ? "Person 1" : "Person 2";
        partnerLabel = "Person 2";
    }

    private static String htmlEscape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        JButton prevBtn = new JButton("< Prev Month");
        JButton nextBtn = new JButton("Next Month >");
        monthLabel = new JLabel("");
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
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
        

        topPanel.add(prevBtn);
        topPanel.add(monthLabel);
        topPanel.add(nextBtn);
        updateButton = new JButton("Update");
        updateButton.setEnabled(false);
        updateButton.setToolTipText("Apply the selected window start date/time to Muhurta");
        updateButton.addActionListener(e -> {
            int row = table == null ? -1 : table.getSelectedRow();
            if (row < 0) return;
            Object cell = table.getValueAt(row, 0);
            if (cell instanceof BalamDateCell) {
                updateAction.accept(new java.util.Date(((BalamDateCell) cell).getRow().getStartTime().getTime()));
            }
        });
        topPanel.add(updateButton);
        // Keep Update beside month navigation, centered as one responsive group.
        add(topPanel, BorderLayout.NORTH);
        
        scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        detailsPane = new JEditorPane("text/html", EMPTY_DETAILS);
        detailsPane.setEditable(false);
        
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, new JScrollPane(detailsPane));
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(170);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
        CalendarResizeHandles.install(this);
    }
    
    private void updateTable() {
        String[] months = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthLabel.setText(months[currentMonth] + " " + currentYear);
        
        TableData<MapTableRow> data = muhurta.getBalamCalendarTable(currentYear, currentMonth, partnerHoroscope);
        
        DefaultColumnMetaData meta;
        if (partnerHoroscope == null) {
            meta = new DefaultColumnMetaData(
                app.astrosoft.consts.AstrosoftTableColumn.Date,
                app.astrosoft.consts.AstrosoftTableColumn.TaraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.ChandraBalam
            );
        } else {
            meta = new DefaultColumnMetaData(
                app.astrosoft.consts.AstrosoftTableColumn.Date,
                app.astrosoft.consts.AstrosoftTableColumn.TaraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.ChandraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.SpouseTaraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.SpouseChandraBalam
            );
        }
        
        tableModel = new AstrosoftTableModel(new DefaultTable(data, meta));
        
        table = new AstrosoftTable(tableModel, app.astrosoft.consts.TableStyle.GRID) {
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

        // GRID style disables selection rendering; re-enable it so the clicked row is highlighted.
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(198, 222, 255));
        table.setSelectionForeground(Color.BLACK);
        
        BalamStarRenderer starRenderer = new BalamStarRenderer();
        table.setCellRenderer(starRenderer, app.astrosoft.consts.AstrosoftTableColumn.TaraBalam);
        table.setCellRenderer(starRenderer, app.astrosoft.consts.AstrosoftTableColumn.ChandraBalam);
        if (partnerHoroscope != null) {
            table.setCellRenderer(starRenderer, app.astrosoft.consts.AstrosoftTableColumn.SpouseTaraBalam);
            table.setCellRenderer(starRenderer, app.astrosoft.consts.AstrosoftTableColumn.SpouseChandraBalam);
        }
        
        table.getColumnModel().getColumn(1).setHeaderValue(primaryLabel + " — Tara");
        table.getColumnModel().getColumn(2).setHeaderValue(primaryLabel + " — Chandra");
        if (partnerHoroscope != null) {
            table.getColumnModel().getColumn(3).setHeaderValue(partnerLabel + " — Tara");
            table.getColumnModel().getColumn(4).setHeaderValue(partnerLabel + " — Chandra");
        }
        table.getTableHeader().repaint();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                updateButton.setEnabled(selectedRow >= 0 && table.getValueAt(selectedRow, 0) instanceof BalamDateCell);
                if (selectedRow >= 0) {
                    detailsPane.setText(buildDetailsHtml(selectedRow));
                    detailsPane.setCaretPosition(0);
                }
            }
        });
        
        detailsPane.setText(EMPTY_DETAILS);
        updateButton.setEnabled(false);
        scrollPane.setViewportView(table);
    }
    
    private static final String EMPTY_DETAILS =
        "<html><body style='font-family:sans-serif; padding:10px;'>Select a row to see details...</body></html>";
    
    /** Renders everything known about the selected transit window for the bottom panel. */
    private String buildDetailsHtml(int row) {
        StringBuilder sb = new StringBuilder("<html><body style='font-family:sans-serif; padding:8px;'>");
        
        Object dateVal = table.getValueAt(row, 0);
        if (dateVal instanceof BalamDateCell) {
            BalamDateCell dc = (BalamDateCell) dateVal;
            sb.append("<h3 style='margin:0 0 6px 0;'>").append(dc.formatStart())
              .append(" &rarr; ").append(dc.formatEnd()).append("</h3>");
            app.astrosoft.consts.Nakshathra nak = dc.getRow().getTransitNakshathra();
            sb.append("<b>Transit Moon:</b> ").append(nak.name())
              .append(" in ").append(dc.getRow().getTransitRasi().name()).append("<br>")
              .append("<b>Lord:</b> ").append(nak.getLord().name()).append("<br><br>");
        } else {
            sb.append("<h3>").append(dateVal).append("</h3>");
        }
        
        sb.append("<table cellpadding='4' cellspacing='0' border='1'>");
        sb.append("<tr><th></th><th align='left'>Tara Balam</th><th align='left'>Chandra Balam</th></tr>");
        appendPersonRow(sb, htmlEscape(primaryLabel), table.getValueAt(row, 1), table.getValueAt(row, 2));
        if (partnerHoroscope != null && table.getColumnCount() >= 5) {
            appendPersonRow(sb, htmlEscape(partnerLabel), table.getValueAt(row, 3), table.getValueAt(row, 4));
        }
        sb.append("</table></body></html>");
        return sb.toString();
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
     * Tara:    2/6/9 = 2 green, 7 = 1 red, others = 1 yellow.
     * Chandra: 3/6/10/11 = 3 green, 1/7 = 2 green, 8 = 1 red, others = 1 green.
     */
    private static class BalamStarRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private static final String STAR = "\u2605";
        private static final String GREEN = "#1E9E3A";
        private static final String RED = "#D32F2F";
        private static final String YELLOW = "#E0A800";

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
            if (value instanceof TaraBalamResult) {
                app.astrosoft.consts.Tara tara = ((TaraBalamResult) value).getTara();
                switch (tara.getThumbRuleGrade()) {
                    case BEST:  setStars(tara.getNumber(), 2, GREEN);  break;
                    case AVOID: setStars(tara.getNumber(), 1, RED);    break;
                    default:    setStars(tara.getNumber(), 1, YELLOW); break;
                }
            } else if (value instanceof ChandraBalamResult) {
                app.astrosoft.consts.ChandraHouse house = ((ChandraBalamResult) value).getHouse();
                switch (house.getThumbRuleGrade()) {
                    case BEST:  setStars(house.getHouseNumber(), 3, GREEN); break;
                    case GOOD:  setStars(house.getHouseNumber(), 2, GREEN); break;
                    case AVOID: setStars(house.getHouseNumber(), 1, RED);   break;
                    default:    setStars(house.getHouseNumber(), 1, GREEN); break;
                }
            }
            return this;
        }

        private void setStars(int number, int count, String color) {
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < count; i++) {
                if (i > 0) stars.append(' ');
                stars.append(STAR);
            }
            setText("<html>" + number + " (<font color='" + color + "'>" + stars + "</font>)</html>");
        }
    }
}
