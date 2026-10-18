package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.beans.Place;
import app.astrosoft.core.MyMuhurta;

public class MuhurtaSummaryDialog extends JDialog {

    private final JTextArea textArea;
    private final JButton copyBtn;
    private final JButton closeBtn;

    public MuhurtaSummaryDialog(Window owner, MyMuhurta muhurta, Place place, MuhurtaProfile person1, MuhurtaProfile person2) {
        super(owner, "Muhurta Summary", ModalityType.APPLICATION_MODAL);

        String summaryText = buildSummaryText(muhurta, place, person1, person2);

        textArea = new JTextArea(summaryText);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        textArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        copyBtn = new JButton("Copy to Clipboard");
        copyBtn.setFocusable(false);
        copyBtn.setToolTipText("Copy summary text to system clipboard");
        copyBtn.addActionListener(e -> {
            String text = textArea.getText();
            StringSelection sel = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            copyBtn.setText("Copied!");
            Timer timer = new Timer(1500, evt -> copyBtn.setText("Copy to Clipboard"));
            timer.setRepeats(false);
            timer.start();
        });

        closeBtn = new JButton("Close");
        closeBtn.setFocusable(false);
        closeBtn.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonPanel.add(copyBtn);
        buttonPanel.add(closeBtn);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Escape key closes the dialog
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setSize(new Dimension(640, 520));
        setLocationRelativeTo(owner);
    }

    public static String buildSummaryText(MyMuhurta muhurta, Place place, MuhurtaProfile person1, MuhurtaProfile person2) {
        if (muhurta == null) return "";
        return muhurta.getSummaryText(place, person1, person2);
    }
}
