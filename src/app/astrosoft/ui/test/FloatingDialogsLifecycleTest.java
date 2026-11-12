package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import javax.swing.JPanel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.RahuNode;
import app.astrosoft.consts.Sex;
import app.astrosoft.core.Horoscope;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.pref.AstrosoftPref.Preference;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.dlg.AshtavargaDialog;
import app.astrosoft.ui.dlg.HouseDetailsDialog;
import app.astrosoft.ui.dlg.PlanetDetailsDialog;
import app.astrosoft.ui.view.HoroscopeDashboard;
import app.astrosoft.ui.view.ViewManager.View;

public class FloatingDialogsLifecycleTest {

	private Horoscope horoscope;

	@Before
	public void setUp() {
		horoscope = SampleHoroscopes.getMyHoroscope();
		PlanetDetailsDialog.closeActiveDialog();
		HouseDetailsDialog.closeActiveDialog();
		AshtavargaDialog.closeActiveDialog();
	}

	@After
	public void tearDown() {
		PlanetDetailsDialog.closeActiveDialog();
		HouseDetailsDialog.closeActiveDialog();
		AshtavargaDialog.closeActiveDialog();
	}

	@Test
	public void testSingleActivePlanetDetailsDialog() {
		PlanetaryInfo info = horoscope.getPlanetaryInfo();
		PlanetDetailsDialog dlg1 = new PlanetDetailsDialog(null, info, horoscope.getAscendant(), horoscope.getShadBala(), null);
		assertNotNull(PlanetDetailsDialog.getActiveDialog());
		assertTrue("dlg1 should be displayable", dlg1.isDisplayable());

		// Creating a second dialog should dispose the first
		PlanetDetailsDialog dlg2 = new PlanetDetailsDialog(null, info, horoscope.getAscendant(), horoscope.getShadBala(), null);
		assertFalse("dlg1 must be disposed when dlg2 is created", dlg1.isDisplayable());
		assertTrue("dlg2 should be displayable", dlg2.isDisplayable());

		PlanetDetailsDialog.closeActiveDialog();
		assertFalse("dlg2 must be disposed after closeActiveDialog", dlg2.isDisplayable());
		assertNull(PlanetDetailsDialog.getActiveDialog());
	}

	@Test
	public void testSingleActiveHouseDetailsDialog() {
		PlanetaryInfo info = horoscope.getPlanetaryInfo();
		HouseDetailsDialog dlg1 = new HouseDetailsDialog(null, info, horoscope.getAscendant(), null);
		assertNotNull(HouseDetailsDialog.getActiveDialog());
		assertTrue("dlg1 should be displayable", dlg1.isDisplayable());

		// Creating a second dialog should dispose the first
		HouseDetailsDialog dlg2 = new HouseDetailsDialog(null, info, horoscope.getAscendant(), null);
		assertFalse("dlg1 must be disposed when dlg2 is created", dlg1.isDisplayable());
		assertTrue("dlg2 should be displayable", dlg2.isDisplayable());

		HouseDetailsDialog.closeActiveDialog();
		assertFalse("dlg2 must be disposed after closeActiveDialog", dlg2.isDisplayable());
		assertNull(HouseDetailsDialog.getActiveDialog());
	}

	@Test
	public void testSingleActiveAshtavargaDialog() {
		AshtavargaDialog dlg1 = new AshtavargaDialog(null, horoscope, null);
		assertNotNull(AshtavargaDialog.getActiveDialog());
		assertTrue("dlg1 should be displayable", dlg1.isDisplayable());

		// Creating a second dialog should dispose the first
		AshtavargaDialog dlg2 = new AshtavargaDialog(null, horoscope, null);
		assertFalse("dlg1 must be disposed when dlg2 is created", dlg1.isDisplayable());
		assertTrue("dlg2 should be displayable", dlg2.isDisplayable());

		AshtavargaDialog.closeActiveDialog();
		assertFalse("dlg2 must be disposed after closeActiveDialog", dlg2.isDisplayable());
		assertNull(AshtavargaDialog.getActiveDialog());
	}

