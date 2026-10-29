package app.astrosoft.ui.test;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.beans.HousePosition;
import app.astrosoft.consts.Planet;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.CalendarChooser;
import app.astrosoft.ui.comp.CalendarSpinner;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.dlg.DefaultTransitChartComputation;
import app.astrosoft.ui.dlg.TransitChartComputation;
import app.astrosoft.ui.dlg.TransitChartDialog;
import junit.framework.TestCase;

import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import java.awt.Component;
import java.awt.Container;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;

public class TransitChartDialogTest extends TestCase {

    public void testDialogCanBeConstructed() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new StubTransitChartComputation());
        assertNotNull(dialog);
        assertTrue("Dialog should be resizable", dialog.isResizable());
        assertEquals("Transit (D1)", TransitChartDialog.CHART_TITLE);
        assertNotNull("Chart should be initialized", dialog.getChart());
        assertNotNull("Subtitle should be initialized with date and place", dialog.getChart().getSubtitle());
        assertTrue("Subtitle should contain city", dialog.getChart().getSubtitle().contains(place.city()));
        assertEquals(place, dialog.getCurrentPlace());
        dialog.dispose();
        frame.dispose();
    }

    public void testDialogWithDefaultComputation() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new DefaultTransitChartComputation());
        assertNotNull(dialog);
        assertTrue("Dialog should be resizable", dialog.isResizable());
        assertNotNull(dialog.getChart());
        assertNotNull(dialog.getEditButton());
        dialog.dispose();
        frame.dispose();
    }

    public void testActionOpensTransitChartDialog() {
        AstroSoft frame = new AstroSoft();
        frame.getActionManager().getAction(app.astrosoft.consts.Command.TRANSIT_CHART)
                .actionPerformed(new java.awt.event.ActionEvent(this, 1, app.astrosoft.consts.Command.TRANSIT_CHART.name()));
        assertNotNull(frame.getTransitChartDialog());
        assertTrue(frame.getTransitChartDialog().isVisible());
        assertTrue("Opened dialog should be resizable", frame.getTransitChartDialog().isResizable());
        frame.getTransitChartDialog().dispose();
        frame.dispose();
    }

    public void testTransitChartTooltipDoesNotShowHouseNumber() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new StubTransitChartComputation());
        Chart chart = dialog.getChart();
        assertNotNull(chart);

        // Verify that none of the house panels or tables contain "House:" in their tooltips
        boolean foundTooltip = false;
        for (Component comp : chart.getComponents()) {
            if (comp instanceof JPanel) {
                String tip = ((JPanel) comp).getToolTipText();
                if (tip != null && tip.contains("deg;")) {
                    foundTooltip = true;
                    assertFalse("Transit chart house tooltip must NOT contain 'House:'", tip.contains("House:"));
                }
            }
        }
        assertTrue("Should have checked at least one house tooltip", foundTooltip);

        dialog.dispose();
        frame.dispose();
    }

    public void testTransitChartPlanetTooltipFormat() {
        assertEquals("11:19", Chart.formatSignRelativeOrb(191.32));
        assertEquals("191:19", Chart.formatAbsoluteLongitude(191.32));
    }

    public void testSignTransitionCrossingBoundary() {
        Place place = AstroSoft.getPreferences().getPlace();
        Calendar cal = Calendar.getInstance(place.astrosoftTimeZone().getTimeZone());
        cal.set(2026, Calendar.NOVEMBER, 16, 19, 41, 0);
        Date chartTime = cal.getTime();

        DefaultTransitChartComputation comp = new DefaultTransitChartComputation();
        PlanetaryInfo info = comp.compute(chartTime, place);
        java.util.Map<Planet, TransitChartComputation.SignTransition> transitions = comp.computeSignTransitions(chartTime, place, info);
        TransitChartComputation.SignTransition sunTrans = transitions.get(Planet.Sun);
        assertNotNull(sunTrans);
        assertEquals(app.astrosoft.consts.Rasi.Thula, sunTrans.getFromRasi());
        assertEquals(app.astrosoft.consts.Rasi.Vrichika, sunTrans.getToRasi());

        Date exact = sunTrans.getTransitionDate();
        // 1 minute before exact transition: must be in Thula
        PlanetaryInfo before = comp.compute(new Date(exact.getTime() - 60000L), place);
        assertEquals(app.astrosoft.consts.Rasi.Thula, app.astrosoft.consts.Rasi.ofDeg(before.getPlanetPosition().get(Planet.Sun)));

        // 1 minute after exact transition: must be in Vrichika
        PlanetaryInfo after = comp.compute(new Date(exact.getTime() + 60000L), place);
        assertEquals(app.astrosoft.consts.Rasi.Vrichika, app.astrosoft.consts.Rasi.ofDeg(after.getPlanetPosition().get(Planet.Sun)));

        // Test rounding up to next minute (ceiling) so chart position matches displayed minute
        Date rounded = TransitChartDialog.roundUpToMinute(exact);
        long expectedRoundedTime = exact.getTime() + (60000L - (exact.getTime() % 60000L));
        assertEquals(expectedRoundedTime, rounded.getTime());
        // Exact minute boundary remains unchanged
        assertEquals(rounded, TransitChartDialog.roundUpToMinute(rounded));

        // At the rounded minute (e.g., 7:43 PM), planet is guaranteed to be in the new sign
        PlanetaryInfo atRoundedMinute = comp.compute(rounded, place);
        assertEquals(app.astrosoft.consts.Rasi.Vrichika, app.astrosoft.consts.Rasi.ofDeg(atRoundedMinute.getPlanetPosition().get(Planet.Sun)));
    }

    public void testApplyConfigUpdateChangesChartTime() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        java.util.TimeZone tz = place.astrosoftTimeZone() != null ? place.astrosoftTimeZone().getTimeZone() : java.util.TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2026, Calendar.OCTOBER, 28, 21, 0, 0); // 9:00 PM in place's timezone
        Date initialTime = cal.getTime();

        TransitChartDialog dialog = new TransitChartDialog(frame, initialTime, place, new StubTransitChartComputation());
        assertTrue("Initial subtitle should show 9:00 PM: " + dialog.getChart().getSubtitle(),
                dialog.getChart().getSubtitle().contains("9:00") || dialog.getChart().getSubtitle().contains("09:00"));

        // Simulate changing time to 11:00 PM
        cal.set(2026, Calendar.OCTOBER, 28, 23, 0, 0); // 11:00 PM in place's timezone
        Date updatedTime = cal.getTime();

        dialog.applyConfigUpdate(updatedTime, updatedTime, place);

        assertEquals(updatedTime, dialog.getSelectedInstant());
        assertTrue("Updated subtitle should show 11:00 PM: " + dialog.getChart().getSubtitle(),
                dialog.getChart().getSubtitle().contains("11:00"));
        assertFalse("Updated subtitle should no longer show 9:00 PM",
                dialog.getChart().getSubtitle().contains("9:00 PM") || dialog.getChart().getSubtitle().contains("09:00 PM"));

        dialog.dispose();
        frame.dispose();
    }

    public void testCalendarSpinnerCommitsTyping() throws Exception {
        CalendarSpinner timeSpinner = CalendarChooser.getTimeChooser();
        timeSpinner.getChooser(); // ensures spinner is created
        SimpleDateFormat sdf = new SimpleDateFormat(CalendarSpinner.FMT_TIME);
        Date d9 = sdf.parse("09 : 00 : 00 PM");
        timeSpinner.setSelectedDate(d9);

        // Find the JFormattedTextField inside chooserPanel
        JFormattedTextField textField = null;
        for (Component c : timeSpinner.getChooser().getComponents()) {
            if (c instanceof javax.swing.JSpinner) {
                javax.swing.JSpinner js = (javax.swing.JSpinner) c;
                if (js.getEditor() instanceof javax.swing.JSpinner.DefaultEditor) {
                    textField = ((javax.swing.JSpinner.DefaultEditor) js.getEditor()).getTextField();
                }
            }
        }
        assertNotNull("Spinner text field should be found", textField);

        // Simulate user typing "11 : 00 : 00 PM"
        textField.setText("11 : 00 : 00 PM");

        // getSelectedDate() must commit the edit and return 11 PM, not 9 PM
        Date selected = timeSpinner.getSelectedDate();
        assertNotNull(selected);
        String formatted = sdf.format(selected);
        assertTrue("getSelectedDate() should commit typed text 11 PM: was " + formatted, formatted.contains("11 : 00 : 00 PM"));
    }

    public void testMoreButtonExpandsAndCollapses() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new StubTransitChartComputation());

        assertNotNull(dialog.getEditButton());
        assertNotNull(dialog.getMoreButton());
        assertEquals("More...", dialog.getMoreButton().getText());
        assertFalse(dialog.isExpanded());
        int initialHeight = dialog.getHeight();

        // Click More... to expand
        dialog.getMoreButton().doClick();
        assertTrue(dialog.isExpanded());
        assertEquals("Less...", dialog.getMoreButton().getText());
        assertTrue(dialog.getHeight() > initialHeight);
        assertTrue("Expanded height should be >= 500", dialog.getHeight() >= 500);

        // Click Less... to collapse
        dialog.getMoreButton().doClick();
        assertFalse(dialog.isExpanded());
        assertEquals("More...", dialog.getMoreButton().getText());
        assertEquals(395, dialog.getHeight());

        dialog.dispose();
        frame.dispose();
    }

    public void testButtonsAreBetweenChartAndTable() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new StubTransitChartComputation());
        dialog.setExpanded(true);
        dialog.pack();
        dialog.setSize(360, 595);
        dialog.validate();

        java.awt.Rectangle chartBounds = javax.swing.SwingUtilities.convertRectangle(
                dialog.getChart().getParent(), dialog.getChart().getBounds(), dialog.getContentPane());
        int chartBottomY = chartBounds.y + chartBounds.height;

        java.awt.Rectangle buttonBounds = javax.swing.SwingUtilities.convertRectangle(
                dialog.getEditButton().getParent(), dialog.getEditButton().getBounds(), dialog.getContentPane());
        int buttonTopY = buttonBounds.y;
        int buttonBottomY = buttonBounds.y + buttonBounds.height;

        javax.swing.JScrollPane scroll = (javax.swing.JScrollPane) dialog.getDetailsTable().getParent().getParent();
        java.awt.Rectangle scrollBounds = javax.swing.SwingUtilities.convertRectangle(
                scroll.getParent(), scroll.getBounds(), dialog.getContentPane());
        int tableTopY = scrollBounds.y;

        assertTrue("Buttons must be below chart: chartBottom=" + chartBottomY + ", buttonTop=" + buttonTopY,
                buttonTopY >= chartBottomY);
        assertTrue("Table must be below buttons: buttonBottom=" + buttonBottomY + ", tableTop=" + tableTopY,
                tableTopY >= buttonBottomY);

        java.awt.Rectangle updateBounds = javax.swing.SwingUtilities.convertRectangle(
                dialog.getUpdateButton().getParent(), dialog.getUpdateButton().getBounds(), dialog.getContentPane());
        int updateTopY = updateBounds.y;
        int tableBottomY = scrollBounds.y + scrollBounds.height;
        assertTrue("Update button must be below table: tableBottom=" + tableBottomY + ", updateTop=" + updateTopY,
                updateTopY >= tableBottomY);

        dialog.dispose();
        frame.dispose();
    }

    public void testTableResizesVerticallyWhenDialogStretched() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new StubTransitChartComputation());
        dialog.setExpanded(true);
        dialog.pack();
        dialog.setSize(360, 595);
        dialog.validate();

        javax.swing.JScrollPane scroll = (javax.swing.JScrollPane) dialog.getDetailsTable().getParent().getParent();
        int initialHeight = scroll.getHeight();

        // Stretch dialog vertically to 750
        dialog.setSize(360, 750);
        dialog.validate();

        int stretchedHeight = scroll.getHeight();
        assertTrue("Table scroll pane should expand vertically when dialog is stretched (initial: "
                + initialHeight + ", stretched: " + stretchedHeight + ")", stretchedHeight > initialHeight);

        dialog.dispose();
        frame.dispose();
    }

    public void testDetailsTableColumnsAndRows() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new StubTransitChartComputation());

        javax.swing.JTable table = dialog.getDetailsTable();
        assertNotNull(table);
        assertEquals(3, table.getColumnCount());
        assertEquals("Planet", table.getColumnName(0));
        assertEquals("Sign Change", table.getColumnName(1));
        assertEquals("Date Change", table.getColumnName(2));

        assertEquals(9, table.getRowCount()); // 9 grahas
        assertEquals("Sun", table.getValueAt(0, 0));
        assertEquals("Mesha -> Vrishabha", table.getValueAt(0, 1));
        assertNotNull(table.getValueAt(0, 2));

        dialog.dispose();
        frame.dispose();
    }

    public void testUpdateButtonStateAndSelection() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        TransitChartDialog dialog = new TransitChartDialog(frame, new Date(), place, new StubTransitChartComputation());
        javax.swing.JButton updateBtn = dialog.getUpdateButton();
        javax.swing.JTable table = dialog.getDetailsTable();

        assertNotNull(updateBtn);
        assertNotNull(table);
        assertTrue("Row selection must be allowed", table.getRowSelectionAllowed());
        assertEquals(javax.swing.ListSelectionModel.SINGLE_SELECTION, table.getSelectionModel().getSelectionMode());

        // Initially disabled
        assertFalse("Update button should be disabled when no row is selected", updateBtn.isEnabled());

        // Select a row
        table.setRowSelectionInterval(1, 1);
        assertTrue("Update button should be enabled when a row is selected", updateBtn.isEnabled());

        // Deselect
        table.clearSelection();
        assertFalse("Update button should be disabled when selection is cleared", updateBtn.isEnabled());

        dialog.dispose();
        frame.dispose();
    }

    public void testUpdateActionWithStubComputation() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        Date initialInstant = new Date(1700000000000L);
        TransitChartDialog dialog = new TransitChartDialog(frame, initialInstant, place, new StubTransitChartComputation());
        javax.swing.JButton updateBtn = dialog.getUpdateButton();
        javax.swing.JTable table = dialog.getDetailsTable();

        assertEquals(initialInstant, dialog.getSelectedInstant());

        // Select Moon row (index 1)
        table.setRowSelectionInterval(1, 1);
        assertTrue(updateBtn.isEnabled());

        // Click Update
        updateBtn.doClick();

        // Stub returns instant + 1 day (86400000L)
        Date expectedDate = TransitChartDialog.roundUpToMinute(new Date(initialInstant.getTime() + 86400000L));
        assertEquals(expectedDate, dialog.getSelectedInstant());
        assertFalse("Update button should be disabled after update", updateBtn.isEnabled());
        assertEquals(-1, table.getSelectedRow());

        dialog.dispose();
        frame.dispose();
    }

    public void testUpdateActionWithRealComputation() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();
        Calendar cal = Calendar.getInstance(place.astrosoftTimeZone().getTimeZone());
        cal.set(2026, Calendar.NOVEMBER, 1, 0, 0, 0); // 1 Nov 2026, 12:00 AM
        Date initialTime = cal.getTime();

        TransitChartDialog dialog = new TransitChartDialog(frame, initialTime, place, new DefaultTransitChartComputation());
        dialog.setExpanded(true);
        javax.swing.JButton updateBtn = dialog.getUpdateButton();
        javax.swing.JTable table = dialog.getDetailsTable();

        // Row 1 is Moon
        assertEquals("Moon", table.getValueAt(1, 0));
        assertEquals("Mithuna -> Kataka", table.getValueAt(1, 1));
        String initialMoonDateStr = (String) table.getValueAt(1, 2);
        assertTrue("Moon transition should be on 1 Nov 2026: " + initialMoonDateStr,
                initialMoonDateStr.contains("1 Nov 2026") || initialMoonDateStr.contains("Nov 1, 2026"));

        // Select Moon row
        table.setRowSelectionInterval(1, 1);
        assertTrue(updateBtn.isEnabled());

        // Click Update
        updateBtn.doClick();

        // Selected instant should now be the Moon transition time
        assertTrue("Chart subtitle should reflect new time: " + dialog.getChart().getSubtitle(),
                dialog.getChart().getSubtitle().contains("1 Nov 2026") || dialog.getChart().getSubtitle().contains("Nov 1, 2026"));

        // Moon in the updated chart must now have entered Kataka (>= 90 deg)
        app.astrosoft.beans.PlanetChartData planetChartData = (app.astrosoft.beans.PlanetChartData) dialog.getChart().getChartData();
        assertNotNull(planetChartData);
        double moonPos = planetChartData.getPlanetLongitude(Planet.Moon);
        assertEquals(app.astrosoft.consts.Rasi.Kataka, app.astrosoft.consts.Rasi.ofDeg(moonPos));
        assertTrue("Kataka should contain Moon", planetChartData.getPlanetsInRasi().get(app.astrosoft.consts.Rasi.Kataka).contains(Planet.Moon));

        // Ascendant should be calculated for the new time
        assertNotNull(planetChartData.getAscendant());
        assertNotNull(planetChartData.getPlanetLongitude(Planet.Ascendant));

        // Update button should be disabled and selection cleared
        assertFalse("Update button should be disabled after update completes", updateBtn.isEnabled());
        assertEquals(-1, table.getSelectedRow());

        // The Moon row in the newly refreshed table should now show its NEXT transition (Kataka -> Simha)
        assertEquals("Kataka -> Simha", table.getValueAt(1, 1));

        dialog.dispose();
        frame.dispose();
    }

    public void testPlaceChooserSearchHardening() throws Exception {
        app.astrosoft.ui.comp.PlaceChooser chooser = new app.astrosoft.ui.comp.PlaceChooser(new java.awt.Dimension(400, 140), false);
        java.lang.reflect.Field f = app.astrosoft.ui.comp.PlaceChooser.class.getDeclaredField("placeCombo");
        f.setAccessible(true);
        javax.swing.JComboBox combo = (javax.swing.JComboBox) f.get(chooser);
        javax.swing.JTextField editor = (javax.swing.JTextField) combo.getEditor().getEditorComponent();

        editor.setText("Erode, Tamil Nadu, India");
        java.util.List<Place> places = app.astrosoft.util.PlaceFinder.findPlace(editor.getText());
        assertNotNull(places);
        assertFalse("Search with full place text should find city in XML fallback", places.isEmpty());
        assertEquals("Erode", places.get(0).city());
    }

    public void testDefaultTransitChartComputationComputesSignChanges() {
        Place place = AstroSoft.getPreferences().getPlace();
        Date now = new Date();
        DefaultTransitChartComputation comp = new DefaultTransitChartComputation();
        PlanetaryInfo info = comp.compute(now, place);
        assertNotNull(info);
        java.util.Map<Planet, Date> changes = comp.computeSignChanges(now, place, info);
        assertNotNull(changes);
        assertEquals(9, changes.size());
        for (Planet p : Planet.allPlanets()) {
            Date change = changes.get(p);
            assertNotNull("Sign change date must be computed for " + p, change);
            assertTrue("Sign change date must be in future for " + p, change.after(now));
        }

        java.util.Map<Planet, TransitChartComputation.SignTransition> transitions = comp.computeSignTransitions(now, place, info);
        assertNotNull(transitions);
        assertEquals(9, transitions.size());
        for (Planet p : Planet.allPlanets()) {
            TransitChartComputation.SignTransition trans = transitions.get(p);
            assertNotNull("SignTransition must be computed for " + p, trans);
            assertNotNull("fromRasi must not be null for " + p, trans.getFromRasi());
            assertNotNull("toRasi must not be null for " + p, trans.getToRasi());
            assertTrue("Transition string should contain '->'", trans.getTransitionString().contains(" -> "));
            assertNotNull("Transition date must not be null for " + p, trans.getTransitionDate());
            assertTrue("Transition date must be in future for " + p, trans.getTransitionDate().after(now));
        }
    }

    private static class StubTransitChartComputation implements TransitChartComputation {
        public PlanetaryInfo compute(Date instant, Place place) {
            EnumMap<Planet, Double> positions = new EnumMap<Planet, Double>(Planet.class);
            EnumMap<Planet, Boolean> directions = new EnumMap<Planet, Boolean>(Planet.class);
            double[] cusps = new double[13];
            for (int i = 1; i <= 12; i++) {
                cusps[i] = (i - 1) * 30.0;
            }
            HousePosition houses = new HousePosition(cusps);
            for (Planet p : Planet.values()) {
                positions.put(p, 0.0);
                directions.put(p, Boolean.TRUE);
            }
            return new PlanetaryInfo(positions, directions, houses);
        }

        @Override
        public java.util.Map<Planet, Date> computeSignChanges(Date instant, Place place, PlanetaryInfo planetaryInfo) {
            java.util.Map<Planet, Date> map = new java.util.EnumMap<Planet, Date>(Planet.class);
            for (Planet p : Planet.allPlanets()) {
                map.put(p, new Date(instant.getTime() + 86400000L));
            }
            return map;
        }

        @Override
        public java.util.Map<Planet, TransitChartComputation.SignTransition> computeSignTransitions(
                Date instant, Place place, PlanetaryInfo planetaryInfo) {
            java.util.Map<Planet, TransitChartComputation.SignTransition> map =
                    new java.util.EnumMap<Planet, TransitChartComputation.SignTransition>(Planet.class);
            for (Planet p : Planet.allPlanets()) {
                map.put(p, new TransitChartComputation.SignTransition(
                        app.astrosoft.consts.Rasi.Mesha,
                        app.astrosoft.consts.Rasi.Vrishabha,
                        new Date(instant.getTime() + 86400000L)));
            }
            return map;
        }
    }
}
