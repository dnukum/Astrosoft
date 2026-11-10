package app.astrosoft.ui.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
import app.astrosoft.ui.comp.chart.HouseContentModel;
import app.astrosoft.ui.comp.chart.NorthIndianGeometry;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIConsts.ChartStyle;

public class NorthIndianChartRenderTest {

    private ChartStyle originalStyle;

    @Before
    public void setUp() {
        originalStyle = UIConsts.getChartStyle();
        UIConsts.setChartStyle(ChartStyle.NORTH_INDIAN);
    }

    @After
    public void tearDown() {
        Chart.resetFontOffset();
        UIConsts.setChartStyle(originalStyle);
    }

    @Test
    public void testRenderPlanetChartsOffscreen() {
        EnumMap<Planet, Integer> pos = new EnumMap<>(Planet.class);
        EnumMap<Planet, Boolean> dir = new EnumMap<>(Planet.class);
        EnumMap<Planet, Nakshathra> stars = new EnumMap<>(Planet.class);
        EnumMap<Planet, Double> longs = new EnumMap<>(Planet.class);

        pos.put(Planet.Ascendant, 4); // Cancer (matches sample)
        pos.put(Planet.Sun, 9);
        pos.put(Planet.Moon, 8);
        pos.put(Planet.Mars, 10);
        pos.put(Planet.Mercury, 8);
        pos.put(Planet.Jupiter, 10);
        pos.put(Planet.Venus, 9);
        pos.put(Planet.Saturn, 2);
        pos.put(Planet.Rahu, 12);
        pos.put(Planet.Ketu, 6);

        for (Planet p : Planet.planetsAsc()) {
            dir.put(p, p == Planet.Rahu || p == Planet.Ketu);
            stars.put(p, Nakshathra.Ashwini);
            longs.put(p, (pos.get(p) - 1) * 30.0 + 13.56);
        }

        Varga[] vargas = { Varga.Rasi, Varga.Navamsa, Varga.Bhava, Varga.Dasamsa };
        for (Varga v : vargas) {
            Chart chart = new Chart(new PlanetChartData(v, pos, dir, stars, longs), new Dimension(418, 365));
            chart.setTitleBadge(UIConsts.NAVY_BADGE_BG, UIConsts.NAVY_BADGE_BG, Color.WHITE);
            chart.setSubtitle("(Test)");
            renderOffscreen(chart, 418, 365);
            assertNotNull(chart.getNorthIndianCanvas());
            assertTrue("All items should fit in " + v, chart.getNorthIndianCanvas().allItemsFitInsideHouses());
        }

        // Transit-style chart without house numbers in tooltip
        Chart transit = new Chart(new PlanetChartData(Varga.Rasi, pos, dir, stars, longs),
                new Dimension(340, 320), "Nov 9, 2026", false);
        renderOffscreen(transit, 340, 320);
        assertTrue(transit.getNorthIndianCanvas().allItemsFitInsideHouses());
    }

    @Test
    public void testCrowdedNinePlanetsInAllTriangleOrientationsFullAndMini() {
        // Test H2, H3, H6, H9 with both single-digit (Asc=1) and two-digit (Asc=10 -> signs 11,12,3,6) sign numbers
        int[] ascendants = { 1, 10 };
        int[] houseOffsets = { 1, 2, 5, 8 }; // H2, H3, H6, H9
        for (int asc : ascendants) {
            for (int offset : houseOffsets) {
                int sign = ((asc - 1 + offset) % 12) + 1;
                EnumMap<Planet, Integer> pos = new EnumMap<>(Planet.class);
                EnumMap<Planet, Boolean> dir = new EnumMap<>(Planet.class);
                EnumMap<Planet, Double> longs = new EnumMap<>(Planet.class);

                pos.put(Planet.Ascendant, asc);
                longs.put(Planet.Ascendant, (asc - 1) * 30.0 + 12.34);
                dir.put(Planet.Ascendant, false);
                for (Planet p : Planet.planetsAsc()) {
                    if (p.isAsc()) continue;
                    pos.put(p, sign);
                    dir.put(p, p == Planet.Saturn);
                    longs.put(p, (sign - 1) * 30.0 + 15.25);
                }

                PlanetChartData data = new PlanetChartData(Varga.Rasi, pos, dir, null, longs);

                Chart fullChart = new Chart(data, new Dimension(380, 360));
                renderOffscreen(fullChart, 380, 360);
                assertTrue("9 planets in asc=" + asc + ", sign=" + sign + " must fit in full-size chart",
                        fullChart.getNorthIndianCanvas().allItemsFitInsideHouses());

                Chart miniChart = new Chart(data, new Dimension(180, 175));
                renderOffscreen(miniChart, 180, 175);
                assertTrue("9 planets in asc=" + asc + ", sign=" + sign + " must fit in mini chart",
                        miniChart.getNorthIndianCanvas().allItemsFitInsideHouses());
            }
        }
    }

