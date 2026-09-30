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
        
        dateChooser = CalendarChooser.getDateChooser();
        dateChooser.getChooser(); // init
        dateChooser.setSelectedDate(initDate);
        
        timeChooser = CalendarChooser.getTimeChooser();
        timeChooser.getChooser(); // init
        timeChooser.setSelectedDate(initTime);
        
        placeChooser = new PlaceChooser(new Dimension(400, 140), false);
        placeChooser.addPlaces(java.util.Collections.singletonList(initPlace));
        placeChooser.setSelectedPlace(initPlace);
        
        dlgPanel.setLayout(new BorderLayout());
        
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new javax.swing.JLabel("Date: "));
        datePanel.add(dateChooser.getChooser());
        
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new javax.swing.JLabel("Time: "));
        timePanel.add(timeChooser.getChooser());
        
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
                listener.configChanged(dateChooser.getSelectedDate(), timeChooser.getSelectedDate(), placeChooser.getSelectedPlace());
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
