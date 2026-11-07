/**
 * OptionDialog.java
 *
 * Created on December 27, 2002, 4:51 PM
 * @author  E. Rajasekar
 */
package app.astrosoft.ui.dlg;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.Utils;


public class OptionDialog  {

	private static final long serialVersionUID = 1L;
	
	public static int showDialog(String message, int messageType){

		int optionType = JOptionPane.DEFAULT_OPTION;
		String title = null;
		
		if (messageType == JOptionPane.ERROR_MESSAGE){
			title = "Error ";
			optionType = JOptionPane.DEFAULT_OPTION;
		}else if (messageType == JOptionPane.QUESTION_MESSAGE){
			title = "Confirm ";
			optionType = JOptionPane.YES_NO_OPTION;
		}
		else if (messageType == JOptionPane.INFORMATION_MESSAGE){
			title = "Information ";
			optionType = JOptionPane.DEFAULT_OPTION;
		}
		
		JOptionPane pane = new JOptionPane(message, messageType, optionType);
		showPaneDialog(pane, null, title);
		
		Object selectedValue = pane.getValue();
	    if(selectedValue instanceof Integer) {
	    	return ((Integer)selectedValue).intValue();
		}
	    return JOptionPane.CLOSED_OPTION;
	}

	public static void showMessageDialog(Component parent, Object message, String title, int messageType) {
		JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.DEFAULT_OPTION);
		showPaneDialog(pane, parent, title);
	}

	public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType) {
		JOptionPane pane = new JOptionPane(message, messageType, optionType);
		showPaneDialog(pane, parent, title);
		Object selectedValue = pane.getValue();
		if (selectedValue instanceof Integer) {
			return ((Integer) selectedValue).intValue();
		}
		return JOptionPane.CLOSED_OPTION;
	}

	public static int showOptionDialog(Component parent, Object message, String title, int optionType, int messageType,
			Icon icon, Object[] options, Object initialValue) {
		JOptionPane pane = new JOptionPane(message, messageType, optionType, icon, options, initialValue);
		showPaneDialog(pane, parent, title);
		Object selectedValue = pane.getValue();
		if (selectedValue == null) {
			return JOptionPane.CLOSED_OPTION;
		}
		if (options == null) {
			if (selectedValue instanceof Integer) {
				return ((Integer) selectedValue).intValue();
			}
			return JOptionPane.CLOSED_OPTION;
		}
		for (int i = 0; i < options.length; i++) {
			if (options[i].equals(selectedValue)) {
				return i;
			}
		}
		return JOptionPane.CLOSED_OPTION;
	}

	private static void showPaneDialog(JOptionPane pane, Component parent, String title) {
		Component parentComp = (parent != null) ? parent : AstroSoft.getInstance();
		JDialog dialog = pane.createDialog(parentComp, title);
		dialog.setAlwaysOnTop(true);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				dialog.toFront();
				dialog.requestFocus();
			}
		});

		UIUtil.applyOptionPaneBackground(pane, UIConsts.OPTIONPANE_BACKGROUND);

		SwingUtilities.invokeLater(() -> {
			dialog.toFront();
			dialog.requestFocus();
		});
		dialog.setVisible(true);
	}
}
