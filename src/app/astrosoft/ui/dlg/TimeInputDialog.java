/**
 * TimeInputDialog.java
 * Created On 2006, Apr 11, 2006 6:29:09 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.DateListener;
import app.astrosoft.ui.comp.modern.TimePickerPanel;
import app.astrosoft.ui.util.UIConsts;

/**
 * Modern Time Input Dialog using TimePickerPanel.
 * Replaces manual typing of time with scrollable Hour, Min, Sec, AM/PM lists.
 */
public class TimeInputDialog extends AstrosoftDialog {

	private static final Dimension DLG_SIZE = new Dimension(335, 415);

	private TimePickerPanel timePickerPanel;

	public TimeInputDialog(Enum title, AstroSoft parent, Date initialValue, final DateListener listener) {
		this(title, parent, initialValue, listener, true);
	}

	public TimeInputDialog(Enum title, AstroSoft parent, Date initialValue, final DateListener listener, boolean autoShow) {
		super(parent, title.toString(), DLG_SIZE);
		setModal(true);
		initComponents(initialValue, listener);
		if (autoShow) {
			setVisible(true);
		}
	}

	private void initComponents(final Date initialValue, final DateListener listener) {
		LocalTime initTime;
		if (initialValue != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(initialValue);
			initTime = LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
		} else {
			initTime = LocalTime.now();
		}

		timePickerPanel = new TimePickerPanel(initTime);
		timePickerPanel.setOnConfirmListener(() -> {
			LocalTime selected = timePickerPanel.getSelectedTime();
			Calendar cal = Calendar.getInstance();
			if (initialValue != null) {
				cal.setTime(initialValue);
			}
			cal.set(Calendar.HOUR_OF_DAY, selected.getHour());
			cal.set(Calendar.MINUTE, selected.getMinute());
			cal.set(Calendar.SECOND, selected.getSecond());
			cal.set(Calendar.MILLISECOND, 0);
			if (listener != null) {
				listener.dateChanged(cal.getTime());
			}
			closeDialog();
		});

		timePickerPanel.setOnCancelListener(this::closeDialog);

		dlgPanel.setLayout(new BorderLayout());
		dlgPanel.add(timePickerPanel, BorderLayout.CENTER);
		add(dlgPanel);
		setBackground(UIConsts.THEME_CLR);
		setResizable(false);
	}

	public TimePickerPanel getTimePickerPanel() {
		return timePickerPanel;
	}

	public LocalTime getSelectedTime() {
		return timePickerPanel != null ? timePickerPanel.getSelectedTime() : null;
	}

	public void setSelectedTime(LocalTime time) {
		if (timePickerPanel != null && time != null) {
			timePickerPanel.setSelectedTime(time);
		}
	}

	public Date getSelectedDate() {
		LocalTime selected = getSelectedTime();
		if (selected == null) return null;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, selected.getHour());
		cal.set(Calendar.MINUTE, selected.getMinute());
		cal.set(Calendar.SECOND, selected.getSecond());
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
