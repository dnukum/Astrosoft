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

import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.ui.table.DefaultColumnMetaData;

public class BalamCalendarDialog extends JDialog {
    private MyMuhurta muhurta;
    private int currentYear;
    private int currentMonth;
    
    private AstrosoftTable table;
    private AstrosoftTableModel tableModel;
    private JLabel monthLabel;

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
        add(topPanel, BorderLayout.NORTH);
        
        tableModel = new AstrosoftTableModel(new DefaultTable(
                TableDataFactory.emptyTableData(), 
                new DefaultColumnMetaData(
                    app.astrosoft.consts.AstrosoftTableColumn.StartDate,
                    app.astrosoft.consts.AstrosoftTableColumn.EndDate,
                    app.astrosoft.consts.AstrosoftTableColumn.Nakshathra,
                    app.astrosoft.consts.AstrosoftTableColumn.Rasi,
                    app.astrosoft.consts.AstrosoftTableColumn.TaraBalam,
                    app.astrosoft.consts.AstrosoftTableColumn.ChandraBalam,
                    app.astrosoft.consts.AstrosoftTableColumn.OverallScore
                )
        ));
        
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
        
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
    
    private void updateTable() {
        String[] months = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthLabel.setText(months[currentMonth] + " " + currentYear);
        
        TableData<MapTableRow> data = muhurta.getBalamCalendarTable(currentYear, currentMonth);
        tableModel.updateData(data);
    }
}
