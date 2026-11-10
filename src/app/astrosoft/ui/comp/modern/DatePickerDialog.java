package app.astrosoft.ui.comp.modern;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.time.LocalDate;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * Modern Material-inspired Date Picker Dialog.
 * Delegates layout and logic to DatePickerPanel and displays as a modal dialog.
 */
public class DatePickerDialog extends JDialog {

    public static final int MIN_YEAR = DatePickerPanel.MIN_YEAR;
    public static final int MAX_YEAR = DatePickerPanel.MAX_YEAR;

    private final DatePickerPanel panel;

    public DatePickerDialog(Window owner, LocalDate initialDate) {
        super(owner, "Select Date", ModalityType.APPLICATION_MODAL);
        this.panel = new DatePickerPanel(initialDate);

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

    public static LocalDate showDatePicker(Component parent, LocalDate initialDate) {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return initialDate;
        }
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        DatePickerDialog dlg = new DatePickerDialog(owner, initialDate);
        SwingUtilities.invokeLater(() -> {
            dlg.toFront();
            dlg.requestFocus();
        });
        dlg.setVisible(true);
        return dlg.isConfirmed() ? dlg.getSelectedDate() : null;
    }

    public boolean isConfirmed() {
        return panel.isConfirmed();
    }

    public LocalDate getSelectedDate() {
        return panel.getSelectedDate();
    }

    public void setSelectedDate(LocalDate date) {
        panel.setSelectedDate(date);
    }

    public DatePickerPanel getPanel() {
        return panel;
    }
}
