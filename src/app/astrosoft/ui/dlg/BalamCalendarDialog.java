package app.astrosoft.ui.dlg;

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

public class BalamCalendarDialog extends JDialog {
    private MyMuhurta muhurta;
    private int currentYear;
    private int currentMonth;
    
    private AstrosoftTable table;
    private AstrosoftTableModel tableModel;
    private JLabel monthLabel;
    private Horoscope partnerHoroscope;
    private JButton partnerBtn;
    private JScrollPane scrollPane;
    private JEditorPane detailsPane;
    private JSplitPane splitPane;

    public BalamCalendarDialog(Window owner, MyMuhurta muhurta) {
        super(owner, "Balam Calendar", ModalityType.APPLICATION_MODAL);
        this.muhurta = muhurta;
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(muhurta.getPanchang().getDate().getTime());
        this.currentYear = cal.get(Calendar.YEAR);
        this.currentMonth = cal.get(Calendar.MONTH) + 1;
        
        initComponents();
        updateTable();
        
        setSize(800, 720);
        setLocationRelativeTo(owner);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout());
        JButton prevBtn = new JButton("< Prev Month");
        JButton nextBtn = new JButton("Next Month >");
        monthLabel = new JLabel("");
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        monthLabel.setPreferredSize(new Dimension(150, 30));
        monthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        partnerBtn = new JButton("Select Partner...");
        partnerBtn.addActionListener(e -> {
            if (partnerHoroscope == null) {
                String selectedFile = FileOps.openFileDialog(BalamCalendarDialog.this, FileOps.FileDialogMode.OPEN, AstrosoftFileFilter.HOROSCOPE_EXTN);
                if (selectedFile != null) {
                    partnerHoroscope = Horoscope.createFromFile(selectedFile);
                    if (partnerHoroscope != null) {
                        partnerBtn.setText("Partner: " + partnerHoroscope.getBirthData().name());
                        updateTable();
                    }
                }
            } else {
                partnerHoroscope = null;
                partnerBtn.setText("Select Partner...");
                updateTable();
            }
        });
        
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
        
        topPanel.add(partnerBtn);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(prevBtn);
        topPanel.add(monthLabel);
        topPanel.add(nextBtn);
        add(topPanel, BorderLayout.NORTH);
        
        scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        detailsPane = new JEditorPane("text/html", EMPTY_DETAILS);
        detailsPane.setEditable(false);
        
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, new JScrollPane(detailsPane));
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(330);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
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
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    detailsPane.setText(buildDetailsHtml(selectedRow));
                    detailsPane.setCaretPosition(0);
                }
            }
        });
        
        detailsPane.setText(EMPTY_DETAILS);
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
            sb.append("<b>Transit Moon:</b> ").append(nak.toString())
              .append(" in ").append(dc.getRow().getTransitRasi().toString()).append("<br>")
              .append("<b>Lord:</b> ").append(nak.getLord().toString()).append("<br><br>");
        } else {
            sb.append("<h3>").append(dateVal).append("</h3>");
        }
        
        sb.append("<table cellpadding='4' cellspacing='0' border='1'>");
        sb.append("<tr><th></th><th align='left'>Tara Balam</th><th align='left'>Chandra Balam</th></tr>");
        appendPersonRow(sb, "Self", table.getValueAt(row, 1), table.getValueAt(row, 2));
        if (partnerHoroscope != null && table.getColumnCount() >= 5) {
            appendPersonRow(sb, "Partner", table.getValueAt(row, 3), table.getValueAt(row, 4));
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
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (starCount <= 0 || starColor == null) return;

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
}
