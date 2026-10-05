package app.astrosoft.beans;

import java.util.EnumSet;
import app.astrosoft.consts.Nakshathra;

/** Display value carrying precomputed Core matches, without matching calculations. */
public final class BalamMatchCell {
    private final EnumSet<Nakshathra> matches;

    public BalamMatchCell(EnumSet<Nakshathra> matches) {
        this.matches = matches.clone();
    }

    public EnumSet<Nakshathra> getMatches() { return matches.clone(); }

    public String toString() {
        if (matches.isEmpty()) return "Null: Astronomically impossible combination.";
        StringBuilder text = new StringBuilder();
        for (Nakshathra star : matches) {
            if (text.length() > 0) text.append(", ");
            text.append(star.toString());
        }
        return text.toString();
    }
}
