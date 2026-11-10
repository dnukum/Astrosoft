package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import org.junit.Test;

import app.astrosoft.consts.Rasi;
import app.astrosoft.ui.comp.chart.NorthIndianGeometry;

public class NorthIndianGeometryTest {

    @Test
    public void testTwelveHousesPartitionSquareWithoutOverlap() {
        double size = 400.0;
        NorthIndianGeometry geom = new NorthIndianGeometry(10.0, 20.0, size);

        Area union = new Area();
        double totalArea = 0.0;
        for (int h = 1; h <= 12; h++) {
            Shape s = geom.getHouseShape(h);
            Area a = new Area(s);
            assertFalse("House " + h + " should be non-empty", a.isEmpty());

            // Check no overlap with union so far
            Area intersection = new Area(union);
            intersection.intersect(a);
            assertTrue("House " + h + " overlaps previous houses (area=" + polygonArea(intersection) + ")",
                    polygonArea(intersection) < 1e-3);

            union.add(a);
            totalArea += polygonArea(a);
        }

        assertEquals("Sum of 12 house areas must equal square area", size * size, totalArea, 1e-2);
        assertEquals("Union area must equal square area", size * size, polygonArea(union), 1e-2);
    }

    @Test
    public void testCounterClockwiseHouseOrder() {
        NorthIndianGeometry geom = new NorthIndianGeometry(0.0, 0.0, 400.0);
        double cx = 200.0;
        double cy = 200.0;

        // House 1 is top diamond: centroid directly above center
        Point2D.Double c1 = geom.getHouseCentroid(1);
        assertEquals(cx, c1.x, 1e-6);
        assertTrue(c1.y < cy);

        // House 4 is left diamond, House 7 is bottom diamond, House 10 is right diamond
        assertTrue(geom.getHouseCentroid(4).x < cx);
        assertEquals(cy, geom.getHouseCentroid(4).y, 1e-6);

        assertEquals(cx, geom.getHouseCentroid(7).x, 1e-6);
        assertTrue(geom.getHouseCentroid(7).y > cy);

        assertTrue(geom.getHouseCentroid(10).x > cx);
        assertEquals(cy, geom.getHouseCentroid(10).y, 1e-6);

        // Strictly increasing CCW angle starting from House 1 (top = +90 deg in standard math coords where y points up)
        double prevAngle = ccwAngleFromTop(cx, cy, geom.getHouseCentroid(1));
        for (int h = 2; h <= 12; h++) {
            double ang = ccwAngleFromTop(cx, cy, geom.getHouseCentroid(h));
            assertTrue("House " + h + " should be counter-clockwise after house " + (h - 1)
                    + " (prev=" + prevAngle + ", cur=" + ang + ")", ang > prevAngle);
            prevAngle = ang;
        }
    }

    @Test
    public void testSignRotationAllAscendantsAndSampleCancer() {
        // Check against user's Cancer (Kataka = sign 4) sample chart
        assertEquals(4, NorthIndianGeometry.signForHouse(Rasi.Kataka, 1).ordinal() + 1);
        assertEquals(5, NorthIndianGeometry.signForHouse(Rasi.Kataka, 2).ordinal() + 1);
        assertEquals(6, NorthIndianGeometry.signForHouse(Rasi.Kataka, 3).ordinal() + 1);
        assertEquals(7, NorthIndianGeometry.signForHouse(Rasi.Kataka, 4).ordinal() + 1);
        assertEquals(10, NorthIndianGeometry.signForHouse(Rasi.Kataka, 7).ordinal() + 1);
        assertEquals(1, NorthIndianGeometry.signForHouse(Rasi.Kataka, 10).ordinal() + 1);
        assertEquals(2, NorthIndianGeometry.signForHouse(Rasi.Kataka, 11).ordinal() + 1);
        assertEquals(3, NorthIndianGeometry.signForHouse(Rasi.Kataka, 12).ordinal() + 1);

        // Round-trip for all 12 ascendants x 12 houses
        for (Rasi asc : Rasi.values()) {
            for (int h = 1; h <= 12; h++) {
                Rasi sign = NorthIndianGeometry.signForHouse(asc, h);
                assertEquals(h, NorthIndianGeometry.houseForRasi(asc, sign));
                assertEquals(h, sign.bhava(asc));
            }
        }
    }

    @Test
    public void testHitTestingCentroidsAndSignAnchors() {
        NorthIndianGeometry geom = new NorthIndianGeometry(0.0, 0.0, 360.0);
        for (int h = 1; h <= 12; h++) {
            assertEquals("Centroid must hit house " + h, h, geom.houseAt(geom.getHouseCentroid(h)));
            assertEquals("Sign anchor must hit house " + h, h, geom.houseAt(geom.getSignAnchor(h)));
            assertEquals("Content anchor must hit house " + h, h, geom.houseAt(geom.getContentAnchor(h)));
        }
        assertEquals(-1, geom.houseAt(new Point2D.Double(-5, 100)));
        assertEquals(-1, geom.houseAt(new Point2D.Double(400, 100)));
    }

    @Test
    public void testRectangularPartitionWithoutOverlap() {
        double width = 414.0;
        double height = 329.0;
        NorthIndianGeometry geom = new NorthIndianGeometry(4.0, 2.0, width, height);

        assertEquals(width, geom.getWidth(), 1e-6);
        assertEquals(height, geom.getHeight(), 1e-6);
        Area union = new Area();
        double totalArea = 0.0;
        for (int h = 1; h <= 12; h++) {
            Shape s = geom.getHouseShape(h);
            Area a = new Area(s);
            assertFalse("House " + h + " should be non-empty", a.isEmpty());
            Area intersection = new Area(union);
            intersection.intersect(a);
            assertTrue("House " + h + " overlaps previous houses", polygonArea(intersection) < 1e-3);
            union.add(a);
            totalArea += polygonArea(a);
            assertEquals("Centroid must hit house " + h, h, geom.houseAt(geom.getHouseCentroid(h)));
        }
        assertEquals("Sum of 12 house areas must equal rectangle area", width * height, totalArea, 1e-2);
        assertEquals("Union area must equal rectangle area", width * height, polygonArea(union), 1e-2);
    }

    private static double ccwAngleFromTop(double cx, double cy, Point2D.Double p) {
        // Screen y increases downward: math y = cy - p.y, x = p.x - cx.
        // Top = (0, +1) -> atan2(+1, 0) = +pi/2. CCW moves to (-1, 0) = pi, (0, -1) = 3pi/2, etc.
        double dx = p.x - cx;
        double dy = cy - p.y;
        double angle = Math.atan2(dy, dx) - Math.PI / 2.0;
        if (angle < -1e-9) {
            angle += 2.0 * Math.PI;
        }
        return angle;
    }

    private static double polygonArea(Shape shape) {
        PathIterator it = shape.getPathIterator(null, 0.5);
        double[] coords = new double[6];
        double sum = 0.0;
        double startX = 0, startY = 0, prevX = 0, prevY = 0;
        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    startX = prevX = coords[0];
                    startY = prevY = coords[1];
                    break;
                case PathIterator.SEG_LINETO:
                    sum += prevX * coords[1] - coords[0] * prevY;
                    prevX = coords[0];
                    prevY = coords[1];
                    break;
                case PathIterator.SEG_CLOSE:
                    sum += prevX * startY - startX * prevY;
                    prevX = startX;
                    prevY = startY;
                    break;
                default:
                    break;
            }
            it.next();
        }
        return Math.abs(sum) * 0.5;
    }
}
