import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

class MyMuhurtaFlyoutTest(unittest.TestCase):
    def test_calendar_is_embeddable_and_has_close_action(self):
        path = ROOT / "src/app/astrosoft/ui/comp/BalamCalendarPanel.java"
        self.assertTrue(path.exists(), "Calendar panel must exist")
        source = path.read_text()
        self.assertIn("extends JPanel", source)
        self.assertIn("updateAction.accept", source)
        self.assertNotIn("APPLICATION_MODAL", source)
        self.assertNotIn("setLocationRelativeTo", source)

    def test_calendar_preserves_model_and_row_details(self):
        path = ROOT / "src/app/astrosoft/ui/comp/BalamCalendarPanel.java"
        self.assertTrue(path.exists(), "Calendar panel must exist")
        source = path.read_text()
        self.assertIn("getBalamCalendarTable", source)
        self.assertIn("addListSelectionListener", source)
        self.assertIn("Next Month", source)
        self.assertNotIn("Select Partner", source)

    def test_view_opens_and_dismisses_inline_panel(self):
        source = (ROOT / "src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertNotIn("BalamCalendarDialog", source)
        self.assertIn("showBalamFlyout()", source)
        self.assertIn("flyoutHost.remove(balamFlyout)", source)
        self.assertIn("flyoutHost.add(balamFlyout", source)
        self.assertIn("flyoutHost.revalidate()", source)


class FlyoutLifecycleRegressionTest(unittest.TestCase):
    def test_button_toggles_and_label_tracks_visibility(self):
        source = (ROOT / "src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("toggleBalamFlyout();", source)
        self.assertIn('balamBtn.setText("Hide Balam Calendar")', source)
        self.assertIn('balamBtn.setText("Show Balam Calendar")', source)
        self.assertIn("private javax.swing.JButton balamBtn;", source)

    def test_context_refresh_updates_existing_panel_only_when_open(self):
        source = (ROOT / "src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("balamFlyout.setContext(muhurta);", source)
        panel = (ROOT / "src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        self.assertIn("public void setContext(MyMuhurta muhurta)", panel)
        self.assertIn("setContext(muhurta);", panel)

    def test_swing_lifecycle_on_event_thread(self):
        import subprocess
        import tempfile
        view = (ROOT / "src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        panel = (ROOT / "src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        def method(source, signature):
            start = source.index(signature)
            opening = source.index("{", start)
            depth = 1
            end = opening + 1
            while depth:
                depth += (source[end] == "{") - (source[end] == "}")
                end += 1
            return source[start:end]
        methods = "\n".join(method(view, "private void " + name + "()") for name in
                            ["toggleBalamFlyout", "showBalamFlyout", "hideBalamFlyout"])
        context_method = method(panel, "public void setContext(MyMuhurta muhurta)")
        methods = methods.replace("app.astrosoft.ui.comp.BalamCalendarPanel", "BalamCalendarPanel")
        java = """
import javax.swing.*;
import java.awt.*;
import java.util.*;
public class FlyoutLifecycleHarness extends JPanel {
    JPanel flyoutHost = new JPanel(new BorderLayout());
    BalamCalendarPanel balamFlyout;
    JButton balamBtn = new JButton("Show Balam Calendar");
    Object person1Horoscope, person2Horoscope;
    MyMuhurta context = new MyMuhurta(2026, 10);
    MyMuhurta createMuhurta() { return context; }
    void applyCalendarDate(java.util.Date date) {}
    static class MyMuhurta {
        Calendar date = Calendar.getInstance();
        MyMuhurta(int year, int month) { date.set(year, month - 1, 15); }
        MyMuhurta getPanchang() { return this; }
        Calendar getDate() { return date; }
    }
    static class BalamCalendarPanel extends JPanel {
        MyMuhurta muhurta;
        int currentYear, currentMonth, updates;
        Object partnerHoroscope = new Object();
        java.util.function.Consumer<java.util.Date> updateAction;
        BalamCalendarPanel(MyMuhurta m, java.util.function.Consumer<java.util.Date> update, Object person1, Object person2) { updateAction = update; setContext(m); }
        void setPeople(Object person1, Object person2) {}
        void updateTable() { updates++; }
        CONTEXT_METHOD
    }
    METHODS
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            FlyoutLifecycleHarness h = new FlyoutLifecycleHarness();
            h.hideBalamFlyout();
            h.toggleBalamFlyout();
            check(h.flyoutHost.getComponentCount() == 1);
            check(h.balamBtn.getText().equals("Hide Balam Calendar"));
            BalamCalendarPanel original = h.balamFlyout;
            h.showBalamFlyout();
            check(h.balamFlyout == original);
            Object partner = original.partnerHoroscope;
            original.setContext(new MyMuhurta(2027, 1));
            check(original.currentYear == 2027 && original.currentMonth == 1);
            check(original.partnerHoroscope == partner);
            check(original.updates == 2);
            h.toggleBalamFlyout();
            check(h.balamFlyout == null && h.flyoutHost.getComponentCount() == 0);
            check(h.balamBtn.getText().equals("Show Balam Calendar"));
            h.toggleBalamFlyout();
            h.hideBalamFlyout();
            check(h.balamFlyout == null && h.flyoutHost.getComponentCount() == 0);
            h.hideBalamFlyout();
            h.context = new MyMuhurta(2028, 12);
            h.toggleBalamFlyout();
            check(h.balamFlyout.muhurta == h.context);
            check(h.balamFlyout.currentYear == 2028 && h.balamFlyout.currentMonth == 12);
        });
    }
}
""".replace("CONTEXT_METHOD", context_method).replace("METHODS", methods)
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "FlyoutLifecycleHarness.java"
            path.write_text(java)
            compiled = subprocess.run(["javac", str(path)], capture_output=True, text=True)
            self.assertEqual(compiled.returncode, 0, compiled.stderr)
            ran = subprocess.run(["java", "-Djava.awt.headless=true", "-cp", directory,
                                  "FlyoutLifecycleHarness"], capture_output=True, text=True)
            self.assertEqual(ran.returncode, 0, ran.stderr)

if __name__ == "__main__":
    unittest.main()
