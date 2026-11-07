/**
 * AstroConsts.java
 *
 * Created on October 01, 2005, 5:15 PM
 *
 * @author  E. Rajasekar
 */
package app.astrosoft.consts;

import swisseph.SweConst;

/** Represents all astro consts used in this application
 *
 */
public class AstroConsts {

    /*public static final int[] planetOwners =
        { 2, 5, 3, 1, 0, 3, 5, 2, 4, 6, 6, 4
         };*/
    
    // Corresponds to planet numbers in swiss eph.
    /*public static final int[] planetNo = { 0, 1, 4, 2, 5, 3, 6, 11, 12
         };*/

	public static final double nakLength = ( double ) ( 360.00 / 27.0 );
	
	public static final double padaLength = ( double ) ( 360.00 / 108.0 );
	
	public static final double thithiLength = 12.0;
	
	public static final double karanaLength = 6.0;
	
	public static final double yogaLength = nakLength;
	
	public static final double rasiLength = 30.0;
	
	public static final double MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
	
	public static final long MILLIS_IN_HR = (1000 * 60 * 60);
	
	/*public static int[] maxKutas = { 3, 6, 1, 1, 4, 7, 5, 2, 1, 1, 1, 8
	 };

	public static int[] monthLength =
	{ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
	 };*/

	/*public static final String[] months =
	    {
	        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
	        "Nov", "Dec"
	    
	    };*/
	
	public static final int iflag = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_SPEED  | SweConst.SEFLG_SIDEREAL ;
	
	public static int permanentRel(Planet i, Planet j){
		int[][] permRel =
        {
            { 2, 1, 1, 0, 1, -1, -1
             },
            { 1, 2, 0, 1, 0, 0, 0
             },
            { 1, 1, 2, -1, 1, 0, 0
             },
            { 1, -1, 0, 2, 0, 1, 0
             },
            { 1, 1, 1, -1, 2, -1, 0
             },
            { -1, -1, 0, 1, 0, 2, 1
             },
            { -1, -1, -1, 1, 0, 1, 2
             },
        
        };
		
		return permRel[i.ordinal()][j.ordinal()];
	}

	// =========================================================================
	// O(1) Aspect & Drishti Lookup Tables
	// =========================================================================

	// Aspect offsets (distances from source house, 1-based)
	// Sun: 7; Moon: 7; Mars: 4, 7, 8; Mercury: 7; Jupiter: 5, 7, 9; Venus: 7; Saturn: 3, 7, 10; Rahu: 5, 7, 9; Ketu: 5, 7, 9
	public static final int[][] PLANET_ASPECT_OFFSETS = {
		{7},             // Sun (0)
		{7},             // Moon (1)
		{4, 7, 8},       // Mars (2)
		{7},             // Mercury (3)
		{5, 7, 9},       // Jupiter (4)
		{7},             // Venus (5)
		{3, 7, 10},      // Saturn (6)
		{5, 7, 9},       // Rahu (7)
		{5, 7, 9}        // Ketu (8)
	};

	// Target house array for each planet and source house: [planetOrdinal 0..8][sourceHouse 0..12]
	// Precomputed so PLANET_ASPECT_TARGETS[planet][sourceHouse] gives the target houses directly without runtime math.
	public static final int[][][] PLANET_ASPECT_TARGETS = new int[9][13][];

	// Direct Aspect Name lookup: [planetOrdinal 0..8][sourceHouse 0..12][targetHouse 0..12] -> e.g. "4th aspect", "7th aspect", or null
	public static final String[][][] ASPECT_NAME = new String[9][13][13];

	// Direct Aspect Distance lookup: [planetOrdinal 0..8][sourceHouse 0..12][targetHouse 0..12] -> e.g. 4, 7, 8, 10, or 0 if not aspecting
	public static final int[][][] ASPECT_DISTANCE = new int[9][13][13];

	// =========================================================================
	// O(1) House Classifications & Yoga Matrices
	// =========================================================================
	// Kendras: 1, 4, 7, 10
	public static final boolean[] IS_KENDRA = {false, true, false, false, true, false, false, true, false, false, true, false, false};

	// Trikonas: 1, 5, 9
	public static final boolean[] IS_TRIKONA = {false, true, false, false, false, true, false, false, false, true, false, false, false};

	// Panapharas (Succedent): 2, 5, 8, 11
	public static final boolean[] IS_PANAPHARA = {false, false, true, false, false, true, false, false, true, false, false, true, false};

	// Apoklimas (Cadent): 3, 6, 9, 12
	public static final boolean[] IS_APOKLIMA = {false, false, false, true, false, false, true, false, false, true, false, false, true};