	@Test
	public void testHoroscopeDashboardDisposeFloatingDialogs() {
		PlanetaryInfo info = horoscope.getPlanetaryInfo();
		JPanel dummy = new JPanel();
		HoroscopeDashboard dashboard = new HoroscopeDashboard(
				dummy, dummy, dummy, dummy, dummy, false, c -> {}, info, horoscope.getAscendant(), horoscope.getShadBala(),
				horoscope.getAshtaVarga(), horoscope.getRasi(), horoscope.getPlanetRasis(), horoscope);

		// Toggle on Planet Details
		dashboard.getPositionsCheck().doClick();
		assertNotNull("PlanetDetailsDialog should be created", dashboard.getPlanetDetailsDialog());
		assertTrue("PlanetDetailsDialog should be displayable", dashboard.getPlanetDetailsDialog().isDisplayable());
		assertTrue("Checkbox should be checked", dashboard.getPositionsCheck().isSelected());

		// Toggle on House Details
		dashboard.getHouseDetailsCheck().doClick();
		assertNotNull("HouseDetailsDialog should be created", dashboard.getHouseDetailsDialog());
		assertTrue("HouseDetailsDialog should be displayable", dashboard.getHouseDetailsDialog().isDisplayable());
		assertTrue("Checkbox should be checked", dashboard.getHouseDetailsCheck().isSelected());

		// Toggle on Ashtavarga
		dashboard.getAshtavargaCheck().doClick();
		assertNotNull("AshtavargaDialog should be created", dashboard.getAshtavargaDialog());
		assertTrue("AshtavargaDialog should be displayable", dashboard.getAshtavargaDialog().isDisplayable());
		assertTrue("Checkbox should be checked", dashboard.getAshtavargaCheck().isSelected());

		// Call disposeFloatingDialogs()
		dashboard.disposeFloatingDialogs();
		assertNull("planetDetailsDialog reference should be null", dashboard.getPlanetDetailsDialog());
		assertNull("houseDetailsDialog reference should be null", dashboard.getHouseDetailsDialog());
		assertNull("ashtavargaDialog reference should be null", dashboard.getAshtavargaDialog());
		assertFalse("positionsCheck should be unchecked", dashboard.getPositionsCheck().isSelected());
		assertFalse("houseDetailsCheck should be unchecked", dashboard.getHouseDetailsCheck().isSelected());
		assertFalse("ashtavargaCheck should be unchecked", dashboard.getAshtavargaCheck().isSelected());
	}

	@Test
	public void testHoroscopeViewTransitionsPreserveFloatingDialogs() throws Exception {
		AstroSoft app = new AstroSoft();
		app.computeHoroscope(horoscope.getBirthData());

		// Show Planet Details and House Details
		PlanetDetailsDialog.showDialog(app, horoscope, null);
		HouseDetailsDialog.showDialog(app, horoscope, null);

		assertNotNull(PlanetDetailsDialog.getActiveDialog());
		assertTrue("PlanetDetails must be visible", PlanetDetailsDialog.getActiveDialog().isVisible());
		assertNotNull(HouseDetailsDialog.getActiveDialog());
		assertTrue("HouseDetails must be visible", HouseDetailsDialog.getActiveDialog().isVisible());

		View[] horoscopeScreens = new View[] {
			View.DASAS_VIEW,
			View.PLANET_POS_VIEW,
			View.BHAVA_POS_VIEW,
			View.DIV_CHART_VIEW,
			View.SHADBALA_VIEW,
			View.YOGA_COMBINATIONS_VIEW,
			View.CHART_VIEW
		};

		for (View v : horoscopeScreens) {
			app.viewChanged(v);
			assertNotNull("PlanetDetails must not be null in " + v, PlanetDetailsDialog.getActiveDialog());
			assertTrue("PlanetDetails must remain visible in " + v, PlanetDetailsDialog.getActiveDialog().isVisible());
			assertNotNull("HouseDetails must not be null in " + v, HouseDetailsDialog.getActiveDialog());
			assertTrue("HouseDetails must remain visible in " + v, HouseDetailsDialog.getActiveDialog().isVisible());
		}
	}

