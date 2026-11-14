/*
 * AstrosoftLabel.java
 *
 * Created on October 11, 2005, 8:28 PM
 *
 *@author Rajasekar Elango
 */

package app.astrosoft.ui.comp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JLabel;

import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;

public class TitleLabel extends JLabel {
  
	private static Color DEF_TEXT_CLR  = UIConsts.ROSE;  
   
	public TitleLabel(String text){
        this(text, 12, DEF_TEXT_CLR);
    }
    
    public TitleLabel(String text, int fontSize) {
    	this(text, fontSize, DEF_TEXT_CLR);
	}
    
    public TitleLabel(String text, int fontSize, Color textColor){
    	super(text);
        this.setForeground(textColor);
        this.setFont(UIUtil.getFont(Font.BOLD, fontSize));
    }
    
    public TitleLabel(Enum str){
    	this(str.toString());
    }

    @Override
    public Dimension getPreferredSize() {
        String text = getText();
        if (text == null || !text.contains("\n")) {
            return super.getPreferredSize();
        }
        FontMetrics fm = getFontMetrics(getFont());
        String[] lines = text.split("\n", -1);
        int maxW = 0;
        for (String line : lines) {
            maxW = Math.max(maxW, fm.stringWidth(line));
        }
        Insets in = getInsets();
        int h = lines.length * fm.getHeight() + in.top + in.bottom;
        return new Dimension(maxW + in.left + in.right, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        String text = getText();
        if (text == null || !text.contains("\n")) {
            super.paintComponent(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(getFont());
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        String[] lines = text.split("\n", -1);
        int totalH = lines.length * fm.getHeight();
        int y = (getHeight() - totalH) / 2 + fm.getAscent();
        for (String line : lines) {
            int x = (getWidth() - fm.stringWidth(line)) / 2;
            g2.drawString(line, x, y);
            y += fm.getHeight();
        }
        g2.dispose();
    }
}
