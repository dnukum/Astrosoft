package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import app.astrosoft.beans.Place;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.PlaceChooser;
import app.astrosoft.ui.comp.modern.DatePickerDialog;
import app.astrosoft.ui.comp.modern.ModernButton;
import app.astrosoft.ui.comp.modern.TimePickerDialog;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;

/**
 * Modern Muhurta / Transit Date & Time Configuration Dialog.
 * Replaces manual typing into spinners with visual DatePickerDialog and TimePickerDialog.
 */
public class MuhurtaConfigDialog extends AstrosoftDialog {
    
    private static final Dimension DLG_SIZE = new Dimension(500, 360);
    
    private LocalDate selectedDate;
    private LocalTime selectedTime;
    private PlaceChooser placeChooser;
    
    private JButton dateButton;
    private JButton timeButton;
    private JButton okButton;
    private JButton cancelButton;
    
    public interface ConfigListener {
        void configChanged(Date date, Date time, Place place);
    }
    
    public MuhurtaConfigDialog(AstroSoft parent, Date initDate, Date initTime, Place initPlace, final ConfigListener listener) {
        this(parent, initDate, initTime, initPlace, listener, false);
    }

    public MuhurtaConfigDialog(AstroSoft parent, Date initDate, Date initTime, Place initPlace, final ConfigListener listener, boolean autoShow) {
        super(parent, "New Muhurta", DLG_SIZE);
        setModal(true);
        
        final java.util.TimeZone zone = initPlace == null ? java.util.TimeZone.getDefault()
            : initPlace.astrosoftTimeZone().getTimeZone();

        Date chooserDate = app.astrosoft.util.MuhurtaDateTime.toChooserDate(initDate != null ? initDate : new Date(), zone);
        Calendar dCal = Calendar.getInstance();
        dCal.setTime(chooserDate);
        this.selectedDate = LocalDate.of(dCal.get(Calendar.YEAR), dCal.get(Calendar.MONTH) + 1, dCal.get(Calendar.DAY_OF_MONTH));

        Date chooserTime = app.astrosoft.util.MuhurtaDateTime.toChooserDate(initTime != null ? initTime : new Date(), zone);
        Calendar tCal = Calendar.getInstance();
        tCal.setTime(chooserTime);
        this.selectedTime = LocalTime.of(tCal.get(Calendar.HOUR_OF_DAY), tCal.get(Calendar.MINUTE), tCal.get(Calendar.SECOND));

        placeChooser = new PlaceChooser(new Dimension(400, 140), false);
        placeChooser.addPlaces(java.util.Collections.singletonList(initPlace));
        placeChooser.setSelectedPlace(initPlace);

        final Runnable refreshTimezoneLabels = () -> {
            Calendar dC = Calendar.getInstance();
            dC.clear();
            dC.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
            Calendar tC = Calendar.getInstance();
            tC.clear();
            tC.set(Calendar.HOUR_OF_DAY, selectedTime.getHour());
            tC.set(Calendar.MINUTE, selectedTime.getMinute());
            tC.set(Calendar.SECOND, selectedTime.getSecond());
            placeChooser.setLocalDateForTimezone(dC.getTime(), tC.getTime());
        };

        initComponents(zone, refreshTimezoneLabels, listener);
        refreshTimezoneLabels.run();

        if (autoShow) {
            setVisible(true);
        }
    }
    
    private void initComponents(final java.util.TimeZone zone, final Runnable refreshTimezoneLabels, final ConfigListener listener) {
        dlgPanel.setLayout(new BorderLayout());
        
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        inputPanel.setBackground(UIConsts.THEME_CLR);
        
        Font labelFont = UIUtil.getFont();
        if (labelFont == null) labelFont = new Font("SansSerif", Font.PLAIN, 12);
        Font boldFont = labelFont.deriveFont(Font.BOLD);
        Color textColor = UIConsts.getChartTheme().getPlanetForeground();
        Border fieldBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(190, 195, 200), 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        );

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        datePanel.setBackground(UIConsts.THEME_CLR);
        JLabel dateLabel = new JLabel("Date: ");
        dateLabel.setFont(boldFont);
        dateLabel.setForeground(textColor);
        dateLabel.setPreferredSize(new Dimension(50, 26));
        datePanel.add(dateLabel);

