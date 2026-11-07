/**
 * HouseDetailsDialog.java
 *
 * Floating, modeless, resizable dialog for comprehensive House PAC
 * (Placement, Aspect, Conjunction) analysis.
 */
package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.view.HousePACView;

public class HouseDetailsDialog extends JDialog {

	private static final Dimension DEFAULT_SIZE = new Dimension(880, 520);
	private static final Dimension MIN_SIZE = new Dimension(620, 380);

	private static HouseDetailsDialog activeDialog;
	private static java.awt.Point lastLocation;

	private final HousePACView pacView;
	private Runnable onCloseCallback;

	public static HouseDetailsDialog getActiveDialog() {
		return activeDialog;
	}

	public static void closeActiveDialog() {
		if (activeDialog != null) {
			HouseDetailsDialog dlg = activeDialog;
			activeDialog = null;
			dlg.dispose();
		}
	}

	public static HouseDetailsDialog showDialog(Window owner, Horoscope horoscope, Runnable onClose) {
		HouseDetailsDialog dlg = new HouseDetailsDialog(owner, horoscope, onClose);
		dlg.setVisible(true);
		return dlg;
	}

	public HouseDetailsDialog(Window owner, Horoscope horoscope, Runnable onClose) {
		this(owner, horoscope != null ? horoscope.getPlanetaryInfo() : null,
				horoscope != null ? horoscope.getAscendant() : null, onClose);
	}

	public HouseDetailsDialog(Window owner, PlanetaryInfo planetaryInfo, Rasi ascendant, Runnable onClose) {
		super(owner != null ? owner : AstroSoft.getInstance(), "House Details - PAC Analysis", ModalityType.MODELESS);
		this.onCloseCallback = onClose;

		if (activeDialog != null && activeDialog != this) {
			activeDialog.dispose();
		}
		activeDialog = this;

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(true);

		pacView = new HousePACView(planetaryInfo, ascendant);
		pacView.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(UIConsts.CANVAS_BACKGROUND);
		getContentPane().add(pacView, BorderLayout.CENTER);

		setPreferredSize(DEFAULT_SIZE);
		setMinimumSize(MIN_SIZE);
		pack();

		if (lastLocation != null) {
			setLocation(lastLocation);
		} else if (owner != null) {
			setLocationRelativeTo(owner);
		} else {
			Dimension screen = AstroSoft.getScreenSize();
			setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
		}

		addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentMoved(java.awt.event.ComponentEvent e) {
				if (isShowing()) {
					lastLocation = getLocation();
				}
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (activeDialog == HouseDetailsDialog.this) {
					activeDialog = null;
				}
				if (onCloseCallback != null) {
					onCloseCallback.run();
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
				if (activeDialog == HouseDetailsDialog.this) {
					activeDialog = null;
				}
				if (onCloseCallback != null) {
					onCloseCallback.run();
				}
			}
		});
	}

	@Override
	public void dispose() {
		if (activeDialog == this) {
			activeDialog = null;
		}
		super.dispose();
	}

	public void setOnCloseCallback(Runnable onCloseCallback) {
		this.onCloseCallback = onCloseCallback;
	}

	public HousePACView getPACView() {
		return pacView;
	}
}
