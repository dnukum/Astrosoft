import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class NakshatraTooltipTest(unittest.TestCase):
    def test_chart_data_exposes_precomputed_nakshatra(self):
        s=(ROOT/"src/app/astrosoft/beans/PlanetChartData.java").read_text()
        self.assertIn("getPlanetNakshathra(Planet planet)",s)
        self.assertIn("planetaryInfo.getPlanetNakshathra()",s)
    def test_hover_only_looks_up_nakshatra(self):
        s=(ROOT/"src/app/astrosoft/ui/comp/Chart.java").read_text()
        self.assertIn("getToolTipText(java.awt.event.MouseEvent event)",s)
        self.assertIn("getPlanetNakshathra((Planet) value)",s)
        self.assertIn("nak.toString()",s)
    def test_muhurta_domain_supplies_result_map(self):
        s=(ROOT/"src/app/astrosoft/core/Panchang.java").read_text()
        self.assertIn("planetNakshathra.put",s)
        self.assertIn("getPlanetNakshathra()",s)
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("muhurta.getPanchang().getPlanetNakshathra()",s)
