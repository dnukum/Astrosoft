package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;

import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.CalendarChooser;
import app.astrosoft.ui.comp.PlaceChooser;
import app.astrosoft.beans.Place;
import app.astrosoft.ui.util.UIConsts;

public class MuhurtaConfigDialog extends AstrosoftDialog {
    
    private static final Dimension dlgSize = new Dimension(500, 350);
    
    private CalendarChooser dateChooser;
    private CalendarChooser timeChooser;
    private PlaceChooser placeChooser;
    
    private JButton okButton = new JButton("OK");
    private JButton cancelButton = new JButton("Cancel");
    
    public interface ConfigListener {
        void configChanged(Date date, Date time, Place place);
    }
    
    public MuhurtaConfigDialog(AstroSoft parent, Date initDate, Date initTime, Place initPlace, final ConfigListener listener) {
        super(parent, "New Muhurta", dlgSize);
        setModal(true); // Ensure it blocks interactions
        
        final java.util.TimeZone zone = initPlace == null ? java.util.TimeZone.getDefault()
            : initPlace.astrosoftTimeZone().getTimeZone();
        dateChooser = CalendarChooser.getDateChooser();
        JPanel dateChooserPanel = dateChooser.getChooser();
        dateChooser.setSelectedDate(app.astrosoft.util.MuhurtaDateTime.toChooserDate(initDate, zone));
        
        timeChooser = CalendarChooser.getTimeChooser();
        JPanel timeChooserPanel = timeChooser.getChooser();
        timeChooser.setSelectedDate(app.astrosoft.util.MuhurtaDateTime.toChooserDate(initTime, zone));
        
        placeChooser = new PlaceChooser(new Dimension(400, 140), false);
        placeChooser.addPlaces(java.util.Collections.singletonList(initPlace));
        placeChooser.setSelectedPlace(initPlace);
        final Runnable refreshTimezoneLabels = () -> placeChooser.setLocalDateForTimezone(
            dateChooser.getSelectedDate(), timeChooser.getSelectedDate());
        dateChooser.addDateListener(date -> refreshTimezoneLabels.run());
        timeChooser.addDateListener(time -> refreshTimezoneLabels.run());
        refreshTimezoneLabels.run();

        
        dlgPanel.setLayout(new BorderLayout());
        
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new javax.swing.JLabel("Date: "));
        datePanel.add(dateChooserPanel);
        
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new javax.swing.JLabel("Time: "));
        timePanel.add(timeChooserPanel);
        
        inputPanel.add(datePanel);
        inputPanel.add(timePanel);
        inputPanel.add(placeChooser);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
                    Date instant = app.astrosoft.util.MuhurtaDateTime.fromChooserValues(
                        dateChooser.getSelectedDate(), timeChooser.getSelectedDate(), selectedZone);
                    listener.configChanged(instant, instant, place);
                } catch (IllegalArgumentException invalidTime) {
                    javax.swing.JOptionPane.showMessageDialog(MuhurtaConfigDialog.this,
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
    
    public void showDialog() {
        setVisible(true);
    }
}
