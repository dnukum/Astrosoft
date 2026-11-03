package app.astrosoft.ui.comp.modern;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Modern rounded button with antialiased rendering, subtle depth gradients,
 * interactive hover/press states, and zero Look & Feel boxiness.
 */
public class ModernButton extends JButton {

    public enum Style {
        PRIMARY,
        SECONDARY
    }

    private final Style style;
    private Color primaryColor;
    private int cornerRadius = 10;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public ModernButton(String text, Style style, Color primaryColor) {
        super(text);
        this.style = style;
        this.primaryColor = (primaryColor != null) ? primaryColor : new Color(25, 118, 210);

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (style == Style.PRIMARY) {
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setForeground(Color.WHITE);
        } else {
            setFont(new Font("SansSerif", Font.PLAIN, 12));
            setForeground(new Color(51, 65, 85));
        }

        setPreferredSize(new Dimension(86, 30));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public static ModernButton createPrimary(String text, Color primaryColor) {
        return new ModernButton(text, Style.PRIMARY, primaryColor);
    }

    public static ModernButton createSecondary(String text) {
        return new ModernButton(text, Style.SECONDARY, null);
    }

    public void setPrimaryColor(Color c) {
        if (c != null) {
            this.primaryColor = c;
            repaint();
        }
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int r = cornerRadius;

        if (style == Style.PRIMARY) {
            // Primary Button: Smooth gradient from slightly lighter to base, or solid with subtle top glow
            Color topColor;
            Color bottomColor;

            if (isPressed) {
                topColor = darker(primaryColor, 0.85f);
                bottomColor = darker(primaryColor, 0.75f);
            } else if (isHovered) {
                topColor = brighter(primaryColor, 1.15f);
                bottomColor = primaryColor;
            } else {
                topColor = brighter(primaryColor, 1.08f);
                bottomColor = darker(primaryColor, 0.95f);
            }

            LinearGradientPaint gradient = new LinearGradientPaint(
                0, 1, 0, h - 2,
                new float[]{0.0f, 1.0f},
                new Color[]{topColor, bottomColor}
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(1, 1, w - 2, h - 2, r, r);

            // Subtle top highlight line for modern glass/depth feel
            g2.setColor(new Color(255, 255, 255, isPressed ? 30 : 65));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, r, r);

            // Crisp white centered text
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2 + (isPressed ? 1 : 0);
            g2.drawString(getText(), tx, ty);

        } else {
            // Secondary Button: Modern soft card button with smooth border and subtle hover tint
            Color bg;
            Color border;
            Color text;

            if (isPressed) {
                bg = new Color(226, 232, 240); // #E2E8F0
                border = new Color(148, 163, 184); // #94A3B8
                text = new Color(15, 23, 42);
            } else if (isHovered) {
                bg = new Color(241, 245, 249); // #F1F5F9
                border = new Color(148, 163, 184);
                text = new Color(15, 23, 42);
            } else {
                bg = Color.WHITE;
                border = new Color(203, 213, 225); // #CBD5E1
                text = new Color(51, 65, 85);      // #334155
            }

            g2.setColor(bg);
            g2.fillRoundRect(1, 1, w - 2, h - 2, r, r);

            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, r, r);

            // Centered text
            g2.setColor(text);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2 + (isPressed ? 1 : 0);
            g2.drawString(getText(), tx, ty);
        }

        g2.dispose();
    }

    private Color brighter(Color c, float factor) {
        int r = Math.min(255, (int)(c.getRed() * factor));
        int g = Math.min(255, (int)(c.getGreen() * factor));
        int b = Math.min(255, (int)(c.getBlue() * factor));
        return new Color(r, g, b, c.getAlpha());
    }

    private Color darker(Color c, float factor) {
        int r = Math.max(0, (int)(c.getRed() * factor));
        int g = Math.max(0, (int)(c.getGreen() * factor));
        int b = Math.max(0, (int)(c.getBlue() * factor));
        return new Color(r, g, b, c.getAlpha());
    }
}