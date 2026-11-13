/**
 * AstrosoftTabbedPane.java
 * Created On 2006, Mar 8, 2006 6:50:43 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.comp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import app.astrosoft.ui.util.UIUtil;

public class AstrosoftTabbedPane extends JTabbedPane {
	
	public AstrosoftTabbedPane(){
		super();
		setFont(UIUtil.getFont(Font.BOLD, 12));
	}

	public AstrosoftTabbedPane(Dimension paneSize){
		super();
		setPreferredSize(paneSize);
		setFont(UIUtil.getFont(Font.BOLD, 12));
	}
	
	@Override
	public void addTab(String title, Component component) {
		addTab(title, component, false);
	}

	public void addTab(String title, Component component, boolean fill) {
		JPanel panel;
		if (fill) {
			panel = new JPanel(new BorderLayout());
			panel.add(component, BorderLayout.CENTER);
		} else {
			panel = new JPanel();
			panel.add(component);
		}
		panel.setBorder(BorderFactory.createEtchedBorder());
		super.addTab(title, panel);
	}
	
	public void addTab(Enum title, Component component){
		this.add(title.toString(), component);
	}

}
