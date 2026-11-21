package app.astrosoft.ui.test;

import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.junit.Test;

import app.astrosoft.core.Compatibility;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.view.MarriageMatchView;

/**
 * Verifies that Candidate Profiles in MarriageMatchView wrap long names,
 * places, and coordinates without clipping any row vertically.
 */
public class MarriageMatchProfileLayoutTest {

    @Test
    public void testCandidateProfilesDoNotClipWrappedRows() throws Exception {
        Horoscope boy = new Horoscope("Candidate One", 15, 7, 1998, 21, 51,
                122 + (12.0 / 60.0), 47 + (36.0 / 60.0), -8.0,
                "Bellevue, Washington, United States");
        Horoscope girl = new Horoscope("Candidate Two LongSurname", 4, 11, 1998, 20, 22,
                55 + (18.0 / 60.0), 25 + (4.0 / 60.0), 4.0,
                "Dubai, Dubai, United Arab Emirates");

        Compatibility c = new Compatibility(boy, girl);
        MarriageMatchView view = new MarriageMatchView(c, new Point(0, 0));
        view.setViewMode(MarriageMatchView.ViewMode.DASHBOARD);
        view.setSize(new Dimension(1300, 900));

        // Force full Swing layout validation
        SwingUtilities.invokeAndWait(() -> layoutTree(view));

        List<JTextArea> areas = findTextAreas(view);
        assertTrue("Expected profile JTextAreas", areas.size() >= 10);

        for (JTextArea area : areas) {
            Dimension pref = area.getPreferredSize();
            assertTrue(
                    "JTextArea clipped for text '" + area.getText() + "': height="
                            + area.getHeight() + " < pref=" + pref.height,
                    area.getHeight() >= pref.height);
        }
    }

    private static void layoutTree(Container root) {
        root.doLayout();
        for (Component comp : root.getComponents()) {
            if (comp instanceof Container) {
                layoutTree((Container) comp);
            }
        }
    }

    private static List<JTextArea> findTextAreas(Container root) {
        List<JTextArea> list = new ArrayList<>();
        for (Component comp : root.getComponents()) {
            if (comp instanceof JTextArea) {
                list.add((JTextArea) comp);
            } else if (comp instanceof Container) {
                list.addAll(findTextAreas((Container) comp));
            }
        }
        return list;
    }
}
