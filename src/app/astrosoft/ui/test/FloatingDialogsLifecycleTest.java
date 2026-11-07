package app.astrosoft.ui.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.swing.JPanel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.dlg.AshtavargaDialog;
import app.astrosoft.ui.dlg.HouseDetailsDialog;
import app.astrosoft.ui.dlg.PlanetDetailsDialog;
import app.astrosoft.ui.view.HoroscopeDashboard;

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
}
