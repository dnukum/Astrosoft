package app.astrosoft.ui.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import app.astrosoft.core.Compactibility;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.view.MarriageMatchView;

public class MarriageMatchVisualTest {

	public static void main(String[] args) throws Exception {
		Horoscope boy = new Horoscope("Elango", 17, 4, 1957, 7, 10,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode, Tamil Nadu, India");
		Horoscope girl = new Horoscope("Mani", 10, 8, 1960, 5, 30,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode, Tamil Nadu, India");

		Compactibility c = new Compactibility(boy, girl);

		// Render Dashboard (2-Column)
		MarriageMatchView viewDashboard = new MarriageMatchView(c, new Point(0, 0));
		viewDashboard.setViewMode(MarriageMatchView.ViewMode.DASHBOARD);
		viewDashboard.setSize(new Dimension(1450, 920));
		javax.swing.JFrame f1 = new javax.swing.JFrame();
		f1.setContentPane(viewDashboard);
		f1.setSize(new Dimension(1450, 920));
		f1.addNotify();
		viewDashboard.validate();

		BufferedImage imgDashboard = new BufferedImage(1450, 920, BufferedImage.TYPE_INT_RGB);
		Graphics2D g1 = imgDashboard.createGraphics();
		viewDashboard.paint(g1);
		g1.dispose();

		File out1 = new File("/Users/mukunds100/.gemini/antigravity/brain/9cbad7af-32b0-406c-936b-c81bfa03e7f3/marriage_match_dashboard.png");
		ImageIO.write(imgDashboard, "png", out1);
		System.out.println("Saved Dashboard image to: " + out1.getAbsolutePath());

		f1.dispose();
		System.exit(0);
	}

	private static void layoutRecursively(java.awt.Container c) {
		for (java.awt.Component child : c.getComponents()) {
			if (child instanceof java.awt.Container) {
				child.doLayout();
				layoutRecursively((java.awt.Container) child);
			}
		}
	}
}
