/**
 * MuhurtaProfile.java
 *
 * Represents a person's profile for Muhurta and Balam calculations.
 * Can be initialized either from a full Horoscope file, or via Quick Entry
 * with just Janma Rasi and Janma Nakshatra.
 */
package app.astrosoft.beans;

import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;

public class MuhurtaProfile {

    private final String name;
    private final Rasi rasi;
    private final Nakshathra nakshathra;
    private final Horoscope horoscope; // null for quick entry

    public MuhurtaProfile(Horoscope horoscope) {
        if (horoscope == null) {
            throw new IllegalArgumentException("Horoscope cannot be null");
        }
        this.horoscope = horoscope;
        this.name = horoscope.getPersonName() != null ? horoscope.getPersonName() : "";
        this.rasi = horoscope.getRasi();
        this.nakshathra = (horoscope.getNakshathra() != null) ? horoscope.getNakshathra().getNak() : null;
    }

    public MuhurtaProfile(String name, Rasi rasi, Nakshathra nakshathra) {
        this.name = (name != null) ? name.trim() : "";
        this.rasi = rasi;
        this.nakshathra = nakshathra;
        this.horoscope = null;
    }

    public String getName() {
        return name;
    }

    public Rasi getRasi() {
        return rasi;
    }

    public Nakshathra getNakshathra() {
        return nakshathra;
    }

    public Horoscope getHoroscope() {
        return horoscope;
    }

    public boolean hasHoroscope() {
        return horoscope != null;
    }

    public String getDisplayName(String defaultLabel) {
        String baseName = (!name.isEmpty()) ? name : defaultLabel;
        if (rasi != null && nakshathra != null) {
            return baseName + " (" + rasi.toString() + " / " + nakshathra.toString() + ")" + (hasHoroscope() ? "" : " [Quick Entry]");
        }
        return baseName;
    }

    @Override
    public String toString() {
        return getDisplayName("Person");
    }
}
