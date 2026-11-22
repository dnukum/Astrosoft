package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.time.LocalTime;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import app.astrosoft.beans.Place;
import app.astrosoft.ui.comp.modern.ModernButton;
import app.astrosoft.ui.comp.modern.PlaceSearchPanel;
import app.astrosoft.ui.comp.modern.TimePickerPanel;
import app.astrosoft.ui.util.UIConsts;

/**
 * Modal dialog for configuring session-only Ephemeris calculation time and location.
 * Embeds {@link TimePickerPanel} and {@link PlaceSearchPanel} side-by-side.
 */
public class EphemerisConfigDialog extends JDialog {

    /** Result returned when the user clicks Apply. */
    public static class Result {
        private final LocalTime time;
        private final Place place;

        public Result(LocalTime time, Place place) {
            this.time = time;
            this.place = place;
        }

        public LocalTime getTime() {
            return time;
        }

        public Place getPlace() {
            return place;
        }
    }

    private final TimePickerPanel timePickerPanel;
    private final PlaceSearchPanel placeSearchPanel;
    private boolean confirmed = false;

    public EphemerisConfigDialog(Window owner, LocalTime initialTime, Place initialPlace) {
        super(owner, "Ephemeris Time & Location", ModalityType.APPLICATION_MODAL);

        LocalTime initT = (initialTime != null) ? initialTime.withSecond(0).withNano(0) : LocalTime.MIDNIGHT;
        Place initP = (initialPlace != null) ? initialPlace : Place.getDefault();

        timePickerPanel = new TimePickerPanel(initT);
        timePickerPanel.setEmbeddedMode(true);
        timePickerPanel.setPreferredSize(new Dimension(310, 360));

        placeSearchPanel = new PlaceSearchPanel(initP);
        placeSearchPanel.setEmbeddedMode(true);
        placeSearchPanel.setPreferredSize(new Dimension(470, 360));
        placeSearchPanel.setOnConfirmListener(() -> {
            confirmed = true;
            dispose();
        });

        JPanel content = new JPanel(new BorderLayout(10, 0));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        content.add(timePickerPanel, BorderLayout.WEST);

        JPanel centerWrap = new JPanel(new BorderLayout(10, 0));
        centerWrap.setBackground(Color.WHITE);
        centerWrap.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);
        centerWrap.add(placeSearchPanel, BorderLayout.CENTER);
        content.add(centerWrap, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 225, 225)));

        ModernButton cancelBtn = ModernButton.createSecondary("Cancel");
        cancelBtn.setPreferredSize(new Dimension(88, 32));
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        Color accent = UIConsts.getChartTheme().getPrimaryAccentColor();
        ModernButton applyBtn = ModernButton.createPrimary("Apply", accent);
        applyBtn.setPreferredSize(new Dimension(88, 32));
        applyBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        footer.add(cancelBtn);
        footer.add(applyBtn);

        setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(applyBtn);
        setPreferredSize(new Dimension(820, 440));
        pack();
        setResizable(false);
        setAlwaysOnTop(true);
        if (owner != null) {
            setLocationRelativeTo(owner);
        }
    }

    /**
     * Shows the modal dialog and returns the selected time &amp; place, or {@code null} if cancelled
     * or running in headless/unattended test mode.
     */
    public static Result showConfigDialog(Component parent, LocalTime initialTime, Place initialPlace) {
        if (GraphicsEnvironment.isHeadless() || Boolean.getBoolean("astrosoft.test.unattended")) {
            return null;
        }
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        EphemerisConfigDialog dlg = new EphemerisConfigDialog(owner, initialTime, initialPlace);
        SwingUtilities.invokeLater(() -> {
            dlg.toFront();
            dlg.requestFocus();
        });
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) {
            return null;
        }
        return new Result(dlg.getSelectedTime(), dlg.getSelectedPlace());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public LocalTime getSelectedTime() {
        return timePickerPanel.getSelectedTime().withSecond(0).withNano(0);
    }

    public Place getSelectedPlace() {
        return placeSearchPanel.getSelectedPlace();
    }

    public TimePickerPanel getTimePickerPanel() {
        return timePickerPanel;
    }

    public PlaceSearchPanel getPlaceSearchPanel() {
        return placeSearchPanel;
    }
}
