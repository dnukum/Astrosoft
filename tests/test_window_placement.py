import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class WindowPlacementIntegrationTest(unittest.TestCase):
    def test_restore_before_show(self):
        s=(ROOT/"src/app/astrosoft/ui/AstroSoft.java").read_text()
        self.assertNotIn("setPreferredSize(getScreenSize())",s)
        self.assertLess(s.index("pack();"),s.index("WindowPlacement.install(this)"))
        self.assertLess(s.index("WindowPlacement.install(this)"),s.index("setVisible(true)"))
    def test_save_before_exit(self):
        s=(ROOT/"src/app/astrosoft/ui/AstroSoft.java").read_text()
        self.assertLess(s.index("WindowPlacement.saveNow(this)"),s.index("System.exit(0)"))
    def test_display_and_persistence_guardrails(self):
        s=(ROOT/"src/app/astrosoft/ui/WindowPlacement.java").read_text()
        for marker in ["getDefaultTransform()", "getScreenInsets(config)", "Collections.sort(displays)", "preferences.flush()", "frame.getExtendedState() == Frame.NORMAL", "valid && maximized", "saveTimer.setRepeats(false)"]:
            self.assertIn(marker,s)
