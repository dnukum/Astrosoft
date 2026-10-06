package app.astrosoft.ui.test;

import java.awt.Rectangle;
import app.astrosoft.ui.WindowPlacement;

/** Pure geometry tests: no screen or persistent preferences required. */
public class WindowPlacementTest {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        Rectangle large = new Rectangle(1920, 25, 2560, 1415);
        Rectangle laptop = new Rectangle(0, 25, 1440, 835);
        Rectangle defaults = WindowPlacement.defaults(large);
        check(defaults.equals(new Rectangle(1940, 45, 1400, 900)), "Large monitor cap and left/top margin");
        Rectangle small = WindowPlacement.defaults(laptop);
        check(small.equals(new Rectangle(20, 45, 1296, 752)), "Laptop uses 90 percent of usable area");
        Rectangle saved = new Rectangle(2030, 90, 1100, 700);
        check(WindowPlacement.restore(saved, "same", "same", large).equals(saved), "Restore exact saved bounds");
        check(WindowPlacement.restore(saved, "old", "new", large).equals(defaults), "Display change resets bounds");
        check(WindowPlacement.restore(new Rectangle(-3000, 0, 1000, 700), "same", "same", large).equals(defaults), "Off-screen bounds reset");
        check(WindowPlacement.restore(new Rectangle(2000, 30, -1, 700), "same", "same", large).equals(defaults), "Invalid dimensions reset");
        check(WindowPlacement.restore(null, "", "same", large).equals(defaults), "First launch defaults");
        Rectangle tiny = new Rectangle(-800, 0, 320, 200);
        check(tiny.contains(WindowPlacement.defaults(tiny)), "Tiny/negative-coordinate display remains visible");
        Rectangle copy = WindowPlacement.restore(saved, "same", "same", large);
        copy.x = 0; check(saved.x == 2030, "Defensive bounds copy");
        System.out.println("WindowPlacementTest: all 9 geometry checks passed");
    }
}
