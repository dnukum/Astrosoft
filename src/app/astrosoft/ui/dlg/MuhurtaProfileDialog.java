/**
 * MuhurtaProfileDialog.java
 *
 * Allows user to select a person's profile for Muhurta either via:
 * 1) A full Horoscope file, or
 * 2) Quick Entry with Janma Rasi and Janma Nakshatra.
 *
 * The modes are strictly mutually exclusive via radio buttons to prevent conflicts.
 */
package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.util.AstrosoftFileFilter;
import app.astrosoft.util.FileOps;

public class MuhurtaProfileDialog extends JDialog {

    private final String personLabel;
    private JRadioButton radioHoroscope;
    private JRadioButton radioQuick;
    private JButton browseBtn;
    private JLabel fileStatusLabel;
    private JTextField nameField;
    private JComboBox<Rasi> rasiCombo;
    private JComboBox<Nakshathra> nakCombo;

    private Horoscope loadedHoroscope;
    private MuhurtaProfile resultProfile;
    private boolean isApplied = false;

    public MuhurtaProfileDialog(Window owner, String personLabel, MuhurtaProfile currentProfile) {
        super(owner, "Select Profile — " + personLabel, ModalityType.APPLICATION_MODAL);
        this.personLabel = personLabel;
        initComponents();
        populateCurrent(currentProfile);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        // Radio buttons for mode selection
        radioHoroscope = new JRadioButton("Full Horoscope (from file)");
        radioQuick = new JRadioButton("Quick Entry (Rasi & Nakshatra only)");
        radioHoroscope.setFont(new Font("Verdana", Font.BOLD, 11));
        radioQuick.setFont(new Font("Verdana", Font.BOLD, 11));
        radioHoroscope.setForeground(UIConsts.DARK_BLUE);
        radioQuick.setForeground(UIConsts.DARK_BLUE);

        ButtonGroup group = new ButtonGroup();
        group.add(radioHoroscope);
        group.add(radioQuick);

        // Section 1: Horoscope file
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filePanel.setBorder(BorderFactory.createTitledBorder("Horoscope File"));
        browseBtn = new JButton("Browse Horoscope File...");
        fileStatusLabel = new JLabel("No file selected");
        fileStatusLabel.setFont(new Font("Verdana", Font.PLAIN, 11));
        filePanel.add(browseBtn);
        filePanel.add(fileStatusLabel);

        // Section 2: Quick Entry
        JPanel quickPanel = new JPanel(new GridBagLayout());
        quickPanel.setBorder(BorderFactory.createTitledBorder("Janma Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        nameField = new JTextField(14);
        rasiCombo = new JComboBox<Rasi>(Rasi.values());
        nakCombo = new JComboBox<Nakshathra>(Nakshathra.values());

        gbc.gridx = 0; gbc.gridy = 0;
        quickPanel.add(new JLabel("Name / Label:"), gbc);
        gbc.gridx = 1;
        quickPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        quickPanel.add(new JLabel("Janma Rasi:"), gbc);
        gbc.gridx = 1;
        quickPanel.add(rasiCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        quickPanel.add(new JLabel("Janma Nakshatra:"), gbc);
        gbc.gridx = 1;
        quickPanel.add(nakCombo, gbc);

        // Mode change listeners
        ActionListener modeListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateModeState();
            }
        };
        radioHoroscope.addActionListener(modeListener);
        radioQuick.addActionListener(modeListener);

        browseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browseHoroscopeFile();
            }
        });

        mainPanel.add(radioHoroscope);
        mainPanel.add(javax.swing.Box.createVerticalStrut(4));
        mainPanel.add(filePanel);
        mainPanel.add(javax.swing.Box.createVerticalStrut(10));
        mainPanel.add(radioQuick);
        mainPanel.add(javax.swing.Box.createVerticalStrut(4));
        mainPanel.add(quickPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Buttons at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton applyBtn = new JButton("Apply");
        JButton clearBtn = new JButton("Clear Profile");
        JButton cancelBtn = new JButton("Cancel");

        applyBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applySelection();
            }
        });

        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultProfile = null;
                isApplied = true;
                dispose();
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isApplied = false;
                dispose();
            }
        });

        buttonPanel.add(clearBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(applyBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateModeState() {
        boolean isFileMode = radioHoroscope.isSelected();
        browseBtn.setEnabled(isFileMode);
        fileStatusLabel.setEnabled(isFileMode);

        nameField.setEnabled(!isFileMode);
        rasiCombo.setEnabled(!isFileMode);
        nakCombo.setEnabled(!isFileMode);

        if (isFileMode) {
            if (loadedHoroscope != null) {
                fileStatusLabel.setText("Loaded: " + loadedHoroscope.getPersonName());
                nameField.setText(loadedHoroscope.getPersonName());
                rasiCombo.setSelectedItem(loadedHoroscope.getRasi());
                if (loadedHoroscope.getNakshathra() != null) {
                    nakCombo.setSelectedItem(loadedHoroscope.getNakshathra().getNak());
                }
            } else {
                fileStatusLabel.setText("No file selected");
            }
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
        fileStatusLabel.setText("<html><b>" + h.getPersonName() + "</b> (" + h.getRasi() + " / " + h.getNakshathra().getNak() + ")</html>");
        nameField.setText(h.getPersonName());
        rasiCombo.setSelectedItem(h.getRasi());
        if (h.getNakshathra() != null) {
            nakCombo.setSelectedItem(h.getNakshathra().getNak());
        }
    }

    private void populateCurrent(MuhurtaProfile current) {
        if (current == null) {
            radioQuick.setSelected(true);
            nameField.setText(personLabel);
            updateModeState();
            return;
        }

        if (current.hasHoroscope()) {
            radioHoroscope.setSelected(true);
            loadedHoroscope = current.getHoroscope();
            updateModeState();
        } else {
            radioQuick.setSelected(true);
            nameField.setText(current.getName());
            if (current.getRasi() != null) {
                rasiCombo.setSelectedItem(current.getRasi());
            }
            if (current.getNakshathra() != null) {
                nakCombo.setSelectedItem(current.getNakshathra());
            }
            updateModeState();
        }
    }

    private void applySelection() {
        if (radioHoroscope.isSelected()) {
            if (loadedHoroscope == null) {
                JOptionPane.showMessageDialog(this,
                    "Please click 'Browse Horoscope File...' to load a horoscope file,\nor switch to 'Quick Entry' mode.",
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

    public boolean isApplied() {
        return isApplied;
    }

    public MuhurtaProfile getSelectedProfile() {
        return resultProfile;
    }
}
