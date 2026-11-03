package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import app.astrosoft.beans.BirthData;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.modern.ModernBirthDataPanel;
import app.astrosoft.ui.util.UIConsts;

/**
 * Modern Birth Input Dialog.
 * Clean, beautifully proportioned dialog with Ok and Cancel buttons.
 */
public class ModernBirthInputDialog extends AstrosoftDialog {

    private static final Dimension DLG_SIZE = new Dimension(420, 390);

    private ModernBirthDataPanel birthDataPanel;
    private JButton okButton = new JButton(DisplayStrings.OK_STR.toString());
    private JButton cancelButton = new JButton("Cancel");
    private boolean isEdit = false;

    public ModernBirthInputDialog(AstroSoft parent, BirthData birthData, boolean autoShow) {
        super(parent, "Enter Birth Details", DLG_SIZE);
        setModal(true);
        this.isEdit = (birthData != null);
        initComponents(birthData);
        if (autoShow) {
            setVisible(true);
        }
    }

    public ModernBirthInputDialog(AstroSoft parent, BirthData birthData) {
        this(parent, birthData, true);
    }

    public ModernBirthInputDialog(AstroSoft parent) {
        this(parent, null, true);
    }

    private void initComponents(BirthData birthData) {
        dlgPanel.setLayout(new BorderLayout(0, 0));
        dlgPanel.setBackground(UIConsts.THEME_CLR);

        birthDataPanel = new ModernBirthDataPanel(birthData);
        birthDataPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 4, 16));
        dlgPanel.add(birthDataPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        buttonPanel.setBackground(UIConsts.THEME_CLR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 16, 12, 16));

        // Ok button
        okButton.setPreferredSize(new Dimension(90, 30));
        okButton.setBackground(new Color(25, 118, 210));
        okButton.setForeground(Color.WHITE);
        okButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        okButton.setFocusPainted(false);
        okButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> onOk());

        // Cancel button
        cancelButton.setPreferredSize(new Dimension(90, 30));
        cancelButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> closeDialog());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dlgPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(dlgPanel);
        setBackground(UIConsts.THEME_CLR);
        resetSize(DLG_SIZE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                if (birthDataPanel.getNameField() != null) {
                    birthDataPanel.getNameField().requestFocusInWindow();
                }
            }
        });

        SwingUtilities.invokeLater(() -> {
            if (birthDataPanel.getNameField() != null) {
                birthDataPanel.getNameField().requestFocusInWindow();
            }
        });
    }

    public void onOk() {
        BirthData bd = birthDataPanel.getBirthData();
        if (bd != null) {
            if (parent != null) {
                parent.computeHoroscope(bd, isEdit);
            }
            closeDialog();
        }
    }

    public ModernBirthDataPanel getBirthDataPanel() {
        return birthDataPanel;
    }

    public JButton getOkButton() {
        return okButton;
    }

    public JButton getCalculateButton() {
        return okButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }
}
