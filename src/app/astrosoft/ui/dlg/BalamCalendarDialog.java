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

    public BalamCalendarDialog(Window owner, MyMuhurta muhurta) {
        super(owner, "Balam Calendar", ModalityType.APPLICATION_MODAL);
        this.muhurta = muhurta;
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(muhurta.getPanchang().getDate().getTime());
        this.currentYear = cal.get(Calendar.YEAR);
        this.currentMonth = cal.get(Calendar.MONTH) + 1;
        
        initComponents();
        updateTable();
        
        setSize(800, 500);
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
        add(scrollPane, BorderLayout.CENTER);
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
                app.astrosoft.consts.AstrosoftTableColumn.SpouseChandraBalam,
                app.astrosoft.consts.AstrosoftTableColumn.CombinedScore
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
                               + "<i>" + tr.getTara().getEffect() + "</i><br><b>Rank:</b> " + tr.getFinalRank().name() + "</html>";
                    } else if (val instanceof ChandraBalamResult) {
                        ChandraBalamResult cr = (ChandraBalamResult) val;
                        return "<html><b>" + cr.getHouse().getName() + "</b><br>" 
                               + "<i>" + cr.getHouse().getEffect() + "</i><br><b>Rank:</b> " + cr.getFinalRank().name() + "</html>";
                    }
                }
                return super.getToolTipText(e);
            }
        };
        
        scrollPane.setViewportView(table);
    }
}
