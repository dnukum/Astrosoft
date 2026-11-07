package app.astrosoft.ui.comp.modern;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.time.LocalTime;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * Modern Material-inspired Time Picker Dialog.
 * Delegates layout and logic to TimePickerPanel and displays as a modal dialog.
 */
public class TimePickerDialog extends JDialog {

    private final TimePickerPanel panel;

    public TimePickerDialog(Window owner, LocalTime initialTime) {
        super(owner, "Select Time", ModalityType.APPLICATION_MODAL);
        this.panel = new TimePickerPanel(initialTime);

        panel.setOnConfirmListener(this::dispose);
        panel.setOnCancelListener(this::dispose);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        pack();
        setResizable(false);
        setAlwaysOnTop(true);
        if (owner != null) {
            setLocationRelativeTo(owner);
        }
    }

    public static LocalTime showTimePicker(Component parent, LocalTime initialTime) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        TimePickerDialog dlg = new TimePickerDialog(owner, initialTime);
        SwingUtilities.invokeLater(() -> {
            dlg.toFront();
            dlg.requestFocus();
        });
        dlg.setVisible(true);
        return dlg.isConfirmed() ? dlg.getSelectedTime() : null;
    }

    public boolean isConfirmed() {
        return panel.isConfirmed();
    }

    public LocalTime getSelectedTime() {
        return panel.getSelectedTime();
    }

    public void setSelectedTime(LocalTime time) {
        panel.setSelectedTime(time);
    }

    public TimePickerPanel getPanel() {
        return panel;
    }
}
