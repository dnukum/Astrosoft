package app.astrosoft.ui.test;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.beans.HousePosition;
import app.astrosoft.consts.Planet;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.dlg.DefaultTransitChartComputation;
import app.astrosoft.ui.dlg.TransitChartComputation;
import app.astrosoft.ui.dlg.TransitChartDialog;
import junit.framework.TestCase;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Container;
import java.util.Date;
import java.util.EnumMap;

public class TransitChartDialogTest extends TestCase {

    public void testDialogCanBeConstructed() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new StubTransitChartComputation());
        assertNotNull(dialog);
        assertTrue("Dialog should be resizable", dialog.isResizable());
        assertEquals("Transit (D1)", TransitChartDialog.CHART_TITLE);
        assertNotNull("Chart should be initialized", dialog.getChart());
        assertNotNull("Subtitle should be initialized with date and place", dialog.getChart().getSubtitle());
        assertTrue("Subtitle should contain city", dialog.getChart().getSubtitle().contains(place.city()));
        assertEquals(place, dialog.getCurrentPlace());
        dialog.dispose();
        frame.dispose();
    }

    public void testDialogWithDefaultComputation() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new DefaultTransitChartComputation());
        assertNotNull(dialog);
        assertTrue("Dialog should be resizable", dialog.isResizable());
        assertNotNull(dialog.getChart());
        assertNotNull(dialog.getEditButton());
        dialog.dispose();
        frame.dispose();
    }

    public void testActionOpensTransitChartDialog() {
        AstroSoft frame = new AstroSoft();
        frame.getActionManager().getAction(app.astrosoft.consts.Command.TRANSIT_CHART)
                .actionPerformed(new java.awt.event.ActionEvent(this, 1, app.astrosoft.consts.Command.TRANSIT_CHART.name()));
        assertNotNull(frame.getTransitChartDialog());
        assertTrue(frame.getTransitChartDialog().isVisible());
        assertTrue("Opened dialog should be resizable", frame.getTransitChartDialog().isResizable());
        frame.getTransitChartDialog().dispose();
        frame.dispose();
    }

    public void testTransitChartTooltipDoesNotShowHouseNumber() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new StubTransitChartComputation());
        Chart chart = dialog.getChart();
        assertNotNull(chart);

        // Verify that none of the house panels or tables contain "House:" in their tooltips
        boolean foundTooltip = false;
        for (Component comp : chart.getComponents()) {
            if (comp instanceof JPanel) {
                String tip = ((JPanel) comp).getToolTipText();
                if (tip != null && tip.contains("deg;")) {
                    foundTooltip = true;
                    assertFalse("Transit chart house tooltip must NOT contain 'House:'", tip.contains("House:"));
                }
            }
        }
        assertTrue("Should have checked at least one house tooltip", foundTooltip);

        dialog.dispose();
        frame.dispose();
    }

    private static class StubTransitChartComputation implements TransitChartComputation {
        public PlanetaryInfo compute(Date instant, Place place) {
            EnumMap<Planet, Double> positions = new EnumMap<Planet, Double>(Planet.class);
            EnumMap<Planet, Boolean> directions = new EnumMap<Planet, Boolean>(Planet.class);
            double[] cusps = new double[13];
            for (int i = 1; i <= 12; i++) {
                cusps[i] = (i - 1) * 30.0;
            }
            HousePosition houses = new HousePosition(cusps);
            for (Planet p : Planet.values()) {
                positions.put(p, 0.0);
                directions.put(p, Boolean.TRUE);
            }
            return new PlanetaryInfo(positions, directions, houses);
        }
    }
}
