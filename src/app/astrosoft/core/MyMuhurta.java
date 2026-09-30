package app.astrosoft.core;

import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;

public class MyMuhurta {

    /**
     * Calculates the Tara Balam (Star Strength) between a birth Nakshatra and transit Nakshatra.
     * Returns a 1-based index (1-9) representing the Tara.
     */
    public static int calcTaraIndex(Nakshathra birth, Nakshathra transit) {
        return (((transit.ordinal() - birth.ordinal()) + 27) % 9) + 1;
    }
    
    /**
     * Returns the Paryaya (row) of the transit star relative to the birth star.
     * 1 = 1st cycle (stars 1-9)
     * 2 = 2nd cycle (stars 10-18)
     * 3 = 3rd cycle (stars 19-27)
     */
    public static int calcParyaya(Nakshathra birth, Nakshathra transit) {
        int diff = ((transit.ordinal() - birth.ordinal()) + 27) % 27;
        return (diff / 9) + 1;
    }

    /**
     * Evaluates Tara Balam based on the strict hierarchy rules.
     */
    public static BalamRank evalTaraBalam(Nakshathra birth, Nakshathra transit) {
        int taraIndex = calcTaraIndex(birth, transit);
        int paryaya = calcParyaya(birth, transit);
        
        switch (taraIndex) {
            case 2: // Sampat
            case 6: // Sadhana
            case 9: // Parama Mitra
                return BalamRank.BEST;
                
            case 4: // Kshema
            case 8: // Mitra
                return BalamRank.SECOND_BEST;
                
            case 1: // Janma
            case 5: // Pratyak
                return BalamRank.ACCEPTABLE; // Good for women/marriage if nullified
                
            case 3: // Vipat
                if (paryaya == 3) {
                    return BalamRank.CONDITIONAL; // No evil results in 3rd Paryaya
                }
                return BalamRank.AVOID; // Otherwise bad
                
            case 7: // Naidhana
                return BalamRank.STRICTLY_REJECTED;
                
            default:
                return BalamRank.AVOID;
        }
    }

    /**
     * Calculates the inclusive house count from birth Rasi to transit Rasi.
     * 1-based index (1 to 12).
     */
    public static int calcChandraHouse(Rasi birth, Rasi transit) {
        return (((transit.ordinal() - birth.ordinal()) + 12) % 12) + 1;
    }

    /**
     * Evaluates Chandra Balam based on house positions from Natal Moon.
     */
    public static BalamRank evalChandraBalam(Rasi birth, Rasi transit) {
        int house = calcChandraHouse(birth, transit);
        
        switch (house) {
            case 3:
            case 10:
            case 11:
                return BalamRank.BEST; // Upachaya/growth
                
            case 1:
            case 7:
                return BalamRank.ACCEPTABLE;
                
            case 6:
                return BalamRank.MIXED; // Upachaya, but generally avoided
                
            case 12:
                return BalamRank.AVOID; // Loss
                
            case 8:
                return BalamRank.STRICTLY_REJECTED; // Chandra Ashtama
                
            default:
                // 2, 4, 5, 9 are generally neutral/contextual depending on other rules
                return BalamRank.MIXED; 
        }
    }
}
