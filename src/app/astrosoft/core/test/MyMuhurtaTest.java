package app.astrosoft.core.test;

import junit.framework.TestCase;
import app.astrosoft.core.MyMuhurta;
import app.astrosoft.core.BalamRank;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;

public class MyMuhurtaTest extends TestCase {

    public void testTaraIndex() {
        assertEquals(1, MyMuhurta.calcTaraIndex(Nakshathra.Ashwini, Nakshathra.Ashwini));
        assertEquals(2, MyMuhurta.calcTaraIndex(Nakshathra.Ashwini, Nakshathra.Bharani));
        assertEquals(9, MyMuhurta.calcTaraIndex(Nakshathra.Bharani, Nakshathra.Ashwini));
        assertEquals(1, MyMuhurta.calcTaraIndex(Nakshathra.Ashwini, Nakshathra.Magam));
    }

    public void testParyaya() {
        assertEquals(1, MyMuhurta.calcParyaya(Nakshathra.Ashwini, Nakshathra.Ashwini));
        assertEquals(1, MyMuhurta.calcParyaya(Nakshathra.Ashwini, Nakshathra.Ayilyam));
        assertEquals(2, MyMuhurta.calcParyaya(Nakshathra.Ashwini, Nakshathra.Magam));
        assertEquals(2, MyMuhurta.calcParyaya(Nakshathra.Ashwini, Nakshathra.Kettai));
        assertEquals(3, MyMuhurta.calcParyaya(Nakshathra.Ashwini, Nakshathra.Moolam));
        assertEquals(3, MyMuhurta.calcParyaya(Nakshathra.Bharani, Nakshathra.Ashwini));
    }

    public void testEvalTaraBalam() {
        assertEquals(BalamRank.ACCEPTABLE, MyMuhurta.evalTaraBalam(Nakshathra.Ashwini, Nakshathra.Ashwini));
        assertEquals(BalamRank.BEST, MyMuhurta.evalTaraBalam(Nakshathra.Ashwini, Nakshathra.Bharani));
        assertEquals(BalamRank.STRICTLY_REJECTED, MyMuhurta.evalTaraBalam(Nakshathra.Ashwini, Nakshathra.Punarpoosam));
        assertEquals(BalamRank.AVOID, MyMuhurta.evalTaraBalam(Nakshathra.Ashwini, Nakshathra.Krithika));
        assertEquals(BalamRank.CONDITIONAL, MyMuhurta.evalTaraBalam(Nakshathra.Ashwini, Nakshathra.Uththiradam));
    }

    public void testChandraHouse() {
        assertEquals(1, MyMuhurta.calcChandraHouse(Rasi.Mesha, Rasi.Mesha));
        assertEquals(8, MyMuhurta.calcChandraHouse(Rasi.Mesha, Rasi.Vrichika));
        assertEquals(12, MyMuhurta.calcChandraHouse(Rasi.Vrishabha, Rasi.Mesha));
    }

    public void testEvalChandraBalam() {
        assertEquals(BalamRank.ACCEPTABLE, MyMuhurta.evalChandraBalam(Rasi.Mesha, Rasi.Mesha));
        assertEquals(BalamRank.BEST, MyMuhurta.evalChandraBalam(Rasi.Mesha, Rasi.Mithuna));
        assertEquals(BalamRank.STRICTLY_REJECTED, MyMuhurta.evalChandraBalam(Rasi.Mesha, Rasi.Vrichika));
        assertEquals(BalamRank.AVOID, MyMuhurta.evalChandraBalam(Rasi.Mesha, Rasi.Meena));
    }

    public void testExhaustiveTaraBalam() {
        Nakshathra[] naks = Nakshathra.values();
        for (Nakshathra birth : naks) {
            for (Nakshathra transit : naks) {
                int index = MyMuhurta.calcTaraIndex(birth, transit);
                BalamRank rank = MyMuhurta.evalTaraBalam(birth, transit);
                assertTrue(index >= 1 && index <= 9);
                if (index == 2 || index == 6 || index == 9 || index == 4 || index == 8) {
                    assertTrue(rank == BalamRank.BEST || rank == BalamRank.SECOND_BEST);
                } else if (index == 1 || index == 5) {
                    assertEquals(BalamRank.ACCEPTABLE, rank);
                } else if (index == 3 || index == 7) {
                    assertTrue(rank == BalamRank.AVOID || rank == BalamRank.CONDITIONAL || rank == BalamRank.STRICTLY_REJECTED);
                }
            }
        }
    }

    public void testExhaustiveChandraBalam() {
        Rasi[] rasis = Rasi.values();
        for (Rasi birth : rasis) {
            for (Rasi transit : rasis) {
                int house = MyMuhurta.calcChandraHouse(birth, transit);
                BalamRank rank = MyMuhurta.evalChandraBalam(birth, transit);
                assertTrue(house >= 1 && house <= 12);
                if (house == 3 || house == 10 || house == 11 || house == 1 || house == 7) {
                    assertTrue(rank == BalamRank.BEST || rank == BalamRank.ACCEPTABLE);
                } else if (house == 2 || house == 4 || house == 5 || house == 6 || house == 9) {
                    assertEquals(BalamRank.MIXED, rank);
                } else if (house == 8 || house == 12) {
                    assertTrue(rank == BalamRank.AVOID || rank == BalamRank.STRICTLY_REJECTED);
                }
            }
        }
    }
}
