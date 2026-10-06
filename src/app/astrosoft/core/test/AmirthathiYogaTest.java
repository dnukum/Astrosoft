package app.astrosoft.core.test;
import app.astrosoft.consts.*;
import app.astrosoft.util.Tuple;
public class AmirthathiYogaTest {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        String[] expected = {"SPSSSSSSSMSASSSMMMASAAMSSAA",
            "SSMASSASSMSSSPAMSSSMMASSSSS",
            "SSSASMSSSSSASSSMSMASPSSMMAS",
            "MSASSSSSSSAAMSSSSSMAASPSASM",
            "ASMMMMASSASMSSASSPSSSSSMSSS",
            "ASSMSSSMMMSSASSSSMAPSMSSSSS",
            "SSSASSSSMASMMMSSSSSSSSSAMSP"};
        for (WeekDay day : WeekDay.values()) {
            check(day.next() == WeekDay.ofIndex(day.ordinal()+1), "Weekday next/wrap");
            for (Nakshathra nak : Nakshathra.values()) {
                check(nak.next() == Nakshathra.ofIndex(nak.ordinal()+1), "Star next/wrap");
                for (boolean sameDay : new boolean[] {true, false}) {
                    Tuple<AmirthathiYoga> result = AmirthathiYoga.ofWeekdayAndNakshatra(day, nak, sameDay);
                    check(result.getFirst().getCode() == expected[day.ordinal()].charAt(nak.ordinal()), "First: "+day.name()+"/"+nak.name());
                    char second = sameDay ? expected[day.ordinal()].charAt((nak.ordinal()+1)%27)
                        : expected[(day.ordinal()+1)%7].charAt(nak.ordinal());
                    check(result.getSecond().getCode() == second, "Second: "+day.name()+"/"+nak.name()+"/"+sameDay);
                }
            }
        }
        for (AmirthathiYoga yoga : AmirthathiYoga.values()) check(AmirthathiYoga.findByCode(yoga.getCode()) == yoga, "Code round trip");
        check(AmirthathiYoga.findByCode('X') == null, "Preserve unknown-code null");
        try { AmirthathiYoga.ofWeekdayAndNakshatra(null,Nakshathra.Ashwini,true); throw new AssertionError("Null day"); }
        catch (NullPointerException expectedError) { }
        try { AmirthathiYoga.ofWeekdayAndNakshatra(WeekDay.Sunday,null,true); throw new AssertionError("Null star"); }
        catch (NullPointerException expectedError) { }
        System.out.println("AmirthathiYogaTest: all 189 cells, both branches (378 pairs), wraparound, codes and null behavior passed");
    }
}
