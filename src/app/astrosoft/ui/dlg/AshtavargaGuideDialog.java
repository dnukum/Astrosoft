/**
 * AshtavargaGuideDialog.java
 *
 * Explanatory dialog displaying score thresholds and color categorization
 * for Sarva Ashtakavarga (SAV) and Bhinna Ashtakavarga (BAV).
 */
package app.astrosoft.ui.dlg;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.util.UIConsts;

public class AshtavargaGuideDialog extends JDialog {

    private static final Dimension DIALOG_SIZE = new Dimension(540, 520);

    public static void showDialog(Component parentComponent) {
        Window owner = (parentComponent != null) ? SwingUtilities.getWindowAncestor(parentComponent) : null;
        if (owner == null) {
            owner = AstroSoft.getInstance();
        }
        AshtavargaGuideDialog dlg = new AshtavargaGuideDialog(owner);
        dlg.setVisible(true);
    }

    public AshtavargaGuideDialog(Window owner) {
        super(owner, "Ashtakavarga Strength Guide", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initComponents();
        pack();
        if (owner != null) {
            setLocationRelativeTo(owner);
        } else {
            Dimension screen = AstroSoft.getScreenSize();
            setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
        }
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(UIConsts.getChartBackground());
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // 1. Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Ashtakavarga Strength & Color Guide");
        titleLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLbl.setForeground(UIConsts.TEXT_DARK_NAVY);
        titleLbl.setAlignmentX(0.0f);

        JLabel subLbl = new JLabel("Classical bindu score thresholds and chart cell color indicators");
        subLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subLbl.setForeground(new Color(80, 85, 95));
        subLbl.setAlignmentX(0.0f);

        headerPanel.add(titleLbl);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subLbl);
        root.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content Panel (SAV & BAV cards)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Section 1: Sarva Ashtakavarga (SAV)
        contentPanel.add(createSectionHeader("Sarva Ashtakavarga (SAV)", 
            "Total cumulative bindus across all 7 planets (out of 337 points, avg ~28 / house):"));
        contentPanel.add(Box.createVerticalStrut(6));

        JPanel savCard = createCardPanel();
        savCard.add(createLegendRow(UIConsts.ASHTAVARGA_STRONG_BG, "Strong", ">= 30 points", "Auspicious, high vitality, productive house"));
        savCard.add(Box.createVerticalStrut(6));
        savCard.add(createLegendRow(Color.WHITE, "Average", "26 - 29 points", "Moderate, balanced baseline results"));
        savCard.add(Box.createVerticalStrut(6));
        savCard.add(createLegendRow(UIConsts.ASHTAVARGA_WEAK_BG, "Weak", "<= 25 points", "Challenging, reduced vitality, extra effort needed"));
        contentPanel.add(savCard);

        contentPanel.add(Box.createVerticalStrut(14));

        // Section 2: Bhinna Ashtakavarga (BAV)
        contentPanel.add(createSectionHeader("Bhinna Ashtakavarga (BAV) - Planet Scores", 
            "Individual planet bindus in each sign (Sun, Moon, Mars, Mer, Jup, Ven, Sat - out of 8 points):"));
        contentPanel.add(Box.createVerticalStrut(6));

        JPanel bavCard = createCardPanel();
        bavCard.add(createLegendRow(UIConsts.ASHTAVARGA_STRONG_BG, "Strong", ">= 5 points (5 to 8)", "Strong planetary support & favorable transit influence"));
        bavCard.add(Box.createVerticalStrut(6));
        bavCard.add(createLegendRow(Color.WHITE, "Average", "3 - 4 points", "Balanced, neutral planetary transit influence"));
        bavCard.add(Box.createVerticalStrut(6));
        bavCard.add(createLegendRow(UIConsts.ASHTAVARGA_WEAK_BG, "Weak", "<= 2 points (0 to 2)", "Low planetary support, challenging transit influence"));
        contentPanel.add(bavCard);

        root.add(contentPanel, BorderLayout.CENTER);

        // 3. Bottom Button Bar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        closeBtn.setPreferredSize(new Dimension(85, 28));
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(closeBtn);
        root.add(buttonPanel, BorderLayout.SOUTH);

        // ESC key dismiss
        root.registerKeyboardAction(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setContentPane(root);
        getRootPane().setDefaultButton(closeBtn);
    }

    private JPanel createSectionHeader(String title, String desc) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(0.0f);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        titleLbl.setForeground(UIConsts.TEXT_DARK_NAVY);
        titleLbl.setAlignmentX(0.0f);

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        descLbl.setForeground(new Color(100, 105, 115));
        descLbl.setAlignmentX(0.0f);

        p.add(titleLbl);
        p.add(Box.createVerticalStrut(2));
        p.add(descLbl);
        return p;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConsts.getChartBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setAlignmentX(0.0f);
        return card;
    }

    private JPanel createLegendRow(final Color swatchBg, String level, String points, String description) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(0.0f);

        // Color Swatch with rounded border
        JPanel swatch = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(swatchBg);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 6, 6);
                g2.setColor(new Color(190, 195, 205));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 6, 6);
                g2.dispose();
            }
        };
        swatch.setOpaque(false);
        swatch.setPreferredSize(new Dimension(28, 20));
        swatch.setMinimumSize(new Dimension(28, 20));
        swatch.setMaximumSize(new Dimension(28, 20));
        row.add(swatch, BorderLayout.WEST);

        // Labels
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        textPanel.setOpaque(false);

        JLabel levelLbl = new JLabel(level);
        levelLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        levelLbl.setForeground(UIConsts.TEXT_DARK_NAVY);
        levelLbl.setPreferredSize(new Dimension(65, 20));

        JLabel pointsLbl = new JLabel(points);
        pointsLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        pointsLbl.setForeground(new Color(40, 70, 120));
        pointsLbl.setPreferredSize(new Dimension(135, 20));

        JLabel descLbl = new JLabel(description);
        descLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        descLbl.setForeground(new Color(70, 75, 85));

        textPanel.add(levelLbl);
        textPanel.add(pointsLbl);
        textPanel.add(descLbl);

        row.add(textPanel, BorderLayout.CENTER);
        return row;
    }
}
