package app.astrosoft.ui.dlg;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Varga;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.util.UIConsts;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransitChartDialog extends AstrosoftDialog {

    private static final Logger log = Logger.getLogger(TransitChartDialog.class.getName());

    public static final String CHART_TITLE = "Transit (D1)";
    private static final Dimension DLG_SIZE = new Dimension(360, 395);
    private static final Dimension MIN_DLG_SIZE = new Dimension(320, 360);
    private static final Dimension CHART_SIZE = new Dimension(300, 300);

    private final JButton editButton;
    private final JPanel chartHolder;
    private final TransitChartComputation computation;

    private Place currentPlace;
    private Date selectedInstant;
    private Chart chart;

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

        // Top bar: Only [Edit...] button aligned on the right (no text labels above the chart)
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        editButton = new JButton("Edit...");
        editButton.addActionListener(e -> openEditDialog());
        topBar.add(editButton);
        dlgPanel.add(topBar, BorderLayout.NORTH);

        // Center: Clean bounding border box around the chart (no text label on the border line)
        chartHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        chartHolder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 8, 8, 8),
                BorderFactory.createLineBorder(new java.awt.Color(190, 185, 170), 1)
        ));
        dlgPanel.add(chartHolder, BorderLayout.CENTER);

        add(dlgPanel);
        setBackground(UIConsts.THEME_CLR);

        updateChart();
    }

    public void showDialog() {
        setVisible(true);
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
            chartHolder.revalidate();
            chartHolder.repaint();
        } catch (IllegalArgumentException ex) {
            log.log(Level.WARNING, "Invalid transit input: " + ex.getMessage());
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
}
