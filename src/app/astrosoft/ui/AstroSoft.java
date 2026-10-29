/**
 * AstroSoft.java
 *
 * Created on December 13, 2002, 5:16 PM
 *
 * @author  E. Rajasekar
 */
package app.astrosoft.ui;

import java.io.File;
import java.util.logging.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.FutureTask;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import app.astrosoft.ui.comp.ChartNotesPanel;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.beans.AshtaVargaChartData;
import app.astrosoft.consts.AshtavargaName;
import app.astrosoft.consts.Ayanamsa;
import app.astrosoft.consts.Command;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.Compatibility;
import app.astrosoft.core.Ephemeris;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.Muhurtha;
import app.astrosoft.core.Panchang;
import app.astrosoft.core.Ephemeris.Mode;
import app.astrosoft.export.AstrosoftExporter;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.pref.AstrosoftPref.Preference;
import app.astrosoft.ui.comp.AstrosoftMenuBar;
import app.astrosoft.ui.comp.AstrosoftToolBar;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.DateListener;
import app.astrosoft.ui.dlg.AboutDialog;
import app.astrosoft.ui.dlg.BirthInputDialog;
import app.astrosoft.util.Version;
import app.astrosoft.ui.dlg.CompatibilityInputDialog;
import app.astrosoft.ui.dlg.ExportDialog;
import app.astrosoft.ui.dlg.MuhurthaInput;
import app.astrosoft.ui.dlg.ComputeNumberDialog;
import app.astrosoft.ui.dlg.OptionDialog;
import app.astrosoft.ui.dlg.PlaceOptionDialog;
import app.astrosoft.ui.dlg.PrintDialog;
import app.astrosoft.ui.dlg.TimeInputDialog;
import app.astrosoft.ui.dlg.TransitChartDialog;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.view.AshtavargaView;
import app.astrosoft.ui.view.BhavaView;
import app.astrosoft.ui.view.CompatibilityView;
import app.astrosoft.ui.view.EphemerisView;
import app.astrosoft.ui.view.FindNameView;
import app.astrosoft.ui.view.InfoView;
import app.astrosoft.ui.view.MuhurthaPanel;
import app.astrosoft.ui.view.PanchangView;
import app.astrosoft.ui.view.PlanetView;
import app.astrosoft.ui.view.ShadBalaView;
import app.astrosoft.ui.view.VargaChartView;
import app.astrosoft.ui.view.ViewContainer;
import app.astrosoft.ui.view.ViewManager;
import app.astrosoft.ui.view.VimDasaView;
import app.astrosoft.ui.view.YogaCombinationsView;
import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.core.MuhurtaDocument;
import app.astrosoft.export.XMLHelper;
import app.astrosoft.ui.view.ViewManager.View;
import app.astrosoft.util.AstroUtil;
import app.astrosoft.util.AstrosoftFileFilter;
import app.astrosoft.util.FileOps;

