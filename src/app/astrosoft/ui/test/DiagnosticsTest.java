package app.astrosoft.ui.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.dlg.DiagnosticsDialog;
import app.astrosoft.ui.util.SystemDiagnostics;
import app.astrosoft.ui.util.SystemDiagnostics.FontCandidateInfo;

public class DiagnosticsTest {

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("astrosoft.test.unattended", "true");
    }

    @Test
    public void testGenerateReportContainsVitalDiagnostics() {
        String report = SystemDiagnostics.generateReport();
        assertNotNull(report);
        assertTrue("Report must contain header", report.contains("ASTROSOFT 2.0 SYSTEM DIAGNOSTICS"));
        assertTrue("Report must contain OS section", report.contains("[2] OPERATING SYSTEM"));
        assertTrue("Report must contain Java section", report.contains("[3] JAVA RUNTIME"));
        assertTrue("Report must contain Display & Graphics", report.contains("[4] DISPLAY & GRAPHICS"));
        assertTrue("Report must contain Font section", report.contains("[6] FONT & TAMIL GLYPH DIAGNOSTICS"));
        assertTrue("Report must contain Log file section", report.contains("[7] LOG FILE STATUS"));
        assertTrue("Report must contain candidate font evaluation", report.contains("Candidate Font Inventory:"));
    }

    @Test
    public void testGetLogFileResolution() {
        File logFile = SystemDiagnostics.getLogFile();
        assertNotNull("Log file must not be null", logFile);
        assertTrue("Log file name must end with .log", logFile.getName().endsWith(".log"));
    }

    @Test
    public void testCandidateFontInventory() {
        List<FontCandidateInfo> candidates = SystemDiagnostics.getCandidateFontStatus();
        assertNotNull(candidates);
        assertTrue("Candidate list should not be empty", candidates.size() > 0);

        boolean foundNotoOrTamil = false;
        for (FontCandidateInfo info : candidates) {
            assertNotNull(info.getFontName());
            if (info.getFontName().contains("Tamil") || info.getFontName().contains("Noto")) {
                foundNotoOrTamil = true;
            }
        }
        assertTrue("Must evaluate at least one Tamil or Noto candidate", foundNotoOrTamil);
    }

    @Test
    public void testDiagnosticsDialogCreationAndContent() {
        AstroSoft frame = new AstroSoft();
        DiagnosticsDialog dialog = new DiagnosticsDialog(frame);
        assertNotNull(dialog);
        String text = dialog.getReportText();
        assertNotNull(text);
        assertTrue("Dialog text must contain report header", text.contains("ASTROSOFT 2.0 SYSTEM DIAGNOSTICS"));
        dialog.dispose();
        frame.dispose();
    }

    @Test
    public void testShowDiagnosticsActionDoesNotThrow() {
        AstroSoft frame = new AstroSoft();
        frame.showDiagnostics();
        frame.dispose();
    }
}
