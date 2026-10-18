package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

import app.astrosoft.beans.ChandraBalamResult;
import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.beans.NakshathraPada;
import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.beans.TaraBalamResult;
import app.astrosoft.consts.AmirthathiYoga;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.MyMuhurta;
import app.astrosoft.core.PanchangaWindowEngine;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.util.AstroUtil;

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

        Date eventInstant = muhurta.getEventInstant();
        Place placeToUse = place != null ? place : (AstroSoft.getPreferences() != null ? AstroSoft.getPreferences().getPlace() : null);
        TimeZone tz = (placeToUse != null && placeToUse.astrosoftTimeZone() != null)
            ? placeToUse.astrosoftTimeZone().getTimeZone()
            : TimeZone.getDefault();

        SimpleDateFormat df = new SimpleDateFormat("MMMM d, yyyy");
        SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss a");
        df.setTimeZone(tz);
        tf.setTimeZone(tz);
        String dateStr = df.format(eventInstant);
        String timeStr = tf.format(eventInstant);

        String placeStr = "";
        if (placeToUse != null) {
            if (placeToUse.city() != null && !placeToUse.city().trim().isEmpty()) {
                placeStr = placeToUse.city().trim();
            }
            if (placeToUse.state() != null && !placeToUse.state().trim().isEmpty()) {
                placeStr += (placeStr.isEmpty() ? "" : ", ") + placeToUse.state().trim();
            }
            if (placeToUse.country() != null && !placeToUse.country().trim().isEmpty()) {
                placeStr += (placeStr.isEmpty() ? "" : ", ") + placeToUse.country().trim();
            }
        }
        if (placeStr.isEmpty()) {
            placeStr = "Default Place";
        }
        String tzStr = (placeToUse != null && placeToUse.astrosoftTimeZone() != null)
            ? placeToUse.astrosoftTimeZone().toString(eventInstant)
            : tz.getID();
        String locationWithTz = placeStr + ", " + tzStr;

        List<PanchangaWindowEngine.Segment> segments = (placeToUse != null)
            ? PanchangaWindowEngine.generate(eventInstant, new Date(eventInstant.getTime() + 60000), tz, placeToUse)
            : PanchangaWindowEngine.generate(eventInstant, new Date(eventInstant.getTime() + 60000), tz);
        PanchangaWindowEngine.Segment segment = segments.get(0);

        Horoscope horoscope = muhurta.getEventHoroscope();
        PlanetaryInfo pi = horoscope.getPlanetaryInfo();

        String pakshaThithi = segment.paksha + " Paksha, " + segment.thithi;
        String vara = segment.vara.toString();

        NakshathraPada moonNakPada = pi.getPlanetNakshathra().get(Planet.Moon);
        String moonNakStr = moonNakPada != null
            ? (moonNakPada.getNak().toString() + " (Pada " + moonNakPada.getPada() + ")")
            : segment.nak.toString();

        AmirthathiYoga amirthathi = AmirthathiYoga.ofWeekdayAndNakshatra(segment.vara, segment.nak, false).getFirst();
        String amirthathiStr = amirthathi != null ? amirthathi.toString() : "";

        String yogaStr = segment.yoga.toString();
        String karanaStr = segment.karana.toString();

        Nakshathra transitNak = (moonNakPada != null) ? moonNakPada.getNak() : segment.nak;
        Rasi transitRasi = pi.getPlanetRasi().get(Planet.Moon);

        boolean hasP1 = person1 != null && person1.getNakshathra() != null && person1.getRasi() != null;
        boolean hasP2 = person2 != null && person2.getNakshathra() != null && person2.getRasi() != null;
        StringBuilder balamSb = new StringBuilder();

        if (hasP1 || hasP2) {
            if (hasP1) {
                TaraBalamResult tr1 = MyMuhurta.getTaraBalamResult(person1.getNakshathra(), transitNak);
                ChandraBalamResult cr1 = MyMuhurta.getChandraBalamResult(person1.getRasi(), transitRasi);
                String name1 = (person1.getName() != null && !person1.getName().trim().isEmpty()) ? person1.getName() : "Person 1";
                balamSb.append("Person 1 (").append(name1).append("): Tara Balam: ")
                       .append(tr1.getTara().getNumber()).append(" (").append(tr1.getTara().getName()).append(", ").append(tr1.getFinalRank()).append("), ")
                       .append("Chandra Balam: ").append(cr1.getHouse().getHouseNumber()).append(" (").append(cr1.getFinalRank()).append(")");
            }
            if (hasP2) {
                if (balamSb.length() > 0) {
                    balamSb.append("\n                          ");
                }
                TaraBalamResult tr2 = MyMuhurta.getTaraBalamResult(person2.getNakshathra(), transitNak);
                ChandraBalamResult cr2 = MyMuhurta.getChandraBalamResult(person2.getRasi(), transitRasi);
                String name2 = (person2.getName() != null && !person2.getName().trim().isEmpty()) ? person2.getName() : "Person 2";
                balamSb.append("Person 2 (").append(name2).append("): Tara Balam: ")
                       .append(tr2.getTara().getNumber()).append(" (").append(tr2.getTara().getName()).append(", ").append(tr2.getFinalRank()).append("), ")
                       .append("Chandra Balam: ").append(cr2.getHouse().getHouseNumber()).append(" (").append(cr2.getFinalRank()).append(")");
            }
        } else {
            balamSb.append("Not evaluated (No person selected)");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("1) Muhurta Date: ").append(dateStr).append("\n");
        sb.append("2) Muhurta Time: ").append(timeStr).append("\n");
        sb.append("3) Location with timezone: ").append(locationWithTz).append("\n");
        sb.append("4) Paksha and Thithi: ").append(pakshaThithi).append("\n");
        sb.append("5) Vara: ").append(vara).append("\n");
        sb.append("6) Nakshatra: ").append(moonNakStr).append("\n");
        sb.append("7) Amirthathi Yoga: ").append(amirthathiStr).append("\n");
        sb.append("8) Tara and Chandra Balam: ").append(balamSb.toString()).append("\n");
        sb.append("9) Panchanga Yoga: ").append(yogaStr).append("\n");
        sb.append("10) Karana: ").append(karanaStr).append("\n\n");

        sb.append("Planetary Positions:\n");

        Rasi ascRasi = pi.getPlanetRasi().get(Planet.Ascendant);
        List<Planet> planetList = Arrays.asList(
            Planet.Ascendant, Planet.Sun, Planet.Moon, Planet.Mars,
            Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn,
            Planet.Rahu, Planet.Ketu
        );

        for (int step = 0; step < 12; step++) {
            Rasi currentSign = Rasi.ofIndex((ascRasi.ordinal() + step) % 12);
            List<Planet> inSign = new ArrayList<>();
            for (Planet p : planetList) {
                if (pi.getPlanetRasi().get(p) == currentSign) {
                    inSign.add(p);
                }
            }
            if (inSign.isEmpty()) continue;

            inSign.sort((p1, p2) -> {
                if (p1 == Planet.Ascendant) return -1;
                if (p2 == Planet.Ascendant) return 1;
                Double pos1 = pi.getPlanetRasiPosition().get(p1);
                Double pos2 = pi.getPlanetRasiPosition().get(p2);
                return Double.compare(pos1 != null ? pos1 : 0.0, pos2 != null ? pos2 : 0.0);
            });

            for (Planet p : inSign) {
                Double rasiPos = pi.getPlanetRasiPosition().get(p);
                int[] dms = AstroUtil.int_dms(rasiPos != null ? rasiPos : 0.0);
                String dmsStr = String.format("%02d° %02d' %02d\"", Math.abs(dms[0]), Math.abs(dms[1]), Math.abs(dms[2]));
                NakshathraPada np = pi.getPlanetNakshathra().get(p);
                String nakWithPada = np != null ? (np.getNak().toString() + " (Pada " + np.getPada() + ")") : "";
                String pName = (p == Planet.Ascendant) ? "ASC" : p.toString();
                sb.append(pName).append(", ")
                  .append(currentSign.toString()).append(", ")
                  .append(dmsStr).append(", ")
                  .append(nakWithPada).append("\n");
            }
        }

        return sb.toString();
    }
}