	// Upachayas: 3, 6, 10, 11
	public static final boolean[] IS_UPACHAYA = {false, false, false, true, false, false, true, false, false, false, true, true, false};

	// Dusthanas (Trika): 6, 8, 12
	public static final boolean[] IS_DUSTHANA = {false, false, false, false, false, false, true, false, true, false, false, false, true};

	// Marakas: 2, 7
	public static final boolean[] IS_MARAKA = {false, false, true, false, false, false, false, true, false, false, false, false, false};

	// Kendra-Trikona Raja Yoga Pair Matrix: [house1][house2]
	// True if one house is a Kendra (1, 4, 7, 10) and the other is a Trikona (1, 5, 9) and house1 != house2
	public static final boolean[][] IS_KENDRA_TRIKONA_PAIR = new boolean[13][13];

	// Dhana Yoga Pair Matrix: [house1][house2]
	// Classical Dhana yoga occurs between wealth/gain lords (2, 11) and dharma/fortune/lagna lords (1, 5, 9)
	public static final boolean[][] IS_DHANA_PAIR = new boolean[13][13];

	static {
		// Populate Aspect tables
		for (int p = 0; p < 9; p++) {
			int[] offsets = PLANET_ASPECT_OFFSETS[p];
			for (int src = 1; src <= 12; src++) {
				int[] targets = new int[offsets.length];
				for (int i = 0; i < offsets.length; i++) {
					int dist = offsets[i];
					int tgt = ((src - 1 + dist - 1) % 12) + 1;
					targets[i] = tgt;
					ASPECT_DISTANCE[p][src][tgt] = dist;
					ASPECT_NAME[p][src][tgt] = dist + getOrdinalSuffix(dist) + " aspect";
				}
				PLANET_ASPECT_TARGETS[p][src] = targets;
			}
		}

		// Populate Kendra-Trikona Raja Yoga matrix
		for (int h1 = 1; h1 <= 12; h1++) {
			for (int h2 = 1; h2 <= 12; h2++) {
				if (h1 != h2) {
					boolean h1Kendra = IS_KENDRA[h1];
					boolean h1Trikona = IS_TRIKONA[h1];
					boolean h2Kendra = IS_KENDRA[h2];
					boolean h2Trikona = IS_TRIKONA[h2];
					if ((h1Kendra && h2Trikona) || (h1Trikona && h2Kendra)) {
						IS_KENDRA_TRIKONA_PAIR[h1][h2] = true;
					}
				}
			}
		}

		// Populate Dhana Yoga matrix
		int[] dhanaHouses = {1, 2, 5, 9, 11};
		for (int a : dhanaHouses) {
			for (int b : dhanaHouses) {
				if (a != b) {
					if (a == 2 || b == 2 || a == 11 || b == 11 || (a == 5 && b == 9) || (a == 9 && b == 5)) {
						IS_DHANA_PAIR[a][b] = true;
					}
				}
			}
		}
	}

	private static String getOrdinalSuffix(int n) {
		if (n >= 11 && n <= 13) return "th";
		switch (n % 10) {
			case 1: return "st";
			case 2: return "nd";
			case 3: return "rd";
			default: return "th";
		}
	}

	public static String getAspectName(Planet planet, int sourceHouse, int targetHouse) {
		if (planet == null || planet.ordinal() >= 9 || sourceHouse < 1 || sourceHouse > 12 || targetHouse < 1 || targetHouse > 12) {
			return null;
		}
		return ASPECT_NAME[planet.ordinal()][sourceHouse][targetHouse];
	}

	public static int getAspectDistance(Planet planet, int sourceHouse, int targetHouse) {
		if (planet == null || planet.ordinal() >= 9 || sourceHouse < 1 || sourceHouse > 12 || targetHouse < 1 || targetHouse > 12) {
			return 0;
		}
		return ASPECT_DISTANCE[planet.ordinal()][sourceHouse][targetHouse];
	}

	public static int[] getAspectTargets(Planet planet, int sourceHouse) {
		if (planet == null || planet.ordinal() >= 9 || sourceHouse < 1 || sourceHouse > 12) {
			return new int[0];
		}
		return PLANET_ASPECT_TARGETS[planet.ordinal()][sourceHouse];
	}

	public static boolean isKendraTrikonaPair(int houseA, int houseB) {
		if (houseA < 1 || houseA > 12 || houseB < 1 || houseB > 12) return false;
		return IS_KENDRA_TRIKONA_PAIR[houseA][houseB];
	}

	public static boolean isDhanaPair(int houseA, int houseB) {
		if (houseA < 1 || houseA > 12 || houseB < 1 || houseB > 12) return false;
		return IS_DHANA_PAIR[houseA][houseB];
	}

	
}
