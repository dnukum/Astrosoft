/**
 * DasaTimelineDialog.java
 *
 * Floating, modeless, resizable dialog for the single-horoscope 3-level
 * Dasa / Bhukti / Pratyantara timeline.
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

import app.astrosoft.consts.Language;
import app.astrosoft.core.DasaTimelineEngine.WindowFilter;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.SingleDasaTimelinePanel;
import app.astrosoft.ui.util.UIConsts;

public class DasaTimelineDialog extends JDialog {

	private static final Dimension DEFAULT_SIZE = new Dimension(740, 440);
	private static final Dimension MIN_SIZE = new Dimension(560, 280);

	private static DasaTimelineDialog activeDialog;
	private static java.awt.Point lastLocation;

	private final SingleDasaTimelinePanel timelinePanel;
	private Runnable onCloseCallback;

	public static DasaTimelineDialog getActiveDialog() {
		return activeDialog;
	}

	public static void closeActiveDialog() {
		if (activeDialog != null) {
			DasaTimelineDialog dlg = activeDialog;
			activeDialog = null;
			dlg.dispose();
		}
	}

	public static DasaTimelineDialog showDialog(Window owner, Horoscope horoscope, Runnable onClose) {
		DasaTimelineDialog dlg = new DasaTimelineDialog(owner, horoscope, onClose);
		dlg.setVisible(true);
		return dlg;
	}

	public DasaTimelineDialog(Window owner, Horoscope horoscope, Runnable onClose) {
		super(owner != null ? owner : AstroSoft.getInstance(),
				AstroSoft.getPreferences().getLanguage() == Language.TAMIL
						? "விம்ஷோத்தரி தசை"
						: "Vimshottari Dasa",
				ModalityType.MODELESS);
		this.onCloseCallback = onClose;

		if (activeDialog != null && activeDialog != this) {
			activeDialog.dispose();
		}
		activeDialog = this;

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(true);

		timelinePanel = new SingleDasaTimelinePanel(horoscope);

		JPanel content = (JPanel) getContentPane();
		content.setLayout(new BorderLayout());
		content.setBackground(UIConsts.getChartBackground());
		content.setBorder(BorderFactory.createEmptyBorder(10, 14, 12, 14));
		content.add(timelinePanel, BorderLayout.CENTER);

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
				if (activeDialog == DasaTimelineDialog.this) {
					activeDialog = null;
				}
				if (onCloseCallback != null) {
					onCloseCallback.run();
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
				if (activeDialog == DasaTimelineDialog.this) {
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

	public WindowFilter getWindowFilter() {
		return timelinePanel != null ? timelinePanel.getCurrentWindow() : null;
	}

	public void setWindowFilter(WindowFilter filter) {
		if (timelinePanel != null) {
			timelinePanel.setWindowFilter(filter);
		}
	}
}
