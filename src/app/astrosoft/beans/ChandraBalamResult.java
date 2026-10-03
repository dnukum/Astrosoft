package app.astrosoft.beans;

import app.astrosoft.consts.ChandraHouse;
import app.astrosoft.core.BalamRank;

public class ChandraBalamResult {
    private ChandraHouse house;
    private BalamRank finalRank;

    public ChandraBalamResult(ChandraHouse house, BalamRank finalRank) {
        this.house = house;
        this.finalRank = finalRank;
    }

    public ChandraHouse getHouse() { return house; }
    public BalamRank getFinalRank() { return finalRank; }

    @Override
    public String toString() {
        return house.getHouseNumber() + " (" + finalRank.name() + ")";
    }
}
