import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class BalamWindowHeaderTest(unittest.TestCase):
    def renderer(self):
        s=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        return s[s.index("private static String buildDetailsHtml"):s.index("private void appendCandidateSets")]
    def test_details_show_explicit_start_and_end(self):
        s=self.renderer()
        self.assertEqual(1,s.count("<b>Start:</b>"))
        self.assertEqual(1,s.count("<b>End:</b>"))
        self.assertIn("details.getStart()",s)
        self.assertIn("details.getEnd()",s)
    def test_calendar_date_column_remains_start_only(self):
        s=(ROOT/"src/app/astrosoft/beans/BalamDateCell.java").read_text()
        self.assertIn("public String toString() { return formatStart(); }",s)
    def test_renderer_only_consumes_results(self):
        s=self.renderer()
        self.assertIn("details.getEntries()",s)
        for forbidden in ["BalamWindowEngine", "getBalamWindowDetails", "SwissHelper", "Calendar.", "<table", "appendCandidateSets(", "appendPersonRow("]:
            self.assertNotIn(forbidden,s)
        self.assertIn("weekdays[entry.getWeekday()]",s)
    def test_worker_handles_stale_selection(self):
        s=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        self.assertIn("doInBackground()",s)
        self.assertIn("context.getBalamWindowDetails(start, end)",s)
        self.assertIn("isCancelled() || request != detailsRequest",s)
        self.assertIn("detailsWorker.cancel(true)",s)
        self.assertIn("removeNotify()",s)
