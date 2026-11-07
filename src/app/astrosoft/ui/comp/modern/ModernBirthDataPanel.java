package app.astrosoft.ui.comp.modern;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.beans.Place.Direction;
import app.astrosoft.ui.dlg.OptionDialog;
import app.astrosoft.beans.Place.Location;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Sex;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.AstrosoftTimeZone;
import app.astrosoft.util.PlaceFinder;

/**
 * Modern Birth Data Entry Panel.
 * Uses Astrosoft's native theme with modern proportions and HCI best practices.
 * Supports both automatic place selection/search and manual coordinate/timezone entry.
 */
public class ModernBirthDataPanel extends JPanel {

    private static final int FIELD_HEIGHT = 28;

    // State
    private LocalDate selectedDate;
    private LocalTime selectedTime;
    private Place selectedPlace;
    private String notes;

    // UI Fields
    private JTextField nameField;
    private JRadioButton maleRadio;
    private JRadioButton femaleRadio;

    private JButton dateButton;
    private JButton timeButton;

    private JComboBox<Object> placeCombo;
    private JButton searchButton;
    private JCheckBox manualCoordCheck;

    // Coordinate inputs
    private JTextField latDegField;
    private JTextField latMinField;
    private JComboBox<Direction> latDirCombo;

    private JTextField lonDegField;
    private JTextField lonMinField;
    private JComboBox<Direction> lonDirCombo;

    private JTextField timeZoneField;
    private JComboBox<AstrosoftTimeZone> timeZoneCombo;

    public ModernBirthDataPanel() {
        this(null);
    }

    public ModernBirthDataPanel(BirthData initialData) {
        setBackground(UIConsts.THEME_CLR);
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        this.selectedDate = LocalDate.now();
        this.selectedTime = LocalTime.now();
        this.selectedPlace = getInitialDefaultPlace();

        initComponents();

        if (initialData != null) {
            setBirthData(initialData);
        } else {
            updateAllReadouts();
        }
    }

    private Place getInitialDefaultPlace() {
        try {
            if (AstroSoft.getPreferences() != null && AstroSoft.getPreferences().getPlace() != null) {
                return AstroSoft.getPreferences().getPlace();
            }
        } catch (Exception ignored) {}
        return Place.getDefault();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        Font labelFont = UIUtil.getFont();
        if (labelFont == null) labelFont = new Font("SansSerif", Font.PLAIN, 12);
        Font boldLabelFont = labelFont.deriveFont(Font.BOLD);

        UIConsts.ChartTheme theme = UIConsts.getChartTheme();
        Color primaryAccent = theme.getPrimaryAccentColor();
        Color textColor = theme.getPlanetForeground();
        Color borderColor = theme.getBorderColor();

        Border fieldBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(190, 195, 200), 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 1. Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formLabel(this, "Name", boldLabelFont, textColor, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        nameField.setEditable(true);
        nameField.setEnabled(true);
        nameField.setFocusable(true);
        nameField.setPreferredSize(new Dimension(220, FIELD_HEIGHT));
        nameField.setBorder(fieldBorder);
        add(nameField, gbc);
        row++;

        // 2. Gender
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formLabel(this, "Gender", boldLabelFont, textColor, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        genderPanel.setBackground(UIConsts.THEME_CLR);

        maleRadio = new JRadioButton("Male", true);
        maleRadio.setBackground(UIConsts.THEME_CLR);
        maleRadio.setForeground(textColor);
        maleRadio.setFont(labelFont);
        maleRadio.setFocusPainted(false);

        femaleRadio = new JRadioButton("Female", false);
        femaleRadio.setBackground(UIConsts.THEME_CLR);
        femaleRadio.setForeground(textColor);
        femaleRadio.setFont(labelFont);
        femaleRadio.setFocusPainted(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(maleRadio);
        bg.add(femaleRadio);

        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        add(genderPanel, gbc);
        row++;

        // 3. Date (Button that triggers modern DatePickerDialog)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formLabel(this, "Date", boldLabelFont, textColor, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        dateButton = new JButton();
        dateButton.setFont(labelFont);
        dateButton.setBackground(Color.WHITE);
        dateButton.setForeground(textColor);
        dateButton.setOpaque(true);
        dateButton.setBorder(fieldBorder);
        dateButton.setHorizontalAlignment(SwingConstants.LEFT);
        dateButton.setPreferredSize(new Dimension(220, FIELD_HEIGHT));
        dateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dateButton.setFocusPainted(false);
        dateButton.addActionListener(e -> openDatePicker());
        add(dateButton, gbc);
        row++;

        // 4. Birth Time (Button that triggers modern TimePickerDialog)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formLabel(this, "BirthTime", boldLabelFont, textColor, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        timeButton = new JButton();
        timeButton.setFont(labelFont);
        timeButton.setBackground(Color.WHITE);
        timeButton.setForeground(textColor);
        timeButton.setOpaque(true);
        timeButton.setBorder(fieldBorder);
        timeButton.setHorizontalAlignment(SwingConstants.LEFT);
        timeButton.setPreferredSize(new Dimension(220, FIELD_HEIGHT));
        timeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        timeButton.setFocusPainted(false);
        timeButton.addActionListener(e -> openTimePicker());
        add(timeButton, gbc);
        row++;

        // 5. Place (In-place expandable combo box with search button)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formLabel(this, "Place", boldLabelFont, textColor, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        JPanel placePanel = new JPanel(new BorderLayout(4, 0));
        placePanel.setBackground(UIConsts.THEME_CLR);

        placeCombo = new JComboBox<>();
        placeCombo.setEditable(true);
        placeCombo.setFont(labelFont);
        placeCombo.setPreferredSize(new Dimension(190, FIELD_HEIGHT));
        if (selectedPlace != null) {
            placeCombo.addItem(selectedPlace);
        }

        placeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Object item = e.getItem();
                    if (item instanceof Place) {
                        setSelectedPlace((Place) item);
                    }
                }
            }
        });

