package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.util.EnumSet;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.astrosoft.consts.Command;
import app.astrosoft.consts.Planet;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.ui.table.PlanetCellRenderer;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIConsts.ChartTheme;

public class ChartThemeTest {

    private ChartTheme originalTheme;

    @Before
    public void setUp() {
        originalTheme = UIConsts.getChartTheme();
    }

    @After
    public void tearDown() {
        UIConsts.setChartTheme(originalTheme);
    }

    @Test
    public void testAllThemesDefinedAndConfigured() {
        assertEquals("Should have exactly 5 themes", 5, ChartTheme.values().length);
        for (ChartTheme theme : ChartTheme.values()) {
            assertNotNull(theme.displayName());
            assertNotNull(theme.getBackground());
            assertNotNull(theme.getBorder());
            assertNotNull(theme.getBorderColor());
            assertNotNull(theme.getPlanetForeground());
            assertNotNull(theme.getAscendantColor());
            assertNotNull(theme.getMoonColor());
            assertNotNull(theme.getScoreColor());
            assertNotNull(theme.getLabelColor());
        }
    }

    @Test
    public void testThemeOptionsCommandSet() {
        EnumSet<Command> themeOptions = Command.themeOptions();
        assertEquals("Command.themeOptions() should have 5 items", 5, themeOptions.size());
        assertTrue(themeOptions.contains(Command.THEME_WARM_IVORY));
        assertTrue(themeOptions.contains(Command.THEME_SOFT_ALABASTER));
        assertTrue(themeOptions.contains(Command.THEME_PALE_GOLD_MIST));
        assertTrue(themeOptions.contains(Command.THEME_CALM_SAGE));
        assertTrue(themeOptions.contains(Command.THEME_CLASSIC));
    }

    @Test
    public void testClassicThemeColors() {
        UIConsts.setChartTheme(ChartTheme.CLASSIC);
        assertEquals("Classic background must be #FFFFFC", new Color(255, 255, 252), UIConsts.getChartBackground());
        assertEquals("Classic planet text must be LIGHT_BLUE", new Color(0, 0, 204), UIConsts.getChartPlanetForeground());
        assertEquals("Classic Ascendant must be RED", Color.RED, UIConsts.getChartAscendantColor());
        assertEquals("Classic Moon must be MAGENTA", Color.MAGENTA, UIConsts.getChartMoonColor());
        assertEquals("Classic score must be DARK_BLUE", new Color(0, 0, 140), UIConsts.getChartScoreColor());
        assertTrue("Classic border should be EtchedBorder", UIConsts.getChartBorder() instanceof EtchedBorder);
    }

    @Test
    public void testWarmIvoryThemeColors() {
        UIConsts.setChartTheme(ChartTheme.WARM_IVORY);
        assertEquals("Warm Ivory background must be #FAF8F2", new Color(250, 248, 242), UIConsts.getChartBackground());
        assertEquals("Warm Ivory planet text must be #1E293B", new Color(30, 41, 59), UIConsts.getChartPlanetForeground());
        assertEquals("Warm Ivory Ascendant must be #DC2626", new Color(220, 38, 38), UIConsts.getChartAscendantColor());
        assertEquals("Warm Ivory Moon must be #0284C7", new Color(2, 132, 199), UIConsts.getChartMoonColor());
        assertTrue("Warm Ivory border should be LineBorder", UIConsts.getChartBorder() instanceof LineBorder);
    }

    @Test
    public void testPlanetCellRendererReflectsTheme() {
        PlanetCellRenderer renderer = new PlanetCellRenderer(new javax.swing.table.DefaultTableCellRenderer());
        JTable table = new JTable(1, 3);

        // Under CLASSIC theme
        UIConsts.setChartTheme(ChartTheme.CLASSIC);
        Component cAsc = renderer.getTableCellRendererComponent(table, Planet.Ascendant, false, false, 0, 0);
        assertTrue(cAsc instanceof JLabel);
        assertEquals("Classic Asc color must be RED", Color.RED, cAsc.getForeground());

        Component cMoon = renderer.getTableCellRendererComponent(table, Planet.Moon, false, false, 0, 1);
        assertEquals("Classic Moon color must be MAGENTA", Color.MAGENTA, cMoon.getForeground());

        Component cSun = renderer.getTableCellRendererComponent(table, Planet.Sun, false, false, 0, 2);
        assertEquals("Classic Sun color must be LIGHT_BLUE", new Color(0, 0, 204), cSun.getForeground());

        // Under WARM_IVORY theme
        UIConsts.setChartTheme(ChartTheme.WARM_IVORY);
        cAsc = renderer.getTableCellRendererComponent(table, Planet.Ascendant, false, false, 0, 0);
        assertEquals("Warm Ivory Asc color must be #DC2626", new Color(220, 38, 38), cAsc.getForeground());

        cMoon = renderer.getTableCellRendererComponent(table, Planet.Moon, false, false, 0, 1);
        assertEquals("Warm Ivory Moon color must be #0284C7", new Color(2, 132, 199), cMoon.getForeground());

        cSun = renderer.getTableCellRendererComponent(table, Planet.Sun, false, false, 0, 2);
        assertEquals("Warm Ivory Sun color must be #1E293B", new Color(30, 41, 59), cSun.getForeground());
    }

    @Test
    public void testPreferencePersistence() {
        AstrosoftPref pref = new AstrosoftPref();
        pref.setChartTheme(ChartTheme.CALM_SAGE);
        assertEquals(ChartTheme.CALM_SAGE, pref.getChartTheme());

        pref.setChartTheme(ChartTheme.PALE_GOLD_MIST);
        assertEquals(ChartTheme.PALE_GOLD_MIST, pref.getChartTheme());

        // Restore default
        pref.setChartTheme(ChartTheme.WARM_IVORY);
        assertEquals(ChartTheme.WARM_IVORY, pref.getChartTheme());
    }

    @Test
    public void testThemeLabelColors() {
        UIConsts.setChartTheme(ChartTheme.CLASSIC);
        assertEquals(new Color(0, 70, 140), UIConsts.getChartLabelColor());

        UIConsts.setChartTheme(ChartTheme.WARM_IVORY);
        assertEquals(new Color(100, 116, 139), UIConsts.getChartLabelColor());
    }
}
