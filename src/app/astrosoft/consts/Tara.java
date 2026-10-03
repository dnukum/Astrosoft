package app.astrosoft.consts;
import app.astrosoft.core.BalamRank;

public enum Tara {
    JANMA(1, "Janma", "Danger to body", BalamRank.ACCEPTABLE),
    SAMPAT(2, "Sampat", "Wealth and Prosperity", BalamRank.BEST),
    VIPAT(3, "Vipat", "Danger, losses and accidents", BalamRank.AVOID),
    KSHEMA(4, "Kshema", "Prosperity", BalamRank.SECOND_BEST),
    PRATYAK(5, "Pratyak", "Obstacles", BalamRank.ACCEPTABLE),
    SADHANA(6, "Sadhana", "Realisation of ambitions", BalamRank.BEST),
    NAIDHANA(7, "Naidhana", "Dangers", BalamRank.STRICTLY_REJECTED),
    MITRA(8, "Mitra", "Good", BalamRank.SECOND_BEST),
    PARAMA_MITRA(9, "Parama Mitra", "Very favourable", BalamRank.BEST);

    private int number;
    private String name;
    private String effect;
    private BalamRank defaultRank;

    Tara(int number, String name, String effect, BalamRank defaultRank) {
        this.number = number;
        this.name = name;
        this.effect = effect;
        this.defaultRank = defaultRank;
    }

    public int getNumber() { return number; }
    public String getName() { return name; }
    public String getEffect() { return effect; }
    public BalamRank getDefaultRank() { return defaultRank; }

    public static Tara of(int index) {
        for (Tara t : values()) {
            if (t.number == index) return t;
        }
        return JANMA; // default fallback
    }
}