        // Search on Enter key in place editor
        placeCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    performPlaceSearch();
                }
            }
        });

        searchButton = new JButton();
        try {
            searchButton.setIcon(UIUtil.createImageIcon("Loc_Search"));
        } catch (Exception ignored) {
            searchButton.setText("🔍");
        }
        searchButton.setPreferredSize(new Dimension(FIELD_HEIGHT, FIELD_HEIGHT));
        searchButton.setToolTipText("Search Place");
        searchButton.setFocusPainted(false);
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> performPlaceSearch());

        placePanel.add(placeCombo, BorderLayout.CENTER);
        placePanel.add(searchButton, BorderLayout.EAST);
        add(placePanel, gbc);
        row++;

        // Manual Coordinates Checkbox (placed below Place field, above coordinates divider)
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        manualCoordCheck = new JCheckBox("Manual Coordinates");
        manualCoordCheck.setBackground(UIConsts.THEME_CLR);
        manualCoordCheck.setForeground(textColor);
        manualCoordCheck.setFont(labelFont);
        manualCoordCheck.setFocusPainted(false);
        manualCoordCheck.addActionListener(e -> toggleManualMode(manualCoordCheck.isSelected()));
        add(manualCoordCheck, gbc);
        row++;

        // Subtle horizontal divider separating user inputs and derived coordinates
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 4, 6, 4);
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setForeground(borderColor);
        add(sep, gbc);
        row++;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 6, 4, 6);

        // 6. Latitude (degree, minute, direction)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formLabel(this, "Latitude", boldLabelFont, textColor, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        JPanel latPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        latPanel.setBackground(UIConsts.THEME_CLR);

        latDegField = createReadOnlyField(4, theme);
        latMinField = createReadOnlyField(4, theme);
        latDirCombo = new JComboBox<>(new Direction[]{Direction.NORTH, Direction.SOUTH});
        latDirCombo.setFont(labelFont);
        latDirCombo.setBackground(Color.WHITE);
        latDirCombo.setForeground(textColor);
        latDirCombo.setPreferredSize(new Dimension(88, FIELD_HEIGHT));
        latDirCombo.setEnabled(false);

        latPanel.add(latDegField);
        JLabel latDegLbl = new JLabel("°"); latDegLbl.setForeground(textColor); latPanel.add(latDegLbl);
        latPanel.add(latMinField);
        JLabel latMinLbl = new JLabel("'"); latMinLbl.setForeground(textColor); latPanel.add(latMinLbl);
        latPanel.add(latDirCombo);
        add(latPanel, gbc);
        row++;

        // 7. Longitude (degree, minute, direction)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formLabel(this, "Longitude", boldLabelFont, textColor, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        JPanel lonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        lonPanel.setBackground(UIConsts.THEME_CLR);

        lonDegField = createReadOnlyField(4, theme);
        lonMinField = createReadOnlyField(4, theme);
        lonDirCombo = new JComboBox<>(new Direction[]{Direction.EAST, Direction.WEST});
        lonDirCombo.setFont(labelFont);
        lonDirCombo.setBackground(Color.WHITE);
        lonDirCombo.setForeground(textColor);
        lonDirCombo.setPreferredSize(new Dimension(88, FIELD_HEIGHT));
        lonDirCombo.setEnabled(false);

        lonPanel.add(lonDegField);
        JLabel lonDegLbl = new JLabel("°"); lonDegLbl.setForeground(textColor); lonPanel.add(lonDegLbl);
        lonPanel.add(lonMinField);
        JLabel lonMinLbl = new JLabel("'"); lonMinLbl.setForeground(textColor); lonPanel.add(lonMinLbl);
        lonPanel.add(lonDirCombo);
        add(lonPanel, gbc);
        row++;

        // 8. Time Zone
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formLabel(this, "Time Zone", boldLabelFont, textColor, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        JPanel tzContainer = new JPanel(new BorderLayout(0, 0));
        tzContainer.setBackground(UIConsts.THEME_CLR);

        timeZoneField = createReadOnlyField(22, theme);
        timeZoneField.setHorizontalAlignment(JTextField.LEFT);

        timeZoneCombo = new JComboBox<>(AstrosoftTimeZone.availableTimeZones());
        timeZoneCombo.setFont(labelFont);
        timeZoneCombo.setBackground(Color.WHITE);
        timeZoneCombo.setForeground(textColor);
        timeZoneCombo.setPreferredSize(new Dimension(220, FIELD_HEIGHT));
        timeZoneCombo.setVisible(false);

        tzContainer.add(timeZoneField, BorderLayout.NORTH);
        tzContainer.add(timeZoneCombo, BorderLayout.CENTER);
        add(tzContainer, gbc);
        row++;

        // Bottom filler row to absorb vertical expansion
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weighty = 1.0;
        add(Box.createVerticalGlue(), gbc);
    }

    public void toggleManualMode(boolean manual) {
        UIConsts.ChartTheme currentTheme = UIConsts.getChartTheme();
        Color readOnlyBg = currentTheme.getBackground();

        latDegField.setEditable(manual);
        latDegField.setFocusable(manual);
        latMinField.setEditable(manual);
        latMinField.setFocusable(manual);
        lonDegField.setEditable(manual);
        lonDegField.setFocusable(manual);
        lonMinField.setEditable(manual);
        lonMinField.setFocusable(manual);

        latDirCombo.setEnabled(manual);
        lonDirCombo.setEnabled(manual);
        searchButton.setEnabled(!manual);

        if (manual) {
            latDegField.setBackground(Color.WHITE);
            latMinField.setBackground(Color.WHITE);
            lonDegField.setBackground(Color.WHITE);
            lonMinField.setBackground(Color.WHITE);

            timeZoneField.setVisible(false);
            timeZoneCombo.setVisible(true);
            if (selectedPlace != null && selectedPlace.astrosoftTimeZone() != null) {
                timeZoneCombo.setSelectedItem(selectedPlace.astrosoftTimeZone());
            }
            latDegField.requestFocusInWindow();
        } else {
            latDegField.setBackground(readOnlyBg);
            latMinField.setBackground(readOnlyBg);
            lonDegField.setBackground(readOnlyBg);
            lonMinField.setBackground(readOnlyBg);

            timeZoneCombo.setVisible(false);
            timeZoneField.setVisible(true);
            updatePlaceAndCoordinates();
            updateTimezoneReadout();
        }
        revalidate();
        repaint();
    }

    private void formLabel(JPanel parent, String text, Font font, Color color, GridBagConstraints gbc) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        if (color != null) label.setForeground(color);
        parent.add(label, gbc);
    }

    private JTextField createReadOnlyField(int columns, UIConsts.ChartTheme theme) {
        JTextField tf = new JTextField(columns);
        tf.setEditable(false);
        tf.setFocusable(false);
        Color bg = (theme != null) ? theme.getBackground() : new Color(246, 247, 249);
        Color fg = (theme != null) ? theme.getPlanetForeground() : new Color(40, 45, 55);
        Color bc = (theme != null) ? theme.getBorderColor() : new Color(205, 210, 215);

        tf.setBackground(bg);
        tf.setForeground(fg);
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, FIELD_HEIGHT));
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bc, 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));
        return tf;
    }

    private void openDatePicker() {
        LocalDate picked = DatePickerDialog.showDatePicker(this, selectedDate);
        if (picked != null) {
            this.selectedDate = picked;
            updateDateAndTimeButtons();
            updateTimezoneReadout();
        }
    }

    private void openTimePicker() {
        LocalTime picked = TimePickerDialog.showTimePicker(this, selectedTime);
        if (picked != null) {
            this.selectedTime = picked;
            updateDateAndTimeButtons();
            updateTimezoneReadout();
        }
    }

    private void performPlaceSearch() {
        String query = "";
        Object editorItem = placeCombo.getEditor().getItem();
        if (editorItem != null) {
            query = editorItem.toString().trim();
        }
        if (query.isEmpty() && placeCombo.getSelectedItem() != null) {
            query = placeCombo.getSelectedItem().toString().trim();
        }

        if (query.isEmpty()) return;

        final String finalQuery = query;
        searchButton.setEnabled(false);

        new SwingWorker<List<Place>, Void>() {
            @Override
            protected List<Place> doInBackground() {
                return PlaceFinder.findPlace(finalQuery);
            }

            @Override
            protected void done() {
                searchButton.setEnabled(true);
                try {
                    List<Place> result = get();
                    if (result != null && !result.isEmpty()) {
                        placeCombo.removeAllItems();
                        for (Place p : result) {
                            placeCombo.addItem(p);
                        }
                        setSelectedPlace(result.get(0));
                        SwingUtilities.invokeLater(() -> {
                            Component editorComp = placeCombo.getEditor().getEditorComponent();
                            if (editorComp instanceof JTextField) {
                                ((JTextField) editorComp).setCaretPosition(0);
                            }
                        });
                        if (placeCombo.isShowing()) {
                            placeCombo.showPopup();
                        }
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    public void updateAllReadouts() {
        updateDateAndTimeButtons();
        updatePlaceAndCoordinates();
        updateTimezoneReadout();
    }

    private void updateDateAndTimeButtons() {
        if (dateButton != null && selectedDate != null) {
            dateButton.setText("📅  " + selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        }
        if (timeButton != null && selectedTime != null) {
            timeButton.setText("🕒  " + selectedTime.format(DateTimeFormatter.ofPattern("hh : mm : ss a")));
        }
    }

    private void updatePlaceAndCoordinates() {
        if (selectedPlace == null) return;

        Location lat = selectedPlace.latitudeLocation();
        Location lon = selectedPlace.longitudeLocation();

        if (latDegField != null && lat != null) {
            latDegField.setText(String.valueOf(lat.deg()));
            latMinField.setText(String.format("%02d", lat.min()));
            if (latDirCombo != null && lat.dir() != null) {
                latDirCombo.setSelectedItem(lat.dir());
            }
        }

        if (lonDegField != null && lon != null) {
            lonDegField.setText(String.valueOf(lon.deg()));
            lonMinField.setText(String.format("%02d", lon.min()));
            if (lonDirCombo != null && lon.dir() != null) {
                lonDirCombo.setSelectedItem(lon.dir());
            }
        }
    }

    private void updateTimezoneReadout() {
        if (selectedPlace == null) return;

        AstrosoftTimeZone atz = selectedPlace.astrosoftTimeZone();
        String tzId = (atz != null && atz.id() != null && !atz.id().isEmpty()) ? atz.id() : "Asia/Kolkata";

        if (timeZoneCombo != null) {
            for (int i = 0; i < timeZoneCombo.getItemCount(); i++) {
                AstrosoftTimeZone at = timeZoneCombo.getItemAt(i);
                if (at != null && tzId.equals(at.id())) {
                    timeZoneCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (timeZoneField != null) {
            try {
                TimeZone tz = (atz != null) ? atz.getTimeZone() : TimeZone.getTimeZone(tzId);
                Calendar cal = new GregorianCalendar(tz);
                cal.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth(),
                        selectedTime.getHour(), selectedTime.getMinute(), selectedTime.getSecond());

                int offsetMillis = tz.getOffset(cal.getTimeInMillis());
                int totalMins = Math.abs(offsetMillis / 60000);
                int hrs = totalMins / 60;
                int mins = totalMins % 60;
                String sign = (offsetMillis < 0) ? "-" : "+";

                timeZoneField.setText(String.format("%s (GMT %s%02d:%02d)", tzId, sign, hrs, mins));
            } catch (Exception ex) {
                timeZoneField.setText(tzId);
            }
        }
    }

    public boolean validateInputs() {
        String name = getPersonName();
        if (name == null || name.trim().isEmpty()) {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                OptionDialog.showMessageDialog(this,
                        "Person Name is required.\nPlease enter a name before proceeding.",
                        "Incomplete Birth Details",
                        JOptionPane.WARNING_MESSAGE);
            }
            nameField.requestFocusInWindow();
            return false;
        }

        if (manualCoordCheck != null && manualCoordCheck.isSelected()) {
            // Validate manual latitude
            int latDeg = -1;
            int latMin = 0;
            try {
                String degStr = latDegField.getText().trim();
                if (degStr.isEmpty()) {
                    showValidationError("Latitude degree is required.");
                    latDegField.requestFocusInWindow();
                    return false;
                }
                latDeg = Integer.parseInt(degStr);
                String minStr = latMinField.getText().trim();
                if (!minStr.isEmpty()) {
                    latMin = Integer.parseInt(minStr);
                }
            } catch (NumberFormatException e) {
                showValidationError("Latitude degrees and minutes must be valid numbers.");
                latDegField.requestFocusInWindow();
                return false;
            }

            if (latDeg < 0 || latDeg > 90 || latMin < 0 || latMin > 59 || (latDeg == 90 && latMin > 0)) {
                showValidationError("Invalid Latitude.\nDegrees must be between 0 and 90, minutes between 0 and 59.");
                latDegField.requestFocusInWindow();
                return false;
            }

            // Validate manual longitude
            int lonDeg = -1;
            int lonMin = 0;
            try {
                String degStr = lonDegField.getText().trim();
                if (degStr.isEmpty()) {
                    showValidationError("Longitude degree is required.");
                    lonDegField.requestFocusInWindow();
                    return false;
                }
                lonDeg = Integer.parseInt(degStr);
                String minStr = lonMinField.getText().trim();
                if (!minStr.isEmpty()) {
                    lonMin = Integer.parseInt(minStr);
                }
            } catch (NumberFormatException e) {
                showValidationError("Longitude degrees and minutes must be valid numbers.");
                lonDegField.requestFocusInWindow();
                return false;
            }

            if (lonDeg < 0 || lonDeg > 180 || lonMin < 0 || lonMin > 59 || (lonDeg == 180 && lonMin > 0)) {
                showValidationError("Invalid Longitude.\nDegrees must be between 0 and 180, minutes between 0 and 59.");
                lonDegField.requestFocusInWindow();
                return false;
            }
        } else {
            if (selectedPlace == null) {
                if (!java.awt.GraphicsEnvironment.isHeadless()) {
                    OptionDialog.showMessageDialog(this,
                            "Birth Place is required.\nPlease select a valid place.",
                            "Incomplete Birth Details",
                            JOptionPane.WARNING_MESSAGE);
                }
                return false;
            }
        }

        return true;
    }

    private void showValidationError(String message) {
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            OptionDialog.showMessageDialog(this,
                    message,
                    "Invalid Coordinates",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public BirthData getBirthData() {
        if (!validateInputs()) {
            return null;
        }

        String name = getPersonName().trim();
        Sex sex = maleRadio.isSelected() ? Sex.Male : Sex.Female;
        int date = selectedDate.getDayOfMonth();
        int month = selectedDate.getMonthValue();
        int year = selectedDate.getYear();
        int hr = selectedTime.getHour();
        int min = selectedTime.getMinute();
        int sec = selectedTime.getSecond();

        Place placeToUse = getSelectedPlace();

        BirthData bd = new BirthData(name, sex, date, month, year, hr, min, sec, placeToUse);
        if (notes != null) {
            bd.setNotes(notes);
        }
        return bd;
    }

    public void setBirthData(BirthData bd) {
        if (bd == null) return;

        setPersonName(bd.name());
        if (bd.sex() != null && bd.sex().isFemale()) {
            femaleRadio.setSelected(true);
        } else {
            maleRadio.setSelected(true);
        }

        int sec = (bd.birthDay() != null) ? bd.birthDay().get(Calendar.SECOND) : bd.seconds();
        this.selectedDate = LocalDate.of(bd.year(), bd.month(), bd.date());
        this.selectedTime = LocalTime.of(bd.hour(), bd.minutes(), sec);
        setManualCoordMode(false);
        setSelectedPlace(bd.getBirthPlace());
        this.notes = bd.getNotes();

        updateAllReadouts();
    }

    public String getPersonName() {
        return nameField != null ? nameField.getText() : "";
    }

    public void setPersonName(String name) {
        if (nameField != null) {
            nameField.setText(name);
        }
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate d) {
        this.selectedDate = d;
        updateDateAndTimeButtons();
        updateTimezoneReadout();
    }

    public LocalTime getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(LocalTime t) {
        this.selectedTime = t;
        updateDateAndTimeButtons();
        updateTimezoneReadout();
    }

    public Place getSelectedPlace() {
        if (manualCoordCheck != null && manualCoordCheck.isSelected()) {
            try {
                int latDeg = Integer.parseInt(latDegField.getText().trim());
                int latMin = latMinField.getText().trim().isEmpty() ? 0 : Integer.parseInt(latMinField.getText().trim());
                Direction latDir = (Direction) latDirCombo.getSelectedItem();
                if (latDir == null) latDir = Direction.NORTH;

                int lonDeg = Integer.parseInt(lonDegField.getText().trim());
                int lonMin = lonMinField.getText().trim().isEmpty() ? 0 : Integer.parseInt(lonMinField.getText().trim());
                Direction lonDir = (Direction) lonDirCombo.getSelectedItem();
                if (lonDir == null) lonDir = Direction.EAST;

                AstrosoftTimeZone tz = (AstrosoftTimeZone) timeZoneCombo.getSelectedItem();
                String tzId = (tz != null && tz.id() != null) ? tz.id() : "GMT";

                String placeText = "";
                Object editorItem = placeCombo.getEditor().getItem();
                if (editorItem != null) {
                    placeText = editorItem.toString().trim();
                } else if (placeCombo.getSelectedItem() != null) {
                    placeText = placeCombo.getSelectedItem().toString().trim();
                }

                String city = "Custom";
                String state = "";
                String country = "";
                if (!placeText.isEmpty()) {
                    String[] parts = placeText.split(",");
                    if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                        city = parts[0].trim();
                    }
                    if (parts.length > 1) {
                        state = parts[1].trim();
                    }
                    if (parts.length > 2) {
                        country = parts[2].trim();
                    }
                }
                return new Place(city, state, country, new Place.Location(latDeg, latMin, latDir), new Place.Location(lonDeg, lonMin, lonDir), tzId);
            } catch (Exception e) {
                return selectedPlace;
            }
        }
        return selectedPlace;
    }

    public void setSelectedPlace(Place p) {
        if (p == null) return;
        this.selectedPlace = p;

        if (placeCombo != null) {
            boolean found = false;
            for (int i = 0; i < placeCombo.getItemCount(); i++) {
                if (p.equals(placeCombo.getItemAt(i))) {
                    placeCombo.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                placeCombo.addItem(p);
                placeCombo.setSelectedItem(p);
            }
            SwingUtilities.invokeLater(() -> {
                Component editorComp = placeCombo.getEditor().getEditorComponent();
                if (editorComp instanceof JTextField) {
                    ((JTextField) editorComp).setCaretPosition(0);
                }
            });
        }
        updatePlaceAndCoordinates();
        updateTimezoneReadout();
    }

    public Sex getSelectedSex() {
        return maleRadio.isSelected() ? Sex.Male : Sex.Female;
    }

    public void setSelectedSex(Sex sex) {
        if (sex == Sex.Female) {
            femaleRadio.setSelected(true);
        } else {
            maleRadio.setSelected(true);
        }
    }

    public JTextField getNameField() {
        return nameField;
    }

    public JCheckBox getManualCoordCheck() {
        return manualCoordCheck;
    }

    public void setManualCoordMode(boolean manual) {
        if (manualCoordCheck != null) {
            manualCoordCheck.setSelected(manual);
        }
        toggleManualMode(manual);
    }

    public JTextField getLatDegField() {
        return latDegField;
    }

    public JTextField getLatMinField() {
        return latMinField;
    }

    public JComboBox<Direction> getLatDirCombo() {
        return latDirCombo;
    }

    public JTextField getLonDegField() {
        return lonDegField;
    }

    public JTextField getLonMinField() {
        return lonMinField;
    }

    public JComboBox<Direction> getLonDirCombo() {
        return lonDirCombo;
    }

    public JComboBox<AstrosoftTimeZone> getTimeZoneCombo() {
        return timeZoneCombo;
    }

    public JComboBox<Object> getPlaceCombo() {
        return placeCombo;
    }
}