	@Test
	public void testAshtavargaSmartSuppression() throws Exception {
		AstroSoft app = new AstroSoft();
		app.computeHoroscope(horoscope.getBirthData());

		AshtavargaDialog.showDialog(app, horoscope, null);
		assertNotNull(AshtavargaDialog.getActiveDialog());
		assertTrue(AshtavargaDialog.getActiveDialog().isVisible());

		// Switch to full-screen Ashtavarga screen -> should smart-hide dialog
		app.viewChanged(View.ASHTAVARGA_VIEW);
		assertNotNull("AshtavargaDialog must not be disposed", AshtavargaDialog.getActiveDialog());
		assertFalse("AshtavargaDialog must be smart-hidden on ASHTAVARGA_VIEW", AshtavargaDialog.getActiveDialog().isVisible());

		// Switch to DASA view -> should restore visibility
		app.viewChanged(View.DASAS_VIEW);
		assertNotNull(AshtavargaDialog.getActiveDialog());
		assertTrue("AshtavargaDialog must be restored on DASAS_VIEW", AshtavargaDialog.getActiveDialog().isVisible());

		// Switch back to CHART view -> should remain visible
		app.viewChanged(View.CHART_VIEW);
		assertNotNull(AshtavargaDialog.getActiveDialog());
		assertTrue("AshtavargaDialog must remain visible on CHART_VIEW", AshtavargaDialog.getActiveDialog().isVisible());
	}

	@Test
	public void testUtilityViewTransitionHidesDialogsAndRestores() throws Exception {
		AstroSoft app = new AstroSoft();
		app.computeHoroscope(horoscope.getBirthData());

		PlanetDetailsDialog.showDialog(app, horoscope, null);
		HouseDetailsDialog.showDialog(app, horoscope, null);
		AshtavargaDialog.showDialog(app, horoscope, null);

		assertTrue(PlanetDetailsDialog.getActiveDialog().isVisible());
		assertTrue(HouseDetailsDialog.getActiveDialog().isVisible());
		assertTrue(AshtavargaDialog.getActiveDialog().isVisible());

		// Switch to Ephemeris
		app.viewChanged(View.EPHEMERIS_VIEW);
		assertNotNull(PlanetDetailsDialog.getActiveDialog());
		assertFalse("PlanetDetailsDialog must be hidden in Ephemeris", PlanetDetailsDialog.getActiveDialog().isVisible());
		assertNotNull(HouseDetailsDialog.getActiveDialog());
		assertFalse("HouseDetailsDialog must be hidden in Ephemeris", HouseDetailsDialog.getActiveDialog().isVisible());
		assertNotNull(AshtavargaDialog.getActiveDialog());
		assertFalse("AshtavargaDialog must be hidden in Ephemeris", AshtavargaDialog.getActiveDialog().isVisible());

		// Switch to Panchang
		app.viewChanged(View.PANCHANG_VIEW);
		assertFalse("PlanetDetailsDialog must stay hidden in Panchang", PlanetDetailsDialog.getActiveDialog().isVisible());
		assertFalse("HouseDetailsDialog must stay hidden in Panchang", HouseDetailsDialog.getActiveDialog().isVisible());
		assertFalse("AshtavargaDialog must stay hidden in Panchang", AshtavargaDialog.getActiveDialog().isVisible());

		// Return to Dasa screen (Horoscope context) -> All should restore!
		app.viewChanged(View.DASAS_VIEW);
		assertTrue("PlanetDetailsDialog must restore on Dasa", PlanetDetailsDialog.getActiveDialog().isVisible());
		assertTrue("HouseDetailsDialog must restore on Dasa", HouseDetailsDialog.getActiveDialog().isVisible());
		assertTrue("AshtavargaDialog must restore on Dasa", AshtavargaDialog.getActiveDialog().isVisible());
	}

