package app.astrosoft.consts;
import app.astrosoft.core.BalamRank;

public enum ChandraHouse {
    HOUSE_1(1, "1st House", "Acceptable", BalamRank.ACCEPTABLE),
    HOUSE_2(2, "2nd House", "Mixed", BalamRank.MIXED),
    HOUSE_3(3, "3rd House", "Growth (Upachaya)", BalamRank.BEST),
    HOUSE_4(4, "4th House", "Mixed", BalamRank.MIXED),
    HOUSE_5(5, "5th House", "Mixed", BalamRank.MIXED),
    HOUSE_6(6, "6th House", "Growth (Upachaya)", BalamRank.MIXED), // Astrosoft uses MIXED here currently
    HOUSE_7(7, "7th House", "Acceptable", BalamRank.ACCEPTABLE),
    HOUSE_8(8, "8th House", "Chandra Ashtama", BalamRank.STRICTLY_REJECTED),
    HOUSE_9(9, "9th House", "Mixed", BalamRank.MIXED),
    HOUSE_10(10, "10th House", "Growth (Upachaya)", BalamRank.BEST),
    HOUSE_11(11, "11th House", "Growth (Upachaya)", BalamRank.BEST),
    HOUSE_12(12, "12th House", "Loss", BalamRank.AVOID);

    private int houseNumber;
    private String name;
    private String effect;
    private BalamRank defaultRank;

    ChandraHouse(int houseNumber, String name, String effect, BalamRank defaultRank) {
        this.houseNumber = houseNumber;
        this.name = name;
        this.effect = effect;
        this.defaultRank = defaultRank;
    }

    public int getHouseNumber() { return houseNumber; }
    public String getName() { return name; }
    public String getEffect() { return effect; }
    public BalamRank getDefaultRank() { return defaultRank; }

    public static ChandraHouse of(int house) {
        for (ChandraHouse ch : values()) {
            if (ch.houseNumber == house) return ch;
        }
        return HOUSE_1; // fallback
    }

    /**
     * Simplified grading per Muthu's "Chandra Balam" slide:
     * 3, 6, 10, 11 from Natal Moon are best; 1 and 7 are good;
     * 8th (Ashtama Chandra) should be avoided; all others are neutral.
     */
    public enum ThumbRuleGrade { BEST, GOOD, NEUTRAL, AVOID }

    public ThumbRuleGrade getThumbRuleGrade() {
        switch (this) {
            case HOUSE_3:
            case HOUSE_6:
            case HOUSE_10:
            case HOUSE_11:
                return ThumbRuleGrade.BEST;
            case HOUSE_1:
            case HOUSE_7:
                return ThumbRuleGrade.GOOD;
            case HOUSE_8:
                return ThumbRuleGrade.AVOID;
            default:
                return ThumbRuleGrade.NEUTRAL;
        }
    }
}
