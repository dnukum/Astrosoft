package app.astrosoft.ui.dlg;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Varga;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.util.UIConsts;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransitChartDialog extends AstrosoftDialog {

    private static final Logger log = Logger.getLogger(TransitChartDialog.class.getName());

    public static final String CHART_TITLE = "Transit (D1)";
    private static final Dimension DLG_SIZE = new Dimension(360, 395);
    private static final Dimension MIN_DLG_SIZE = new Dimension(320, 360);
    private static final Dimension CHART_SIZE = new Dimension(300, 300);

    private final JButton editButton;
    private final JButton moreButton;
    private final JPanel chartHolder;
    private final JScrollPane detailsScrollPane;
    private final JTable detailsTable;
    private final DefaultTableModel tableModel;
    private final TransitChartComputation computation;

    private Place currentPlace;
    private Date selectedInstant;
    private Chart chart;
    private boolean isExpanded = false;

    public TransitChartDialog(AstroSoft parent, Date initInstant, Place initPlace) {
        this(parent, initInstant, initPlace, new DefaultTransitChartComputation());
    }

    public TransitChartDialog(AstroSoft parent, Date initInstant, Place initPlace, TransitChartComputation computation) {
        super(parent, "Transit Chart", DLG_SIZE);
        this.computation = computation;
        this.currentPlace = initPlace != null ? initPlace : AstroSoft.getPreferences().getPlace();
        this.selectedInstant = initInstant != null ? initInstant : new Date();

        setResizable(true);
        setMinimumSize(MIN_DLG_SIZE);

        dlgPanel.setLayout(new BorderLayout(0, 0));

        // Center: vertical container for the chart and the expandable table
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIConsts.THEME_CLR);

        // Clean bounding border box around the chart (no text label on the border line)
        chartHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        chartHolder.setAlignmentX(Component.CENTER_ALIGNMENT);
        chartHolder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(6, 8, 4, 8),
                BorderFactory.createLineBorder(new java.awt.Color(190, 185, 170), 1)
        ));
        chartHolder.setPreferredSize(new Dimension(318, 318));
        chartHolder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 318));
        contentPanel.add(chartHolder);

        // Control buttons bar: [Edit...] and [More...] / [Less...] placed between chart and table
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 6));
        buttonBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonBar.setBackground(UIConsts.THEME_CLR);
        buttonBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        editButton = new JButton("Edit...");
        editButton.addActionListener(e -> openEditDialog());
        buttonBar.add(editButton);

        moreButton = new JButton("More...");
        moreButton.addActionListener(e -> toggleMore());
        buttonBar.add(moreButton);

        contentPanel.add(buttonBar);

        // Expandable table with 3 columns: Planet, Sign Change, Date Change
        String[] columnNames = {"Planet", "Sign Change", "Date Change"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        detailsTable = new JTable(tableModel);
        detailsTable.setRowHeight(20);
        detailsTable.setFont(new Font("Verdana", Font.PLAIN, 11));
        detailsTable.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 11));
        detailsTable.getTableHeader().setReorderingAllowed(false);
        detailsTable.setRowSelectionAllowed(false);
        detailsTable.setFocusable(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        detailsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        detailsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        detailsTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        detailsTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        detailsTable.getColumnModel().getColumn(2).setPreferredWidth(145);

        detailsScrollPane = new JScrollPane(detailsTable);
        detailsScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        detailsScrollPane.setPreferredSize(new Dimension(320, 206));
        detailsScrollPane.setMinimumSize(new Dimension(280, 100));
        detailsScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        detailsScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 10, 8, 10),
                BorderFactory.createLineBorder(new java.awt.Color(190, 185, 170), 1)
        ));
        detailsScrollPane.setVisible(false);
        contentPanel.add(detailsScrollPane);

        dlgPanel.add(contentPanel, BorderLayout.CENTER);

        add(dlgPanel);
        setBackground(UIConsts.THEME_CLR);

        updateChart();
    }

    public void showDialog() {
        setVisible(true);
    }

    public void toggleMore() {
        setExpanded(!isExpanded);
    }

    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        moreButton.setText(isExpanded ? "Less..." : "More...");
        detailsScrollPane.setVisible(isExpanded);
        if (isExpanded) {
            setMinimumSize(new Dimension(320, 540));
            setSize(new Dimension(Math.max(getWidth(), 360), 615));
        } else {
            setMinimumSize(MIN_DLG_SIZE);
            setSize(new Dimension(getWidth(), 395));
        }
        dlgPanel.revalidate();
        dlgPanel.repaint();
    }

    private void openEditDialog() {
        new MuhurtaConfigDialog(parent, getSelectedInstant(), getSelectedInstant(), currentPlace, (date, time, place) -> {
            applyConfigUpdate(date, time, place);
        }).showDialog();
    }

    public void applyConfigUpdate(Date date, Date time, Place place) {
        if (place != null) {
            currentPlace = place;
        }
        if (date != null) {
            selectedInstant = date;
        } else if (time != null) {
            selectedInstant = time;
        }
        updateChart();
    }

    private void updateChart() {
        if (currentPlace == null) {
            return;
        }
        try {
            PlanetaryInfo planetaryInfo = computation.compute(getSelectedInstant(), currentPlace);
            PlanetChartData chartData = new PlanetChartData(CHART_TITLE, Varga.Rasi, planetaryInfo);
            String subtitle = formatSubtitle(getSelectedInstant(), currentPlace);

            if (chart == null) {
                // showHouseNumber = false so tooltips show planet orbs without house numbers
                chart = new Chart(chartData, CHART_SIZE, subtitle, false);
                chartHolder.add(chart);
            } else {
                chart.updateChartData(chartData, subtitle);
            }

            updateTable(planetaryInfo);

            chartHolder.revalidate();
            chartHolder.repaint();
        } catch (IllegalArgumentException ex) {
            log.log(Level.WARNING, "Invalid transit input: " + ex.getMessage());
        }
    }

    private void updateTable(PlanetaryInfo planetaryInfo) {
        if (tableModel == null) {
            return;
        }
        tableModel.setRowCount(0);
        Map<Planet, TransitChartComputation.SignTransition> transitions =
                computation.computeSignTransitions(getSelectedInstant(), currentPlace, planetaryInfo);

        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy, h:mm a");
        if (currentPlace != null && currentPlace.astrosoftTimeZone() != null) {
            sdf.setTimeZone(currentPlace.astrosoftTimeZone().getTimeZone());
        }

        for (Planet p : Planet.allPlanets()) {
            TransitChartComputation.SignTransition trans = transitions != null ? transitions.get(p) : null;
            String signChangeStr = trans != null ? trans.getTransitionString() : "—";
            String dateChangeStr = (trans != null && trans.getTransitionDate() != null)
                    ? sdf.format(trans.getTransitionDate())
                    : "—";

            tableModel.addRow(new Object[]{p.toString(), signChangeStr, dateChangeStr});
        }
    }

    private String formatSubtitle(Date instant, Place place) {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy, h:mm a");
        if (place.astrosoftTimeZone() != null) {
            sdf.setTimeZone(place.astrosoftTimeZone().getTimeZone());
        }
        String dateTimeStr = sdf.format(instant);

        String city = place.city() != null ? place.city().trim() : "";
        String country = place.country() != null ? place.country().trim() : "";
        String placeStr;
        if (!city.isEmpty() && !country.isEmpty()) {
            placeStr = city + ", " + country;
        } else if (!city.isEmpty()) {
            placeStr = city;
        } else {
            placeStr = country;
        }

        return "<html><center><font color='#14328c'><b>" + dateTimeStr + "</b></font><br><font color='#555555'>" + placeStr + "</font></center></html>";
    }

    public Date getSelectedInstant() {
        return selectedInstant != null ? selectedInstant : new Date();
    }

    public Place getCurrentPlace() {
        return currentPlace;
    }

    public Chart getChart() {
        return chart;
    }

    public JButton getEditButton() {
        return editButton;
    }

    public JButton getMoreButton() {
        return moreButton;
    }

    public JTable getDetailsTable() {
        return detailsTable;
    }

    public boolean isExpanded() {
        return isExpanded;
    }
}