    @Test
    public void testHouseBackgroundFontOffsetAndHtmlTitle() {
        EnumMap<Planet, Integer> pos = new EnumMap<>(Planet.class);
        EnumMap<Planet, Boolean> dir = new EnumMap<>(Planet.class);
        for (Planet p : Planet.planetsAsc()) {
            pos.put(p, 1);
            dir.put(p, false);
        }
        Chart chart = new Chart(new PlanetChartData(Varga.Rasi, pos, dir) {
            @Override
            public String getChartName() {
                return "<html><center>Muhurta<br>Rasi</center></html>";
            }
        }, new Dimension(256, 256));
        chart.setHouseBackground(Rasi.Mesha, Color.YELLOW);
        Chart.adjustFontOffset(2);
        renderOffscreen(chart, 256, 256);
        assertNotNull(chart.getNorthIndianCanvas());
        assertTrue(chart.getNorthIndianCanvas().allItemsFitInsideHouses());
    }

    @Test
    public void testRenderAshtavargaChartsOffscreen() {
        EnumMap<Rasi, Integer> bindus = new EnumMap<>(Rasi.class);
        for (Rasi r : Rasi.values()) {
            bindus.put(r, (r.ordinal() % 7) + 1);
        }

        // Test Asc + Moon markers across every sign at miniChartSize (196x162) so corner containment is verified
        for (Rasi asc : Rasi.values()) {
            AshtaVargaChartData bav = new AshtaVargaChartData("SU", bindus, asc, asc.next(),
                    AshtavargaName.Sun, asc);
            Chart miniBav = new Chart(bav, new Dimension(196, 162));
            renderOffscreen(miniBav, 196, 162);
            assertTrue("Mini Ashtavarga markers must stay inside house bounds for asc=" + asc,
                    miniBav.getNorthIndianCanvas().allAshtavargaMarkersFit());

            AshtaVargaChartData sameHouseBav = new AshtaVargaChartData("MO", bindus, asc, asc,
                    AshtavargaName.Moon, asc);
            Chart sameMini = new Chart(sameHouseBav, new Dimension(196, 162));
            renderOffscreen(sameMini, 196, 162);
            assertTrue("Combined Asc+Mo markers must stay inside house bounds for asc=" + asc,
                    sameMini.getNorthIndianCanvas().allAshtavargaMarkersFit());
        }

        // Full-size BAV with badge
        AshtaVargaChartData bav = new AshtaVargaChartData("SU", bindus, Rasi.Simha, Rasi.Simha,
                AshtavargaName.Sun, Rasi.Makara);
        Chart fullBav = new Chart(bav, new Dimension(300, 290));
        fullBav.setTitleBadge(new Color(254, 243, 199), new Color(212, 163, 75), UIConsts.TEXT_DARK_NAVY);
        renderOffscreen(fullBav, 300, 290);
        assertNotNull(fullBav.getNorthIndianCanvas());
        assertTrue(fullBav.getNorthIndianCanvas().allAshtavargaMarkersFit());

        // Trikona oriented via orientationAscendant without changing getAscendant()
        AshtaVargaChartData trikona = new AshtaVargaChartData("Trikona", bindus, null, null, AshtavargaName.Trikona);
        trikona.setOrientationAscendant(Rasi.Simha);
        org.junit.Assert.assertNull("South Indian getAscendant must stay null", trikona.getAscendant());
        HouseContentModel triModel = HouseContentModel.from(trikona);
        org.junit.Assert.assertEquals(Rasi.Simha, triModel.getAscendant());
        org.junit.Assert.assertEquals(Rasi.Simha, NorthIndianGeometry.signForHouse(triModel.getAscendant(), 1));

        Chart triChart = new Chart(trikona, new Dimension(260, 240));
        renderOffscreen(triChart, 260, 240);
        assertNotNull(triChart.getNorthIndianCanvas());
    }

    private static void renderOffscreen(Chart chart, int w, int h) {
        chart.setSize(w, h);
        chart.doLayout();
        if (chart.getNorthIndianCanvas() != null) {
            chart.getNorthIndianCanvas().doLayout();
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        chart.printAll(g2);
        g2.dispose();
    }
}
