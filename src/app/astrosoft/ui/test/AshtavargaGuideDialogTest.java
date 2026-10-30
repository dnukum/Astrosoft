package app.astrosoft.ui.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import org.junit.Test;

import app.astrosoft.ui.dlg.AshtavargaGuideDialog;

public class AshtavargaGuideDialogTest {

    @Test
    public void testDialogLayoutAndRender() throws Exception {
        AshtavargaGuideDialog dlg = new AshtavargaGuideDialog(null);
        dlg.pack();
        Dimension size = dlg.getSize();
        System.out.println("Dialog size: " + size);
        assertTrue("Dialog width >= 500", size.width >= 500);
        assertTrue("Dialog height >= 400", size.height >= 400);

        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        dlg.getContentPane().paint(g2);
        g2.dispose();

        File out = new File("/Users/mukunds100/.gemini/antigravity/brain/e51df240-e409-4ed2-87fb-a28dc0d811bf/guide_dialog_render.png");
        ImageIO.write(img, "png", out);
        System.out.println("Saved dialog render to " + out.getAbsolutePath());

        dlg.dispose();

        // Also test and render the Ashtavarga header with the Info Button
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 6, 4));
        p.setBackground(java.awt.Color.WHITE);
        javax.swing.JLabel l = new javax.swing.JLabel("Ashtavarga");
        l.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 12));
        l.setForeground(app.astrosoft.ui.util.UIConsts.TEXT_DARK_NAVY);
        p.add(l);
        p.add(app.astrosoft.ui.view.AshtavargaView.createAshtavargaInfoButton(p));
        p.setSize(160, 36);
        p.doLayout();

        BufferedImage iconImg = new BufferedImage(160, 36, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gIcon = iconImg.createGraphics();
        p.paint(gIcon);
        gIcon.dispose();

        File iconOut = new File("/Users/mukunds100/.gemini/antigravity/brain/e51df240-e409-4ed2-87fb-a28dc0d811bf/ashtavarga_icon_visual.png");
        ImageIO.write(iconImg, "png", iconOut);
        System.out.println("Saved icon visual render to " + iconOut.getAbsolutePath());
    }
}
