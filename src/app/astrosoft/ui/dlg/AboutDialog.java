package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.Version;

public class AboutDialog extends AstrosoftDialog {

    public AboutDialog(AstroSoft parent) {
        super(parent, "About AstroSoft", new Dimension(460, 315));
        setModal(true);
        initComponents();
    }

    private void initComponents() {
        dlgPanel.setLayout(new BorderLayout(0, 14));
        dlgPanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));

        // 1. Top Header Banner
        ImageIcon bannerIcon = loadScaledBanner(420);
        if (bannerIcon != null) {
            JLabel bannerLabel = new JLabel(bannerIcon);
            bannerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            bannerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 195, 140), 1),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ));
            dlgPanel.add(bannerLabel, BorderLayout.NORTH);
        }

        // 2. Center Content Panel with Chart Icon
        JPanel centerPanel = new JPanel(new BorderLayout(14, 0));
        centerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = UIUtil.createImageIcon("Chart");
            if (icon != null) {
                iconLabel.setIcon(icon);
            }
        } catch (Exception ignored) {}
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        centerPanel.add(iconLabel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Astrosoft 2.0");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        titleLabel.setForeground(UIConsts.DARK_BLUE);
        titleLabel.setAlignmentX(0.0f);

        JLabel descLabel = new JLabel("Vedic Astrology Software");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        descLabel.setForeground(new Color(60, 60, 60));
        descLabel.setAlignmentX(0.0f);

        JLabel versionLabel = new JLabel("Version: " + Version.getVersion());
        versionLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        versionLabel.setForeground(UIConsts.GREENISH_BLUE);
        versionLabel.setAlignmentX(0.0f);

        JLabel javaLabel = new JLabel("Java: " + System.getProperty("java.version"));
        javaLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        javaLabel.setForeground(Color.GRAY);
        javaLabel.setAlignmentX(0.0f);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        contentPanel.add(descLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(versionLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        contentPanel.add(javaLabel);

        centerPanel.add(contentPanel, BorderLayout.CENTER);
        dlgPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        JButton okBtn = new JButton("OK");
        okBtn.setPreferredSize(new Dimension(75, 26));
        okBtn.addActionListener(closeListener);
        buttonPanel.add(okBtn);
        dlgPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Close on Escape or Enter
        getRootPane().setDefaultButton(okBtn);
        getRootPane().registerKeyboardAction(closeListener,
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        add(dlgPanel);
        setBackground();
    }

    private ImageIcon loadScaledBanner(int targetWidth) {
        try {
            URL url = getClass().getResource("/resources/images/AboutBanner.png");
            if (url == null) {
                url = getClass().getResource("/images/AboutBanner.png");
            }
            if (url == null) {
                File f = new File("resources/images/AboutBanner.png");
                if (f.exists()) {
                    url = f.toURI().toURL();
                }
            }
            if (url != null) {
                Image orig = ImageIO.read(url);
                if (orig != null) {
                    double ratio = (double) orig.getHeight(null) / orig.getWidth(null);
                    int targetHeight = (int) (targetWidth * ratio);
                    Image scaled = orig.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
