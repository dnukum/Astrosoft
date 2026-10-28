package app.astrosoft.ui.test;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

import app.astrosoft.core.Compatibility;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.view.MarriageMatchView;

public class MarriageMatchDasaTimelineVisualTest {

	public static void main(String[] args) throws Exception {
		Horoscope boy = new Horoscope("Elango", 17, 4, 1957, 7, 10,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode, Tamil Nadu, India");
		Horoscope girl = new Horoscope("Mani", 10, 8, 1960, 5, 30,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode, Tamil Nadu, India");

		Compatibility c = new Compatibility(boy, girl);

		MarriageMatchView view = new MarriageMatchView(c, new Point(0, 0));
		view.setTimelineVisible(true);

		// Test horizontal stretching: stretch card to 850px width (outbox must grow!)
		view.resizeTimelineCard(850, 400);

		int todayIdx = view.getDasaTimelinePanel().getTableModel().getTodayRowIndex();
		System.out.println("Today row index: " + todayIdx);
		// Select the row right after today so both Today and Selected row are visible!
		int targetRow = (todayIdx >= 0 && todayIdx + 1 < view.getDasaTimelinePanel().getTable().getRowCount()) ? todayIdx + 1 : 2;
		view.getDasaTimelinePanel().getTable().setRowSelectionInterval(targetRow, targetRow);
		System.out.println("Selected row in table: " + view.getDasaTimelinePanel().getTable().getSelectedRow());

		view.setSize(new Dimension(1500, 1020));

		JFrame frame = new JFrame();
		frame.setContentPane(view);
		frame.setSize(new Dimension(1500, 1020));
		frame.addNotify();
		view.validate();
		view.doLayout();

		System.out.println("Selected row in table: " + view.getDasaTimelinePanel().getTable().getSelectedRow());
		System.out.println("isRowSelected(2): " + view.getDasaTimelinePanel().getTable().isRowSelected(2));
		System.out.println("Table bounds: " + view.getDasaTimelinePanel().getTable().getBounds());
		System.out.println("Table rowCount: " + view.getDasaTimelinePanel().getTable().getRowCount());
		System.out.println("Viewport bounds: " + view.getDasaTimelinePanel().getTable().getParent().getBounds());

		BufferedImage img = new BufferedImage(1450, 1000, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		view.paint(g);
		g.dispose();

		File outFile = new File("/Users/mukunds100/.gemini/antigravity/brain/9cbad7af-32b0-406c-936b-c81bfa03e7f3/marriage_match_dasa_timeline.png");
		ImageIO.write(img, "png", outFile);
		System.out.println("Screenshot saved to: " + outFile.getAbsolutePath());

		frame.dispose();
		System.exit(0);
	}
}
