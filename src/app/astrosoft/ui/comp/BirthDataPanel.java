/**
 * BirthDataPanel.java
 * Created On 2006, May 27, 2006 1:12:00 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.comp;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Command;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Language;
import app.astrosoft.ui.comp.modern.DatePickerDialog;
import app.astrosoft.ui.comp.modern.TimePickerDialog;
import app.astrosoft.ui.util.SpringUtilities;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.AstrosoftFileFilter;
import app.astrosoft.util.AstroUtil;
import app.astrosoft.util.FileOps;
import app.astrosoft.export.XMLHelper;

public class BirthDataPanel extends JPanel {
	
	private static final Logger log = Logger.getLogger(BirthDataPanel.class.getName());
	
	private JLabel l_name = new JLabel(DisplayStrings.NAME_STR.toString(Language.ENGLISH));
	private JLabel l_date = new JLabel(DisplayStrings.DATE_STR.toString(Language.ENGLISH));
	private JLabel l_time = new JLabel(DisplayStrings.BIRTH_TIME_STR.toString(Language.ENGLISH));

	private JTextField name = new JTextField();
	private JButton openButton = new JButton(UIUtil.createImageIcon(Command.OPEN.name()));
	
	private LocalDate selectedDate = LocalDate.now();
	private LocalTime selectedTime = LocalTime.now();
	private JButton dateButton;
	private JButton timeButton;

	private PlaceChooser placeChooser;
	
	private Dimension panelSize;
	private String title;
	private String notes;
	
	private Worker w = new Worker();
	
	private class Worker extends SwingWorker<PlaceChooser, Void>{
		
		@Override
        public PlaceChooser doInBackground() {
			try {
				PlaceChooser pc = new PlaceChooser(new Dimension(panelSize.width - 60, panelSize.height - (int)(panelSize.height * 0.50)), false);
				return pc;
			} catch (Exception e){
				log.log(Level.SEVERE, "Exception in constructing BirthDataPanel " , e);
			}
			return null;
		}
	
		@Override
		protected void done() {}
	}
	
	public BirthDataPanel(Dimension size, String title){
		this.panelSize = size;
		this.title = title;
		
		w.execute();
		
		openButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				browseHoroscope();
			}
		});
		
		addComponents();
		setPreferredSize(panelSize);
	}

	private void addComponents() {
		setLayout(new SpringLayout());
		
		JPanel birthPanel = new JPanel(new SpringLayout());
		
		JPanel namePanel = new JPanel(new SpringLayout());
		namePanel.add(name);
		namePanel.add(openButton);
		openButton.setPreferredSize(UIConsts.BUTTON_ICON_SIZE);
		SpringUtilities.makeCompactGrid(namePanel, 1, 2, 0, 0, 1, 1);

		Font labelFont = UIUtil.getFont();
		if (labelFont == null) labelFont = new Font("SansSerif", Font.PLAIN, 12);
		Color textColor = UIConsts.getChartTheme().getPlanetForeground();
		Border fieldBorder = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(190, 195, 200), 1),
			BorderFactory.createEmptyBorder(2, 6, 2, 6)
		);

		dateButton = new JButton();
		dateButton.setFont(labelFont);
		dateButton.setBackground(Color.WHITE);
		dateButton.setForeground(textColor);
		dateButton.setOpaque(true);
		dateButton.setBorder(fieldBorder);
		dateButton.setHorizontalAlignment(SwingConstants.LEFT);
		dateButton.setPreferredSize(new Dimension(170, 24));
		dateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		dateButton.setFocusPainted(false);
		dateButton.addActionListener(e -> {
			LocalDate picked = DatePickerDialog.showDatePicker(this, selectedDate);
			if (picked != null) {
				selectedDate = picked;
				updateDateButtonText();
				if (placeChooser != null) {
					placeChooser.setDateForTimezone(getBirthTime());
				}
			}
		});

		timeButton = new JButton();
		timeButton.setFont(labelFont);
		timeButton.setBackground(Color.WHITE);
		timeButton.setForeground(textColor);
		timeButton.setOpaque(true);
		timeButton.setBorder(fieldBorder);
		timeButton.setHorizontalAlignment(SwingConstants.LEFT);
		timeButton.setPreferredSize(new Dimension(170, 24));
		timeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		timeButton.setFocusPainted(false);
		timeButton.addActionListener(e -> {
			LocalTime picked = TimePickerDialog.showTimePicker(this, selectedTime);
			if (picked != null) {
				selectedTime = picked;
				updateTimeButtonText();
				if (placeChooser != null) {
					placeChooser.setDateForTimezone(getBirthTime());
				}
			}
		});

		updateDateButtonText();
		updateTimeButtonText();
		
		birthPanel.add(l_name);
		birthPanel.add(namePanel);
		
		birthPanel.add(l_date);
		birthPanel.add(dateButton);

		birthPanel.add(l_time);
		birthPanel.add(timeButton);
		
		SpringUtilities.makeCompactGrid(birthPanel, 3, 2, 5, 5, 10, 15);
		
		add(birthPanel);
		
		try {
			placeChooser = w.get();
		} catch(Exception e){
			log.log(Level.SEVERE, "Exception in constructing BirthDataPanel " , e);
		}
		
		if (placeChooser != null) {
			add(placeChooser);
		}
		
		new javax.swing.Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (placeChooser != null) {
					try {
						placeChooser.setDateForTimezone(getBirthTime());
					} catch (Exception ex) {}
				}
			}
		}).start();
		
		SpringUtilities.makeCompactGrid(this, 2, 1, 5, 5, 0, 0);
		
		setBorder(UIConsts.getTitleBorder(title));
	}
	
	private void updateDateButtonText() {
		if (dateButton != null && selectedDate != null) {
			dateButton.setText("📅  " + selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
		}
	}

	private void updateTimeButtonText() {
		if (timeButton != null && selectedTime != null) {
			timeButton.setText("🕒  " + selectedTime.format(DateTimeFormatter.ofPattern("hh : mm : ss a")));
		}
	}

	public String getPersonName() {
		return name.getText();
	}
	
	public Place getBirthPlace(){
		return placeChooser != null ? placeChooser.getSelectedPlace() : null;
	}
	
	public Calendar getBirthTime(){
		Calendar birthCal = Calendar.getInstance();
		birthCal.set(
			selectedDate.getYear(),
			selectedDate.getMonthValue() - 1,
			selectedDate.getDayOfMonth(),
			selectedTime.getHour(),
			selectedTime.getMinute(),
			selectedTime.getSecond()
		);
		birthCal.set(Calendar.MILLISECOND, 0);
		return birthCal;
	}
	
	public BirthData getBirthData(){
		BirthData bd = new BirthData(getPersonName(), getBirthTime(), getBirthPlace());
		if (notes != null) {
			bd.setNotes(notes);
		}
		return bd;
	}
	
	public void setPersonName(String personName){
		name.setText(personName);
	}
	
	public void setBirthPlace(Place place){
		if (placeChooser != null) {
			placeChooser.setSelectedPlace(place);
		}
	}
	
	public void setBirthTime(Calendar cal){
		if (cal != null) {
			this.selectedDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
			this.selectedTime = LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
			updateDateButtonText();
			updateTimeButtonText();
			if (placeChooser != null) {
				placeChooser.setDateForTimezone(getBirthTime());
			}
		}
	}
	
	public void setBirthData(BirthData birthData){
		if (birthData != null) {
			setPersonName(birthData.name());
			setBirthTime(birthData.birthDay());
			setBirthPlace(birthData.getBirthPlace());
			this.notes = birthData.getNotes();
		}
	}

	public LocalDate getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(LocalDate d) {
		if (d != null) {
			this.selectedDate = d;
			updateDateButtonText();
			if (placeChooser != null) {
				placeChooser.setDateForTimezone(getBirthTime());
			}
		}
	}

	public LocalTime getSelectedTime() {
		return selectedTime;
	}

	public void setSelectedTime(LocalTime t) {
		if (t != null) {
			this.selectedTime = t;
			updateTimeButtonText();
			if (placeChooser != null) {
				placeChooser.setDateForTimezone(getBirthTime());
			}
		}
	}

	public JButton getDateButton() {
		return dateButton;
	}

	public JButton getTimeButton() {
		return timeButton;
	}
	
	private void browseHoroscope(){
		String file = FileOps.openFileDialog(this, FileOps.FileDialogMode.OPEN, AstrosoftFileFilter.HOROSCOPE_EXTN);
		if (file != null) {
			setBirthData(BirthData.valueOfXMLNode(XMLHelper.parseXML(file).getChildNodes().item(0)));
		}
	}
}
