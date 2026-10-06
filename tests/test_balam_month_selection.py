import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class MonthSelectionTest(unittest.TestCase):
    def test_dropdowns_not_free_text(self):
        s=(ROOT/'src/app/astrosoft/ui/comp/BalamCalendarPanel.java').read_text()
        self.assertIn('monthChoice.setEditable(false)',s)
        self.assertIn('yearChoice.setEditable(false)',s)
        self.assertIn('syncMonthChoices()',s)
        self.assertIn('syncingMonthChoices',s)
        self.assertIn('currentMonth = monthChoice.getSelectedIndex() + 1',s)
        self.assertIn('currentYear = (Integer) yearChoice.getSelectedItem()',s)
    def test_update_separated_on_right(self):
        s=(ROOT/'src/app/astrosoft/ui/comp/BalamCalendarPanel.java').read_text()
        self.assertIn('topPanel.add(updatePanel, BorderLayout.EAST)',s)
    def test_toggle_below_chart(self):
        s=(ROOT/'src/app/astrosoft/ui/view/MyMuhurtaView.java').read_text()
        self.assertIn('chartControls.add(balamBtn)',s)
        self.assertIn('chartArea.add(chartControlStrip, BorderLayout.SOUTH)',s)
        self.assertIn('new Dimension(chartSize.width, 42)',s)
        self.assertIn('balamBtn.setVisible(hasPeople)',s)
        self.assertIn('chartControlStrip.setVisible(hasPeople)',s)
        self.assertNotIn('muhurtaChartColumn',s)
        self.assertNotIn('footerHeight + scrollHeight',s)
