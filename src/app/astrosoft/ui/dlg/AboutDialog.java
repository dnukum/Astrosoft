package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;

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
        super(parent, "About AstroSoft", new Dimension(420, 250));
        setModal(true);
        initComponents();
    }

    private void initComponents() {
        dlgPanel.setLayout(new BorderLayout(15, 15));
        dlgPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        // Center Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Astrosoft 2.0");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(UIConsts.DARK_BLUE);
        titleLabel.setAlignmentX(0.0f);

        JLabel descLabel = new JLabel("Hindu Astrology Computation Software");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        descLabel.setForeground(Color.DARK_GRAY);
        descLabel.setAlignmentX(0.0f);

        JLabel versionLabel = new JLabel("Version: " + Version.getVersion());
        versionLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        versionLabel.setForeground(UIConsts.GREENISH_BLUE);
        versionLabel.setAlignmentX(0.0f);

        JLabel javaLabel = new JLabel("Java: " + System.getProperty("java.version"));
        javaLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        javaLabel.setForeground(Color.GRAY);
        javaLabel.setAlignmentX(0.0f);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        contentPanel.add(descLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        contentPanel.add(versionLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        contentPanel.add(javaLabel);

        // Icon on the left
        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = UIUtil.createImageIcon("Chart");
            if (icon != null) {
                iconLabel.setIcon(icon);
            }
        } catch (Exception ignored) {}
        iconLabel.setVerticalAlignment(SwingConstants.TOP);

        dlgPanel.add(iconLabel, BorderLayout.WEST);
        dlgPanel.add(contentPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        JButton okBtn = new JButton("OK");
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
}
