package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.regex.Pattern;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.astrosoft.consts.Command;
import app.astrosoft.consts.Language;
import app.astrosoft.pref.AstrosoftPref.Preference;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.AstrosoftActionManager;
import app.astrosoft.ui.comp.AstrosoftMenuBar;

public class MenuEnglishUniformityTest {

    private static final Pattern TAMIL_CHAR_PATTERN = Pattern.compile("[\\u0B80-\\u0BFF]");
    private Language originalLang;

    @Before
    public void setUp() {
        System.setProperty("astrosoft.test.unattended", "true");
        System.setProperty("java.util.prefs.PreferencesFactory", "app.astrosoft.pref.UserPreferencesFactory");
        originalLang = AstroSoft.getPreferences().getLanguage();
    }

    @After
    public void tearDown() {
        if (originalLang != null) {
            AstroSoft.getPreferences().preferenceChanged(Preference.Language, originalLang);
        }
    }

    @Test
    public void testCommandsAlwaysReturnEnglishActionStrings() {
        assertEquals("Default Location", Command.Place.action());
        assertEquals("Ephemeris Time", Command.EphCalcTime.action());
        assertEquals("Panchang Time", Command.PanCalcTime.action());
        assertEquals("Compute Number", Command.COMPUTE_NUMBER.action());
        assertEquals("Export Name DB", Command.EXPORT_NAME_DB.action());
        assertEquals("Import Name DB", Command.IMPORT_NAME_DB.action());
        assertEquals("Import Names", Command.IMPORT_NAME.action());
    }

    @Test
    public void testMenuBarHasNoTamilInTamilMode() {
        AstroSoft.getPreferences().preferenceChanged(Preference.Language, Language.TAMIL);

        AstroSoft frame = new AstroSoft();
        AstrosoftMenuBar menuBar = new AstrosoftMenuBar(frame.getActionManager());

        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu != null) {
                String menuText = menu.getText();
                assertNotNull("Menu heading must have text", menuText);
                assertFalse("Menu heading '" + menuText + "' must not contain Tamil characters",
                        TAMIL_CHAR_PATTERN.matcher(menuText).find());

                assertMenuItemsEnglish(menu);
            }
        }
        frame.dispose();
    }

    private void assertMenuItemsEnglish(JMenu menu) {
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item != null) {
                String itemText = item.getText();
                if (itemText != null && !itemText.isEmpty()) {
                    assertFalse("Menu item '" + itemText + "' under '" + menu.getText() + "' must not contain Tamil characters",
                            TAMIL_CHAR_PATTERN.matcher(itemText).find());
                }
                if (item instanceof JMenu) {
                    assertMenuItemsEnglish((JMenu) item);
                }
            }
        }
    }
}
