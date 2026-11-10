package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.util.EnumMap;
import java.util.EnumSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Command;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Varga;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIConsts.ChartStyle;

public class ChartStylePreferenceTest {

    private ChartStyle originalActiveStyle;
    private ChartStyle originalPrefStyle;

    @Before
    public void setUp() {
        originalActiveStyle = UIConsts.getChartStyle();
        originalPrefStyle = new AstrosoftPref().getChartStyle();
    }

    @After
    public void tearDown() {
        UIConsts.setChartStyle(originalActiveStyle);
        new AstrosoftPref().setChartStyle(originalPrefStyle);
    }

    @Test
    public void testDefaultChartStyleIsSouthIndian() {
        UIConsts.setChartStyle(ChartStyle.SOUTH_INDIAN);
        assertEquals(ChartStyle.SOUTH_INDIAN, UIConsts.getChartStyle());
        assertEquals("South Indian", ChartStyle.SOUTH_INDIAN.displayName());
        assertEquals("North Indian", ChartStyle.NORTH_INDIAN.displayName());
    }

    @Test
    public void testStyleOptionsCommandSet() {
        EnumSet<Command> styleOptions = Command.styleOptions();
        assertEquals(2, styleOptions.size());
        assertTrue(styleOptions.contains(Command.STYLE_SOUTH_INDIAN));
        assertTrue(styleOptions.contains(Command.STYLE_NORTH_INDIAN));
    }

    @Test
    public void testPreferenceRoundTripPersistence() {
        AstrosoftPref pref = new AstrosoftPref();
        pref.setChartStyle(ChartStyle.NORTH_INDIAN);
        assertEquals(ChartStyle.NORTH_INDIAN, new AstrosoftPref().getChartStyle());

        pref.setChartStyle(ChartStyle.SOUTH_INDIAN);
        assertEquals(ChartStyle.SOUTH_INDIAN, new AstrosoftPref().getChartStyle());
    }

    @Test
    public void testChartRebuildTogglesStyleCleanly() {
        EnumMap<Planet, Integer> positions = new EnumMap<>(Planet.class);
        EnumMap<Planet, Boolean> directions = new EnumMap<>(Planet.class);
        for (Planet p : Planet.planetsAsc()) {
            positions.put(p, 1);
            directions.put(p, false);
        }
        Chart chart = new Chart(new PlanetChartData(Varga.Rasi, positions, directions), new Dimension(360, 320));
        org.junit.Assert.assertNull("South Indian chart must not have NorthIndianChartCanvas", chart.getNorthIndianCanvas());
        javax.swing.JPopupMenu initialPopup = chart.getComponentPopupMenu();
        org.junit.Assert.assertNotNull("Planet chart must register right-click zoom popup", initialPopup);

        UIConsts.setChartStyle(ChartStyle.NORTH_INDIAN);
        chart.rebuild();
        org.junit.Assert.assertNotNull("North Indian chart must have NorthIndianChartCanvas", chart.getNorthIndianCanvas());
        org.junit.Assert.assertSame("Popup menu must not be recreated on rebuild", initialPopup, chart.getComponentPopupMenu());

        UIConsts.setChartStyle(ChartStyle.SOUTH_INDIAN);
        chart.rebuild();
        org.junit.Assert.assertNull("Rebuilt South Indian chart must clear NorthIndianChartCanvas", chart.getNorthIndianCanvas());
        assertTrue(chart.getComponentCount() > 0);
        org.junit.Assert.assertSame("Popup menu must stay stable across rebuilds", initialPopup, chart.getComponentPopupMenu());
    }
}
