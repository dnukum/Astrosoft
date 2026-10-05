import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class TimezoneEntryTest(unittest.TestCase):
    def test_no_preload_or_role_prompt(self):
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertNotIn("showOptionDialog",s)
        self.assertNotIn("getPersonSex()",s)
        self.assertIn("selectedTime = selectedDate;",s)
    def test_calculation_and_display_use_place_timezone(self):
        s=(ROOT/"src/app/astrosoft/core/MyMuhurta.java").read_text()
        self.assertIn("Calendar systemCal = Calendar.getInstance(tz)",s)
        self.assertIn("Calendar tCal = Calendar.getInstance(tz)",s)
        self.assertIn("df.setTimeZone(tz)",s)
        self.assertIn("tf.setTimeZone(tz)",s)
        self.assertIn("cal.set(Calendar.SECOND, tCal.get(Calendar.SECOND))",s)
    def test_editor_converts_local_wall_clock(self):
        s=(ROOT/"src/app/astrosoft/ui/dlg/MuhurtaConfigDialog.java").read_text()
        self.assertIn("toChooserDate(initDate, zone)",s)
        self.assertIn("fromChooserValues(",s)