public class AstroSoft extends javax.swing.JFrame implements
		AstrosoftActionHandler {

	private static final Logger log = Logger.getLogger(AstroSoft.class.getName());

	private static AstroSoft instance;

	private View currentView = View.CHART_VIEW;

	private app.astrosoft.ui.view.MyMuhurtaView myMuhurtaView;

	private Horoscope h;

	private Ephemeris eph;	

	private Panchang pan;

	private AstrosoftMenuBar mbar;

	private AstrosoftToolBar toolbar;

	private JPanel home;

	private ChartNotesPanel chartNotesPanel;

	private JSplitPane mainSplitPane;

	private JDialog floatingNotesDialog;
	private TransitChartDialog transitChartDialog;

	private boolean isNotesFolded = true;

	private int lastNotesDividerLocation = -1;

	private AstrosoftActionManager actionMgr;

	private ViewManager viewManager;

	private Compatibility compatibility;

	private Muhurtha muhurtha;

	private static AstrosoftPref preferences = new AstrosoftPref();

	//public static Calendar today = new GregorianCalendar(); //AstroSoft.getPreferences().getPlace().astrosoftTimeZone().getTimeZone());

	public static Calendar today = new GregorianCalendar(AstroSoft.getPreferences().getPlace().astrosoftTimeZone().getTimeZone());

	//Until Abslayout is completely removed:

	private static Rectangle bounds = new Rectangle(0,0, getScreenSize().width, getScreenSize().height);

	public static AstroSoft getInstance() {
		return instance;
	}

	public interface SavePromptDelegate {
		int askSave(String title, java.awt.Component parent);
	}

	public interface ExitHandler {
		void exit(int status);
	}

	private static SavePromptDelegate savePromptDelegate = (title, parent) -> {
		Object[] options = new Object[] { "Save", "Don't Save", "Cancel" };
		return JOptionPane.showOptionDialog(
				parent,
				"Do you want to save changes to \"" + title + "\"?",
				"Astrosoft 2.0",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				options,
				options[0]
		);
	};

	private static ExitHandler exitHandler = status -> System.exit(status);

	public static void setSavePromptDelegate(SavePromptDelegate delegate) {
		savePromptDelegate = delegate;
	}

	public static void setExitHandler(ExitHandler handler) {
		exitHandler = handler;
	}

	public AstroSoft() {

		instance = this;
		initComponents();

        // WindowPlacement controls outer startup bounds, not the display dimensions.
		//this.setPreferredSize(new Dimension(800, 600));

		// show();
		getContentPane().setLayout(new BorderLayout());
		formMenuAndToolBar();
		home = new JPanel();
		home.setLayout(null);
		home.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		home.setBounds(bounds);

		initNotes();
		getContentPane().add(mainSplitPane, BorderLayout.CENTER);

		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				getContentPane().validate();
				if (home.getComponentCount() > 0) {
				    java.awt.Component view = home.getComponent(0);
				    if (view.getClass().getName().contains("MyMuhurtaView")
				            || isChartView(view)
				            || view.getClass().getName().contains("MarriageMatchView")) {
					    view.setBounds(0, 0, home.getWidth(), home.getHeight());
				    }
				    // Do absolutely nothing for other views! Let them keep their original internal bounds & offsets.
					view.revalidate();
				}
			}
		});

        enableActions(false);
        pack();
        WindowPlacement.install(this);
        setVisible(true);
		viewManager = new ViewManager(new AstrosoftViewContainer());

	}

	private void initNotes() {
		chartNotesPanel = new ChartNotesPanel(new ChartNotesPanel.NotesListener() {
			@Override
			public void onFold() {
				foldNotes();
			}

			@Override
			public void onTogglePopOut(boolean isPopOut) {
				if (isPopOut) {
					undockNotes();
				} else {
					dockNotes();
				}
			}

			@Override
			public void onNotesModified() {
				actionMgr.enableAction(Command.SAVE, true);
				actionMgr.enableAction(Command.SAVE_AS, true);
			}
		});

		mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, home, new JPanel());
		mainSplitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
			@Override
			protected java.awt.Component createDefaultNonContinuousLayoutDivider() {
				JPanel p = new JPanel();
				p.setOpaque(false);
				return p;
			}
			@Override
			public void finishedPaintingChildren(JSplitPane jc, java.awt.Graphics g) {
			}
		});
		mainSplitPane.getRightComponent().setVisible(false);
		mainSplitPane.setResizeWeight(1.0);
		mainSplitPane.setContinuousLayout(true);
		mainSplitPane.setOneTouchExpandable(false);
		mainSplitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());

		// Start folded
		mainSplitPane.setDividerSize(0);

		home.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				if (home.getComponentCount() > 0) {
					java.awt.Component view = home.getComponent(0);
					if (view.getClass().getName().contains("MyMuhurtaView")
							|| isChartView(view)
							|| view.getClass().getName().contains("MarriageMatchView")) {
						view.setBounds(0, 0, home.getWidth(), home.getHeight());
					}
					view.revalidate();
				}
			}
		});
	}

	public void toggleNotes() {
		if (floatingNotesDialog != null && floatingNotesDialog.isVisible()) {
			floatingNotesDialog.toFront();
			floatingNotesDialog.requestFocus();
			return;
		}

		if (isNotesFolded) {
			unfoldNotes();
		} else {
			foldNotes();
		}
	}

	public boolean isNotesFolded() {
		return isNotesFolded;
	}

	public void foldNotes() {
		if (chartNotesPanel.isVisible() && mainSplitPane.getDividerLocation() > 0) {
			lastNotesDividerLocation = mainSplitPane.getDividerLocation();
		}
		isNotesFolded = true;
		if (mbar != null) {
			mbar.setNotesSelected(false);
		}
		mainSplitPane.setDividerSize(0);
		mainSplitPane.setRightComponent(new JPanel());
		mainSplitPane.getRightComponent().setVisible(false);
		mainSplitPane.setDividerLocation(1.0);
		mainSplitPane.revalidate();
		mainSplitPane.repaint();
		if (home.getComponentCount() > 0) {
			java.awt.Component view = home.getComponent(0);
			if (view.getClass().getName().contains("MyMuhurtaView")
					|| isChartView(view)
					|| view.getClass().getName().contains("MarriageMatchView")) {
				view.setBounds(0, 0, home.getWidth(), home.getHeight());
				view.revalidate();
			}
		}
	}

	public void unfoldNotes() {
		isNotesFolded = false;
		if (mbar != null) {
			mbar.setNotesSelected(true);
		}

		java.awt.Dimension screen = AstroSoft.getScreenSize();
		int currentW = getWidth();
		int desiredW = Math.min(screen.width - 40, currentW + 340);
		if (currentW < 1360 && desiredW > currentW) {
			setSize(desiredW, getHeight());
		}

		mainSplitPane.setRightComponent(chartNotesPanel);
		chartNotesPanel.setVisible(true);
		mainSplitPane.setDividerSize(6);
		javax.swing.SwingUtilities.invokeLater(() -> {
			int w = mainSplitPane.getWidth();
			if (w <= 0) {
				w = getContentPane().getWidth();
			}
			int divLoc = (lastNotesDividerLocation > 0 && lastNotesDividerLocation < w - 50)
					? lastNotesDividerLocation
					: Math.max(200, w - 340);
			mainSplitPane.setDividerLocation(divLoc);
			mainSplitPane.revalidate();
			mainSplitPane.repaint();
			if (home.getComponentCount() > 0) {
				java.awt.Component view = home.getComponent(0);
				if (view.getClass().getName().contains("MyMuhurtaView")
						|| isChartView(view)
						|| view.getClass().getName().contains("MarriageMatchView")) {
					view.setBounds(0, 0, home.getWidth(), home.getHeight());
					view.revalidate();
				}
			}
			chartNotesPanel.getEditor().requestFocusInWindow();
		});
	}

	public void undockNotes() {
		if (chartNotesPanel.isVisible()) {
			lastNotesDividerLocation = mainSplitPane.getDividerLocation();
		}
		isNotesFolded = true;
		if (mbar != null) {
			mbar.setNotesSelected(true);
		}
		chartNotesPanel.setVisible(true);
		chartNotesPanel.setDocked(false);
		mainSplitPane.setRightComponent(new JPanel());
		mainSplitPane.getRightComponent().setVisible(false);
		mainSplitPane.setDividerSize(0);
		mainSplitPane.setDividerLocation(1.0);

		if (floatingNotesDialog == null) {
			floatingNotesDialog = new JDialog(this, "Chart Notes", false);
			floatingNotesDialog.setSize(380, 520);
			floatingNotesDialog.setLocationRelativeTo(this);
			floatingNotesDialog.setAlwaysOnTop(true);
			floatingNotesDialog.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent e) {
					dockNotes();
				}
			});
		}
		floatingNotesDialog.getContentPane().removeAll();
		floatingNotesDialog.getContentPane().add(chartNotesPanel);
		floatingNotesDialog.setVisible(true);
		mainSplitPane.revalidate();
	}

	public void dockNotes() {
		if (floatingNotesDialog != null) {
			floatingNotesDialog.setVisible(false);
			floatingNotesDialog.getContentPane().removeAll();
		}
		chartNotesPanel.setDocked(true);
		mainSplitPane.setRightComponent(chartNotesPanel);
		unfoldNotes();
	}

	private AstroSoft(String[] args){
		

		this();
		
		System.out.println("Logging properties: " + System.getProperty("java.util.logging.config.file"));
		
		if(args.length > 0 && args[0] != null && !args[0].trim().isEmpty()){
			openHoroscope(args[0]);
		}
	}

	public void formMenuAndToolBar() {

		actionMgr = new AstrosoftActionManager(this);

		mbar = new AstrosoftMenuBar(actionMgr);
		setJMenuBar(mbar);

		toolbar = new AstrosoftToolBar(actionMgr);

		this.add(toolbar, BorderLayout.PAGE_START);

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() { // GEN-BEGIN:initComponents
		setTitle("Astrosoft 2.0");
		setName("AstroSoft");
		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {

			public void windowClosing(java.awt.event.WindowEvent evt) {

				exitForm(evt);

			}

		});

		try {
			if (java.awt.Desktop.isDesktopSupported()) {
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				if (desktop.isSupported(java.awt.Desktop.Action.APP_QUIT_HANDLER)) {
					desktop.setQuitHandler((e, response) -> {
						log.info("Desktop.setQuitHandler triggered by macOS!");
						if (canExit()) {
							WindowPlacement.saveNow(AstroSoft.this);
							response.performQuit();
						} else {
							response.cancelQuit();
						}
					});
				}
			}
		} catch (Exception ignored) {}

	} // GEN-END:initComponents

	public boolean canExit() {
		log.info("canExit() invoked!");
		if (compatibility != null && !promptSaveIfModified(compatibility)) {
			return false;
		}
		if (currentView == View.MY_MUHURTA_VIEW) {
			if (myMuhurtaView != null && !promptSaveIfModified(myMuhurtaView.getDocument())) {
				return false;
			}
			if (h != null && !promptSaveIfModified(h)) {
				return false;
			}
		} else {
			if (h != null && !promptSaveIfModified(h)) {
				return false;
			}
			if (myMuhurtaView != null && !promptSaveIfModified(myMuhurtaView.getDocument())) {
				return false;
			}
		}
		return true;
	}

	public void exitApplication() {
		log.info("exitApplication() invoked!");
		exitForm(null);
	}

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) { // GEN-FIRST:event_exitForm
		log.info("exitForm invoked with evt=" + evt);
		if (!canExit()) {
			log.info("exitForm cancelled by canExit()");
			return;
		}
		log.info("exitForm proceeding to exit!");
        WindowPlacement.saveNow(this);
        if (exitHandler != null) {
            exitHandler.exit(0);
        } else {
            System.exit(0);
        }

	} // GEN-LAST:event_exitForm

	static {
		if (System.getProperty("awt.useSystemAAFontSettings") == null) {
			System.setProperty("awt.useSystemAAFontSettings", "lcd");
		}
		if (System.getProperty("swing.aatext") == null) {
			System.setProperty("swing.aatext", "true");
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		
		try {

			//FIXME:
			 //UIManager.setLookAndFeel(
			 //"com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

			// UIManager.setLookAndFeel(new
			// com.incors.plaf.kunststoff.KunststoffLookAndFeel());
			// UIManager.setLookAndFeel(new
			// net.sourceforge.mlf.metouia.MetouiaLookAndFeel());
			//UIManager
				//.setLookAndFeel("com.digitprop.tonic.TonicLookAndFeel");

			// Skin theSkinToUse = SkinLookAndFeel.loadThemePack(args[0] +
			// "/lib/" + "themepack.zip");
			// SkinLookAndFeel.setSkin(theSkinToUse);

			// finally set the Skin Look And Feel
			// UIManager.setLookAndFeel(new SkinLookAndFeel());

			 //UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
			 //com.jgoodies.looks.plastic.PlasticLookAndFeel.setMyCurrentTheme(new
			 //ExperienceGreen());

			//Plastic3DLookAndFeel.setMyCurrentTheme(new ExperienceGreen());
   		    //UIManager.setLookAndFeel(new Plastic3DLookAndFeel());

			/*
			 * Skin theSkinToUse =
			 * SkinLookAndFeel.loadThemePack("C:\\Data\\AstroSoft\\lib\\toxicthemepack.zip");
			 * SkinLookAndFeel.setSkin(theSkinToUse);
			 *
			 * UIManager.setLookAndFeel(new SkinLookAndFeel());
			 */


		} catch (Exception e) {

			e.printStackTrace();
		}

		setUIDefaults();
		AstroSoft as = new AstroSoft(args);
		//as.setVisible(true);
	}

	private static void setUIDefaults() {

		try {
			UIManager.setLookAndFeel(UIConsts.getLookAndFeel());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
			String[] components = {"TextField", "TextArea", "TextPane", "EditorPane", "PasswordField", "ComboBox", "FormattedTextField"};
			for (String comp : components) {
				javax.swing.InputMap im = (javax.swing.InputMap) UIManager.get(comp + ".focusInputMap");
				if (im != null) {
					im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.META_DOWN_MASK), javax.swing.text.DefaultEditorKit.copyAction);
					im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.META_DOWN_MASK), javax.swing.text.DefaultEditorKit.pasteAction);
					im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.META_DOWN_MASK), javax.swing.text.DefaultEditorKit.cutAction);
					im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.META_DOWN_MASK), javax.swing.text.DefaultEditorKit.selectAllAction);
				}
			}
		}
		
		//JFrame.setDefaultLookAndFeelDecorated(true);
		Map defaults = UIConsts.getUIDefaults();

		for(Object key : defaults.keySet()){
			UIManager.put(key, defaults.get(key));
		}
	}

	public void computeHoroscope(BirthData birthData){
		computeHoroscope(birthData, false);
	}

	public void computeHoroscope(BirthData birthData, boolean isEdit){
		String prevFilePath = (isEdit && h != null) ? h.getFilePath() : null;
		if (isEdit && birthData.getNotes() == null && h != null && h.getNotes() != null) {
			birthData.setNotes(h.getNotes());
		}
		h = new Horoscope(birthData);
		if (prevFilePath != null) {
			h.setFilePath(prevFilePath);
			setTitle("Astrosoft 2.0 - " + new File(prevFilePath).getName());
		} else {
			setTitle("Astrosoft 2.0 - " + birthData.name() + " (Unsaved)");
		}
		if (chartNotesPanel != null) {
			chartNotesPanel.setHoroscope(h);
		}
		h.setModified(true);
		enableActions(true);
		viewManager.showView(View.CHART_VIEW);
	}

	public JPanel createChartView() {

		JPanel infoView = new InfoView(h.getHoroscopeInfo(), new Point(20,20));
		
		final Chart rasi = new Chart(new PlanetChartData(Varga.Rasi, h.getPlanetaryInfo()), new Dimension(360,320));
		final Chart navamsa = new Chart(new PlanetChartData(Varga.Navamsa, h.getPlanetaryInfo()), new Dimension(360,320));
		rasi.setTitleBadge(UIConsts.NAVY_BADGE_BG, UIConsts.NAVY_BADGE_BG, Color.WHITE);
		navamsa.setTitleBadge(UIConsts.NAVY_BADGE_BG, UIConsts.NAVY_BADGE_BG, Color.WHITE);
		rasi.setBorder(javax.swing.BorderFactory.createLineBorder(UIConsts.ASHTAVARGA_DIVIDER_CLR, 1));
		navamsa.setBorder(javax.swing.BorderFactory.createLineBorder(UIConsts.ASHTAVARGA_DIVIDER_CLR, 1));

		final JPanel rasiNavamsaPanel = new JPanel(new GridLayout(1, 2, 20, 0));
		rasiNavamsaPanel.setOpaque(false);
		rasiNavamsaPanel.add(rasi);
		rasiNavamsaPanel.add(navamsa);
		rasiNavamsaPanel.setPreferredSize(new Dimension(740, 320));
		rasiNavamsaPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

		final javax.swing.JComponent ashtavargaPane = app.astrosoft.ui.view.AshtavargaView.createAshtavargaGridPanel(h.getAshtaVarga(), h.getAscendant(), h.getRasi());

		final JPanel ashtavargaWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ashtavargaWrapper.setOpaque(false);
		ashtavargaWrapper.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
		ashtavargaWrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 0, 0, 0));
		ashtavargaWrapper.setPreferredSize(new Dimension(ashtavargaPane.getPreferredSize().width, ashtavargaPane.getPreferredSize().height + 25));
		ashtavargaWrapper.setMaximumSize(new Dimension(ashtavargaPane.getPreferredSize().width, ashtavargaPane.getPreferredSize().height + 25));
		ashtavargaWrapper.add(ashtavargaPane);

		final JPanel rightContainer = new JPanel();
		rightContainer.setLayout(new javax.swing.BoxLayout(rightContainer, javax.swing.BoxLayout.Y_AXIS));
		rightContainer.setOpaque(false);
		rightContainer.add(rasiNavamsaPanel);
		rightContainer.setPreferredSize(new Dimension(760, 320));

		final JPanel rightWrapper = new JPanel(new BorderLayout());
		rightWrapper.setOpaque(false);
		rightWrapper.add(rightContainer, BorderLayout.NORTH);

		final JPanel contentPanel = new JPanel(new BorderLayout(40, 0));
		contentPanel.setOpaque(false);

		final JPanel planetOffsetsView = new app.astrosoft.ui.view.RelativePlanetView(h.getPlanetaryInfo());
		planetOffsetsView.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
		infoView.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

		final JPanel shadbalaBarView = new app.astrosoft.ui.view.ShadBalaBarView(h.getShadBala());
		shadbalaBarView.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

		final javax.swing.JPanel leftPanel = new javax.swing.JPanel();
		leftPanel.setLayout(new javax.swing.BoxLayout(leftPanel, javax.swing.BoxLayout.Y_AXIS));
		leftPanel.setOpaque(false);

		final javax.swing.JPanel leftWrapper = new javax.swing.JPanel(new java.awt.BorderLayout());
		leftWrapper.setOpaque(false);
		leftWrapper.setPreferredSize(new Dimension(530, 540));
		leftWrapper.add(leftPanel, java.awt.BorderLayout.NORTH);

		final javax.swing.JPanel controlsPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 0));
		controlsPanel.setOpaque(false);
		controlsPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
		controlsPanel.setPreferredSize(new Dimension(525, 28));
		controlsPanel.setMinimumSize(new Dimension(300, 28));
		controlsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

		final javax.swing.JCheckBox planetOffsetsCheck = new javax.swing.JCheckBox("Planet Offsets");
		planetOffsetsCheck.setSelected(true);
		planetOffsetsCheck.setOpaque(false);
		planetOffsetsCheck.setFocusable(false);
		planetOffsetsCheck.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 11));
		planetOffsetsCheck.setForeground(UIConsts.TEXT_DARK_NAVY);
		controlsPanel.add(planetOffsetsCheck);

		final javax.swing.JCheckBox shadbalaCheck = new javax.swing.JCheckBox("Shadbala");
		shadbalaCheck.setSelected(false);
		shadbalaCheck.setOpaque(false);
		shadbalaCheck.setFocusable(false);
		shadbalaCheck.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 11));
		shadbalaCheck.setForeground(UIConsts.TEXT_DARK_NAVY);
		controlsPanel.add(shadbalaCheck);

		final javax.swing.JCheckBox ashtaVargaCheck = new javax.swing.JCheckBox("AshtaVarga");
		ashtaVargaCheck.setOpaque(false);
		ashtaVargaCheck.setFocusable(false);
		ashtaVargaCheck.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 11));
		ashtaVargaCheck.setForeground(UIConsts.TEXT_DARK_NAVY);
		controlsPanel.add(ashtaVargaCheck);

		final JPanel contentWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
		contentWrapper.setOpaque(true);
		contentWrapper.setBackground(UIConsts.CANVAS_BACKGROUND);
		contentWrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 25));
		contentWrapper.add(contentPanel);

		final javax.swing.JScrollPane mainScrollPane = new javax.swing.JScrollPane(contentWrapper,
				javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mainScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		mainScrollPane.getViewport().setOpaque(true);
		mainScrollPane.getViewport().setBackground(UIConsts.CANVAS_BACKGROUND);
		mainScrollPane.setOpaque(true);
		mainScrollPane.setBackground(UIConsts.CANVAS_BACKGROUND);
		mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		mainScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

		final Runnable updateLeftLayout = new Runnable() {
			public void run() {
				boolean showOffsets = planetOffsetsCheck.isSelected();
				boolean showShadbala = shadbalaCheck.isSelected();
				leftPanel.removeAll();
				leftPanel.add(infoView);
				leftPanel.add(javax.swing.Box.createVerticalStrut(50));
				javax.swing.JSeparator sep1 = new javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL);
				sep1.setForeground(new Color(226, 232, 240));
				leftPanel.add(sep1);
				leftPanel.add(javax.swing.Box.createVerticalStrut(6));
				leftPanel.add(controlsPanel);
				if (showOffsets) {
					leftPanel.add(javax.swing.Box.createVerticalStrut(6));
					javax.swing.JSeparator sep2 = new javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL);
					sep2.setForeground(new Color(226, 232, 240));
					leftPanel.add(sep2);
					leftPanel.add(javax.swing.Box.createVerticalStrut(12));
					leftPanel.add(planetOffsetsView);
				}
				if (showShadbala) {
					if (!showOffsets) {
						leftPanel.add(javax.swing.Box.createVerticalStrut(6));
						javax.swing.JSeparator sep3 = new javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL);
						sep3.setForeground(new Color(226, 232, 240));
						leftPanel.add(sep3);
					}
					leftPanel.add(javax.swing.Box.createVerticalStrut(12));
					leftPanel.add(shadbalaBarView);
				}
				leftPanel.revalidate();
				leftPanel.repaint();
				int h = leftPanel.getPreferredSize().height;
				leftWrapper.setPreferredSize(new Dimension(530, Math.max(540, h)));
				leftWrapper.revalidate();
				leftWrapper.repaint();
				contentPanel.revalidate();
				contentPanel.repaint();
				contentWrapper.revalidate();
				contentWrapper.repaint();
				mainScrollPane.revalidate();
				mainScrollPane.repaint();
			}
		};

		planetOffsetsCheck.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				updateLeftLayout.run();
			}
		});

		shadbalaCheck.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				updateLeftLayout.run();
			}
		});

		updateLeftLayout.run();

		ashtaVargaCheck.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				boolean show = ashtaVargaCheck.isSelected();
				rightContainer.removeAll();
				rightContainer.add(rasiNavamsaPanel);
				if (show) {
					rightContainer.add(ashtavargaWrapper);
					int rightW = Math.max(760, ashtavargaWrapper.getPreferredSize().width);
					rightContainer.setPreferredSize(new Dimension(rightW, 320 + ashtavargaWrapper.getPreferredSize().height));
				} else {
					rightContainer.setPreferredSize(new Dimension(760, 320));
				}
				rightContainer.revalidate();
				rightContainer.repaint();
				rightWrapper.revalidate();
				rightWrapper.repaint();
				contentPanel.revalidate();
				contentPanel.repaint();
				contentWrapper.revalidate();
				contentWrapper.repaint();
				mainScrollPane.revalidate();
				mainScrollPane.repaint();
			}
		});

		contentPanel.add(leftWrapper, BorderLayout.WEST);
		contentPanel.add(rightWrapper, BorderLayout.CENTER);

		JPanel view = new JPanel(new BorderLayout());
		view.setName("ChartView");
		view.setOpaque(true);
		view.setBackground(UIConsts.CANVAS_BACKGROUND);
		view.add(mainScrollPane, BorderLayout.CENTER);
		view.setBounds(bounds);

		return view;

	}

	public void printHoroscope() {
		new PrintDialog(this);
	}

	public JPanel createShadbalaView(){
		if(h.getShadBala() != null){
			return new ShadBalaView(h.getTitle(), h.getShadBala(), new Point(30,20));
		}else{
			OptionDialog.showDialog("No ShadBala for Birth Year < 1900", JOptionPane.ERROR_MESSAGE);
			return createChartView();
		}
	}

	public void showPanchang(Calendar date) {

		pan = new Panchang(date);
		viewManager.showView(View.PANCHANG_VIEW);

	}

	public void onSwitchToMarriageMatchView() {
		currentView = View.MARRIAGE_MATCH_VIEW;
		if (chartNotesPanel != null) {
			chartNotesPanel.flushToOwner();
			chartNotesPanel.bindNotesOwner(compatibility);
		}
		actionMgr.enableAction(Command.NOTES, true);
		actionMgr.enableAction(Command.SAVE, true);
		actionMgr.enableAction(Command.SAVE_AS, true);
		actionMgr.enableAction(Command.PRINT, true);
		updateMarriageMatchTitle();
	}

	public void onSwitchToCompatibilityView() {
		currentView = View.COMPATIBILITY_VIEW;
		if (chartNotesPanel != null) {
			chartNotesPanel.flushToOwner();
			chartNotesPanel.bindNotesOwner(compatibility);
		}
		actionMgr.enableAction(Command.NOTES, true);
		actionMgr.enableAction(Command.SAVE, true);
		actionMgr.enableAction(Command.SAVE_AS, true);
		actionMgr.enableAction(Command.PRINT, true);
		updateCompatibilityTitle();
	}

	public void updateMarriageMatchTitle() {
		if (compatibility != null) {
			String bName = (compatibility.getBoyName() != null && !compatibility.getBoyName().trim().isEmpty()) ? compatibility.getBoyName() : "Boy";
			String gName = (compatibility.getGirlName() != null && !compatibility.getGirlName().trim().isEmpty()) ? compatibility.getGirlName() : "Girl";
			if (compatibility.getFilePath() != null) {
				setTitle("Astrosoft 2.0 - " + new File(compatibility.getFilePath()).getName() + " [Marriage Match]");
			} else {
				setTitle("Astrosoft 2.0 - Marriage Match (" + bName + " & " + gName + ")");
			}
		}
	}

	public void updateCompatibilityTitle() {
		if (compatibility != null) {
			String bName = (compatibility.getBoyName() != null && !compatibility.getBoyName().trim().isEmpty()) ? compatibility.getBoyName() : "Boy";
			String gName = (compatibility.getGirlName() != null && !compatibility.getGirlName().trim().isEmpty()) ? compatibility.getGirlName() : "Girl";
			if (compatibility.getFilePath() != null) {
				setTitle("Astrosoft 2.0 - " + new File(compatibility.getFilePath()).getName() + " [Compatibility]");
			} else {
				setTitle("Astrosoft 2.0 - Marriage Compatibility (" + bName + " & " + gName + ")");
			}
		}
	}

	public void displayCompatibility(Horoscope boy, Horoscope girl) {
		if (h != null && !promptSaveIfModified(h)) return;
		if (compatibility != null && !promptSaveIfModified(compatibility)) return;
		if (myMuhurtaView != null && !promptSaveIfModified(myMuhurtaView.getDocument())) return;

		compatibility = new Compatibility(boy, girl);
		compatibility.setModified(false);
		onSwitchToCompatibilityView();
		viewManager.showView(View.COMPATIBILITY_VIEW);
	}

	public void displayCompatibility(String bName, String gName, Rasi bRasi,
			Rasi gRasi, Nakshathra bNak, Nakshathra gNak) {
		if (h != null && !promptSaveIfModified(h)) return;
		if (compatibility != null && !promptSaveIfModified(compatibility)) return;
		if (myMuhurtaView != null && !promptSaveIfModified(myMuhurtaView.getDocument())) return;

		compatibility = new Compatibility(bName, gName, bNak, gNak, bRasi, gRasi);
		compatibility.setModified(false);
		onSwitchToCompatibilityView();
		viewManager.showView(View.COMPATIBILITY_VIEW);
	}

	public void displayMarriageMatch(Horoscope boy, Horoscope girl) {
		if (h != null && !promptSaveIfModified(h)) return;
		if (compatibility != null && !promptSaveIfModified(compatibility)) return;
		if (myMuhurtaView != null && !promptSaveIfModified(myMuhurtaView.getDocument())) return;

		compatibility = new Compatibility(boy, girl);
		compatibility.setModified(false);
		onSwitchToMarriageMatchView();
		viewManager.showView(View.MARRIAGE_MATCH_VIEW);
	}

	public void displayMarriageMatch(String bName, String gName, Rasi bRasi,
			Rasi gRasi, Nakshathra bNak, Nakshathra gNak) {
		if (h != null && !promptSaveIfModified(h)) return;
		if (compatibility != null && !promptSaveIfModified(compatibility)) return;
		if (myMuhurtaView != null && !promptSaveIfModified(myMuhurtaView.getDocument())) return;

		compatibility = new Compatibility(bName, gName, bNak, gNak, bRasi, gRasi);
		compatibility.setModified(false);
		onSwitchToMarriageMatchView();
		viewManager.showView(View.MARRIAGE_MATCH_VIEW);
	}

	public void displayCompactibility(Horoscope boy, Horoscope girl) {
		displayCompatibility(boy, girl);
	}

	public void displayCompactibility(String bName, String gName, Rasi bRasi,
			Rasi gRasi, Nakshathra bNak, Nakshathra gNak) {
		displayCompatibility(bName, gName, bRasi, gRasi, bNak, gNak);
	}

	public void displayMuhurtha(Muhurtha m) {
		currentView = View.MUHURTHA_VIEW;
		onSwitchToUtilityView();
		muhurtha = m;
		setTitle("Astrosoft 2.0 - Muhurtha");
		viewManager.showView(View.MUHURTHA_VIEW);
		// Muhurtha m = new Muhurtha(today, Rasi.Mesha, Nakshathra.Mrigasira,
		// false, false, 1);
	}


	public void newHoroscope() {
		if (currentView == View.MY_MUHURTA_VIEW && myMuhurtaView != null && !promptSaveIfModified(myMuhurtaView.getDocument())) {
			return;
		}
		if (!promptSaveIfModified(h)) {
			return;
		}
		new BirthInputDialog(this);
	}

	public void openHoroscope() {
		log.info("openHoroscope() invoked - launching open file dialog");
		String selectedFile = FileOps.openFileDialog(this, FileOps.FileDialogMode.OPEN,
				AstrosoftFileFilter.ALL_ASTROSOFT,
				AstrosoftFileFilter.HOROSCOPE_EXTN,
				AstrosoftFileFilter.MUHURTA_EXTN,
				AstrosoftFileFilter.COMPACTIBILITY_EXTN);
		
		log.info("File selected from open dialog: " + selectedFile);
		if (selectedFile != null) {
			openDocument(selectedFile);
		}
	}

	public void openDocument(String file) {
		log.info("openDocument() invoked for: " + file);
		if (file == null) return;
		String lower = file.toLowerCase();
		if (lower.endsWith(".asm")) {
			openMuhurta(file);
		} else if (lower.endsWith(".asc")) {
			openCompatibility(file);
		} else {
			// Inspect root XML tag for .ash or any file format
			try {
				org.w3c.dom.Document doc = XMLHelper.parseXML(file);
				if (doc != null && doc.getDocumentElement() != null) {
					String rootName = doc.getDocumentElement().getNodeName();
					if ("Muhurta".equalsIgnoreCase(rootName)) {
						openMuhurta(file);
						return;
					} else if ("Compatibility".equalsIgnoreCase(rootName) || "Compactibility".equalsIgnoreCase(rootName)) {
						openCompatibility(file);
						return;
					}
				}
			} catch (Exception ignored) {}
			openHoroscope(file);
		}
	}

	public void openMuhurta(String file) {
		log.info("openMuhurta() invoked for: " + file);
		if (currentView != View.MY_MUHURTA_VIEW && h != null && !promptSaveIfModified(h)) {
			log.info("openMuhurta() cancelled by promptSaveIfModified on Horoscope document");
			return;
		}
		if (myMuhurtaView != null && !promptSaveIfModified(myMuhurtaView.getDocument())) {
			log.info("openMuhurta() cancelled by promptSaveIfModified on Muhurta document");
			return;
		}
		MuhurtaDocument doc = MuhurtaDocument.createFromFile(file);
		if (doc != null) {
			doc.setFilePath(file);
			try {
				getPreferences().setAstrosoftFilesDir(new File(file).getParent() + File.separator);
			} catch (Exception e) {}
			getMyMuhurtaView().loadDocument(doc);
			onSwitchToMuhurtaView();
			viewManager.showView(View.MY_MUHURTA_VIEW);
			if (chartNotesPanel != null) {
				chartNotesPanel.bindNotesOwner(doc);
			}
		} else {
			OptionDialog.showDialog(" Invalid Muhurta File ", JOptionPane.ERROR_MESSAGE);
			this.repaint();
			this.setVisible(true);
		}
	}

	public void openHoroscope(String horoscopeFile) {
		log.info("openHoroscope(file) invoked for: " + horoscopeFile);
		if (currentView == View.MY_MUHURTA_VIEW && myMuhurtaView != null && !promptSaveIfModified(myMuhurtaView.getDocument())) {
			log.info("openHoroscope(file) cancelled by promptSaveIfModified on Muhurta document");
			return;
		}
		if (!promptSaveIfModified(h)) {
			log.info("openHoroscope(file) cancelled by promptSaveIfModified on Horoscope");
			return;
		}

		h = Horoscope.createFromFile(horoscopeFile);

		if (h != null) {
			h.setFilePath(horoscopeFile);
			h.setModified(false);
			setTitle("Astrosoft 2.0 - " + new File(horoscopeFile).getName());
			try {
				getPreferences().setAstrosoftFilesDir(new File(horoscopeFile).getParent() + File.separator);
			} catch (Exception e) {}
			if (chartNotesPanel != null) {
				chartNotesPanel.bindNotesOwner(h);
			}
			enableActions(true);

		} else {
			OptionDialog.showDialog(" Invalid File ", JOptionPane.ERROR_MESSAGE);
			this.repaint();
			this.setVisible(true);
			return;
		}
		currentView = View.CHART_VIEW;
		viewManager.showView(View.CHART_VIEW);
	}

	public boolean promptSaveIfModified(app.astrosoft.beans.NotesOwner owner) {
		if (owner == null) {
			return true;
		}

		if (chartNotesPanel != null) {
			chartNotesPanel.flushToOwner();
		}

		log.info("promptSaveIfModified for " + owner.getNotesOwnerTitle() + ", isModified=" + owner.isModified());

		if (!owner.isModified()) {
			return true;
		}

		String title = owner.getNotesOwnerTitle();
		if (title == null || title.trim().isEmpty()) {
			title = (owner instanceof Horoscope) ? "Horoscope" : "Muhurta";
		}

		int choice = (savePromptDelegate != null) ?
				savePromptDelegate.askSave(title, this) :
				JOptionPane.showOptionDialog(
						this,
						"Do you want to save changes to \"" + title + "\"?",
						"Astrosoft 2.0",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						new Object[] { "Save", "Don't Save", "Cancel" },
						"Save"
				);

		if (choice == 0) { // Save
			if (owner instanceof Horoscope) {
				return saveHoroscopeDirect();
			} else if (owner instanceof MuhurtaDocument) {
				return saveMuhurtaDirect();
			} else if (owner instanceof Compatibility) {
				saveCompatibility();
				return !owner.isModified();
			}
			return true;
		} else if (choice == 1) { // Don't Save
			owner.setModified(false);
			return true;
		} else { // Cancel or Closed (-1)
			return false;
		}
	}

	public void saveHoroscope() {
		if (currentView == View.MY_MUHURTA_VIEW) {
			saveMuhurtaDirect();
		} else if (currentView == View.MARRIAGE_MATCH_VIEW || currentView == View.COMPATIBILITY_VIEW) {
			saveCompatibility();
		} else if (h != null){
			saveHoroscopeDirect();
		}
	}

	public void saveAsHoroscope() {
		if (currentView == View.MY_MUHURTA_VIEW) {
			saveAsMuhurtaDirect();
		} else if (currentView == View.MARRIAGE_MATCH_VIEW || currentView == View.COMPATIBILITY_VIEW) {
			saveAsCompatibility();
		} else if (h != null){
			saveAsHoroscopeDirect();
		}
	}

	public void saveMuhurta() {
		saveMuhurtaDirect();
	}

	public void saveAsMuhurta() {
		saveAsMuhurtaDirect();
	}

	public boolean saveHoroscopeDirect() {
		if (h == null) return true;
		if (chartNotesPanel != null) {
			chartNotesPanel.flushToOwner();
		}

		String currentFile = h.getFilePath();
		if (currentFile != null && !currentFile.trim().isEmpty()) {
			h.saveToFile(currentFile);
			setTitle("Astrosoft 2.0 - " + new File(currentFile).getName());
			log.info("Saved horoscope directly to: " + currentFile);
			return true;
		} else {
			return saveAsHoroscopeDirect();
		}
	}

	public boolean saveAsHoroscopeDirect() {
		if (h == null) return true;
		if (chartNotesPanel != null) {
			chartNotesPanel.flushToOwner();
		}

		String selectedFile = FileOps.openFileDialog(this, FileOps.FileDialogMode.SAVE, AstrosoftFileFilter.HOROSCOPE_EXTN);
		if (selectedFile != null) {
			h.saveToFile(selectedFile);
			setTitle("Astrosoft 2.0 - " + new File(selectedFile).getName());
			return true;
		}
		return false;
	}

	public boolean saveMuhurtaDirect() {
		if (myMuhurtaView == null) return true;
		if (chartNotesPanel != null) {
			chartNotesPanel.flushToOwner();
		}
		MuhurtaDocument doc = myMuhurtaView.getDocument();
		if (doc == null) return true;

		String currentFile = doc.getFilePath();
		if (currentFile != null && !currentFile.trim().isEmpty()) {
			doc.saveToFile(currentFile);
			updateMuhurtaTitle();
			log.info("Saved Muhurta directly to: " + currentFile);
			return true;
		} else {
			return saveAsMuhurtaDirect();
		}
	}

	public boolean saveAsMuhurtaDirect() {
		if (myMuhurtaView == null) return true;
		if (chartNotesPanel != null) {
			chartNotesPanel.flushToOwner();
		}
		MuhurtaDocument doc = myMuhurtaView.getDocument();
		if (doc == null) return true;

		String selectedFile = FileOps.openFileDialog(this, FileOps.FileDialogMode.SAVE, AstrosoftFileFilter.MUHURTA_EXTN);
		if (selectedFile != null) {
			doc.saveToFile(selectedFile);
			updateMuhurtaTitle();
			return true;
		}
		return false;
	}

	public void openCompatibility(){
		String fileName = FileOps.openFileDialog(this, FileOps.FileDialogMode.OPEN, AstrosoftFileFilter.COMPATIBILITY_EXTN);
		if (fileName != null) {
			openCompatibility(fileName);
		}
	}

	public void openCompactibility(){
		openCompatibility();
	}

	public void openCompatibility(String fileName){
		if (fileName != null) {
			if (currentView == View.MY_MUHURTA_VIEW && myMuhurtaView != null && !promptSaveIfModified(myMuhurtaView.getDocument())) {
				return;
			}
			if (h != null && !promptSaveIfModified(h)) {
				return;
			}
			if (compatibility != null && !promptSaveIfModified(compatibility)) {
				return;
			}
			compatibility = Compatibility.createFromXML(fileName);
		
			if (compatibility != null){
				compatibility.setFilePath(fileName);
				try {
					getPreferences().setAstrosoftFilesDir(new File(fileName).getParent() + File.separator);
				} catch (Exception e) {}
				if (compatibility.hasHoroscope()) {
					onSwitchToMarriageMatchView();
					viewManager.showView(View.MARRIAGE_MATCH_VIEW);
				} else {
					onSwitchToCompatibilityView();
					viewManager.showView(View.COMPATIBILITY_VIEW);
				}
			}
		}
	}

	public void openCompactibility(String fileName){
		openCompatibility(fileName);
	}

	public void editCompatibility(){
		if (compatibility != null){
			if (compatibility.hasHoroscope()){
				new CompatibilityInputDialog(this, compatibility.getBoyBirthData(), compatibility.getGirlBirthData());
			}else{
				new CompatibilityInputDialog(this,
						compatibility.getBoyName(),
						compatibility.getGirlName(),
						compatibility.getBoyRasi(),
						compatibility.getGirlRasi(),
						compatibility.getBoyNak(),
						compatibility.getGirlNak());
			}
		}
	}

	public void editCompactibility(){
		editCompatibility();
	}

	public void saveCompatibility(){
		if (compatibility != null){
			if (chartNotesPanel != null) {
				chartNotesPanel.flushToOwner();
			}
			String currentFile = compatibility.getFilePath();
			if (currentFile != null && !currentFile.trim().isEmpty()) {
				compatibility.saveToXML(currentFile);
				log.info("Saved compatibility directly to: " + currentFile);
				if (currentView == View.MARRIAGE_MATCH_VIEW) {
					updateMarriageMatchTitle();
				} else {
					updateCompatibilityTitle();
				}
			} else {
				saveAsCompatibility();
			}
		}
	}

	public void saveCompactibility(){
		saveCompatibility();
	}

	public app.astrosoft.ui.comp.ChartNotesPanel getChartNotesPanel() {
		return chartNotesPanel;
	}

	public void saveAsCompatibility(){
		if (compatibility != null){
			if (chartNotesPanel != null) {
				chartNotesPanel.flushToOwner();
			}
			String selectedFile = FileOps.openFileDialog(this, FileOps.FileDialogMode.SAVE, AstrosoftFileFilter.COMPATIBILITY_EXTN);
			
			if (selectedFile != null) {
				compatibility.saveToXML(selectedFile);
				if (currentView == View.MARRIAGE_MATCH_VIEW) {
					updateMarriageMatchTitle();
				} else {
					updateCompatibilityTitle();
				}
			}
		}
	}

	public void saveAsCompactibility(){
		saveAsCompatibility();
	}

	public void editHoroscope() {

		new BirthInputDialog(this, h.getBirthData());

	}

	public void exportHoroscope2Pdf(){

		if (h != null){

			String selectedFile = FileOps.openFileDialog(this, FileOps.FileDialogMode.SAVE, AstrosoftFileFilter.PDF_EXTN);
			FutureTask<Object> task = AstrosoftExporter.export2Pdf(AstrosoftExporter.Type.Horosocope, h, selectedFile);
			
			ExportDialog exportDlg = new ExportDialog(this,"Export Horosocope ", task,selectedFile);
		}
	}

	public void exportCompatibility2Pdf(){

		if (compatibility != null){

			String selectedFile = FileOps.openFileDialog(this, FileOps.FileDialogMode.SAVE, AstrosoftFileFilter.PDF_EXTN);
			AstrosoftExporter.export2Pdf(AstrosoftExporter.Type.Compatibility, compatibility, selectedFile);
		}
	}

	public void exportCompactibility2Pdf(){
		exportCompatibility2Pdf();
	}

	public void setWaitCursor(){
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void setDefaultCursor(){
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void computeNumeroNumber(){
		new ComputeNumberDialog(this);
	}
	
	public void showAbout(){
		new AboutDialog(this).setVisible(true);
	}

	@Override
	public void showTransitChart() {
		if (transitChartDialog == null || !transitChartDialog.isDisplayable()) {
			transitChartDialog = new TransitChartDialog(this, new Date(), getPreferences().getPlace());
			transitChartDialog.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosed(java.awt.event.WindowEvent e) {
					transitChartDialog = null;
				}
			});
		}
		transitChartDialog.setVisible(true);
		transitChartDialog.toFront();
		transitChartDialog.requestFocus();
	}

	public TransitChartDialog getTransitChartDialog() {
		return transitChartDialog;
	}
	
	private void optionAyanamsaChanged(Ayanamsa ayanamsa) {

		if (eph != null) {

			eph.setAyanamsa(ayanamsa);
			viewManager.showView(View.EPHEMERIS_VIEW);
		}

		if (pan != null) {

			viewManager.showView(View.PANCHANG_VIEW);
		}

		if (h != null) {
			h.setAyanamsa(ayanamsa);
			viewManager.showView(View.CHART_VIEW);
		}

	}

	private void optionLangChanged(String lang) {

		if (eph != null) {

			viewManager.showView(View.EPHEMERIS_VIEW);

		}

		if (pan != null) {

			viewManager.showView(View.PANCHANG_VIEW);

		}

		if (h != null) {

			h.languageChanged();
			viewManager.showView(View.CHART_VIEW);

		}

	}

	public void optionChanged(final Preference preference){

		DateListener listener = new DateListener(){
			public void dateChanged(Date date) {
				preferences.preferenceChanged(preference, date);
			}
		};

		switch(preference){
			case Place: new PlaceOptionDialog(this);
						break;
			case EphCalcTime:
							  new TimeInputDialog(DisplayStrings.EPH_TIME_STR, this, AstroUtil.doubleTimeToDate(preferences.getEphCalcTime()), listener);
							  break;
			case PanCalcTime: new TimeInputDialog(DisplayStrings.PAN_TIME_STR, this, AstroUtil.doubleTimeToDate(preferences.getPanCalcTime()), listener);
							  break;

		}
	}
	public void enableActions(boolean enable) {

		EnumSet<Command> actions = EnumSet.of(Command.SAVE, Command.SAVE_AS, Command.SAVE_COMPATIBILITY,
				Command.EDIT_COMPATIBILITY,
				Command.EDIT_CHART,
				Command.CHART_VIEW, Command.PLANET_POS_VIEW,
				Command.BHAVA_POS_VIEW, Command.DIV_CHART_VIEW,
				Command.ASHTAVARGA_VIEW, Command.SHADBALA_VIEW,
				Command.YOGA_COMBINATIONS_VIEW, Command.DASAS_VIEW,
				Command.NOTES);

		actionMgr.enableActions(actions, enable);
		if (!enable && mbar != null) {
			mbar.setNotesSelected(false);
		}
	}


	public app.astrosoft.ui.view.MyMuhurtaView getMyMuhurtaView() {
		if (myMuhurtaView == null) {
			myMuhurtaView = new app.astrosoft.ui.view.MyMuhurtaView(new Point(30, 20), h);
		}
		return myMuhurtaView;
	}

	public void updateMuhurtaTitle() {
		MuhurtaDocument doc = (myMuhurtaView != null) ? myMuhurtaView.getDocument() : null;
		if (doc != null) {
			if (doc.getFilePath() != null) {
				setTitle("Astrosoft 2.0 - " + new File(doc.getFilePath()).getName() + " [My Muhurta]");
			} else {
				String p1Name = (doc.getPerson1() != null && doc.getPerson1().getName() != null && !doc.getPerson1().getName().trim().isEmpty())
						? doc.getPerson1().getName() : null;
				String p2Name = (doc.getPerson2() != null && doc.getPerson2().getName() != null && !doc.getPerson2().getName().trim().isEmpty())
						? doc.getPerson2().getName() : null;
				if (p1Name != null && p2Name != null) {
					setTitle("Astrosoft 2.0 - My Muhurta (" + p1Name + " & " + p2Name + ")");
				} else if (p1Name != null) {
					setTitle("Astrosoft 2.0 - My Muhurta (" + p1Name + ")");
				} else if (doc.getName() != null && !doc.getName().trim().isEmpty() && !doc.getName().equalsIgnoreCase("Muhurta")) {
					setTitle("Astrosoft 2.0 - My Muhurta (" + doc.getName() + ")");
				} else {
					setTitle("Astrosoft 2.0 - My Muhurta");
				}
			}
		} else {
			setTitle("Astrosoft 2.0 - My Muhurta");
		}
	}

	public void onSwitchToMuhurtaView() {
		currentView = View.MY_MUHURTA_VIEW;
		if (chartNotesPanel != null) {
			chartNotesPanel.flushToOwner();
			chartNotesPanel.bindNotesOwner(getMyMuhurtaView().getDocument());
		}
		actionMgr.enableAction(Command.NOTES, true);
		actionMgr.enableAction(Command.SAVE, true);
		actionMgr.enableAction(Command.SAVE_AS, true);
		actionMgr.enableAction(Command.PRINT, true);
		updateMuhurtaTitle();
	}

	public void onSwitchToHoroscopeView() {
		if (chartNotesPanel != null) {
			chartNotesPanel.flushToOwner();
			if (h != null) {
				chartNotesPanel.bindNotesOwner(h);
			}
		}
		if (h != null) {
			enableActions(true);
			if (h.getFilePath() != null) {
				setTitle("Astrosoft 2.0 - " + new File(h.getFilePath()).getName());
			} else {
				setTitle("Astrosoft 2.0 - " + (h.getPersonName() != null ? h.getPersonName() : "Horoscope") + " (Unsaved)");
			}
		} else {
			enableActions(false);
			setTitle("Astrosoft 2.0");
		}
	}

	public void onSwitchToUtilityView() {
		if (chartNotesPanel != null) {
			chartNotesPanel.flushToOwner();
			chartNotesPanel.bindNotesOwner(null);
		}
		foldNotes();
		if (floatingNotesDialog != null && floatingNotesDialog.isVisible()) {
			floatingNotesDialog.setVisible(false);
		}
		actionMgr.enableAction(Command.NOTES, false);
	}

	public void onDocumentModified() {
		if (currentView == View.MARRIAGE_MATCH_VIEW || currentView == View.COMPATIBILITY_VIEW) {
			if (compatibility != null) {
				compatibility.setModified(true);
			}
		} else if (currentView == View.MY_MUHURTA_VIEW) {
			updateMuhurtaTitle();
		}
		actionMgr.enableAction(Command.SAVE, true);
		actionMgr.enableAction(Command.SAVE_AS, true);
	}

	public void viewChanged(View view) {

		currentView = view;
		switch(view){

			case EPHEMERIS_VIEW:
				onSwitchToUtilityView();
				setTitle("Astrosoft 2.0 - Ephemeris");
				eph = new Ephemeris(AstroUtil.getCalendar(), Mode.Daily);
				viewManager.showView(view);
				break;

			case PANCHANG_VIEW:
				onSwitchToUtilityView();
				setTitle("Astrosoft 2.0 - Panchang");
				pan = new Panchang(today);
				viewManager.showView(view);
				break;
			case MY_MUHURTA_VIEW:
				onSwitchToMuhurtaView();
				viewManager.showView(view);
				break;

			case COMPATIBILITY_VIEW:
				onSwitchToUtilityView();
				new CompatibilityInputDialog(this);
				break;
			case MARRIAGE_MATCH_VIEW:
				onSwitchToUtilityView();
				new CompatibilityInputDialog(this, true);
				break;
			case MUHURTHA_VIEW:
				onSwitchToUtilityView();
				setTitle("Astrosoft 2.0 - Muhurtha");
				new MuhurthaInput(this);
				break;
			case FIND_NAME_VIEW:
				onSwitchToUtilityView();
				setTitle("Astrosoft 2.0 - Find Name");
				viewManager.showView(view);
				break;

			default:
				onSwitchToHoroscopeView();
				viewManager.showView(view);
		}
	}

	public void refreshUI() {

		getContentPane( ).removeAll( );

		getContentPane().add(toolbar, BorderLayout.PAGE_START);

		if (mainSplitPane != null) {
			getContentPane().add(mainSplitPane, BorderLayout.CENTER);
		} else {
			getContentPane().add(home, BorderLayout.CENTER);
		}
		this.repaint();

		this.setVisible(true);

	}

	public void optionChanged(Preference preference, Object value) {

		preferences.preferenceChanged(preference, value);

		switch (preference) {
		case Ayanamsa:
			optionAyanamsaChanged((Ayanamsa) value);
			break;
		case Language:
			optionLangChanged(((Language) value).name());
			break;
		}


	}

	public static AstrosoftPref getPreferences(){
		return preferences;
	}

	public View getCurrentView() {
		return currentView;
	}

   /**
	 * @return Returns the horoscope.
	 */
	public Horoscope getHoroscope() {
		if (currentView == View.MY_MUHURTA_VIEW && myMuhurtaView != null) {
			MuhurtaProfile p1 = myMuhurtaView.getDocument().getPerson1();
			if (p1 != null && p1.hasHoroscope()) {
				return p1.getHoroscope();
			}
			MuhurtaProfile p2 = myMuhurtaView.getDocument().getPerson2();
			if (p2 != null && p2.hasHoroscope()) {
				return p2.getHoroscope();
			}
			if (myMuhurtaView.getOrCreateMuhurta() != null) {
				return myMuhurtaView.getOrCreateMuhurta().getEventHoroscope();
			}
		}
		return h;
	}
	
	public AstrosoftActionManager getActionManager() {
		return actionMgr;
	}

	public Compatibility getCompatibility() {
		return compatibility;
	}

	public Compatibility getCompactibility() {
		return compatibility;
	}

	public static Dimension getScreenSize(){

		return java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		//return new Dimension(800,600);
	}


	private boolean isChartView(java.awt.Component view) {
		return view != null && "ChartView".equals(view.getName());
	}

	private class AstrosoftViewContainer implements ViewContainer{
		public void addView(JPanel view){
			home.removeAll();
			home.add(view);
			
			refreshUI();
			getContentPane().validate();
			
			if (view.getClass().getName().contains("MyMuhurtaView") || isChartView(view) || view.getClass().getName().contains("MarriageMatchView")) {
			    int viewW = home.getWidth();
			    int viewH = home.getHeight();
			    if (viewW <= 0 || viewH <= 0) {
			        java.awt.Dimension size = getContentPane().getSize();
			        if (size.width == 0) size = bounds.getSize(); // fallback if not yet laid out
			        int tbHeight = (toolbar != null && toolbar.isVisible()) ? toolbar.getHeight() : 0;
			        viewW = size.width;
			        viewH = Math.max(0, size.height - tbHeight);
			    }
			    view.setBounds(0, 0, viewW, viewH);
			    view.revalidate();
			    view.validate();
			}
			home.repaint();
		}

		public JPanel createView(View view) {

			switch(view){
				case CHART_VIEW:
					return createChartView();
				case DASAS_VIEW:
					return new VimDasaView(h.getTitle(), h.getVimshottariDasa());
				case BHAVA_POS_VIEW:
					return new BhavaView(h.getTitle(), h.getHousePosition());
				case PLANET_POS_VIEW:
					return new PlanetView(h.getTitle(), h.getPlanetaryInfo());
				case SHADBALA_VIEW:
					return createShadbalaView();
				case DIV_CHART_VIEW:
					return new VargaChartView(h.getTitle(), h.getPlanetaryInfo());
				case ASHTAVARGA_VIEW:
					return new AshtavargaView(h.getTitle(), h.getAshtaVarga(), h.getAscendant(), h.getRasi(), new Point(30,20));
				case PANCHANG_VIEW:
					return new PanchangView(pan, new Point(30,20));
				case MY_MUHURTA_VIEW:
					return getMyMuhurtaView();
				case EPHEMERIS_VIEW:
					return new EphemerisView(eph, new Point(10,2));

				case COMPATIBILITY_VIEW:
					actionMgr.enableActions(EnumSet.of(Command.SAVE_COMPATIBILITY), true);
					return new CompatibilityView(DisplayStrings.MRAG_COMP_STR.toString(Language.ENGLISH), compatibility, new Point(30,20));

				case MARRIAGE_MATCH_VIEW:
					actionMgr.enableActions(EnumSet.of(Command.SAVE_COMPATIBILITY), true);
					return new app.astrosoft.ui.view.MarriageMatchView(compatibility, new Point(10, 10));

				case MUHURTHA_VIEW:
					return new MuhurthaPanel(muhurtha, AstroSoft.this);

				case FIND_NAME_VIEW:
					return new FindNameView();
					
				case YOGA_COMBINATIONS_VIEW: 
					try {
						return new YogaCombinationsView(h.getTitle(),h.getYogaCombinations(),h.getPlanetaryInfo());
					} catch (Throwable t) {
						log.log(java.util.logging.Level.WARNING, "Unable to compute Yoga combinations", t);
						return new JPanel();
					}
				 
			}
			return null;
		}
	}


}