        dateButton = new JButton();
        dateButton.setFont(labelFont);
        dateButton.setBackground(Color.WHITE);
        dateButton.setForeground(textColor);
        dateButton.setOpaque(true);
        dateButton.setBorder(fieldBorder);
        dateButton.setHorizontalAlignment(SwingConstants.LEFT);
        dateButton.setPreferredSize(new Dimension(220, 28));
        dateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dateButton.setFocusPainted(false);
        dateButton.addActionListener(e -> {
            LocalDate picked = DatePickerDialog.showDatePicker(this, selectedDate);
            if (picked != null) {
                selectedDate = picked;
                updateDateButtonText();
                refreshTimezoneLabels.run();
            }
        });
        updateDateButtonText();
        datePanel.add(dateButton);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        timePanel.setBackground(UIConsts.THEME_CLR);
        JLabel timeLabel = new JLabel("Time: ");
        timeLabel.setFont(boldFont);
        timeLabel.setForeground(textColor);
        timeLabel.setPreferredSize(new Dimension(50, 26));
        timePanel.add(timeLabel);

        timeButton = new JButton();
        timeButton.setFont(labelFont);
        timeButton.setBackground(Color.WHITE);
        timeButton.setForeground(textColor);
        timeButton.setOpaque(true);
        timeButton.setBorder(fieldBorder);
        timeButton.setHorizontalAlignment(SwingConstants.LEFT);
        timeButton.setPreferredSize(new Dimension(220, 28));
        timeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        timeButton.setFocusPainted(false);
        timeButton.addActionListener(e -> {
            LocalTime picked = TimePickerDialog.showTimePicker(this, selectedTime);
            if (picked != null) {
                selectedTime = picked;
                updateTimeButtonText();
                refreshTimezoneLabels.run();
            }
        });
        updateTimeButtonText();
        timePanel.add(timeButton);

        inputPanel.add(datePanel);
        inputPanel.add(timePanel);
        inputPanel.add(placeChooser);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonPanel.setBackground(UIConsts.THEME_CLR);

        okButton = ModernButton.createPrimary("OK", UIConsts.getPrimaryAccentColor());
        okButton.setPreferredSize(new Dimension(85, 30));

        cancelButton = ModernButton.createSecondary("Cancel");
        cancelButton.setPreferredSize(new Dimension(85, 30));

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dlgPanel.add(inputPanel, BorderLayout.CENTER);
        dlgPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(dlgPanel);
        setBackground(UIConsts.THEME_CLR);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Place place = placeChooser.getSelectedPlace();
                java.util.TimeZone selectedZone = place == null ? zone : place.astrosoftTimeZone().getTimeZone();
                try {
                    Calendar dC = Calendar.getInstance();
                    dC.clear();
                    dC.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
                    Calendar tC = Calendar.getInstance();
                    tC.clear();
                    tC.set(Calendar.HOUR_OF_DAY, selectedTime.getHour());
                    tC.set(Calendar.MINUTE, selectedTime.getMinute());
                    tC.set(Calendar.SECOND, selectedTime.getSecond());
                    Date instant = app.astrosoft.util.MuhurtaDateTime.fromChooserValues(
                        dC.getTime(), tC.getTime(), selectedZone);
                    listener.configChanged(instant, instant, place);
                } catch (IllegalArgumentException invalidTime) {
                    OptionDialog.showMessageDialog(MuhurtaConfigDialog.this,
                        "This local date/time does not exist in the selected timezone. Choose another time.",
                        "Invalid local time", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
                closeDialog();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        });
    }

    public void updateDateButtonText() {
        if (dateButton != null && selectedDate != null) {
            dateButton.setText("📅  " + selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        }
    }

    public void updateTimeButtonText() {
        if (timeButton != null && selectedTime != null) {
            timeButton.setText("🕒  " + selectedTime.format(DateTimeFormatter.ofPattern("hh : mm : ss a")));
        }
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        if (date != null) {
            this.selectedDate = date;
            updateDateButtonText();
        }
    }

    public LocalTime getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(LocalTime time) {
        if (time != null) {
            this.selectedTime = time;
            updateTimeButtonText();
        }
    }

    public JButton getDateButton() {
        return dateButton;
    }

    public JButton getTimeButton() {
        return timeButton;
    }

    public PlaceChooser getPlaceChooser() {
        return placeChooser;
    }

    public void showDialog() {
        setVisible(true);
    }
}
