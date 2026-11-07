package app.astrosoft.ui.comp.modern;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * ModernScrollBarUI
 *
 * Clean, high-contrast, anti-aliased rounded-pill scrollbar UI.
 * Replaces dated early-2000s beveled arrows and washed-out tracks with a sleek,
 * distinct modern aesthetic inspired by modern desktop editors.
 */
public class ModernScrollBarUI extends BasicScrollBarUI {

    public static final int BASE_SCROLLBAR_WIDTH = 14;
    private static final int MIN_THUMB_SIZE = 28;

    // High-contrast slate color palette
    private static final Color THUMB_NORMAL = new Color(148, 163, 184, 210);   // Slate 400
    private static final Color THUMB_HOVER = new Color(100, 116, 139, 240);    // Slate 500
    private static final Color THUMB_ACTIVE = new Color(71, 85, 105, 255);     // Slate 600
    private static final Color TRACK_COLOR = new Color(248, 250, 252, 140);     // Subtle slate tint

    public static ComponentUI createUI(JComponent c) {
        return new ModernScrollBarUI();
    }

    /**
     * Applies this modern scrollbar UI to both vertical and horizontal scrollbars
     * of a target JScrollPane, configuring unit increments and transparent backgrounds.
     */
    public static void apply(JScrollPane scrollPane) {
        if (scrollPane == null) return;

        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        if (vBar != null) {
            vBar.setUI(new ModernScrollBarUI());
            vBar.setUnitIncrement(16);
            vBar.setBlockIncrement(64);
            vBar.setOpaque(false);
        }

        JScrollBar hBar = scrollPane.getHorizontalScrollBar();
        if (hBar != null) {
            hBar.setUI(new ModernScrollBarUI());
            hBar.setUnitIncrement(16);
            hBar.setBlockIncrement(64);
            hBar.setOpaque(false);
        }
    }

    public static int getDynamicWidth(JComponent c) {
        if (c != null && c.getGraphicsConfiguration() != null) {
            java.awt.geom.AffineTransform tx = c.getGraphicsConfiguration().getDefaultTransform();
            if (tx != null) {
                double scale = tx.getScaleX();
                // Ensure proper logical width across fractional DPI scaling
                if (scale > 1.0 && !System.getProperty("os.name", "").toLowerCase().contains("mac")) {
                    return (int) Math.round(BASE_SCROLLBAR_WIDTH * Math.min(1.5, scale));
                }
            }
        }
        return BASE_SCROLLBAR_WIDTH;
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        return new Dimension(MIN_THUMB_SIZE, MIN_THUMB_SIZE);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        int width = getDynamicWidth(c);
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            return new Dimension(width, super.getPreferredSize(c).height);
        } else {
            return new Dimension(super.getPreferredSize(c).width, width);
        }
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private static JButton createZeroButton() {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(0, 0));
        btn.setMinimumSize(new Dimension(0, 0));
        btn.setMaximumSize(new Dimension(0, 0));
        btn.setFocusable(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(null);
        return btn;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(TRACK_COLOR);
        g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color thumbColor;
        if (isDragging) {
            thumbColor = THUMB_ACTIVE;
        } else if (isThumbRollover()) {
            thumbColor = THUMB_HOVER;
        } else {
            thumbColor = THUMB_NORMAL;
        }

        g2.setColor(thumbColor);

        // Inset pill by 2px on all sides for a floating, polished appearance
        int inset = 2;
        int x = thumbBounds.x + inset;
        int y = thumbBounds.y + inset;
        int w = Math.max(4, thumbBounds.width - (inset * 2));
        int h = Math.max(4, thumbBounds.height - (inset * 2));
        int arc = Math.min(w, h);

        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.dispose();
    }
}
