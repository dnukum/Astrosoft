package app.astrosoft.consts;

/** Event labels ordered exactly as the approved Excel sheet columns. */
public enum MuhurtaEvent {
    Marriage("Marriage"), BabyShower("Baby Shower"), EarPiercing("Ear Piercing"),
    EducationCommencement("Education Commencement"), VehicleBuying("Vehicle Buying"),
    ConstructionCommencement("Construction Commencement"), HouseWarming("House Warming"),
    Travel("Travel"), General("General");
    private final String label;
    MuhurtaEvent(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
