package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import app.astrosoft.ui.comp.ChevronToggleButton;
import app.astrosoft.ui.util.UIConsts;

/**
 * Horoscope screen body: Profile and Planetary Positions on the left, Rasi/Navamsa and
 * Ashtavarga on the right. The left column can be collapsed to give the Notes pane more room
 * without clipping the charts. When collapsed, the Profile card is hidden, the controls row moves
 * above the charts, and the Shadbala and Planetary Positions cards stack below Ashtavarga.
 */
public class HoroscopeDashboard extends JPanel {

	public static final int LEFT_WIDTH = 530;
	public static final int COLUMN_GAP = 40;
	public static final int CHART_WIDTH = 858;
	private static final int MIN_LEFT_HEIGHT = 540;

	private final JComponent infoView;
	private final JComponent positionsView;
	private final JComponent shadbalaView;
	private final JComponent chartsPanel;
	private final JComponent ashtavargaPanel;
	private final Consumer<Boolean> collapseListener;

	private final JPanel leftPanel = new JPanel();
	private final JPanel leftWrapper = new JPanel(new BorderLayout());
	private final JPanel rightContainer = new JPanel();
	private final JPanel rightWrapper = new JPanel(new BorderLayout());
	private final JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
	private final ChevronToggleButton toggleButton = new ChevronToggleButton();
	private final JCheckBox positionsCheck = checkBox("Planetary Positions", true);
	private final JCheckBox shadbalaCheck = checkBox("Shadbala", false);
	private final JCheckBox ashtavargaCheck = checkBox("AshtaVarga", false);

	private boolean collapsed;

	public HoroscopeDashboard(JComponent infoView, JComponent positionsView, JComponent shadbalaView,
			JComponent chartsPanel, JComponent ashtavargaPanel, boolean collapsed,
			Consumer<Boolean> collapseListener) {
		super(new BorderLayout(COLUMN_GAP, 0));
		this.infoView = infoView;
		this.positionsView = positionsView;
		this.shadbalaView = shadbalaView;
		this.chartsPanel = chartsPanel;
		this.ashtavargaPanel = ashtavargaPanel;
		this.collapseListener = collapseListener;
		this.collapsed = collapsed;

		setOpaque(false);
		for (JComponent c : new JComponent[] { infoView, positionsView, shadbalaView, chartsPanel, ashtavargaPanel }) {
			c.setAlignmentX(Component.LEFT_ALIGNMENT);
		}

		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setOpaque(false);
		leftWrapper.setOpaque(false);
		leftWrapper.add(leftPanel, BorderLayout.NORTH);

		rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
		rightContainer.setOpaque(false);
		rightWrapper.setOpaque(false);
		rightWrapper.add(rightContainer, BorderLayout.NORTH);

		toggleButton.addActionListener(e -> {
			setCollapsed(!this.collapsed);
			if (this.collapseListener != null) {
				this.collapseListener.accept(this.collapsed);
			}
		});

		controlsPanel.setOpaque(false);
		controlsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		controlsPanel.setPreferredSize(new Dimension(525, 28));
		controlsPanel.setMinimumSize(new Dimension(300, 28));
		controlsPanel.setMaximumSize(new Dimension(LEFT_WIDTH, 28));
		controlsPanel.add(toggleButton);
		controlsPanel.add(positionsCheck);
		controlsPanel.add(shadbalaCheck);
		controlsPanel.add(ashtavargaCheck);

		positionsCheck.addActionListener(e -> relayout());
		shadbalaCheck.addActionListener(e -> relayout());
		ashtavargaCheck.addActionListener(e -> relayout());

		relayout();
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	/** Changes the layout only; the toggle button also reports the change to the listener. */
	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
		relayout();
	}

	public JPanel getControlsPanel() {
		return controlsPanel;
	}

	public ChevronToggleButton getToggleButton() {
		return toggleButton;
	}

