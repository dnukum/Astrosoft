package app.astrosoft.beans;

import java.util.EnumSet;
import app.astrosoft.consts.Nakshathra;

/** Immutable snapshot of Core results; performs no matching calculations. */
public final class TaraChandraMatchResult {
    private final EnumSet<Nakshathra> taraSet, chandraSet, matches;

    public TaraChandraMatchResult(EnumSet<Nakshathra> tara, EnumSet<Nakshathra> chandra,
            EnumSet<Nakshathra> matches) {
        this.taraSet = tara.clone();
        this.chandraSet = chandra.clone();
        this.matches = matches.clone();
    }

    public EnumSet<Nakshathra> getTaraSet() { return taraSet.clone(); }
    public EnumSet<Nakshathra> getChandraSet() { return chandraSet.clone(); }
    public EnumSet<Nakshathra> getMatches() { return matches.clone(); }
}
