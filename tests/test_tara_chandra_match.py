import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class MatchPresentationTest(unittest.TestCase):
    def details_source(self):
        panel=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        return panel[panel.index("private static String buildDetailsHtml"):panel.index("private void appendCandidateSets")]

    def test_selected_row_displays_window_not_core_candidates(self):
        panel=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        self.assertIn("requestDetails(selectedRow)",panel)
        details=self.details_source()
        self.assertNotIn("appendCandidateSets(",details)
        self.assertNotIn("appendPersonRow(",details)
        self.assertIn("details.getEntries()",details)

    def test_intersection_is_owned_by_core_not_result_bean(self):
        core=(ROOT/"src/app/astrosoft/core/MyMuhurta.java").read_text()
        bean=(ROOT/"src/app/astrosoft/beans/TaraChandraMatchResult.java").read_text()
        self.assertIn("matches.retainAll(chandraSet)",core)
        self.assertNotIn("retainAll",bean)
        self.assertIn("this.matches = matches.clone()",bean)

    def test_calendar_appends_match_column(self):
        model=(ROOT/"src/app/astrosoft/core/BalamCalendarModel.java").read_text()
        self.assertIn("cols.add(AstrosoftTableColumn.Match)",model)
        self.assertIn("find_tara_chandra_match",model)
        self.assertIn("pChandraObj, matchCell",model)

    def test_calendar_ui_metadata_includes_match_for_one_and_two_people(self):
        panel=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        block=panel[panel.index("DefaultColumnMetaData meta;"):panel.index("tableModel = new AstrosoftTableModel")]
        self.assertEqual(2,block.count("app.astrosoft.consts.AstrosoftTableColumn.Match"))
        self.assertIn("AstrosoftTableColumn.ChandraBalam,",block)
        self.assertIn("AstrosoftTableColumn.SpouseChandraBalam,",block)

    def test_balam_details_replaces_score_table_with_varam_paksham_thithi(self):
        details=self.details_source()
        for label in ["Thithi", "Varam"]:
            self.assertIn("<b>"+label+":</b>",details)
        self.assertLess(details.index("<b>Thithi:</b>"),details.index("<b>Varam:</b>"))
        self.assertIn("entry.getPaksha()",details)
        self.assertIn("entry.getThithi()",details)
        self.assertIn("entry.getWeekday()",details)
        self.assertIn("dateFormat.setTimeZone(details.getTimeZone())",details)
        self.assertIn("entry.isWarning()",details)
        self.assertIn("<font color='red'>",details)
        self.assertIn("!thithi.equals(previousThithi)",details)
        self.assertIn("entry.getWeekday() != previousVaram",details)
        self.assertNotIn("<table",details)
        self.assertEqual(1,details.count("<b>Start:</b>"))
        self.assertEqual(1,details.count("<b>End:</b>"))
