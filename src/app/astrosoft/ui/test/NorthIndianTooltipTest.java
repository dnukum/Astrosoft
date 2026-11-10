package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.EnumMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.astrosoft.beans.AshtaVargaChartData;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.AshtavargaName;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Varga;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.chart.ChartTooltips;
import app.astrosoft.ui.comp.chart.NorthIndianChartCanvas;
import app.astrosoft.ui.comp.chart.NorthIndianGeometry;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIConsts.ChartStyle;

public class NorthIndianTooltipTest {

    private ChartStyle originalStyle;

    @Before
    public void setUp() {
        originalStyle = UIConsts.getChartStyle();
        UIConsts.setChartStyle(ChartStyle.NORTH_INDIAN);
    }

    @After
    public void tearDown() {
        UIConsts.setChartStyle(originalStyle);
    }

    @Test
    public void testPlanetAndHouseTooltipsMatchSouthIndian() {
        EnumMap<Planet, Integer> positions = new EnumMap<>(Planet.class);
        EnumMap<Planet, Boolean> directions = new EnumMap<>(Planet.class);
        EnumMap<Planet, Nakshathra> stars = new EnumMap<>(Planet.class);
        EnumMap<Planet, Double> longs = new EnumMap<>(Planet.class);

        positions.put(Planet.Ascendant, 1); // Aries
        positions.put(Planet.Sun, 5);       // Leo (House 5)
        for (Planet p : Planet.planetsAsc()) {
            if (!positions.containsKey(p)) {
                positions.put(p, 10);
            }
            directions.put(p, p == Planet.Saturn);
        }
        stars.put(Planet.Sun, Nakshathra.Magam);
        longs.put(Planet.Sun, 123.75);
        stars.put(Planet.Saturn, Nakshathra.Thiruvonam);
        longs.put(Planet.Saturn, 285.5);

        PlanetChartData data = new PlanetChartData(Varga.Rasi, positions, directions, stars, longs);

        // Capture real South Indian JTable tooltip for Sun and Saturn
        UIConsts.setChartStyle(ChartStyle.SOUTH_INDIAN);
        Chart southChart = new Chart(data, new Dimension(380, 360));
        southChart.setSize(380, 360);
        southChart.doLayout();
        String southSunTip = findSouthPlanetToolTip(southChart, Planet.Sun);
        String southSatTip = findSouthPlanetToolTip(southChart, Planet.Saturn);

        // Compare against North Indian canvas tooltips
        UIConsts.setChartStyle(ChartStyle.NORTH_INDIAN);
        Chart chart = new Chart(data, new Dimension(380, 360));
        chart.setSize(380, 360);
        chart.doLayout();

        NorthIndianChartCanvas canvas = chart.getNorthIndianCanvas();
        assertNotNull(canvas);

        Point2D.Double sunPt = canvas.getPlanetHitCenter(Planet.Sun);
        assertNotNull("Sun hit target should exist", sunPt);
        String actualSunTip = canvas.getToolTipText(new MouseEvent(
                canvas, MouseEvent.MOUSE_MOVED, 0, 0,
                (int) Math.round(sunPt.x), (int) Math.round(sunPt.y), 0, false));
        assertEquals("Sun tooltip must match South Indian Chart", southSunTip, actualSunTip);

        Point2D.Double satPt = canvas.getPlanetHitCenter(Planet.Saturn);
        assertNotNull("Saturn hit target should exist", satPt);
        String actualSatTip = canvas.getToolTipText(new MouseEvent(
                canvas, MouseEvent.MOUSE_MOVED, 0, 0,
                (int) Math.round(satPt.x), (int) Math.round(satPt.y), 0, false));
        assertEquals("Retrograde Saturn tooltip must match South Indian Chart", southSatTip, actualSatTip);

        // Non-D1 (Navamsa) planet tooltip omits functional role tag and matches South Indian Chart
        PlanetChartData d9 = new PlanetChartData(Varga.Navamsa, positions, directions, stars, longs);
        UIConsts.setChartStyle(ChartStyle.SOUTH_INDIAN);
        Chart southD9 = new Chart(d9, new Dimension(360, 340));
        southD9.setSize(360, 340);
        southD9.doLayout();
        String southD9SunTip = findSouthPlanetToolTip(southD9, Planet.Sun);

        UIConsts.setChartStyle(ChartStyle.NORTH_INDIAN);
        Chart d9Chart = new Chart(d9, new Dimension(360, 340));
        d9Chart.setSize(360, 340);
        d9Chart.doLayout();
        Point2D.Double d9Sun = d9Chart.getNorthIndianCanvas().getPlanetHitCenter(Planet.Sun);
        String d9Tip = d9Chart.getNorthIndianCanvas().getToolTipText(new MouseEvent(
                d9Chart.getNorthIndianCanvas(), MouseEvent.MOUSE_MOVED, 0, 0,
                (int) Math.round(d9Sun.x), (int) Math.round(d9Sun.y), 0, false));
        assertEquals("Navamsa Sun tooltip must match South Indian Chart", southD9SunTip, d9Tip);

        // Hover over empty house (House 2 = Taurus, no planets)
        NorthIndianGeometry geom = canvas.getGeometry();
        Point2D.Double h2Center = geom.getHouseCentroid(2);
        String actualHouseTip = canvas.getToolTipText(new MouseEvent(
                canvas, MouseEvent.MOUSE_MOVED, 0, 0,
                (int) Math.round(h2Center.x), (int) Math.round(h2Center.y), 0, false));
        String expectedHouseTip = ChartTooltips.houseToolTip(data, Rasi.Vrishabha, true);
        assertEquals("House tooltip must match ChartTooltips", expectedHouseTip, actualHouseTip);

        // Hover over occupied house outside planet label (House 5 = Leo near top-right corner of triangle)
        Point2D.Double h5EmptyCorner = geom.getSignAnchor(5);
        String actualOccupiedHouseTip = canvas.getToolTipText(new MouseEvent(
                canvas, MouseEvent.MOUSE_MOVED, 0, 0,
                (int) Math.round(h5EmptyCorner.x), (int) Math.round(h5EmptyCorner.y), 0, false));
        assertEquals("Occupied house non-planet hover must match houseToolTip",
                ChartTooltips.houseToolTip(data, Rasi.Simha, true), actualOccupiedHouseTip);
    }

