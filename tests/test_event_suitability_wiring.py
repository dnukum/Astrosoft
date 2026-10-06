import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class SuitabilityWiringTest(unittest.TestCase):
    def test_selected_matches_passed_to_core(self):
        s=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        self.assertIn("table.getValueAt(row, table.getColumnCount() - 1)",s)
        self.assertIn("context.getBalamWindowDetails(start, end, matches)",s)
        self.assertNotIn("NakshatraEventSuitability.eventsFor",s)
        self.assertIn("request != detailsRequest",s)
    def test_rule_catalog_is_core_only(self):
        s=(ROOT/"src/app/astrosoft/beans/BalamWindowDetails.java").read_text()
        self.assertNotIn("NakshatraEventSuitability",s)