	@Test
	public void testHoroscopeIdentityChangeDisposesDialogs() throws Exception {
		AstroSoft app = new AstroSoft();
		app.computeHoroscope(horoscope.getBirthData());

		PlanetDetailsDialog.showDialog(app, horoscope, null);
		HouseDetailsDialog.showDialog(app, horoscope, null);
		AshtavargaDialog.showDialog(app, horoscope, null);

		assertNotNull(PlanetDetailsDialog.getActiveDialog());
		assertNotNull(HouseDetailsDialog.getActiveDialog());
		assertNotNull(AshtavargaDialog.getActiveDialog());

		// Compute a new horoscope -> must dispose all dialogs
		Place place = new Place("Delhi", 28.61, 77.20, 5.5);
		BirthData newBd = new BirthData("NewPerson", Sex.Male, 1, 1, 2000, 12, 0, 0, place);
		app.computeHoroscope(newBd);

		assertNull("PlanetDetailsDialog must be disposed on new horoscope", PlanetDetailsDialog.getActiveDialog());
		assertNull("HouseDetailsDialog must be disposed on new horoscope", HouseDetailsDialog.getActiveDialog());
		assertNull("AshtavargaDialog must be disposed on new horoscope", AshtavargaDialog.getActiveDialog());
	}

	@Test
	public void testPopupModalityAndNotesAlwaysOnTopDisabled() {
		AstroSoft app = new AstroSoft();
		app.undockNotes();
		assertNotNull("floatingNotesDialog must exist", app.getFloatingNotesDialog());
		assertFalse("floatingNotesDialog must NOT be alwaysOnTop to prevent obscuring popups",
				app.getFloatingNotesDialog().isAlwaysOnTop());

		PlanetDetailsDialog pd = new PlanetDetailsDialog(app, horoscope, null);
		assertFalse("PlanetDetailsDialog must NOT be alwaysOnTop", pd.isAlwaysOnTop());
		pd.dispose();

		HouseDetailsDialog hd = new HouseDetailsDialog(app, horoscope, null);
		assertFalse("HouseDetailsDialog must NOT be alwaysOnTop", hd.isAlwaysOnTop());
		hd.dispose();

		AshtavargaDialog ad = new AshtavargaDialog(app, horoscope, null);
		assertFalse("AshtavargaDialog must NOT be alwaysOnTop", ad.isAlwaysOnTop());
		ad.dispose();

		app.astrosoft.ui.dlg.AboutDialog about = new app.astrosoft.ui.dlg.AboutDialog(app);
		assertEquals("AboutDialog must be APPLICATION_MODAL so it stays at the top of z-order",
				java.awt.Dialog.ModalityType.APPLICATION_MODAL, about.getModalityType());
		assertTrue("AboutDialog must be alwaysOnTop so it stays above all windows",
				about.isAlwaysOnTop());
		assertTrue("AboutDialog must have WindowListener for windowOpened toFront hook",
				about.getWindowListeners().length > 0);
		about.dispose();
	}

	@Test
	public void testRahuNodeChangeRefreshesOpenFloatingDialogs() {
		AstrosoftPref pref = AstroSoft.getPreferences();
		RahuNode originalNode = pref.getRahuNode();
		try {
			pref.setRahuNode(RahuNode.MEAN);
			AstroSoft frame = new AstroSoft();
			frame.computeHoroscope(horoscope.getBirthData());

			PlanetDetailsDialog pdBefore = PlanetDetailsDialog.showDialog(frame, frame.getHoroscope(), null);
			HouseDetailsDialog hdBefore = HouseDetailsDialog.showDialog(frame, frame.getHoroscope(), null);
			java.awt.Rectangle customBounds = new java.awt.Rectangle(120, 140, 640, 280);
			pdBefore.setBounds(customBounds);

			frame.optionChanged(Preference.RahuNode, RahuNode.TRUE);

			PlanetDetailsDialog pdAfter = PlanetDetailsDialog.getActiveDialog();
			HouseDetailsDialog hdAfter = HouseDetailsDialog.getActiveDialog();
			assertNotNull("PlanetDetailsDialog must remain open after Lunar Node change", pdAfter);
			assertNotNull("HouseDetailsDialog must remain open after Lunar Node change", hdAfter);
			assertTrue("PlanetDetailsDialog must be rebuilt with fresh data", pdAfter != pdBefore);
			assertTrue("HouseDetailsDialog must be rebuilt with fresh data", hdAfter != hdBefore);
			assertTrue("Refreshed PlanetDetailsDialog must remain visible", pdAfter.isVisible());
			assertEquals("Refreshed PlanetDetailsDialog must preserve bounds", customBounds, pdAfter.getBounds());
		} finally {
			pref.setRahuNode(originalNode);
		}
	}
}
