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
        // First instantiate key views in English so any static Font field caching is exercised
        AstroSoft.getPreferences().setLanguage(Language.ENGLISH);
        app.astrosoft.ui.util.UIUtil.syncHtmlDefaultStyleSheet();
        new app.astrosoft.ui.view.PlanetView(sampleHoroscope.getTitle(), sampleHoroscope.getPlanetaryInfo());
        new app.astrosoft.ui.view.MarriageMatchView(
                new app.astrosoft.core.Compatibility(sampleHoroscope, sampleHoroscope), new Point(0, 0));

        // Now switch Language to Tamil to test localized rendering and dynamic font resolution
        AstroSoft.getPreferences().setLanguage(Language.TAMIL);
        app.astrosoft.ui.util.UIUtil.syncHtmlDefaultStyleSheet();
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

        // F. Floating Inspection Dialogs (HouseDetailsDialog, PlanetDetailsDialog, AshtavargaDialog)
        try {
            app.astrosoft.ui.dlg.HouseDetailsDialog hd = new app.astrosoft.ui.dlg.HouseDetailsDialog(
                    null, sampleHoroscope.getPlanetaryInfo(), sampleHoroscope.getAscendant(), null);
            LocalizationAuditor.auditComponentTree(hd.getContentPane(), "HouseDetailsDialog", report);
            hd.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on HouseDetailsDialog: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.dlg.PlanetDetailsDialog pd = new app.astrosoft.ui.dlg.PlanetDetailsDialog(
                    null, sampleHoroscope, null);
            LocalizationAuditor.auditComponentTree(pd.getContentPane(), "PlanetDetailsDialog", report);
            pd.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on PlanetDetailsDialog: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.dlg.AshtavargaDialog ad = new app.astrosoft.ui.dlg.AshtavargaDialog(
                    null, sampleHoroscope, null);
            LocalizationAuditor.auditComponentTree(ad.getContentPane(), "AshtavargaDialog", report);
            ad.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on AshtavargaDialog: " + e.getMessage());
        }

        // G. Additional Legacy Views & Dialogs (ShadBalaView, AshtavargaView, VimDasaView, PlanetView, BhavaView, VargaChartView, PanchangView, EphemerisView, MarriageMatchView, MuhurtaProfileDialog, DiagnosticsDialog, AshtavargaGuideDialog, AboutDialog)
        try {
            app.astrosoft.ui.view.ShadBalaView sbView = new app.astrosoft.ui.view.ShadBalaView(
                    sampleHoroscope.getTitle(), sampleHoroscope.getShadBala(), new Point(0, 0));
            LocalizationAuditor.auditComponentTree(sbView, "ShadBalaView", report);
        } catch (Exception e) {
            System.err.println("Audit error on ShadBalaView: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.view.AshtavargaView avView = new app.astrosoft.ui.view.AshtavargaView(
                    sampleHoroscope.getTitle(), sampleHoroscope.getAshtaVarga(), sampleHoroscope.getAscendant(),
                    sampleHoroscope.getRasi(), sampleHoroscope.getPlanetRasis(), new Point(0, 0));
            LocalizationAuditor.auditComponentTree(avView, "AshtavargaView", report);
            // Verify AshtavargaView tabs fit on a single row (no wrapping into 2 rows) in Tamil mode
            for (java.awt.Component c : avView.getComponents()) {
                if (c instanceof javax.swing.JTabbedPane) {
                    javax.swing.JTabbedPane tp = (javax.swing.JTabbedPane) c;
                    tp.setSize(tp.getPreferredSize());
                    if (tp.getLayout() != null) {
                        tp.getLayout().layoutContainer(tp);
                    }
                    java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                            tp.getWidth(), tp.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g2 = img.createGraphics();
                    tp.paint(g2);
                    g2.dispose();
                    org.junit.Assert.assertEquals("AshtavargaView Tamil tabs must stay on 1 row", 1, tp.getTabRunCount());
                }
            }
        } catch (Exception e) {
            System.err.println("Audit error on AshtavargaView: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.view.VimDasaView vdView = new app.astrosoft.ui.view.VimDasaView(
                    sampleHoroscope.getTitle(), sampleHoroscope.getVimshottariDasa());
            LocalizationAuditor.auditComponentTree(vdView, "VimDasaView", report);
        } catch (Exception e) {
            System.err.println("Audit error on VimDasaView: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.view.PlanetView pv = new app.astrosoft.ui.view.PlanetView(
                    sampleHoroscope.getTitle(), sampleHoroscope.getPlanetaryInfo());
            LocalizationAuditor.auditComponentTree(pv, "PlanetView", report);
        } catch (Exception e) {
            System.err.println("Audit error on PlanetView: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.view.BhavaView bv = new app.astrosoft.ui.view.BhavaView(
                    sampleHoroscope.getTitle(), sampleHoroscope.getHousePosition());
            LocalizationAuditor.auditComponentTree(bv, "BhavaView", report);
        } catch (Exception e) {
            System.err.println("Audit error on BhavaView: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.view.VargaChartView vcView = new app.astrosoft.ui.view.VargaChartView(
                    sampleHoroscope.getTitle(), sampleHoroscope.getPlanetaryInfo());
            LocalizationAuditor.auditComponentTree(vcView, "VargaChartView", report);
        } catch (Exception e) {
            System.err.println("Audit error on VargaChartView: " + e.getMessage());
        }

        try {
            app.astrosoft.core.Panchang pan = new app.astrosoft.core.Panchang(new Date());
            app.astrosoft.ui.view.PanchangView panView = new app.astrosoft.ui.view.PanchangView(pan, new Point(0, 0));
            LocalizationAuditor.auditComponentTree(panView, "PanchangView", report);
        } catch (Exception e) {
            System.err.println("Audit error on PanchangView: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.view.MarriageMatchView mmView = new app.astrosoft.ui.view.MarriageMatchView(
                    new app.astrosoft.core.Compatibility(sampleHoroscope, sampleHoroscope), new Point(0, 0));
            LocalizationAuditor.auditComponentTree(mmView, "MarriageMatchView", report);
        } catch (Exception e) {
            System.err.println("Audit error on MarriageMatchView: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.dlg.MuhurtaProfileDialog mpd = new app.astrosoft.ui.dlg.MuhurtaProfileDialog(
                    null, "Person 1", null);
            LocalizationAuditor.auditComponentTree(mpd.getContentPane(), "MuhurtaProfileDialog", report);
            mpd.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on MuhurtaProfileDialog: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.dlg.AshtavargaGuideDialog agd = new app.astrosoft.ui.dlg.AshtavargaGuideDialog(null);
            LocalizationAuditor.auditComponentTree(agd.getContentPane(), "AshtavargaGuideDialog", report);
            agd.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on AshtavargaGuideDialog: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.view.FindNameView fnView = new app.astrosoft.ui.view.FindNameView();
            LocalizationAuditor.auditComponentTree(fnView, "FindNameView", report);
        } catch (Exception e) {
            System.err.println("Audit error on FindNameView: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.comp.ComputeNumberPanel cnPanel = new app.astrosoft.ui.comp.ComputeNumberPanel(new java.awt.Dimension(360, 130));
            LocalizationAuditor.auditComponentTree(cnPanel, "ComputeNumberPanel", report);
        } catch (Exception e) {
            System.err.println("Audit error on ComputeNumberPanel: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.view.YogaCombinationsView ycView = new app.astrosoft.ui.view.YogaCombinationsView(
                    sampleHoroscope.getTitle(), sampleHoroscope.getYogaCombinations(), sampleHoroscope.getPlanetaryInfo());
            LocalizationAuditor.auditComponentTree(ycView, "YogaCombinationsView", report);
        } catch (Exception e) {
            System.err.println("Audit error on YogaCombinationsView: " + e.getMessage());
        }

        try {
            app.astrosoft.core.Ephemeris eph = new app.astrosoft.core.Ephemeris(
                    app.astrosoft.util.AstroUtil.getCalendar(), app.astrosoft.core.Ephemeris.Mode.Daily);
            app.astrosoft.ui.view.EphemerisView ephView = new app.astrosoft.ui.view.EphemerisView(eph, new Point(10, 2));
            LocalizationAuditor.auditComponentTree(ephView, "EphemerisView", report);
        } catch (Exception e) {
            System.err.println("Audit error on EphemerisView: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.view.CompatibilityView compView = new app.astrosoft.ui.view.CompatibilityView(
                    app.astrosoft.consts.DisplayStrings.MRAG_COMP_STR.toString(),
                    new app.astrosoft.core.Compatibility(sampleHoroscope, sampleHoroscope),
                    new Point(0, 0));
            LocalizationAuditor.auditComponentTree(compView, "CompatibilityView", report);
        } catch (Exception e) {
            System.err.println("Audit error on CompatibilityView: " + e.getMessage());
        }

        try {
            app.astrosoft.core.Muhurtha muh = new app.astrosoft.core.Muhurtha(
                    new java.util.GregorianCalendar(),
                    sampleHoroscope.getRasi(),
                    sampleHoroscope.getNakshathra().getNak());
            app.astrosoft.ui.view.MuhurthaPanel muhPanel = new app.astrosoft.ui.view.MuhurthaPanel(muh, null);
            LocalizationAuditor.auditComponentTree(muhPanel, "MuhurthaPanel", report);
        } catch (Exception e) {
            System.err.println("Audit error on MuhurthaPanel: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.dlg.MuhurthaInput muhInput = new app.astrosoft.ui.dlg.MuhurthaInput(null);
            LocalizationAuditor.auditComponentTree(muhInput.getContentPane(), "MuhurthaInput", report);
            muhInput.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on MuhurthaInput: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.dlg.ComputeNumberDialog cnDlg = new app.astrosoft.ui.dlg.ComputeNumberDialog(null);
            LocalizationAuditor.auditComponentTree(cnDlg.getContentPane(), "ComputeNumberDialog", report);
            cnDlg.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on ComputeNumberDialog: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.dlg.CompatibilityInputDialog cid = new app.astrosoft.ui.dlg.CompatibilityInputDialog(null, true, false);
            LocalizationAuditor.auditComponentTree(cid.getContentPane(), "CompatibilityInputDialog", report);
            cid.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on CompatibilityInputDialog: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.dlg.PlaceOptionDialog pod = new app.astrosoft.ui.dlg.PlaceOptionDialog(null);
            LocalizationAuditor.auditComponentTree(pod.getContentPane(), "PlaceOptionDialog", report);
            pod.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on PlaceOptionDialog: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.dlg.BirthInputDialog bid = new app.astrosoft.ui.dlg.BirthInputDialog(null);
            LocalizationAuditor.auditComponentTree(bid.getContentPane(), "BirthInputDialog", report);
            bid.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on BirthInputDialog: " + e.getMessage());
        }

        try {
            app.astrosoft.ui.dlg.ModernBirthInputDialog mbid = new app.astrosoft.ui.dlg.ModernBirthInputDialog(null, null, false);
            LocalizationAuditor.auditComponentTree(mbid.getContentPane(), "ModernBirthInputDialog", report);
            mbid.dispose();
        } catch (Exception e) {
            System.err.println("Audit error on ModernBirthInputDialog: " + e.getMessage());
        }

        // Output complete diagnostic summary
        System.out.println("================================================================================");
        System.out.println("                      LOCALIZATION AUDIT DIAGNOSTIC RESULTS                     ");
        System.out.println("================================================================================");
        System.out.println(report.formatSummary());
        System.out.println("================================================================================");

        org.junit.Assert.assertEquals(
                "Swing HTML components with Tamil text must bind HONOR_DISPLAY_PROPERTIES or UIUtil.getHtmlFontFamily()",
                0,
                report.getIssuesByType(app.astrosoft.i18n.LocalizationAuditor.IssueType.HTML_FONT_BINDING_MISSING).size());
        org.junit.Assert.assertEquals(
                "Components rendering Tamil text must use resolved Tamil font, not raw SansSerif/Verdana",
                0,
                report.getIssuesByType(app.astrosoft.i18n.LocalizationAuditor.IssueType.RAW_SANS_SERIF_ON_TAMIL).size());
        org.junit.Assert.assertEquals(
                "Tamil glyph omissions found",
                0,
                report.getIssuesByType(app.astrosoft.i18n.LocalizationAuditor.IssueType.GLYPH_OMISSION_TAMIL).size());
    }
}
