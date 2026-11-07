/**
 * AshtavargaDialog.java
 *
 * Floating, modeless, resizable dialog for the 8-Chart Ashtavarga Grid
 * (Sarva Ashtakavarga + 7 planetary Bhinnashtakavargas).
 */
package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Ashtavarga;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.view.AshtavargaView;

public class AshtavargaDialog extends JDialog {

	private static final Dimension DEFAULT_SIZE = new Dimension(890, 440);
	private static final Dimension MIN_SIZE = new Dimension(870, 420);

	private static AshtavargaDialog activeDialog;

	private final JPanel ashtavargaPanel;
	private Runnable onCloseCallback;

	public static AshtavargaDialog getActiveDialog() {
		return activeDialog;
	}

	public static void closeActiveDialog() {
		if (activeDialog != null) {
			AshtavargaDialog dlg = activeDialog;
			activeDialog = null;
			dlg.dispose();
		}
	}

	public static AshtavargaDialog showDialog(Window owner, Horoscope horoscope, Runnable onClose) {
		AshtavargaDialog dlg = new AshtavargaDialog(owner, horoscope, onClose);
		dlg.setVisible(true);
		return dlg;
	}

	public static AshtavargaDialog showDialog(Window owner, Ashtavarga ashtavarga, Rasi ascendant, Rasi moonRasi,
			EnumMap<Planet, Rasi> planetRasis, Runnable onClose) {
		AshtavargaDialog dlg = new AshtavargaDialog(owner, ashtavarga, ascendant, moonRasi, planetRasis, onClose);
		dlg.setVisible(true);
		return dlg;
	}

	public AshtavargaDialog(Window owner, Horoscope horoscope, Runnable onClose) {
		this(owner, horoscope != null ? horoscope.getAshtaVarga() : null,
				horoscope != null ? horoscope.getAscendant() : null,
				horoscope != null ? horoscope.getRasi() : null,
				horoscope != null ? horoscope.getPlanetRasis() : null, onClose);
	}

	public AshtavargaDialog(Window owner, Ashtavarga ashtavarga, Rasi ascendant, Rasi moonRasi,
			EnumMap<Planet, Rasi> planetRasis, Runnable onClose) {
		super(owner != null ? owner : AstroSoft.getInstance(), "Ashtavarga - 8-Chart Grid", ModalityType.MODELESS);
		this.onCloseCallback = onClose;

		if (activeDialog != null && activeDialog != this) {
			activeDialog.dispose();
		}
		activeDialog = this;

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(true);

		ashtavargaPanel = AshtavargaView.createAshtavargaGridPanel(ashtavarga, ascendant, moonRasi, planetRasis);
		ashtavargaPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(UIConsts.CANVAS_BACKGROUND);
		getContentPane().add(ashtavargaPanel, BorderLayout.CENTER);

		setPreferredSize(DEFAULT_SIZE);
		setMinimumSize(MIN_SIZE);
		pack();

		if (owner != null) {
			setLocationRelativeTo(owner);
		} else {
			Dimension screen = AstroSoft.getScreenSize();
			setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
		}

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (activeDialog == AshtavargaDialog.this) {
					activeDialog = null;
				}
				if (onCloseCallback != null) {
					onCloseCallback.run();
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
				if (activeDialog == AshtavargaDialog.this) {
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

	public JPanel getAshtavargaPanel() {
		return ashtavargaPanel;
	}
}
