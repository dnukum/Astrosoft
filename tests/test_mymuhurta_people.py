import unittest
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
class PeopleSelectionTest(unittest.TestCase):
    def test_shared_people_and_rasi_only(self):
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("person1Horoscope",s)
        self.assertIn("person2Horoscope",s)
        self.assertNotIn("Varga.Navamsa",s)
        self.assertIn('addPersonChart("Person 1", person1Horoscope)',s)
        self.assertIn('addPersonChart("Person 2", person2Horoscope)',s)
    def test_person_independent_table_and_calendar_enablement(self):
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("selectedPlace, null).getTableData()",s)
        self.assertIn("balamBtn.setEnabled(hasPeople)",s)
        self.assertIn("if (!hasPeople)",s)
    def test_calendar_uses_shared_selection_and_names(self):
        s=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        self.assertNotIn("Select Partner",s)
        self.assertNotIn("FileOps.openFileDialog",s)
        self.assertIn("public void setPeople(Horoscope person1, Horoscope person2)",s)
        self.assertIn("setHeaderValue",s)
        self.assertIn("primaryLabel",s)
    def test_menu_is_available_without_natal_chart(self):
        s=(ROOT/"src/app/astrosoft/ui/AstroSoft.java").read_text()
        block=s[s.index("public void enableActions(boolean enable)"):s.index("public void viewChanged")]
        self.assertNotIn("Command.MY_MUHURTA_VIEW",block)

    def test_chart_title_is_compact(self):
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn('role + "<br>Rasi',s)
        self.assertNotIn("wrapChartName",s)

    def test_selection_labels_are_general(self):
        for filename in ["ui/view/MyMuhurtaView.java", "ui/comp/BalamCalendarPanel.java"]:
            s=(ROOT/"src/app/astrosoft"/filename).read_text()
            self.assertIn('"Person 1',s)
            self.assertIn('"Person 2',s)
            self.assertNotIn('"Wife',s)
            self.assertNotIn('"Husband',s)

    def test_calendar_under_charts_and_compact_people(self):
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("flyoutHost.add(chartContainer, BorderLayout.NORTH)",s)
        self.assertNotIn("clearPerson1Btn",s)
        self.assertNotIn("wrapChartName(person.getPersonName())",s)
    def test_update_applies_selected_window_without_closing(self):
        s=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        self.assertIn('new JButton("Update")',s)
        self.assertIn("updateAction.accept",s)
        self.assertIn("getStartTime()",s)
        self.assertNotIn("Close Balam Calendar",s)
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("updateView(false)",s)

    def test_table_and_buttons_are_top_aligned(self):
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("tableColumn.add(tablePanel, BorderLayout.NORTH)",s)
        self.assertIn("return tableColumn;",s)

    def test_view_margins_compact_toolbar_and_calendar_controls(self):
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("createEmptyBorder(15, 15, 20, 20)",s)
        self.assertIn("toolbarHost.add(createPeopleToolbar(), BorderLayout.WEST)",s)
        self.assertIn("resizeChartViewport()",s)
        s=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        self.assertIn("updatePanel.add(updateButton)",s)
        self.assertNotIn("header.add(updateButton, BorderLayout.EAST)",s)

    def test_calendar_height_is_bounded(self):
        s=(ROOT/"src/app/astrosoft/ui/view/MyMuhurtaView.java").read_text()
        self.assertIn("CalendarResizeHandles.fit",s)
        s=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        self.assertIn("new Dimension(800, 360)",s)

    def test_manual_calendar_resize_and_auto_columns(self):
        s=(ROOT/"src/app/astrosoft/ui/comp/BalamCalendarPanel.java").read_text()
        self.assertIn("CalendarResizeHandles.install(this)",s)
        self.assertIn("JTable.AUTO_RESIZE_ALL_COLUMNS",s)
        s=(ROOT/"src/app/astrosoft/ui/comp/CalendarResizeHandles.java").read_text()
        self.assertIn("Cursor.SE_RESIZE_CURSOR",s)
        self.assertIn("mouseDragged",s)
