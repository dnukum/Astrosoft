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
import app.astrosoft.ui.comp.modern.ModernButton;
import app.astrosoft.ui.util.UIConsts;

/**
 * Modern Birth Input Dialog.
 * Clean, beautifully proportioned dialog with modern rounded buttons aligned with Chart Themes.
 */
public class ModernBirthInputDialog extends AstrosoftDialog {

    private static final Dimension DLG_SIZE = new Dimension(425, 395);

    private ModernBirthDataPanel birthDataPanel;
    private JButton okButton;
    private JButton cancelButton;
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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        buttonPanel.setBackground(UIConsts.THEME_CLR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 16, 12, 16));

        Color primaryColor = UIConsts.getPrimaryAccentColor();

        // Modern Primary Ok button
        okButton = ModernButton.createPrimary(DisplayStrings.OK_STR.toString(), primaryColor);
        okButton.setPreferredSize(new Dimension(95, 32));
        okButton.addActionListener(e -> onOk());

        // Modern Secondary Cancel button
        cancelButton = ModernButton.createSecondary("Cancel");
        cancelButton.setPreferredSize(new Dimension(95, 32));
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
