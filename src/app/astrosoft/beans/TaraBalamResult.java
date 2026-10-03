package app.astrosoft.beans;

import app.astrosoft.consts.Tara;
import app.astrosoft.core.BalamRank;

public class TaraBalamResult {
    private Tara tara;
    private int paryaya;
    private BalamRank finalRank;

    public TaraBalamResult(Tara tara, int paryaya, BalamRank finalRank) {
        this.tara = tara;
        this.paryaya = paryaya;
        this.finalRank = finalRank;
    }

    public Tara getTara() { return tara; }
    public int getParyaya() { return paryaya; }
    public BalamRank getFinalRank() { return finalRank; }

    @Override
    public String toString() {
        return tara.getNumber() + " - " + tara.getName() + " (" + finalRank.name() + ")";
    }
}
