package app.astrosoft.ui.test;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Date;
import javax.swing.JPanel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.Horoscope;
import app.astrosoft.i18n.LocalizationAuditor;
import app.astrosoft.i18n.LocalizationAuditor.AuditReport;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.modern.ModernBirthDataPanel;
import app.astrosoft.ui.dlg.DefaultTransitChartComputation;
import app.astrosoft.ui.dlg.TransitChartDialog;
import app.astrosoft.ui.view.AshtavargaView;
import app.astrosoft.ui.view.HoroscopeDashboard;
import app.astrosoft.ui.view.InfoView;
import app.astrosoft.ui.view.MyMuhurtaView;
import app.astrosoft.ui.view.RelativePlanetView;
import app.astrosoft.ui.view.ShadBalaBarView;

/**
 * LocalizationAuditTest executes the LocalizationAuditor crawler across
 * bundles, enums, and live UI screens to extract and identify all problematic strings.
 */
public class LocalizationAuditTest {

    private Language originalLang;
    private static Horoscope sampleHoroscope;

    @Before
    public void setUp() {
        originalLang = AstroSoft.getPreferences().getLanguage();
        if (sampleHoroscope == null) {
            sampleHoroscope = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        }
    }

    @After
    public void tearDown() {
        if (originalLang != null) {
            AstroSoft.getPreferences().setLanguage(originalLang);
        }
    }

    @Test
    public void testRunComprehensiveLocalizationAudit() {
        // Set Language to Tamil to test localized rendering
        AstroSoft.getPreferences().setLanguage(Language.TAMIL);
        AuditReport report = new AuditReport();

        // 1. Audit Resource Bundles
        LocalizationAuditor.auditBundles(report);

        // 2. Audit Domain Enums
        LocalizationAuditor.auditEnums(report);

        // 3. Audit UI Component Trees:
        // A. Profile Card (InfoView)
        try {
            InfoView infoView = new InfoView(sampleHoroscope.getHoroscopeInfo(), new Point(0, 0));
            LocalizationAuditor.auditComponentTree(infoView, "InfoView (Profile)", report);
        } catch (Exception e) {
            System.err.println("Audit error on InfoView: " + e.getMessage());
        }

        // B. Main Dashboard (Charts)
        try {
            InfoView infoView = new InfoView(sampleHoroscope.getHoroscopeInfo(), new Point(0, 0));
            RelativePlanetView relView = new RelativePlanetView(sampleHoroscope.getPlanetaryInfo());
            ShadBalaBarView shadbalaBar = new ShadBalaBarView(sampleHoroscope.getShadBala());
            Chart rasi = new Chart(new PlanetChartData(Varga.Rasi, sampleHoroscope.getPlanetaryInfo()), new Dimension(418, 365));
            Chart navamsa = new Chart(new PlanetChartData(Varga.Navamsa, sampleHoroscope.getPlanetaryInfo()), new Dimension(418, 365));
            JPanel charts = new JPanel(new java.awt.GridLayout(1, 2, 22, 0));
            charts.add(rasi);
            charts.add(navamsa);
            JPanel avPanel = AshtavargaView.createAshtavargaGridPanel(sampleHoroscope.getAshtaVarga(), sampleHoroscope.getAscendant(), sampleHoroscope.getRasi(), sampleHoroscope.getPlanetRasis());
            HoroscopeDashboard dashboard = new HoroscopeDashboard(
                    infoView, relView, shadbalaBar, charts, avPanel, false, c -> {});
            LocalizationAuditor.auditComponentTree(dashboard, "HoroscopeDashboard", report);
        } catch (Exception e) {
            System.err.println("Audit error on HoroscopeDashboard: " + e.getMessage());
        }

        // C. MyMuhurtaView
        try {
            MyMuhurtaView muhurtaView = new MyMuhurtaView(new Point(0, 0), sampleHoroscope);
            LocalizationAuditor.auditComponentTree(muhurtaView, "MyMuhurtaView", report);
        } catch (Exception e) {
            System.err.println("Audit error on MyMuhurtaView: " + e.getMessage());
        }

        // D. TransitChartDialog
        try {
            AstroSoft frame = new AstroSoft();
            Place place = AstroSoft.getPreferences().getPlace();
            TransitChartDialog transitDialog = new TransitChartDialog(frame, new Date(), place, new DefaultTransitChartComputation());
            LocalizationAuditor.auditComponentTree(transitDialog.getContentPane(), "TransitChartDialog", report);
            transitDialog.dispose();
            frame.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on TransitChartDialog: " + e.getMessage());
        }

        // E. ModernBirthDataPanel
        try {
            ModernBirthDataPanel birthPanel = new ModernBirthDataPanel();
            LocalizationAuditor.auditComponentTree(birthPanel, "ModernBirthDataPanel", report);
        } catch (Exception e) {
            System.err.println("Audit error on ModernBirthDataPanel: " + e.getMessage());
        }

        // Output complete diagnostic summary
        System.out.println("================================================================================");
        System.out.println("                      LOCALIZATION AUDIT DIAGNOSTIC RESULTS                     ");
        System.out.println("================================================================================");
        System.out.println(report.formatSummary());
        System.out.println("================================================================================");
    }
}
