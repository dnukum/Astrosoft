package app.astrosoft.consts;

import app.astrosoft.util.Tuple;
import java.util.HashMap;
import java.util.Map;
import static app.astrosoft.consts.WeekDay.*;

/** Mechanical port of AmirthathiYoga.java by E. Rajasekar (2020 Apr 14).
 * Table and two-result behavior preserved from Amirthathi-Yoga-Java.docx.
 */
public enum AmirthathiYoga {
    Amirtha('A'), Siddha('S'), Marana('M'), Prabalarshta('P');
    private char code;
    AmirthathiYoga(char code) { this.code = code; }
    private static Map<Character, AmirthathiYoga> yogasByCode = new HashMap<Character, AmirthathiYoga>() {{
        put('A', Amirtha); put('S', Siddha); put('M', Marana); put('P', Prabalarshta);
    }};
    private static char[] sunYogas = {
        'S', 'P', 'S',
        'S', 'S', 'S',
        'S', 'S', 'S',
        'M', 'S', 'A',
        'S', 'S', 'S',
        'M', 'M', 'M',
        'A', 'S', 'A',
        'A', 'M', 'S',
        'S', 'A', 'A'
    };
    private static char[] monYogas = {
        'S', 'S', 'M',
        'A', 'S', 'S',
        'A', 'S', 'S',
        'M', 'S', 'S',
        'S', 'P', 'A',
        'M', 'S', 'S',
        'S', 'M', 'M',
        'A', 'S', 'S',
        'S', 'S', 'S'
    };
    private static char[] tueYogas = {
        'S', 'S', 'S',
        'A', 'S', 'M',
        'S', 'S', 'S',
        'S', 'S', 'A',
        'S', 'S', 'S',
        'M', 'S', 'M',
        'A', 'S', 'P',
        'S', 'S', 'M',
        'M', 'A', 'S'
    };
    private static char[] wedYogas = {
        'M', 'S', 'A',
        'S', 'S', 'S',
        'S', 'S', 'S',
        'S', 'A', 'A',
        'M', 'S', 'S',
        'S', 'S', 'S',
        'M', 'A', 'A',
        'S', 'P', 'S',
        'A', 'S', 'M'
    };
    private static char[] thuYogas = {
        'A', 'S', 'M',
        'M', 'M', 'M',
        'A', 'S', 'S',
        'A', 'S', 'M',
        'S', 'S', 'A',
        'S', 'S', 'P',
        'S', 'S', 'S',
        'S', 'S', 'M',
        'S', 'S', 'S'
    };
    private static char[] friYogas = {
        'A', 'S', 'S',
        'M', 'S', 'S',
        'S', 'M', 'M',
        'M', 'S', 'S',
        'A', 'S', 'S',
        'S', 'S', 'M',
        'A', 'P', 'S',
        'M', 'S', 'S',
        'S', 'S', 'S'
    };
    private static char[] satYogas = {
        'S', 'S', 'S',
        'A', 'S', 'S',
        'S', 'S', 'M',
        'A', 'S', 'M',
        'M', 'M', 'S',
        'S', 'S', 'S',
        'S', 'S', 'S',
        'S', 'S', 'A',
        'M', 'S', 'P'
    };
    private static Map<WeekDay, char[]> yogaTable = new HashMap<WeekDay, char[]>() {{
        put(Sunday, sunYogas); put(Monday, monYogas); put(Tuesday, tueYogas);
        put(Wednesday, wedYogas); put(Thursday, thuYogas); put(Friday, friYogas); put(Saturday, satYogas);
    }};
    public static Tuple<AmirthathiYoga> ofWeekdayAndNakshatra(WeekDay weekDay, Nakshathra nak, boolean isNextNakshatraSameday) {
        char[] yogas = yogaTable.get(weekDay);
        int nakIndex = nak.ordinal();
        AmirthathiYoga first = findByCode(yogas[nakIndex]);
        AmirthathiYoga second = isNextNakshatraSameday ?
                findByCode(yogas[nak.next().ordinal()]) :
                findByCode(yogaTable.get(weekDay.next())[nakIndex]);
        return new Tuple<>(first, second);
    }
    public static AmirthathiYoga findByCode(char code) { return yogasByCode.get(code); }
    public char getCode() { return code; }
}
