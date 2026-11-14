package app.astrosoft.ui.test;

import org.junit.Test;
import static org.junit.Assert.*;

import app.astrosoft.ui.AstroSoft;
import app.astrosoft.consts.Language;
import app.astrosoft.pref.AstrosoftPref.Preference;
import app.astrosoft.ui.view.ViewManager.View;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;

public class TestMarriageMatchLangChange {

    @Test
    public void testMarriageMatchLanguageChangePreservesView() throws Exception {
        System.setProperty("astrosoft.test.unattended", "true");
        System.setProperty("java.util.prefs.PreferencesFactory", "app.astrosoft.pref.UserPreferencesFactory");

        AstroSoft mainApp = new AstroSoft();
        
        // 1. First open a chart (Most users start by opening a chart!)
        Horoscope chart = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        mainApp.computeHoroscope(chart.getBirthData());
        chart.setModified(false);
        mainApp.getHoroscope().setModified(false);

        assertEquals("Expected CHART_VIEW", View.CHART_VIEW, mainApp.getCurrentView());

        // 2. Open PlanetDetailsDialog (floating palette on main screen)
        if (mainApp.getCurrentDashboard() != null) {
            mainApp.getCurrentDashboard().getPositionsCheck().setSelected(true);
            mainApp.getCurrentDashboard().getPositionsCheck().getActionListeners()[0].actionPerformed(null);
        }

        // 3. User enters Marriage Match with 2 horoscopes
        Horoscope boy = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        Horoscope girl = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_2.ash");
        mainApp.displayMarriageMatch(boy, girl);

        assertEquals("Expected MARRIAGE_MATCH_VIEW", View.MARRIAGE_MATCH_VIEW, mainApp.getCurrentView());
        assertTrue("Home component 0 is MarriageMatchView", 
                mainApp.getHome().getComponent(0).getClass().getName().contains("MarriageMatchView"));

        // 4. Change language to TAMIL
        mainApp.optionChanged(Preference.Language, Language.TAMIL);

        assertEquals("View dropped on TAMIL change!", View.MARRIAGE_MATCH_VIEW, mainApp.getCurrentView());
        assertTrue("Home component 0 was reset on TAMIL change!", 
                mainApp.getHome().getComponent(0).getClass().getName().contains("MarriageMatchView"));

        // 5. Change language back to ENGLISH
        mainApp.optionChanged(Preference.Language, Language.ENGLISH);

        assertEquals("View dropped on ENGLISH change!", View.MARRIAGE_MATCH_VIEW, mainApp.getCurrentView());
        assertTrue("Home component 0 was reset on ENGLISH change!", 
                mainApp.getHome().getComponent(0).getClass().getName().contains("MarriageMatchView"));
    }

    public static void main(String[] args) throws Exception {
        new TestMarriageMatchLangChange().testMarriageMatchLanguageChangePreservesView();
        System.out.println("TestMarriageMatchLangChange PASSED successfully!");
        System.exit(0);
    }
}
