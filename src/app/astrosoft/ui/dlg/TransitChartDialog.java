package app.astrosoft.ui.dlg;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Varga;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.CalendarChooser;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.PlaceFinder;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransitChartDialog extends AstrosoftDialog {

    private static final Logger log = Logger.getLogger(TransitChartDialog.class.getName());

    public static final String CHART_TITLE = "Transit (D1)";
    private static final Dimension DLG_SIZE = new Dimension(640, 520);
    private static final Dimension MIN_DLG_SIZE = new Dimension(540, 460);
    private static final Dimension CHART_SIZE = new Dimension(320, 320);

    private final CalendarChooser dateChooser;
    private final CalendarChooser timeChooser;
    private final JComboBox placeCombo;
    private final JButton searchButton;
    private final JButton editButton;
    private final JLabel metadataLabel;
    private final JPanel chartHolder;
    private final TransitChartComputation computation;

    private Place currentPlace;
    private Chart chart;

    public TransitChartDialog(AstroSoft parent, Date initInstant, Place initPlace) {
        this(parent, initInstant, initPlace, new DefaultTransitChartComputation());
    }

    public TransitChartDialog(AstroSoft parent, Date initInstant, Place initPlace, TransitChartComputation computation) {
        super(parent, "Transit Chart", DLG_SIZE);
        this.computation = computation;
        this.currentPlace = initPlace != null ? initPlace : AstroSoft.getPreferences().getPlace();
        Date effectiveInstant = initInstant != null ? initInstant : new Date();

        setResizable(true);
        setMinimumSize(MIN_DLG_SIZE);

        dateChooser = CalendarChooser.getDateChooser();
        timeChooser = CalendarChooser.getTimeChooser();

        dateChooser.setSelectedDate(app.astrosoft.util.MuhurtaDateTime.toChooserDate(effectiveInstant, currentPlace.astrosoftTimeZone().getTimeZone()));
        timeChooser.setSelectedDate(app.astrosoft.util.MuhurtaDateTime.toChooserDate(effectiveInstant, currentPlace.astrosoftTimeZone().getTimeZone()));

        placeCombo = new JComboBox();
        placeCombo.setPreferredSize(new Dimension(160, 24));
        placeCombo.setEditable(true);
        updatePlaceCombo(currentPlace);

        searchButton = new JButton();
        searchButton.setPreferredSize(new Dimension(28, 24));
        Icon searchIcon = UIUtil.createImageIcon("Loc_Search");
        if (searchIcon != null) {
            searchButton.setIcon(searchIcon);
        } else {
            searchButton.setText("🔍");
        }
        searchButton.setToolTipText("Search Place");
        searchButton.addActionListener(e -> searchPlace());

        placeCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Place) {
                currentPlace = (Place) e.getItem();
                updateMetadataLabel();
                updateChart();
            }
        });

        if (placeCombo.getEditor() != null && placeCombo.getEditor().getEditorComponent() != null) {
            placeCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                        searchPlace();
                    }
                }
            });
        }

        editButton = new JButton("Edit...");
        editButton.addActionListener(e -> openEditDialog());

        metadataLabel = new JLabel();
        metadataLabel.setFont(UIUtil.getFont().deriveFont(java.awt.Font.PLAIN, 11f));
        metadataLabel.setForeground(UIConsts.DARK_BLUE);
        updateMetadataLabel();

        // Top control panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 4, 10));

        // Row 1: Single horizontal controls bar
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row1.add(new JLabel("Date:"));
        row1.add(dateChooser.getChooser());
        row1.add(Box.createHorizontalStrut(4));
        row1.add(new JLabel("Time:"));
        row1.add(timeChooser.getChooser());
        row1.add(Box.createHorizontalStrut(4));
        row1.add(new JLabel("Place:"));
        row1.add(placeCombo);
        row1.add(searchButton);
        row1.add(Box.createHorizontalStrut(6));
        row1.add(editButton);

        // Row 2: Metadata Strip
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.add(metadataLabel);

        topPanel.add(row1);
        topPanel.add(Box.createVerticalStrut(2));
        topPanel.add(row2);

        dlgPanel.setLayout(new BorderLayout(0, 4));
        dlgPanel.add(topPanel, BorderLayout.NORTH);

        // Center: Chart Holder
        chartHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        chartHolder.setBorder(BorderFactory.createTitledBorder(CHART_TITLE));
        dlgPanel.add(chartHolder, BorderLayout.CENTER);

        // Bottom status note
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        JLabel autoSaveNote = new JLabel("Changes stick automatically upon edit");
        autoSaveNote.setFont(UIUtil.getFont().deriveFont(10f));
        autoSaveNote.setForeground(java.awt.Color.GRAY);
        footer.add(autoSaveNote);
        dlgPanel.add(footer, BorderLayout.SOUTH);

        add(dlgPanel);
        setBackground(UIConsts.THEME_CLR);

        // Reactive listeners: immediate updates that stick
        dateChooser.addDateListener(date -> {
            updateMetadataLabel();
            updateChart();
        });
        timeChooser.addDateListener(time -> {
            updateMetadataLabel();
            updateChart();
        });

        updateChart();
    }

    public void showDialog() {
        setVisible(true);
    }

    private void updatePlaceCombo(Place place) {
        placeCombo.removeAllItems();
        if (place != null) {
            placeCombo.addItem(place);
            placeCombo.setSelectedItem(place);
        }
    }

    private void searchPlace() {
        Object item = placeCombo.getEditor() != null ? placeCombo.getEditor().getItem() : placeCombo.getSelectedItem();
        if (item != null) {
            String query = item.toString().trim();
            if (!query.isEmpty()) {
                try {
                    List<Place> results = PlaceFinder.findPlace(query);
                    if (results != null && !results.isEmpty()) {
                        placeCombo.removeAllItems();
                        for (Place p : results) {
                            placeCombo.addItem(p);
                        }
                        currentPlace = results.get(0);
                        placeCombo.setSelectedItem(currentPlace);
                        updateMetadataLabel();
                        updateChart();
                    } else {
                        JOptionPane.showMessageDialog(this, "No place found matching \"" + query + "\"", "Place Not Found", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error searching place: " + query, e);
                }
            }
        }
    }

    private void openEditDialog() {
        new MuhurtaConfigDialog(parent, getSelectedInstant(), getSelectedInstant(), currentPlace, (date, time, place) -> {
            currentPlace = place != null ? place : currentPlace;
            dateChooser.setSelectedDate(app.astrosoft.util.MuhurtaDateTime.toChooserDate(date, currentPlace.astrosoftTimeZone().getTimeZone()));
            timeChooser.setSelectedDate(app.astrosoft.util.MuhurtaDateTime.toChooserDate(time, currentPlace.astrosoftTimeZone().getTimeZone()));
            updatePlaceCombo(currentPlace);
            updateMetadataLabel();
            updateChart();
        }).showDialog();
    }

    private void updateChart() {
        if (currentPlace == null) {
            return;
        }
        try {
            PlanetaryInfo planetaryInfo = computation.compute(getSelectedInstant(), currentPlace);
            PlanetChartData chartData = new PlanetChartData(CHART_TITLE, Varga.Rasi, planetaryInfo);
            if (chart == null) {
                chart = new Chart(chartData, CHART_SIZE);
                chartHolder.add(chart);
            } else {
                chart.updateChartData(chartData);
            }
            chartHolder.revalidate();
            chartHolder.repaint();
        } catch (IllegalArgumentException ex) {
            log.log(Level.WARNING, "Invalid transit input: " + ex.getMessage());
        }
    }

    private void updateMetadataLabel() {
        if (currentPlace != null) {
            Place.Location lat = currentPlace.latitudeLocation();
            Place.Location lon = currentPlace.longitudeLocation();
            Date instant = getSelectedInstant();
            String tzStr = currentPlace.astrosoftTimeZone() != null ? currentPlace.astrosoftTimeZone().toString(instant) : "";
            metadataLabel.setText(String.format("<html><b>%s</b> &bull; %d&deg;%02d' %s, %d&deg;%02d' %s &bull; %s</html>",
                    currentPlace.toString(),
                    lat.deg(), lat.min(), lat.dir(),
                    lon.deg(), lon.min(), lon.dir(),
                    tzStr));
        }
    }

    public Date getSelectedInstant() {
        if (currentPlace == null) {
            return new Date();
        }
        return app.astrosoft.util.MuhurtaDateTime.fromChooserValues(
                dateChooser.getSelectedDate(),
                timeChooser.getSelectedDate(),
                currentPlace.astrosoftTimeZone().getTimeZone());
    }

    public Place getCurrentPlace() {
        return currentPlace;
    }

    public Chart getChart() {
        return chart;
    }
}
