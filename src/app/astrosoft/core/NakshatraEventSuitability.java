package app.astrosoft.core;
import java.util.*;
import app.astrosoft.consts.*;

/** Approved Excel mapping (5 Oct 2026). General is explicit, never a fallback.
 * Nakshatra suitability alone does not approve a complete Muhurta or change Balam ranks.
 */
public final class NakshatraEventSuitability {
    private static final EnumMap<Nakshathra, EnumSet<MuhurtaEvent>> EVENTS = new EnumMap<>(Nakshathra.class);
    static {
        for (Nakshathra star : Nakshathra.values()) EVENTS.put(star, EnumSet.noneOf(MuhurtaEvent.class));
        add(MuhurtaEvent.Marriage, Nakshathra.Rohini, Nakshathra.Mrigasira, Nakshathra.Magam, Nakshathra.Uththiram, Nakshathra.Hastam, Nakshathra.Swathi, Nakshathra.Anusham, Nakshathra.Moolam, Nakshathra.Uththiradam, Nakshathra.Thiruvonam, Nakshathra.Shathayam, Nakshathra.Uttarattadhi, Nakshathra.Revathi);
        add(MuhurtaEvent.BabyShower, Nakshathra.Rohini, Nakshathra.Mrigasira, Nakshathra.Punarpoosam, Nakshathra.Poosam, Nakshathra.Uththiram, Nakshathra.Hastam, Nakshathra.Uththiradam, Nakshathra.Thiruvonam, Nakshathra.Uttarattadhi, Nakshathra.Revathi);
        add(MuhurtaEvent.EarPiercing, Nakshathra.Mrigasira, Nakshathra.Thiruvaadhirai, Nakshathra.Punarpoosam, Nakshathra.Poosam, Nakshathra.Uththiram, Nakshathra.Hastam, Nakshathra.Chittirai, Nakshathra.Uththiradam, Nakshathra.Thiruvonam, Nakshathra.Avittam, Nakshathra.Uttarattadhi, Nakshathra.Revathi);
        add(MuhurtaEvent.EducationCommencement, Nakshathra.Ashwini, Nakshathra.Rohini, Nakshathra.Thiruvaadhirai, Nakshathra.Punarpoosam, Nakshathra.Poosam, Nakshathra.Uththiram, Nakshathra.Hastam, Nakshathra.Chittirai, Nakshathra.Swathi, Nakshathra.Anusham, Nakshathra.Thiruvonam, Nakshathra.Avittam, Nakshathra.Shathayam, Nakshathra.Uttarattadhi, Nakshathra.Revathi);
        add(MuhurtaEvent.VehicleBuying, Nakshathra.Ashwini, Nakshathra.Rohini, Nakshathra.Mrigasira, Nakshathra.Punarpoosam, Nakshathra.Poosam, Nakshathra.Magam, Nakshathra.Uththiram, Nakshathra.Hastam, Nakshathra.Chittirai, Nakshathra.Swathi, Nakshathra.Anusham, Nakshathra.Uththiradam, Nakshathra.Avittam, Nakshathra.Uttarattadhi, Nakshathra.Revathi);
        add(MuhurtaEvent.ConstructionCommencement, Nakshathra.Rohini, Nakshathra.Mrigasira, Nakshathra.Punarpoosam, Nakshathra.Poosam, Nakshathra.Magam, Nakshathra.Uththiram, Nakshathra.Swathi, Nakshathra.Anusham, Nakshathra.Uththiradam, Nakshathra.Thiruvonam, Nakshathra.Shathayam, Nakshathra.Uttarattadhi, Nakshathra.Revathi);
        add(MuhurtaEvent.HouseWarming, Nakshathra.Rohini, Nakshathra.Mrigasira, Nakshathra.Punarpoosam, Nakshathra.Poosam, Nakshathra.Uththiram, Nakshathra.Hastam, Nakshathra.Anusham, Nakshathra.Uththiradam, Nakshathra.Thiruvonam, Nakshathra.Shathayam, Nakshathra.Uttarattadhi, Nakshathra.Revathi);
        add(MuhurtaEvent.Travel, Nakshathra.Ashwini, Nakshathra.Rohini, Nakshathra.Mrigasira, Nakshathra.Punarpoosam, Nakshathra.Poosam, Nakshathra.Uththiram, Nakshathra.Hastam, Nakshathra.Anusham, Nakshathra.Moolam, Nakshathra.Uththiradam, Nakshathra.Thiruvonam, Nakshathra.Avittam, Nakshathra.Shathayam, Nakshathra.Uttarattadhi, Nakshathra.Revathi);
        add(MuhurtaEvent.General, Nakshathra.Magam, Nakshathra.Chittirai, Nakshathra.Swathi);
    }
    private NakshatraEventSuitability() { }
    private static void add(MuhurtaEvent event, Nakshathra... stars) {
        for (Nakshathra star : stars) EVENTS.get(star).add(event);
    }
    public static EnumSet<MuhurtaEvent> eventsFor(Nakshathra star) {
        if (star == null) throw new IllegalArgumentException("Matching star required");
        return EVENTS.get(star).clone();
    }
}
