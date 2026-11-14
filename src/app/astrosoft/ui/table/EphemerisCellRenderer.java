/**
 * EphemerisCellRenderer.java
 * Created On 2006, Apr 7, 2006 6:23:50 PM
 * @author E. Rajasekar
 */
package app.astrosoft.ui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.core.Ephemeris.EphData;
import app.astrosoft.util.AstroUtil;

public class EphemerisCellRenderer extends DefaultTableCellRenderer {

	private static final Color HOUSE_COLOR = new Color(0x00, 0x46, 0x8C);
	private static final Color RETRO_COLOR = new Color(0xC5, 0x30, 0x30);

	private boolean isEphData;
	private String degPart;
	private String symPart;
	private String minPart;
	private String retroSym;

	public EphemerisCellRenderer() {
		super();
		setHorizontalAlignment(JLabel.CENTER);
	}

	@Override
	protected void setValue(Object value) {
		if (value instanceof EphData) {
			EphData ephValue = (EphData) value;
			isEphData = true;
			String[] dms = AstroUtil.todegmin(ephValue.getPosition(), ":").split(":");
			degPart = dms[0] + " ";
			symPart = ephValue.getHouse().toString(DisplayFormat.SYMBOL);
			minPart = " " + (dms.length > 1 ? dms[1] : "00");
			retroSym = ephValue.isReverse() ? DisplayStrings.RETRO_SYM.toString() : null;

			StringBuilder plain = new StringBuilder(degPart).append(symPart).append(minPart);
			if (retroSym != null) {
				plain.append(" [").append(retroSym).append("]");
			}
			super.setValue(plain.toString());
		} else {
			isEphData = false;
			degPart = null;
			symPart = null;
			minPart = null;
			retroSym = null;
			super.setValue(value);
		}
	}

	private static Font resolveSegmentFont(Font font, String text) {
		if (text != null && !text.isEmpty() && font.canDisplayUpTo(text) != -1) {
			return new Font(Font.SANS_SERIF, font.getStyle(), font.getSize());
		}
		return font;
	}

	@Override
	public Dimension getPreferredSize() {
		if (!isEphData || degPart == null) {
			return super.getPreferredSize();
		}
		Font base = getFont();
		if (base == null) {
			return super.getPreferredSize();
		}
		Font plainFont = base.deriveFont(Font.PLAIN);
		Font boldFont = base.deriveFont(Font.BOLD);

		FontMetrics fmDeg = getFontMetrics(resolveSegmentFont(plainFont, degPart));
		FontMetrics fmSym = getFontMetrics(resolveSegmentFont(boldFont, symPart));
		FontMetrics fmMin = getFontMetrics(resolveSegmentFont(plainFont, minPart));

		int totalW = fmDeg.stringWidth(degPart) + fmSym.stringWidth(symPart) + fmMin.stringWidth(minPart);
		if (retroSym != null) {
			FontMetrics fmBrk = getFontMetrics(resolveSegmentFont(boldFont, " []"));
			FontMetrics fmRet = getFontMetrics(resolveSegmentFont(boldFont, retroSym));
			totalW += fmBrk.stringWidth(" [") + fmRet.stringWidth(retroSym) + fmBrk.stringWidth("]");
		}
		int h = Math.max(fmSym.getHeight() + 4, 24);
		return new Dimension(totalW + 8, h);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (!isEphData || degPart == null) {
			super.paintComponent(g);
			return;
		}

		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			Font base = getFont();
			Font plainFont = base.deriveFont(Font.PLAIN);
			Font boldFont = base.deriveFont(Font.BOLD);

			Font fDeg = resolveSegmentFont(plainFont, degPart);
			Font fSym = resolveSegmentFont(boldFont, symPart);
			Font fMin = resolveSegmentFont(plainFont, minPart);

			FontMetrics fmDeg = g2.getFontMetrics(fDeg);
			FontMetrics fmSym = g2.getFontMetrics(fSym);
			FontMetrics fmMin = g2.getFontMetrics(fMin);

			int wDeg = fmDeg.stringWidth(degPart);
			int wSym = fmSym.stringWidth(symPart);
			int wMin = fmMin.stringWidth(minPart);

			Font fBrk = null;
			Font fRet = null;
			int wBrkOpen = 0;
			int wRet = 0;
			int wBrkClose = 0;

			if (retroSym != null) {
				fBrk = resolveSegmentFont(boldFont, " []");
				fRet = resolveSegmentFont(boldFont, retroSym);
				FontMetrics fmBrk = g2.getFontMetrics(fBrk);
				FontMetrics fmRet = g2.getFontMetrics(fRet);
				wBrkOpen = fmBrk.stringWidth(" [");
				wRet = fmRet.stringWidth(retroSym);
				wBrkClose = fmBrk.stringWidth("]");
			}

			int totalW = wDeg + wSym + wMin + wBrkOpen + wRet + wBrkClose;
			int x = Math.max(2, (getWidth() - totalW) / 2);
			int y = (getHeight() - fmSym.getHeight()) / 2 + fmSym.getAscent();

			g2.setColor(getForeground());
			g2.setFont(fDeg);
			g2.drawString(degPart, x, y);
			x += wDeg;

			g2.setColor(HOUSE_COLOR);
			g2.setFont(fSym);
			g2.drawString(symPart, x, y);
			x += wSym;

			g2.setColor(getForeground());
			g2.setFont(fMin);
			g2.drawString(minPart, x, y);
			x += wMin;

			if (retroSym != null) {
				g2.setColor(RETRO_COLOR);
				g2.setFont(fBrk);
				g2.drawString(" [", x, y);
				x += wBrkOpen;

				g2.setFont(fRet);
				g2.drawString(retroSym, x, y);
				x += wRet;

				g2.setFont(fBrk);
				g2.drawString("]", x, y);
			}
		} finally {
			g2.dispose();
		}
	}
}
