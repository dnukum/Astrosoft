import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class EventTimeRegressionTest(unittest.TestCase):
    def test_table_formats_preserved_event_not_panchang_daily_time(self):
        s=(ROOT/"src/app/astrosoft/core/MyMuhurta.java").read_text()
        self.assertIn("df.format(eventInstant)",s)
        self.assertIn("tf.format(eventInstant)",s)
        self.assertNotIn("tf.format(pan.getDate().getTime())",s)
        self.assertLess(s.index("eventInstant = new Date(cal.getTimeInMillis())"),s.index("pan = new Panchang(cal.getTime()"))
    def test_edit_and_calendar_update_use_event_inputs(self):
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("selectedTime = time;",s)
        self.assertIn("selectedDate = new java.util.Date(instant.getTime());",s)
        self.assertIn("selectedTime = selectedDate;",s)
