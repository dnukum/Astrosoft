/**
 * MuhurtaProfileDialog.java
 *
 * Allows user to select a person's profile for Muhurta either via:
 * 1) A full Horoscope file, or
 * 2) Quick Star Entry with Janma Rasi and Janma Nakshatra.
 *
 * Utilizes AstrosoftTabbedPane for clean multi-tab separation ([Horoscope] [Star]),
 * providing visual parity with Marriage Compatibility while eliminating disabled field clutter.
 */
package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.comp.AstrosoftTabbedPane;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.util.AstrosoftFileFilter;
import app.astrosoft.util.AstroUtil;
import app.astrosoft.util.FileOps;

public class MuhurtaProfileDialog extends JDialog {

    private final String personLabel;
    private AstrosoftTabbedPane tabbedPane;

    // Tab 1: Horoscope
    private JButton browseBtn;
    private JLabel fileStatusLabel;
    private JLabel fileDetailsLabel;
    private JPanel fileCardPanel;
    private Horoscope loadedHoroscope;

    // Tab 2: Star (Quick Entry)
    private JTextField nameField;
    private JComboBox<Rasi> rasiCombo;
    private JComboBox<Nakshathra> nakCombo;
    private boolean isUpdatingRasi = false;

    // Bottom action buttons
    private JButton applyBtn;
    private JButton clearBtn;
    private JButton cancelBtn;

    private MuhurtaProfile resultProfile;
    private boolean isApplied = false;

    public MuhurtaProfileDialog(Window owner, String personLabel, MuhurtaProfile currentProfile) {
        super(owner, "Select Profile — " + personLabel, ModalityType.APPLICATION_MODAL);
        this.personLabel = personLabel;
        initComponents();
        populateCurrent(currentProfile);
        pack();
        int width = Math.max(430, getWidth());
        int height = Math.max(285, getHeight());
        setSize(width, height);
        setMinimumSize(new Dimension(420, 275));
        setResizable(false);
        if (owner != null) {
            setLocationRelativeTo(owner);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));

        tabbedPane = new AstrosoftTabbedPane();

        // Create the two tabs
        JPanel horoscopeTab = createHoroscopePanel();
        JPanel starTab = createStarPanel();

