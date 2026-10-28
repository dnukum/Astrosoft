package app.astrosoft.ui.dlg;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Varga;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.CalendarChooser;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.PlaceChooser;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.beans.PlanetChartData;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collections;
import java.util.Date;

public class TransitChartDialog extends AstrosoftDialog {

    private static final Dimension DLG_SIZE = new Dimension(720, 640);
    private static final Dimension CHART_SIZE = new Dimension(420, 420);

    private final CalendarChooser dateChooser;
    private final CalendarChooser timeChooser;
    private final PlaceChooser placeChooser;
    private final TransitChartComputation computation;
    private final JLabel statusLabel = new JLabel(" ");
    private Chart chart;

    public TransitChartDialog(AstroSoft parent, Date initInstant, Place initPlace) {
        this(parent, initInstant, initPlace, new DefaultTransitChartComputation());
    }

    public TransitChartDialog(AstroSoft parent, Date initInstant, Place initPlace, TransitChartComputation computation) {
        super(parent, "Transit Chart", DLG_SIZE);
        this.computation = computation;

        Place effectivePlace = initPlace != null ? initPlace : AstroSoft.getPreferences().getPlace();
        Date effectiveInstant = initInstant != null ? initInstant : new Date();

        dateChooser = CalendarChooser.getDateChooser();
        timeChooser = CalendarChooser.getTimeChooser();
        placeChooser = new PlaceChooser(new Dimension(520, 140), false);

        dateChooser.setSelectedDate(app.astrosoft.util.MuhurtaDateTime.toChooserDate(effectiveInstant, effectivePlace.astrosoftTimeZone().getTimeZone()));
        timeChooser.setSelectedDate(app.astrosoft.util.MuhurtaDateTime.toChooserDate(effectiveInstant, effectivePlace.astrosoftTimeZone().getTimeZone()));
        placeChooser.addPlaces(Collections.singletonList(effectivePlace));
        placeChooser.setSelectedPlace(effectivePlace);
        placeChooser.setLocalDateForTimezone(dateChooser.getSelectedDate(), timeChooser.getSelectedDate());
        dateChooser.addDateListener(date -> placeChooser.setLocalDateForTimezone(dateChooser.getSelectedDate(), timeChooser.getSelectedDate()));
        timeChooser.addDateListener(date -> placeChooser.setLocalDateForTimezone(dateChooser.getSelectedDate(), timeChooser.getSelectedDate()));

        dlgPanel.setLayout(new BorderLayout(10, 10));
        dlgPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Date:"));
        datePanel.add(dateChooser.getChooser());

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("Time:"));
        timePanel.add(timeChooser.getChooser());

        JPanel top = new JPanel(new BorderLayout());
        fields.add(datePanel);
        fields.add(timePanel);
        fields.add(placeChooser);
        top.add(fields, BorderLayout.CENTER);

        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> openEditDialog());
        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        editPanel.add(editButton);
        top.add(editPanel, BorderLayout.SOUTH);

        dlgPanel.add(top, BorderLayout.NORTH);

        JPanel chartHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        chartHolder.setBorder(BorderFactory.createTitledBorder("Rasi"));
        dlgPanel.add(chartHolder, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        statusLabel.setForeground(UIConsts.DARK_BLUE);
        footer.add(statusLabel, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshChart());
        footer.add(refreshButton, BorderLayout.EAST);
        dlgPanel.add(footer, BorderLayout.SOUTH);

        add(dlgPanel);
        setBackground(UIConsts.THEME_CLR);

        try {
            PlanetaryInfo planetaryInfo = computeCurrentPlanetaryInfo();
            chart = new Chart(new PlanetChartData(Varga.Rasi, planetaryInfo), CHART_SIZE);
            chartHolder.add(chart);
            updateStatus();
        } catch (IllegalArgumentException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    public void showDialog() {
        setVisible(true);
    }

    private void openEditDialog() {
        new MuhurtaConfigDialog(parent, getSelectedInstant(), getSelectedInstant(), placeChooser.getSelectedPlace(), (date, time, place) -> {
            Place effectivePlace = place != null ? place : placeChooser.getSelectedPlace();
            dateChooser.setSelectedDate(app.astrosoft.util.MuhurtaDateTime.toChooserDate(date, effectivePlace.astrosoftTimeZone().getTimeZone()));
            timeChooser.setSelectedDate(app.astrosoft.util.MuhurtaDateTime.toChooserDate(time, effectivePlace.astrosoftTimeZone().getTimeZone()));
            placeChooser.addPlaces(Collections.singletonList(effectivePlace));
            placeChooser.setSelectedPlace(effectivePlace);
            placeChooser.setLocalDateForTimezone(dateChooser.getSelectedDate(), timeChooser.getSelectedDate());
            refreshChart();
        }).showDialog();
    }

    private void refreshChart() {
        try {
            PlanetaryInfo planetaryInfo = computeCurrentPlanetaryInfo();
            chart.updateChartData(new PlanetChartData(Varga.Rasi, planetaryInfo));
            updateStatus();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid transit input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PlanetaryInfo computeCurrentPlanetaryInfo() {
        Place place = placeChooser.getSelectedPlace();
        if (place == null) {
            throw new IllegalArgumentException("Place is required.");
        }
        return computation.compute(getSelectedInstant(), place);
    }

    private Date getSelectedInstant() {
        Place place = placeChooser.getSelectedPlace();
        if (place == null) {
            throw new IllegalArgumentException("Place is required.");
        }
        return app.astrosoft.util.MuhurtaDateTime.fromChooserValues(dateChooser.getSelectedDate(), timeChooser.getSelectedDate(),
                place.astrosoftTimeZone().getTimeZone());
    }

    private void updateStatus() {
        Place place = placeChooser.getSelectedPlace();
        statusLabel.setText(place == null ? "" : "Using place: " + place.toString());
    }
}
