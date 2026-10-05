package app.astrosoft.core.test;
import java.util.EnumSet;
import junit.framework.TestCase;
import app.astrosoft.core.MyMuhurta;
import app.astrosoft.beans.TaraChandraMatchResult;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;

public class TaraChandraMatchTest extends TestCase {
    public void testExamplesAndWraparound() {
        assertEquals(EnumSet.of(Nakshathra.Ashwini), MyMuhurta.find_tara_chandra_match(Nakshathra.Ashwini,Rasi.Mesha,1,1).getMatches());
        assertEquals(EnumSet.of(Nakshathra.Krithika), MyMuhurta.find_tara_chandra_match(Nakshathra.Ashwini,Rasi.Mesha,3,2).getMatches());
        assertTrue(MyMuhurta.find_tara_chandra_match(Nakshathra.Ashwini,Rasi.Mesha,1,2).getMatches().isEmpty());
        assertEquals(EnumSet.of(Nakshathra.Ashwini), MyMuhurta.find_tara_chandra_match(Nakshathra.Revathi,Rasi.Meena,2,2).getMatches());
    }
    public void testAllThreePariyayas() {
        for(Nakshathra birth:new Nakshathra[]{Nakshathra.Ashwini,Nakshathra.Magam,Nakshathra.Moolam}) {
            assertEquals(EnumSet.of(Nakshathra.Ashwini,Nakshathra.Magam,Nakshathra.Moolam),MyMuhurta.find_tara_chandra_match(birth,Rasi.Mesha,1,1).getTaraSet());
        }
    }
    public void testBoundarySpanningStar() {
        assertEquals(EnumSet.of(Nakshathra.Krithika),MyMuhurta.find_tara_chandra_match(Nakshathra.Ashwini,Rasi.Mesha,3,1).getMatches());
        assertEquals(EnumSet.of(Nakshathra.Krithika),MyMuhurta.find_tara_chandra_match(Nakshathra.Ashwini,Rasi.Mesha,3,2).getMatches());
    }
    public void testDefensiveCopies() {
        TaraChandraMatchResult result=MyMuhurta.find_tara_chandra_match(Nakshathra.Ashwini,Rasi.Mesha,1,1);
        result.getTaraSet().clear();result.getChandraSet().clear();result.getMatches().clear();
        assertEquals(3,result.getTaraSet().size());assertEquals(3,result.getChandraSet().size());assertEquals(1,result.getMatches().size());
    }
    public void testConstructorCopiesAllInputs() {
        EnumSet<Nakshathra> tara = EnumSet.of(Nakshathra.Ashwini, Nakshathra.Magam, Nakshathra.Moolam);
        EnumSet<Nakshathra> chandra = EnumSet.of(Nakshathra.Ashwini, Nakshathra.Bharani, Nakshathra.Krithika);
        EnumSet<Nakshathra> matches = EnumSet.of(Nakshathra.Ashwini);
        TaraChandraMatchResult result = new TaraChandraMatchResult(tara, chandra, matches);
        tara.clear(); chandra.clear(); matches.clear();
        assertEquals(EnumSet.of(Nakshathra.Ashwini, Nakshathra.Magam, Nakshathra.Moolam), result.getTaraSet());
        assertEquals(EnumSet.of(Nakshathra.Ashwini, Nakshathra.Bharani, Nakshathra.Krithika), result.getChandraSet());
        assertEquals(EnumSet.of(Nakshathra.Ashwini), result.getMatches());
    }

    public void testMatchCellCopiesAndEmptyDisplay() {
        EnumSet<Nakshathra> empty = EnumSet.noneOf(Nakshathra.class);
        app.astrosoft.beans.BalamMatchCell cell = new app.astrosoft.beans.BalamMatchCell(empty);
        empty.add(Nakshathra.Ashwini);
        assertTrue(cell.getMatches().isEmpty());
        assertEquals("Null: Astronomically impossible combination.", cell.toString());
    }

    public void testInvalidInputs() {
        invalid(null,Rasi.Mesha,1,1);invalid(Nakshathra.Ashwini,null,1,1);
        invalid(Nakshathra.Ashwini,Rasi.Mesha,0,1);invalid(Nakshathra.Ashwini,Rasi.Mesha,10,1);
        invalid(Nakshathra.Ashwini,Rasi.Mesha,1,0);invalid(Nakshathra.Ashwini,Rasi.Mesha,1,13);
    }
    private void invalid(Nakshathra n,Rasi r,int t,int c){try{MyMuhurta.find_tara_chandra_match(n,r,t,c);fail("Expected invalid input");}catch(IllegalArgumentException expected){}}
    public void testExhaustiveIndependentOracle() {
        for(Nakshathra birth:Nakshathra.values())for(Rasi r:Rasi.values())for(int t=1;t<=9;t++)for(int c=1;c<=12;c++){
            TaraChandraMatchResult result=MyMuhurta.find_tara_chandra_match(birth,r,t,c);
            int sign=(r.ordinal()+c-1)%12;
            EnumSet<Nakshathra> a=EnumSet.noneOf(Nakshathra.class),b=EnumSet.noneOf(Nakshathra.class),matches=EnumSet.noneOf(Nakshathra.class);
            for(Nakshathra candidate:Nakshathra.values()){
                // Independent integer-pada interval overlap: 4 units/star, 9 units/sign.
                boolean inSign=candidate.ordinal()*4<(sign+1)*9&&(candidate.ordinal()+1)*4>sign*9;
                boolean inTara=MyMuhurta.calcTaraIndex(birth,candidate)==t;
                if(inSign)a.add(candidate);if(inTara)b.add(candidate);if(inSign&&inTara)matches.add(candidate);
            }
            assertEquals(a,result.getChandraSet());assertEquals(b,result.getTaraSet());assertEquals(matches,result.getMatches());
            assertEquals(c,MyMuhurta.calcChandraHouse(r,Rasi.values()[sign]));
        }
    }
}