        tabbedPane.addTab(DisplayStrings.HOROSCOPE_STR.toString(), horoscopeTab, true);
        tabbedPane.addTab(DisplayStrings.STAR_STR.toString(), starTab, true);

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom action buttons row
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));

        clearBtn = new JButton("Clear Profile");
        clearBtn.setToolTipText("Clear current profile selection");

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cancelBtn = new JButton("Cancel");
        applyBtn = new JButton("Apply");
        applyBtn.setFont(new Font("Verdana", Font.BOLD, 11));

        rightButtons.add(cancelBtn);
        rightButtons.add(applyBtn);

        bottomRow.add(clearBtn, BorderLayout.WEST);
        bottomRow.add(rightButtons, BorderLayout.EAST);
        add(bottomRow, BorderLayout.SOUTH);

        // Action Listeners
        browseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browseHoroscopeFile();
            }
        });

        applyBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applySelection();
            }
        });

        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearProfile();
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isApplied = false;
                dispose();
            }
        });

        // Keyboard actions: ESC to dismiss, Enter for Apply
        KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isApplied = false;
                dispose();
            }
        }, escStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().setDefaultButton(applyBtn);
    }

    private JPanel createHoroscopePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        browseBtn = new JButton("Browse Horoscope File (.ash)...");
        browseBtn.setFont(new Font("Verdana", Font.PLAIN, 11));
        browseBtn.setFocusPainted(false);
        topBar.add(browseBtn);
        panel.add(topBar, BorderLayout.NORTH);

        fileCardPanel = new JPanel(new BorderLayout(4, 4));
        fileCardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 225), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        fileCardPanel.setBackground(new Color(248, 250, 252));

        fileStatusLabel = new JLabel("No horoscope file selected");
        fileStatusLabel.setFont(new Font("Verdana", Font.BOLD, 12));
        fileStatusLabel.setForeground(UIConsts.DARK_BLUE);

        fileDetailsLabel = new JLabel("<html><font color='#666666'>Select an Astrosoft horoscope file (.ash) to load full chart data and birth coordinates.</font></html>");
        fileDetailsLabel.setFont(new Font("Verdana", Font.PLAIN, 11));

        fileCardPanel.add(fileStatusLabel, BorderLayout.NORTH);
        fileCardPanel.add(fileDetailsLabel, BorderLayout.CENTER);

        panel.add(fileCardPanel, BorderLayout.CENTER);

        JLabel guide = new JLabel("<html><font color='#777777'><i>Full horoscope provides complete planetary positions and birth coordinates.</i></font></html>");
        guide.setFont(new Font("Verdana", Font.PLAIN, 10));
        panel.add(guide, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStarPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        nameField = new JTextField(15);
        rasiCombo = new JComboBox<Rasi>(Rasi.values());
        nakCombo = new JComboBox<Nakshathra>();
        updateNakshatraCombo(null);

        rasiCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isUpdatingRasi) {
                    updateNakshatraCombo(null);
                }
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLbl = new JLabel("Name / Label:");
        nameLbl.setFont(new Font("Verdana", Font.PLAIN, 11));
        panel.add(nameLbl, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel rasiLbl = new JLabel("Janma Rasi:");
        rasiLbl.setFont(new Font("Verdana", Font.PLAIN, 11));
        panel.add(rasiLbl, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(rasiCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        JLabel nakLbl = new JLabel("Janma Nakshatra:");
        nakLbl.setFont(new Font("Verdana", Font.PLAIN, 11));
        panel.add(nakLbl, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nakCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 6, 2, 6);
        JLabel noteLbl = new JLabel("<html><font color='#777777'><i>Quick entry of Moon sign and star is sufficient for Tara Balam and Chandra Balam.</i></font></html>");
        noteLbl.setFont(new Font("Verdana", Font.PLAIN, 10));
        panel.add(noteLbl, gbc);

        return panel;
    }

    private void updateNakshatraCombo(Nakshathra preferredNak) {
        Rasi selectedRasi = (Rasi) rasiCombo.getSelectedItem();
        if (selectedRasi == null) {
            return;
        }
        nakCombo.removeAllItems();
        java.util.EnumSet<Nakshathra> validNaks = Nakshathra.ofRasi(selectedRasi);
        for (Nakshathra nak : validNaks) {
            nakCombo.addItem(nak);
        }
        if (preferredNak != null && validNaks.contains(preferredNak)) {
            nakCombo.setSelectedItem(preferredNak);
        } else if (nakCombo.getItemCount() > 0) {
            nakCombo.setSelectedIndex(0);
        }
    }

    private void updateHoroscopeDisplay() {
        if (loadedHoroscope != null) {
            fileStatusLabel.setText("✓ " + loadedHoroscope.getPersonName());
            Nakshathra nak = (loadedHoroscope.getNakshathra() != null) ? loadedHoroscope.getNakshathra().getNak() : null;
            String bdStr = (loadedHoroscope.getBirthData() != null) ? loadedHoroscope.getBirthData().birthDayString() : "";
            String placeStr = (loadedHoroscope.getBirthData() != null && loadedHoroscope.getBirthData().place() != null)
                ? loadedHoroscope.getBirthData().place() : "";

            fileDetailsLabel.setText("<html>Rasi: <b>" + loadedHoroscope.getRasi() + "</b> &nbsp;|&nbsp; Nakshatra: <b>"
                + (nak != null ? nak.toString() : "-") + "</b><br><font color='#555555'>"
                + bdStr + (placeStr.isEmpty() ? "" : " · " + placeStr) + "</font></html>");

            // Synchronize with Star tab fields for seamless transition
            nameField.setText(loadedHoroscope.getPersonName());
            isUpdatingRasi = true;
            rasiCombo.setSelectedItem(loadedHoroscope.getRasi());
            isUpdatingRasi = false;
            updateNakshatraCombo(nak);
        } else {
            fileStatusLabel.setText("No horoscope file selected");
            fileDetailsLabel.setText("<html><font color='#666666'>Select an Astrosoft horoscope file (.ash) to load full chart data and birth coordinates.</font></html>");
        }
    }

    private void browseHoroscopeFile() {
        String file = FileOps.openFileDialog(
            this, FileOps.FileDialogMode.OPEN, AstrosoftFileFilter.HOROSCOPE_EXTN);
        if (file == null) return;

        Horoscope h = Horoscope.createFromFile(file);
        if (h == null) {
            JOptionPane.showMessageDialog(this, "Unable to load horoscope file.",
                "Load horoscope", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loadedHoroscope = h;
        updateHoroscopeDisplay();
    }

    private void populateCurrent(MuhurtaProfile current) {
        if (current == null) {
            nameField.setText(personLabel);
            updateNakshatraCombo(null);
            updateHoroscopeDisplay();
            tabbedPane.setSelectedIndex(0);
            return;
        }

        if (current.hasHoroscope()) {
            loadedHoroscope = current.getHoroscope();
            updateHoroscopeDisplay();
            tabbedPane.setSelectedIndex(0);
        } else {
            nameField.setText(current.getName());
            isUpdatingRasi = true;
            if (current.getRasi() != null) {
                rasiCombo.setSelectedItem(current.getRasi());
            }
            isUpdatingRasi = false;
            updateNakshatraCombo(current.getNakshathra());
            updateHoroscopeDisplay();
            tabbedPane.setSelectedIndex(1);
        }
    }

    private void applySelection() {
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab == 0) {
            if (loadedHoroscope == null) {
                JOptionPane.showMessageDialog(this,
                    "Please browse and select a horoscope file,\nor switch to the Star tab for quick entry.",
                    "No Horoscope Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            resultProfile = new MuhurtaProfile(loadedHoroscope);
        } else {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                name = personLabel;
            }
            Rasi rasi = (Rasi) rasiCombo.getSelectedItem();
            Nakshathra nak = (Nakshathra) nakCombo.getSelectedItem();
            resultProfile = new MuhurtaProfile(name, rasi, nak);
        }
        isApplied = true;
        dispose();
    }

    private void clearProfile() {
        resultProfile = null;
        isApplied = true;
        dispose();
    }

    public boolean isApplied() {
        return isApplied;
    }

    public MuhurtaProfile getSelectedProfile() {
        return resultProfile;
    }

    // Accessors for automated headless testing
    public AstrosoftTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public Horoscope getLoadedHoroscope() {
        return loadedHoroscope;
    }

    public void setLoadedHoroscope(Horoscope h) {
        this.loadedHoroscope = h;
        updateHoroscopeDisplay();
    }

    public JTextField getNameField() {
        return nameField;
    }

    public JComboBox<Rasi> getRasiCombo() {
        return rasiCombo;
    }

    public JComboBox<Nakshathra> getNakCombo() {
        return nakCombo;
    }

    public void invokeApply() {
        applySelection();
    }

    public void invokeClear() {
        clearProfile();
    }
}