	public JCheckBox getPositionsCheck() {
		return positionsCheck;
	}

	public JCheckBox getShadbalaCheck() {
		return shadbalaCheck;
	}

	public JCheckBox getAshtavargaCheck() {
		return ashtavargaCheck;
	}

	private void relayout() {
		boolean showPositions = positionsCheck.isSelected();
		boolean showShadbala = shadbalaCheck.isSelected();
		boolean showAshtavarga = ashtavargaCheck.isSelected();

		leftPanel.removeAll();
		rightContainer.removeAll();
		removeAll();

		if (!collapsed) {
			toggleButton.setPointingLeft(true);
			toggleButton.setToolTipText("Hide the left column to give Notes more room");

			leftPanel.add(infoView);
			leftPanel.add(Box.createVerticalStrut(12));
			leftPanel.add(themeSeparator());
			leftPanel.add(Box.createVerticalStrut(9));
			leftPanel.add(controlsPanel);
			if (showPositions) {
				leftPanel.add(Box.createVerticalStrut(9));
				leftPanel.add(themeSeparator());
				leftPanel.add(Box.createVerticalStrut(12));
				leftPanel.add(positionsView);
			}
			if (showShadbala) {
				if (!showPositions) {
					leftPanel.add(Box.createVerticalStrut(9));
					leftPanel.add(themeSeparator());
				}
				leftPanel.add(Box.createVerticalStrut(12));
				leftPanel.add(shadbalaView);
			}

			rightContainer.add(chartsPanel);
			if (showAshtavarga) {
				rightContainer.add(ashtavargaPanel);
			}

			int leftH = Math.max(MIN_LEFT_HEIGHT, leftPanel.getPreferredSize().height);
			leftWrapper.setPreferredSize(new Dimension(LEFT_WIDTH, leftH));
			add(leftWrapper, BorderLayout.WEST);
			add(rightWrapper, BorderLayout.CENTER);
		} else {
			toggleButton.setPointingLeft(false);
			toggleButton.setToolTipText("Show the left column (Profile and Planetary Positions)");

			rightContainer.add(controlsPanel);
			rightContainer.add(Box.createVerticalStrut(10));
			rightContainer.add(chartsPanel);
			if (showAshtavarga) {
				rightContainer.add(ashtavargaPanel);
			}
			if (showShadbala) {
				rightContainer.add(Box.createVerticalStrut(20));
				rightContainer.add(shadbalaView);
			}
			if (showPositions) {
				rightContainer.add(Box.createVerticalStrut(showShadbala ? 12 : 20));
				rightContainer.add(positionsView);
			}
			add(rightWrapper, BorderLayout.CENTER);
		}

		Dimension right = rightContainer.getPreferredSize();
		int rightW = Math.max(CHART_WIDTH, right.width);
		int totalW = collapsed ? rightW : LEFT_WIDTH + COLUMN_GAP + rightW;
		int totalH = collapsed ? right.height : Math.max(leftWrapper.getPreferredSize().height, right.height);
		setPreferredSize(new Dimension(totalW, totalH));

		revalidate();
		repaint();
	}

	private static JCheckBox checkBox(String text, boolean selected) {
		JCheckBox c = new JCheckBox(text);
		c.setSelected(selected);
		c.setOpaque(false);
		c.setFocusable(false);
		c.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
		c.setForeground(UIConsts.getChartPlanetForeground());
		return c;
	}

	private static JComponent themeSeparator() {
		JPanel line = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(UIConsts.getChartBorderColor());
				g.fillRect(0, 0, getWidth(), 1);
			}
		};
		line.setOpaque(false);
		line.setAlignmentX(Component.LEFT_ALIGNMENT);
		line.setPreferredSize(new Dimension(LEFT_WIDTH, 1));
		line.setMaximumSize(new Dimension(LEFT_WIDTH, 1));
		line.setMinimumSize(new Dimension(10, 1));
		return line;
	}
}
