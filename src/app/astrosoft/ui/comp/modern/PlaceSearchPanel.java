package app.astrosoft.ui.comp.modern;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import app.astrosoft.beans.Place;
import app.astrosoft.util.PlaceFinder;

/**
 * Modern Searchable Place Picker Panel.
 * Self-contained panel for searching, browsing, and selecting places. Headless-safe.
 */
public class PlaceSearchPanel extends JPanel {

    private static final Color PRIMARY_BLUE = new Color(59, 89, 152);
    private static final Color SELECTED_BLUE = new Color(51, 103, 214);
    private static final Color HEADER_TEXT_LIGHT = new Color(200, 220, 255);

    private Place selectedPlace;
    private boolean confirmed = false;
    private Runnable onConfirmListener;
    private Runnable onCancelListener;

    private JTextField searchField;
    private JButton searchButton;
    private JLabel statusLabel;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton selectButton;

    private List<Place> currentResults = new ArrayList<>();
    private SwingWorker<List<Place>, Void> searchWorker;

    public PlaceSearchPanel(Place initialPlace) {
        this.selectedPlace = (initialPlace != null) ? initialPlace : Place.getDefault();

        initComponents();
        if (selectedPlace != null) {
            currentResults.add(selectedPlace);
            updateTableResults(currentResults);
            resultsTable.setRowSelectionInterval(0, 0);
        }
    }

    public void setOnConfirmListener(Runnable r) {
        this.onConfirmListener = r;
    }

    public void setOnCancelListener(Runnable r) {
        this.onCancelListener = r;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Place getSelectedPlace() {
        return selectedPlace;
    }

    public void setSelectedPlace(Place place) {
        this.selectedPlace = place;
        if (place != null) {
            currentResults.clear();
            currentResults.add(place);
            updateTableResults(currentResults);
            resultsTable.setRowSelectionInterval(0, 0);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. Top Header Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titleLabel = new JLabel("Place Search");
        titleLabel.setForeground(HEADER_TEXT_LIGHT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel subtitleLabel = new JLabel("Search by city, town or state");
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content: Search input + Results Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));

        // Search Input Bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(Color.WHITE);

        searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });

        searchButton = new JButton("Search");
        searchButton.setBackground(PRIMARY_BLUE);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> performSearch());

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchButton, BorderLayout.EAST);
        centerPanel.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"City / Town", "State", "Country", "Coordinates", "Timezone"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(24);
        resultsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        resultsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));

        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = resultsTable.getSelectedRow();
                boolean hasSelection = selectedRow >= 0 && selectedRow < currentResults.size();
                selectButton.setEnabled(hasSelection);
                if (hasSelection) {
                    selectedPlace = currentResults.get(selectedRow);
                }
            }
        });

        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = resultsTable.getSelectedRow();
                    if (selectedRow >= 0 && selectedRow < currentResults.size()) {
                        selectedPlace = currentResults.get(selectedRow);
                        confirmed = true;
                        if (onConfirmListener != null) onConfirmListener.run();
                    }
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        // Status Label
        statusLabel = new JLabel("Enter a city name and click Search");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);
        centerPanel.add(statusLabel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Action Bar (Cancel, Select)
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(4, 12, 10, 12));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            if (onCancelListener != null) onCancelListener.run();
        });

        selectButton = new JButton("Select");
        selectButton.setBackground(PRIMARY_BLUE);
        selectButton.setForeground(Color.WHITE);
        selectButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        selectButton.setFocusPainted(false);
        selectButton.setEnabled(false);
        selectButton.addActionListener(e -> {
            int selectedRow = resultsTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < currentResults.size()) {
                selectedPlace = currentResults.get(selectedRow);
                confirmed = true;
                if (onConfirmListener != null) onConfirmListener.run();
            }
        });

        bottomBar.add(cancelBtn);
        bottomBar.add(selectButton);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void setSearchText(String text) {
        searchField.setText(text);
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            statusLabel.setText("Please enter a location name to search.");
            return;
        }

        if (searchWorker != null && !searchWorker.isDone()) {
            searchWorker.cancel(true);
        }

        searchButton.setEnabled(false);
        statusLabel.setText("Searching for \"" + query + "\"...");

        searchWorker = new SwingWorker<List<Place>, Void>() {
            @Override
            protected List<Place> doInBackground() {
                return PlaceFinder.findPlace(query);
            }

            @Override
            protected void done() {
                searchButton.setEnabled(true);
                if (isCancelled()) return;

                try {
                    List<Place> places = get();
                    if (places == null || places.isEmpty()) {
                        statusLabel.setText("No locations found for \"" + query + "\".");
                        updateTableResults(new ArrayList<>());
                    } else {
                        currentResults = places;
                        statusLabel.setText("Found " + places.size() + " location(s).");
                        updateTableResults(places);
                        resultsTable.setRowSelectionInterval(0, 0);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Search error: " + ex.getMessage());
                }
            }
        };
        searchWorker.execute();
    }

    private void updateTableResults(List<Place> places) {
        tableModel.setRowCount(0);
        for (Place p : places) {
            String coords = (p.latitudeLocation() != null && p.longitudeLocation() != null)
                    ? p.coordinates()
                    : "";
            String tz = (p.astrosoftTimeZone() != null) ? p.astrosoftTimeZone().id() : "";
            tableModel.addRow(new Object[]{
                    p.city(),
                    p.state() != null ? p.state() : "",
                    p.country() != null ? p.country() : "",
                    coords,
                    tz
            });
        }
    }
}
