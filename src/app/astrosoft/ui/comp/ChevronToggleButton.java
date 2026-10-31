package app.astrosoft.ui.comp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;

import app.astrosoft.ui.util.UIConsts;

/**
 * Pill-shaped icon button showing a large double chevron (&laquo; or &raquo;), painted as vectors so it
 * stays sharp on Retina screens. Navy fill matches the Rasi/Navamsha title badges; it lightens on
 * hover and darkens when pressed.
 */
public class ChevronToggleButton extends JButton {

	private static final int WIDTH = 40;
	private static final int HEIGHT = 26;
	private static final Color HOVER = new Color(40, 62, 104);
	private static final Color PRESSED = new Color(5, 14, 32);

	private boolean pointingLeft = true;

	public ChevronToggleButton() {
		super("");
		setFocusable(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setFocusPainted(false);
		setOpaque(false);
		setRolloverEnabled(true);
		setBorder(BorderFactory.createEmptyBorder());
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		Dimension d = new Dimension(WIDTH, HEIGHT);
		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);
	}

	public boolean isPointingLeft() {
		return pointingLeft;
	}

	public void setPointingLeft(boolean pointingLeft) {
		this.pointingLeft = pointingLeft;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

			ButtonModel m = getModel();
			Color fill = m.isPressed() ? PRESSED : (m.isRollover() ? HOVER : UIConsts.NAVY_BADGE_BG);
			int w = getWidth();
			int h = getHeight();

			// soft drop shadow, then the pill
			g2.setColor(new Color(0, 0, 0, 40));
			g2.fill(new RoundRectangle2D.Double(0.5, 1.5, w - 1, h - 1.5, h - 1, h - 1));
			g2.setColor(fill);
			g2.fill(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 2, h - 2, h - 2));

			// two chevrons, centred
			double ch = h * 0.50;          // chevron height
			double cw = ch * 0.50;         // chevron depth
			double gap = cw * 0.95;        // distance between the two chevrons
			double cx = w / 2.0;
			double cy = (h - 1) / 2.0;
			double dir = pointingLeft ? -1 : 1;

			g2.setColor(Color.WHITE);
			g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			for (double offset : new double[] { -gap / 2, gap / 2 }) {
				double tipX = cx + offset + dir * cw / 2;
				double backX = cx + offset - dir * cw / 2;
				Path2D p = new Path2D.Double();
				p.moveTo(backX, cy - ch / 2);
				p.lineTo(tipX, cy);
				p.lineTo(backX, cy + ch / 2);
				g2.draw(p);
			}
		} finally {
			g2.dispose();
		}
	}
}
