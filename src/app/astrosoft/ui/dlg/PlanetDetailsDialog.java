/**
 * PlanetDetailsDialog.java
 *
 * Floating, modeless, resizable dialog for Planet Details (Positions with
 * Jaimini Karakas, and Shadbala strength bars).
 */
package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.ShadBala;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.view.RelativePlanetView;

public class PlanetDetailsDialog extends JDialog {

	private static Dimension DEFAULT_SIZE = new Dimension(668, 296);
	private static final Dimension MIN_SIZE = new Dimension(530, 260);

	private static PlanetDetailsDialog activeDialog;
	private static java.awt.Point lastLocation;

	private final RelativePlanetView planetView;
	private Runnable onCloseCallback;

	public static PlanetDetailsDialog getActiveDialog() {
		return activeDialog;
	}

	public static void closeActiveDialog() {
		if (activeDialog != null) {
			PlanetDetailsDialog dlg = activeDialog;
			activeDialog = null;
			dlg.dispose();
		}
	}

	public static PlanetDetailsDialog showDialog(Window owner, Horoscope horoscope, Runnable onClose) {
		PlanetDetailsDialog dlg = new PlanetDetailsDialog(owner, horoscope, onClose);
		dlg.setVisible(true);
		return dlg;
	}

	public static PlanetDetailsDialog showDialog(Window owner, PlanetaryInfo planetaryInfo, Rasi ascendant,
			ShadBala shadBala, Runnable onClose) {
		PlanetDetailsDialog dlg = new PlanetDetailsDialog(owner, planetaryInfo, ascendant, shadBala, onClose);
		dlg.setVisible(true);
		return dlg;
	}

	public PlanetDetailsDialog(Window owner, Horoscope horoscope, Runnable onClose) {
		this(owner, horoscope != null ? horoscope.getPlanetaryInfo() : null,
				horoscope != null ? horoscope.getAscendant() : null,
				horoscope != null ? horoscope.getShadBala() : null, onClose);
	}

	public PlanetDetailsDialog(Window owner, PlanetaryInfo planetaryInfo, Rasi ascendant, ShadBala shadBala, Runnable onClose) {
		super(owner != null ? owner : AstroSoft.getInstance(),
				AstroSoft.getPreferences().getLanguage() == Language.TAMIL ? "கிரக விவரங்கள் - பார்வை & ஷட்பலம்" : "Planet Details - Overview & Shadbala",
				ModalityType.MODELESS);
		this.onCloseCallback = onClose;

		if (activeDialog != null && activeDialog != this) {
			activeDialog.dispose();
		}
		activeDialog = this;

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(true);

		planetView = new RelativePlanetView(planetaryInfo, ascendant, shadBala);
		planetView.setFloatingMode(true);

		JPanel content = (JPanel) getContentPane();
		content.setLayout(new BorderLayout());
		content.setBackground(UIConsts.CANVAS_BACKGROUND);
		content.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 14));
		content.add(planetView, BorderLayout.CENTER);

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
				if (activeDialog == PlanetDetailsDialog.this) {
					activeDialog = null;
				}
				if (onCloseCallback != null) {
					onCloseCallback.run();
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
				if (activeDialog == PlanetDetailsDialog.this) {
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

	public RelativePlanetView getPlanetView() {
		return planetView;
	}

	public int getSelectedTab() {
		return (planetView != null) ? planetView.getSelectedTab() : 0;
	}

	public void setSelectedTab(int tabIndex) {
		if (planetView != null) {
			planetView.setSelectedTab(tabIndex);
		}
	}
}
