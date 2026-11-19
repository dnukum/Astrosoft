/**
 * Thithi.java
 * Created On 2006, Jan 21, 2006 5:25:46 PM
 * @author E. Rajasekar
 */

package app.astrosoft.consts;

import app.astrosoft.util.Internalization;

public enum Thithi {

	Prathamai,
	Dvithiyai,
	Thrithiyai,
	Chathruthi,
	Panchami,
	Sashti,
	Sapthami,
	Ashtami,
	Navami,
	Dasami,
	Ekaadasi,
	Dvaadasi,
	Thrayodasi,
	Chathurdasi,
	Pournami,
	Amavasya;
	
	private static Thithi vals[] = values();

	public static Thithi ofIndex(int index) {
		return vals[Math.floorMod(index, vals.length)];
	}
	
	public static Thithi ofDeg(double sun, double moon ) {

        Thithi thithi;
        Paksha pak = Paksha.Shukla;
        double diff;

        if ( moon > sun ) {
            diff = moon - sun;
        } else {
            diff = ( moon + 360 ) - sun;
        }

        if ( diff > 180.0 ) {
            pak = Paksha.Krishna;
            diff = diff - 180;
        }

        thithi = ofIndex(( int ) ( diff / 12 ));

        //For Krishna Paksha 15th thithi is Ammavasya
        if ( ( thithi == Thithi.Pournami ) && ( pak == Paksha.Krishna ) ) { 
            thithi = Thithi.Amavasya;
        }
        return thithi;
    }

	private static final Planet[] LORDS = {
		Planet.Sun,     // Prathamai
		Planet.Moon,    // Dvithiyai
		Planet.Mars,    // Thrithiyai
		Planet.Mercury, // Chathruthi
		Planet.Jupiter, // Panchami
		Planet.Venus,   // Sashti
		Planet.Saturn,  // Sapthami
		Planet.Rahu,    // Ashtami
		Planet.Sun,     // Navami
		Planet.Moon,    // Dasami
		Planet.Mars,    // Ekaadasi
		Planet.Mercury, // Dvaadasi
		Planet.Jupiter, // Thrayodasi
		Planet.Venus,   // Chathurdasi
		Planet.Saturn,  // Pournami
		Planet.Rahu     // Amavasya
	};

	private static final String[] NITYA_DEVIS = {
		"Kameswari",      // Prathamai
		"Bhaga Maalini",  // Dvithiyai
		"Nityaklinna",    // Thrithiyai
		"Bherunda",       // Chathruthi
		"Vahnivasini",    // Panchami
		"Mahavajreswari", // Sashti
		"Sivaduti",       // Sapthami
		"Tvarita",        // Ashtami
		"Kulasundari",    // Navami
		"Nitya",          // Dasami
		"Nilapataka",     // Ekaadasi
		"Vijaya",         // Dvaadasi
		"Sarvamangala",   // Thrayodasi
		"Jwalamalini",    // Chathurdasi
		"Chitra",         // Pournami
		"Lalita"          // Amavasya
	};

	/** Planetary lord of this Tithi. */
	public Planet getLord() {
		return LORDS[ordinal()];
	}

	/** Nitya Devi associated with this Tithi. */
	public String getNityaDevi() {
		return NITYA_DEVIS[ordinal()];
	}

	@Override
	public String toString() {
		return Internalization.getString(this.name());
	}

	public String toString(Language language) {
		return Internalization.getString(language, this.name());
	}

}
