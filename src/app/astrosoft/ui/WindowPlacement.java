package app.astrosoft.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.Timer;
import app.astrosoft.pref.AstrosoftPref;

/** Main-window presentation policy. No astrology or domain beans involved. */
public final class WindowPlacement {
    private static final Logger LOG = Logger.getLogger(WindowPlacement.class.getName());
    private final JFrame frame;
    private final Preferences preferences;
    private Rectangle normalBounds;
    private final Timer saveTimer;

    private WindowPlacement(JFrame frame) {
        this.frame = frame;
        preferences = Preferences.systemNodeForPackage(AstrosoftPref.class).node("MainWindow");
        saveTimer = new Timer(500, event -> save());
        saveTimer.setRepeats(false);
    }

    /** Logical pixels, outer window bounds; left/top margin is constrained on tiny displays. */
    public static Rectangle defaults(Rectangle usable) {
        int width = Math.max(1, Math.min(1400, (int)Math.round(usable.width * 0.9)));
        int height = Math.max(1, Math.min(900, (int)Math.round(usable.height * 0.9)));
        width = Math.min(width, usable.width);
        height = Math.min(height, usable.height);
        return new Rectangle(usable.x + Math.min(20, usable.width - width),
            usable.y + Math.min(20, usable.height - height), width, height);
    }

    public static Rectangle restore(Rectangle saved, String savedDisplay, String currentDisplay, Rectangle usable) {
        if (saved == null || !currentDisplay.equals(savedDisplay) || saved.width <= 0
                || saved.height <= 0 || !usable.contains(saved)) return defaults(usable);
        return new Rectangle(saved);
    }

    private static Rectangle usable(GraphicsConfiguration config) {
        Rectangle b = new Rectangle(config.getBounds());
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
        b.x += insets.left; b.y += insets.top;
        b.width -= insets.left + insets.right;
        b.height -= insets.top + insets.bottom;
        return b;
    }

    /** Detect monitor topology, usable dimensions, origin and scaling changes. */
    private static String displaySignature() {
        java.util.List<String> displays = new ArrayList<>();
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String primary = environment.getDefaultScreenDevice().getIDstring();
        for (GraphicsDevice device : environment.getScreenDevices()) {
            GraphicsConfiguration config = device.getDefaultConfiguration();
            Rectangle b = usable(config);
            displays.add(device.getIDstring() + ":" + b.x + "," + b.y + "," + b.width + "," + b.height
                + ":" + config.getDefaultTransform().toString());
        }
        Collections.sort(displays);
        return primary + "|" + String.join("|", displays);
    }

    /** Called after pack and before showing the main frame. */
    public static void install(JFrame frame) {
        WindowPlacement placement = new WindowPlacement(frame);
        placement.restoreWindow();
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent event) { placement.changed(); }
            @Override public void componentResized(ComponentEvent event) { placement.changed(); }
        });
        frame.addWindowStateListener(event -> placement.saveTimer.restart());
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                placement.saveTimer.stop();
                placement.save();
            }
        });
        // exitForm explicitly calls saveNow before System.exit; listener ordering must not matter.
        frame.getRootPane().putClientProperty("astrosoft.windowPlacement", placement);
    }

    public static void saveNow(JFrame frame) {
        Object value = frame.getRootPane().getClientProperty("astrosoft.windowPlacement");
        if (value instanceof WindowPlacement) {
            WindowPlacement placement = (WindowPlacement)value;
            placement.saveTimer.stop();
            placement.save();
        }
    }

    private void restoreWindow() {
        GraphicsConfiguration target = frame.getGraphicsConfiguration();
        Rectangle saved = null;
        boolean sameDisplay = false;
        boolean maximized = false;
        try {
            sameDisplay = displaySignature().equals(preferences.get("display", ""));
            if (sameDisplay && preferences.getBoolean("saved", false)) {
                saved = new Rectangle(preferences.getInt("x", 0), preferences.getInt("y", 0),
                    preferences.getInt("width", 0), preferences.getInt("height", 0));
                for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                    GraphicsConfiguration config = device.getDefaultConfiguration();
                    if (usable(config).contains(saved)) { target = config; break; }
                }
                maximized = preferences.getBoolean("maximized", false);
            }
        } catch (RuntimeException ex) {
            LOG.warning("Cannot restore window preferences: " + ex.getMessage());
        }
        Rectangle available = usable(target);
        boolean valid = sameDisplay && saved != null && saved.width > 0 && saved.height > 0 && available.contains(saved);
        normalBounds = valid ? new Rectangle(saved) : defaults(available);
        frame.setBounds(normalBounds);
        if (valid && maximized) frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    private void changed() {
        if (frame.isShowing() && frame.getExtendedState() == Frame.NORMAL) normalBounds = frame.getBounds();
        saveTimer.restart();
    }

    private void save() {
        if (normalBounds == null) return;
        try {
            if (frame.isShowing() && frame.getExtendedState() == Frame.NORMAL) normalBounds = frame.getBounds();
            preferences.putInt("x", normalBounds.x); preferences.putInt("y", normalBounds.y);
            preferences.putInt("width", normalBounds.width); preferences.putInt("height", normalBounds.height);
            preferences.putBoolean("maximized", (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH);
            preferences.put("display", displaySignature());
            preferences.putBoolean("saved", true);
            preferences.flush();
        } catch (Exception ex) {
            LOG.warning("Cannot save window preferences: " + ex.getMessage());
        }
    }
}
