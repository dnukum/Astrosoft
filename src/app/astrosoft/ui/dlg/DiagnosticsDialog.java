package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.util.SystemDiagnostics;
import app.astrosoft.ui.util.UIConsts;

/**
 * Modal dialog presenting system environment, font health, and log file status.
 * Provides quick actions to copy diagnostic reports and reveal log folders.
 */
public class DiagnosticsDialog extends AstrosoftDialog {

    private JTextArea reportArea;
    private JLabel statusLabel;
    private JButton copyBtn;
    private JButton openFolderBtn;
    private JButton closeBtn;

    public DiagnosticsDialog(AstroSoft parent) {
        super(parent, "Diagnostics & Logs - Astrosoft 2.0", new Dimension(660, 520));
        setResizable(true);
        initComponents();
    }

    private void initComponents() {
        dlgPanel.setLayout(new BorderLayout(0, 10));
        dlgPanel.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        // 1. Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("System Diagnostics & Troubleshooting");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        titleLabel.setForeground(UIConsts.DARK_BLUE);
        titleLabel.setAlignmentX(0.0f);

        File logFile = SystemDiagnostics.getLogFile();
        JLabel subLabel = new JLabel("Log File: " + logFile.getAbsolutePath());
        subLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        subLabel.setForeground(new Color(90, 90, 90));
        subLabel.setAlignmentX(0.0f);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subLabel);
        headerPanel.add(Box.createVerticalStrut(6));

        dlgPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Report Text Area
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reportArea.setBackground(new Color(252, 252, 254));
        reportArea.setForeground(new Color(30, 30, 30));
        reportArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        String report = SystemDiagnostics.generateReport();
        reportArea.setText(report);
        reportArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(195, 205, 215), 1));
        dlgPanel.add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Button & Status Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        statusLabel.setForeground(new Color(34, 139, 34)); // Forest green
        bottomBar.add(statusLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        copyBtn = new JButton("Copy to Clipboard");
        copyBtn.setFocusable(false);
        copyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean ok = SystemDiagnostics.copyToClipboard(reportArea.getText());
                if (ok) {
                    showStatusMessage("Diagnostics report copied to clipboard!");
                } else {
                    showStatusMessage("Failed to copy to clipboard.");
                }
            }
        });

        openFolderBtn = new JButton("Open Log Folder");
        openFolderBtn.setFocusable(false);
        openFolderBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean ok = SystemDiagnostics.openLogFolder();
                if (ok) {
                    showStatusMessage("Opened log folder.");
                } else {
                    showStatusMessage("Could not open log folder automatically.");
                }
            }
        });

        closeBtn = new JButton("Close");
        closeBtn.addActionListener(closeListener);

        buttonPanel.add(copyBtn);
        buttonPanel.add(openFolderBtn);
        buttonPanel.add(closeBtn);

        bottomBar.add(buttonPanel, BorderLayout.EAST);
        dlgPanel.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(dlgPanel);
    }

    private void showStatusMessage(String msg) {
        statusLabel.setText(msg);
        Timer timer = new Timer(3500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusLabel.setText(" ");
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public String getReportText() {
        return reportArea != null ? reportArea.getText() : "";
    }
}