    private static String findSouthPlanetToolTip(java.awt.Component comp, Planet target) {
        if (comp instanceof javax.swing.JTable) {
            javax.swing.JTable t = (javax.swing.JTable) comp;
            for (int r = 0; r < t.getRowCount(); r++) {
                for (int c = 0; c < t.getColumnCount(); c++) {
                    if (t.getValueAt(r, c) == target) {
                        java.awt.Rectangle rect = t.getCellRect(r, c, true);
                        return t.getToolTipText(new MouseEvent(t, MouseEvent.MOUSE_MOVED, 0, 0,
                                rect.x + 1, rect.y + 1, 0, false));
                    }
                }
            }
        }
        if (comp instanceof java.awt.Container) {
            for (java.awt.Component child : ((java.awt.Container) comp).getComponents()) {
                String res = findSouthPlanetToolTip(child, target);
                if (res != null) return res;
            }
        }
        return null;
    }

    @Test
    public void testCustomHouseTooltipOverride() {
        EnumMap<Planet, Integer> positions = new EnumMap<>(Planet.class);
        EnumMap<Planet, Boolean> directions = new EnumMap<>(Planet.class);
        for (Planet p : Planet.planetsAsc()) {
            positions.put(p, 1);
            directions.put(p, false);
        }
        Chart chart = new Chart(new PlanetChartData(Varga.Rasi, positions, directions), new Dimension(360, 340));
        chart.setSize(360, 340);
        chart.doLayout();
        chart.setHouseToolTip(Rasi.Mithuna, "Custom Gemini Tooltip");

        NorthIndianChartCanvas canvas = chart.getNorthIndianCanvas();
        Point2D.Double h3Center = canvas.getGeometry().getHouseCentroid(3);
        String tip = canvas.getToolTipText(new MouseEvent(
                canvas, MouseEvent.MOUSE_MOVED, 0, 0,
                (int) Math.round(h3Center.x), (int) Math.round(h3Center.y), 0, false));
        assertEquals("Custom Gemini Tooltip", tip);
    }

    @Test
    public void testAshtavargaNatalRingTooltip() {
        EnumMap<Rasi, Integer> bindus = new EnumMap<>(Rasi.class);
        for (Rasi r : Rasi.values()) {
            bindus.put(r, 5);
        }
        // Ascendant = Aries (H1), Natal Sun = Taurus (H2)
        AshtaVargaChartData av = new AshtaVargaChartData("SU", bindus, Rasi.Mesha, Rasi.Kataka,
                AshtavargaName.Sun, Rasi.Vrishabha);
        Chart chart = new Chart(av, new Dimension(300, 290));
        chart.setSize(300, 290);
        chart.doLayout();

        NorthIndianChartCanvas canvas = chart.getNorthIndianCanvas();
        Point2D.Double natalAnchor = canvas.getGeometry().getHouseCentroid(2);
        String tip = canvas.getToolTipText(new MouseEvent(
                canvas, MouseEvent.MOUSE_MOVED, 0, 0,
                (int) Math.round(natalAnchor.x), (int) Math.round(natalAnchor.y), 0, false));
        assertNotNull("Natal ring tooltip should be present", tip);
        org.junit.Assert.assertTrue("Tooltip should mention Vrishabha and 5 bindus: " + tip,
                tip.contains(Rasi.Vrishabha.toString(app.astrosoft.consts.Language.ENGLISH))
                        && tip.contains("5 bindus"));
    }
}
