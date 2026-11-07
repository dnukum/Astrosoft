package app.astrosoft.ui.comp.modern;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import app.astrosoft.beans.Place;

/**
 * Modern Searchable Place Picker Dialog.
 * Delegates layout and logic to PlaceSearchPanel and displays as a modal dialog.
 */
public class PlaceSearchDialog extends JDialog {

    private final PlaceSearchPanel panel;

    public PlaceSearchDialog(Window owner, Place initialPlace) {
        super(owner, "Select Place", ModalityType.APPLICATION_MODAL);
        this.panel = new PlaceSearchPanel(initialPlace);

        panel.setOnConfirmListener(this::dispose);
        panel.setOnCancelListener(this::dispose);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        pack();
        setMinimumSize(new Dimension(580, 420));
        setAlwaysOnTop(true);
        if (owner != null) {
            setLocationRelativeTo(owner);
        }
    }

    public static Place showPlacePicker(Component parent, Place initialPlace) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        PlaceSearchDialog dlg = new PlaceSearchDialog(owner, initialPlace);
        SwingUtilities.invokeLater(() -> {
            dlg.toFront();
            dlg.requestFocus();
        });
        dlg.setVisible(true);
        return dlg.isConfirmed() ? dlg.getSelectedPlace() : null;
    }

    public boolean isConfirmed() {
        return panel.isConfirmed();
    }

    public Place getSelectedPlace() {
        return panel.getSelectedPlace();
    }

    public void setSelectedPlace(Place place) {
        panel.setSelectedPlace(place);
    }

    public PlaceSearchPanel getPanel() {
        return panel;
    }
}
