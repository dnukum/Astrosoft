package app.astrosoft.ui.comp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** Manual calendar sizing retained across recalculation and clamped to its host. */
public final class CalendarResizeHandles {
    private static final String SIZE_KEY = "balam.manualSize";
    private CalendarResizeHandles() {}

    public static Dimension fit(JPanel panel, int availableWidth, int availableHeight) {
        Dimension requested = (Dimension) panel.getClientProperty(SIZE_KEY);
        int width = requested == null ? availableWidth : requested.width;
        int height = requested == null ? 360 : requested.height;
        return clamp(new Dimension(width, height), availableWidth, availableHeight);
    }

    public static Dimension clamp(Dimension requested, int availableWidth, int availableHeight) {
        return new Dimension(Math.min(Math.max(0, availableWidth), Math.max(400, requested.width)),
            Math.min(Math.max(0, availableHeight), Math.max(200, requested.height)));
    }

    public static void install(final JPanel panel) {
        panel.add(handle(panel, true, false, Cursor.E_RESIZE_CURSOR), BorderLayout.EAST);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(handle(panel, false, true, Cursor.S_RESIZE_CURSOR), BorderLayout.CENTER);
        bottom.add(handle(panel, true, true, Cursor.SE_RESIZE_CURSOR), BorderLayout.EAST);
        panel.add(bottom, BorderLayout.SOUTH);
    }

    private static JComponent handle(final JPanel panel, final boolean horizontal,
            final boolean vertical, int cursor) {
        JPanel grip = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.GRAY);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                if (horizontal && vertical) {
                    for (int i = 2; i <= 6; i += 2) g.drawLine(i, 7, 7, i);
                } else if (horizontal) {
                    g.drawLine(cx - 1, cy - 8, cx - 1, cy + 8);
                    g.drawLine(cx + 1, cy - 8, cx + 1, cy + 8);
                } else {
                    g.drawLine(cx - 8, cy - 1, cx + 8, cy - 1);
                    g.drawLine(cx - 8, cy + 1, cx + 8, cy + 1);
                }
            }
        };
        grip.setPreferredSize(new Dimension(10, 10));
        grip.setCursor(Cursor.getPredefinedCursor(cursor));
        grip.setToolTipText("Drag to resize Balam calendar");
        MouseAdapter drag = new MouseAdapter() {
            private Point origin;
            private Dimension initial;
            public void mousePressed(MouseEvent e) {
                origin = e.getLocationOnScreen();
                initial = panel.getSize();
            }
            public void mouseDragged(MouseEvent e) {
                if (origin == null || panel.getParent() == null) return;
                Point current = e.getLocationOnScreen();
                Insets insets = panel.getParent().getInsets();
                int availableWidth = panel.getParent().getWidth() - panel.getX() - insets.right;
                int availableHeight = panel.getParent().getHeight() - panel.getY() - insets.bottom;
                Dimension requested = new Dimension(initial.width + (horizontal ? current.x - origin.x : 0),
                    initial.height + (vertical ? current.y - origin.y : 0));
                panel.putClientProperty(SIZE_KEY, clamp(requested, availableWidth, availableHeight));
                panel.getParent().revalidate();
                panel.getParent().repaint();
            }
            public void mouseReleased(MouseEvent e) { origin = null; }
        };
        grip.addMouseListener(drag);
        grip.addMouseMotionListener(drag);
        return grip;
    }
}
