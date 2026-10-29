/*
 * AstrosoftToolBar.java
 *
 * Created on Septembar 24, 2005, 3:23 PM
 *
 * @author E. Rajasekar.
 */

package app.astrosoft.ui.comp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import app.astrosoft.consts.Command;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.AstrosoftActionManager;
import app.astrosoft.ui.util.UIConsts;

public class AstrosoftToolBar extends JToolBar
{
	
	List<JButton> buttons ;
	private JSeparator fontSeparator;
	private JPanel fontControlPanel;
	private JButton btnFontDec;
	private JButton btnFontInc;
	private JButton btnFontReset;
	private Runnable onIncFont;
	private Runnable onDecFont;
	private Runnable onResetFont;
	
	public AstrosoftToolBar(AstrosoftActionManager actionMgr){
		this(Color.WHITE, actionMgr);
	}
	
    public AstrosoftToolBar(Color mclr, AstrosoftActionManager actionMgr) {
        
    	
    	buttons = new ArrayList<JButton>();
    	
    	for(Command toolBarItem : Command.toolBarItems()){
    		buttons.add(new JButton(actionMgr.getAction(toolBarItem)));
    	}
    	
        for (JButton b:buttons){

        	add(b);
        	String cmd = b.getActionCommand();
        	if (cmd.equals(Command.PRINT) || cmd.equals(Command.SHADBALA_VIEW)  || cmd.equals(Command.PANCHANG_VIEW)){
        		//addSeparator();
        		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
                add(separator);
        		
        	}
        	b.setText("");
        }

        fontSeparator = new JSeparator(SwingConstants.VERTICAL);
        fontSeparator.setMaximumSize(new Dimension(2, 24));
        add(fontSeparator);

        fontControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        fontControlPanel.setOpaque(false);

        btnFontDec = new JButton("A-");
        btnFontDec.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        btnFontDec.setToolTipText("Decrease Chart Font Size");
        btnFontDec.setFocusable(false);
        btnFontDec.setMargin(new Insets(1, 3, 1, 3));
        btnFontDec.setPreferredSize(new Dimension(30, 24));
        btnFontDec.setForeground(UIConsts.TEXT_DARK_NAVY);
        btnFontDec.addActionListener(e -> {
            if (onDecFont != null) onDecFont.run();
        });

        btnFontInc = new JButton("A+");
        btnFontInc.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        btnFontInc.setToolTipText("Increase Chart Font Size");
        btnFontInc.setFocusable(false);
        btnFontInc.setMargin(new Insets(1, 3, 1, 3));
        btnFontInc.setPreferredSize(new Dimension(30, 24));
        btnFontInc.setForeground(UIConsts.TEXT_DARK_NAVY);
        btnFontInc.addActionListener(e -> {
            if (onIncFont != null) onIncFont.run();
        });

        btnFontReset = new JButton("100%");
        btnFontReset.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        btnFontReset.setToolTipText("Reset Chart Font Size to Default");
        btnFontReset.setFocusable(false);
        btnFontReset.setMargin(new Insets(1, 2, 1, 2));
        btnFontReset.setPreferredSize(new Dimension(42, 24));
        btnFontReset.setForeground(UIConsts.LABEL_MUTED_COLOR);
        btnFontReset.addActionListener(e -> {
            if (onResetFont != null) onResetFont.run();
        });

        fontControlPanel.add(btnFontDec);
        fontControlPanel.add(btnFontInc);
        fontControlPanel.add(btnFontReset);
        add(fontControlPanel);
        setChartFontControlsVisible(false);
    }

    public void setFontAdjustmentActions(Runnable onInc, Runnable onDec, Runnable onReset) {
        this.onIncFont = onInc;
        this.onDecFont = onDec;
        this.onResetFont = onReset;
    }

    public void setChartFontControlsVisible(boolean visible) {
        if (fontSeparator != null) fontSeparator.setVisible(visible);
        if (fontControlPanel != null) fontControlPanel.setVisible(visible);
        revalidate();
        repaint();
    }
}
