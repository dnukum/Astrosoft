package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Window;
import java.util.EnumMap;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Ashtavarga;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.ShadBala;
import app.astrosoft.ui.comp.ChevronToggleButton;
import app.astrosoft.ui.dlg.AshtavargaDialog;
import app.astrosoft.ui.dlg.HouseDetailsDialog;
import app.astrosoft.ui.dlg.PlanetDetailsDialog;
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
	private final JPanel leftWrapper = new JPanel(new BorderLayout()) {
		@Override
		public Dimension getPreferredSize() {
			int h = Math.max(MIN_LEFT_HEIGHT, leftPanel.getPreferredSize().height);
			return new Dimension(LEFT_WIDTH, h);
		}
	};
	private final JPanel rightContainer = new JPanel();
	private final JPanel rightWrapper = new JPanel(new BorderLayout());
	private final JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
	private final ChevronToggleButton toggleButton = new ChevronToggleButton();
	private final JCheckBox positionsCheck = checkBox("Planet Details", false);
	private final JCheckBox shadbalaCheck = checkBox("Shadbala", false);
	private final JCheckBox ashtavargaCheck = checkBox("AshtaVarga", false);
	private final JCheckBox houseDetailsCheck = checkBox("House Details", false);

	private HouseDetailsDialog houseDetailsDialog;
	private PlanetDetailsDialog planetDetailsDialog;
	private AshtavargaDialog ashtavargaDialog;
	private PlanetaryInfo planetaryInfo;
	private Rasi ascendant;
	private ShadBala shadBala;
	private Ashtavarga ashtavarga;
	private Rasi moonRasi;
	private EnumMap<Planet, Rasi> planetRasis;
	private Horoscope horoscope;

	private boolean collapsed;

	public HoroscopeDashboard(JComponent infoView, JComponent positionsView, JComponent shadbalaView,
			JComponent chartsPanel, JComponent ashtavargaPanel, boolean collapsed,
			Consumer<Boolean> collapseListener) {
		this(infoView, positionsView, shadbalaView, chartsPanel, ashtavargaPanel, collapsed, collapseListener, null, null, null);
	}

	public HoroscopeDashboard(JComponent infoView, JComponent positionsView, JComponent shadbalaView,
			JComponent chartsPanel, JComponent ashtavargaPanel, boolean collapsed,
			Consumer<Boolean> collapseListener, PlanetaryInfo planetaryInfo, Rasi ascendant) {
		this(infoView, positionsView, shadbalaView, chartsPanel, ashtavargaPanel, collapsed, collapseListener, planetaryInfo, ascendant, null);
	}

	public HoroscopeDashboard(JComponent infoView, JComponent positionsView, JComponent shadbalaView,
			JComponent chartsPanel, JComponent ashtavargaPanel, boolean collapsed,
			Consumer<Boolean> collapseListener, PlanetaryInfo planetaryInfo, Rasi ascendant, ShadBala shadBala) {
		this(infoView, positionsView, shadbalaView, chartsPanel, ashtavargaPanel, collapsed, collapseListener,
				planetaryInfo, ascendant, shadBala, null, null, null, null);
	}

	public HoroscopeDashboard(JComponent infoView, JComponent positionsView, JComponent shadbalaView,
			JComponent chartsPanel, JComponent ashtavargaPanel, boolean collapsed,
			Consumer<Boolean> collapseListener, PlanetaryInfo planetaryInfo, Rasi ascendant, ShadBala shadBala,
			Ashtavarga ashtavarga, Rasi moonRasi, EnumMap<Planet, Rasi> planetRasis, Horoscope horoscope) {
		super(new BorderLayout(COLUMN_GAP, 0));
		this.infoView = infoView;
		this.positionsView = positionsView;
		this.shadbalaView = shadbalaView;
		this.chartsPanel = chartsPanel;
		this.ashtavargaPanel = ashtavargaPanel;
		this.collapseListener = collapseListener;
		this.collapsed = collapsed;
		this.planetaryInfo = planetaryInfo;
		this.ascendant = ascendant;
		this.shadBala = shadBala;
		this.ashtavarga = ashtavarga;
		this.moonRasi = moonRasi;
		this.planetRasis = planetRasis;
		this.horoscope = horoscope;

		if (planetaryInfo == null) {
			positionsCheck.setSelected(true);
		} else {
			positionsCheck.setSelected(false);
		}

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
		// Shadbala option consolidated into multi-tab "Planet Details" card; shadbalaCheck retained for backward compatibility
		controlsPanel.add(ashtavargaCheck);
		controlsPanel.add(houseDetailsCheck);

		positionsCheck.addActionListener(e -> togglePlanetDetailsDialog());
		shadbalaCheck.addActionListener(e -> relayout());
		ashtavargaCheck.addActionListener(e -> toggleAshtavargaDialog());
		houseDetailsCheck.addActionListener(e -> toggleHouseDetailsDialog());

		PlanetDetailsDialog activePlanet = PlanetDetailsDialog.getActiveDialog();
		if (activePlanet != null && activePlanet.isDisplayable()) {
			this.planetDetailsDialog = activePlanet;
			this.positionsCheck.setSelected(activePlanet.isVisible());
			activePlanet.setOnCloseCallback(() -> positionsCheck.setSelected(false));
		}
		HouseDetailsDialog activeHouse = HouseDetailsDialog.getActiveDialog();
		if (activeHouse != null && activeHouse.isDisplayable()) {
			this.houseDetailsDialog = activeHouse;
			this.houseDetailsCheck.setSelected(activeHouse.isVisible());
			activeHouse.setOnCloseCallback(() -> houseDetailsCheck.setSelected(false));
		}
		AshtavargaDialog activeAsh = AshtavargaDialog.getActiveDialog();
		if (activeAsh != null && activeAsh.isDisplayable()) {
			this.ashtavargaDialog = activeAsh;
			this.ashtavargaCheck.setSelected(activeAsh.isVisible());
			activeAsh.setOnCloseCallback(() -> ashtavargaCheck.setSelected(false));
		}

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

	public JCheckBox getHouseDetailsCheck() {
		return houseDetailsCheck;
	}

	public HouseDetailsDialog getHouseDetailsDialog() {
		return houseDetailsDialog;
	}

	public PlanetDetailsDialog getPlanetDetailsDialog() {
		return planetDetailsDialog;
	}

	public AshtavargaDialog getAshtavargaDialog() {
		return ashtavargaDialog;
	}

	private void togglePlanetDetailsDialog() {
		if (planetaryInfo != null) {
			if (positionsCheck.isSelected()) {
				if (planetDetailsDialog == null || !planetDetailsDialog.isDisplayable()) {
					Window owner = SwingUtilities.getWindowAncestor(this);
					planetDetailsDialog = new PlanetDetailsDialog(owner, planetaryInfo, ascendant, shadBala, () -> {
						positionsCheck.setSelected(false);
					});
				}
				planetDetailsDialog.setVisible(true);
				planetDetailsDialog.toFront();
			} else {
				if (planetDetailsDialog != null) {
					planetDetailsDialog.dispose();
					planetDetailsDialog = null;
				}
			}
		} else {
			relayout();
		}
	}

	private void toggleAshtavargaDialog() {
		if (ashtavarga != null || horoscope != null) {
			if (ashtavargaCheck.isSelected()) {
				if (ashtavargaDialog == null || !ashtavargaDialog.isDisplayable()) {
					Window owner = SwingUtilities.getWindowAncestor(this);
					ashtavargaDialog = new AshtavargaDialog(owner, ashtavarga, ascendant, moonRasi, planetRasis, () -> {
						ashtavargaCheck.setSelected(false);
					});
				}
				ashtavargaDialog.setVisible(true);
				ashtavargaDialog.toFront();
			} else {
				if (ashtavargaDialog != null) {
					ashtavargaDialog.dispose();
					ashtavargaDialog = null;
				}
			}
		} else {
			relayout();
		}
	}

	public void setPlanetDetailsData(PlanetaryInfo planetaryInfo, Rasi ascendant, ShadBala shadBala) {
		this.planetaryInfo = planetaryInfo;
		this.ascendant = ascendant;
		this.shadBala = shadBala;
		if (planetDetailsDialog != null && planetDetailsDialog.isDisplayable()) {
			boolean wasVisible = planetDetailsDialog.isVisible();
			Window owner = planetDetailsDialog.getOwner();
			planetDetailsDialog.dispose();
			planetDetailsDialog = null;
			if (wasVisible) {
				planetDetailsDialog = new PlanetDetailsDialog(owner, planetaryInfo, ascendant, shadBala, () -> {
					positionsCheck.setSelected(false);
				});
				planetDetailsDialog.setVisible(true);
			}
		}
	}

	private void toggleHouseDetailsDialog() {
		if (houseDetailsCheck.isSelected()) {
			if (houseDetailsDialog == null || !houseDetailsDialog.isDisplayable()) {
				Window owner = SwingUtilities.getWindowAncestor(this);
				houseDetailsDialog = new HouseDetailsDialog(owner, planetaryInfo, ascendant, () -> {
					houseDetailsCheck.setSelected(false);
				});
			}
			houseDetailsDialog.setVisible(true);
			houseDetailsDialog.toFront();
		} else {
			if (houseDetailsDialog != null) {
				houseDetailsDialog.dispose();
				houseDetailsDialog = null;
			}
		}
	}

	public void disposeFloatingDialogs() {
		if (planetDetailsDialog != null) {
			planetDetailsDialog.dispose();
			planetDetailsDialog = null;
		}
		if (houseDetailsDialog != null) {
			houseDetailsDialog.dispose();
			houseDetailsDialog = null;
		}
		if (ashtavargaDialog != null) {
			ashtavargaDialog.dispose();
			ashtavargaDialog = null;
		}
		positionsCheck.setSelected(false);
		houseDetailsCheck.setSelected(false);
		ashtavargaCheck.setSelected(false);
	}

	public void setHouseDetailsData(PlanetaryInfo planetaryInfo, Rasi ascendant) {
		this.planetaryInfo = planetaryInfo;
		this.ascendant = ascendant;
		if (houseDetailsDialog != null && houseDetailsDialog.isDisplayable()) {
			boolean wasVisible = houseDetailsDialog.isVisible();
			Window owner = houseDetailsDialog.getOwner();
			houseDetailsDialog.dispose();
			if (wasVisible) {
				houseDetailsDialog = new HouseDetailsDialog(owner, planetaryInfo, ascendant, () -> {
					houseDetailsCheck.setSelected(false);
				});
				houseDetailsDialog.setVisible(true);
			}
		}
	}

	public void setAshtavargaData(Ashtavarga ashtavarga, Rasi ascendant, Rasi moonRasi, EnumMap<Planet, Rasi> planetRasis) {
		this.ashtavarga = ashtavarga;
		this.ascendant = ascendant;
		this.moonRasi = moonRasi;
		this.planetRasis = planetRasis;
		if (ashtavargaDialog != null && ashtavargaDialog.isDisplayable()) {
			boolean wasVisible = ashtavargaDialog.isVisible();
			Window owner = ashtavargaDialog.getOwner();
			ashtavargaDialog.dispose();
			ashtavargaDialog = null;
			if (wasVisible) {
				ashtavargaDialog = new AshtavargaDialog(owner, ashtavarga, ascendant, moonRasi, planetRasis, () -> {
					ashtavargaCheck.setSelected(false);
				});
				ashtavargaDialog.setVisible(true);
			}
		}
	}

	private void relayout() {
		boolean showPositions = positionsCheck.isSelected();
		boolean showShadbala = shadbalaCheck.isSelected();
		boolean showAshtavarga = ashtavargaCheck.isSelected() && ashtavarga == null && horoscope == null;

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
			if (showPositions && planetaryInfo == null) {
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
			if (showPositions && planetaryInfo == null) {
				rightContainer.add(Box.createVerticalStrut(showShadbala ? 12 : 20));
				rightContainer.add(positionsView);
			}
			add(rightWrapper, BorderLayout.CENTER);
		}

		updateLayout();
	}

	public void updateLayout() {
		Dimension right = rightContainer.getPreferredSize();
		int rightW = Math.max(CHART_WIDTH, right.width);
		int totalW = collapsed ? rightW : LEFT_WIDTH + COLUMN_GAP + rightW;
		int totalH = collapsed ? right.height : Math.max(leftWrapper.getPreferredSize().height, right.height);
		setPreferredSize(new Dimension(totalW, totalH));

		revalidate();
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension right = rightContainer.getPreferredSize();
		int rightW = Math.max(CHART_WIDTH, right.width);
		int totalW = collapsed ? rightW : LEFT_WIDTH + COLUMN_GAP + rightW;
		int totalH = collapsed ? right.height : Math.max(leftWrapper.getPreferredSize().height, right.height);
		return new Dimension(totalW, totalH);
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
