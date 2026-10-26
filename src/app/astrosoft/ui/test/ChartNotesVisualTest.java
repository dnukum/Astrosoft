package app.astrosoft.ui.test;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;

import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.comp.ChartNotesPanel;

public class ChartNotesVisualTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chart Notes Component Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 600);
            frame.setLayout(new BorderLayout());

            Horoscope h = SampleHoroscopes.getHoroscope("samples/suba.ash");
            if (h != null) {
                h.setNotes("<html><body><p><b>Consultation Notes:</b></p><p>Analysis for Suba - Job transition during <i>Jupiter-Venus</i> dasha.</p><p><u>Remedies:</u> Chant Navagraha Stotram.</p></body></html>");
            }

            ChartNotesPanel notesPanel = new ChartNotesPanel(new ChartNotesPanel.NotesListener() {
                @Override
                public void onFold() {
                    System.out.println("Fold clicked!");
                }

                @Override
                public void onTogglePopOut(boolean isPopOut) {
                    System.out.println("PopOut toggled: " + isPopOut);
                }

                @Override
                public void onNotesModified() {
                    System.out.println("Notes modified! Content length: " + (h != null && h.getNotes() != null ? h.getNotes().length() : 0));
                }
            });

            notesPanel.setHoroscope(h);
            frame.add(notesPanel, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            System.out.println("ChartNotesVisualTest running successfully.");
        });
    }
}
