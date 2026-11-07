/**
 * Rasi.java
 * Created On 2005, Oct 20, 2005 8:18:06 PM
 * @author E. Rajasekar
 */
package app.astrosoft.consts;

import static app.astrosoft.consts.AstroConsts.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import app.astrosoft.beans.Interval;
import app.astrosoft.beans.PlanetCharacter;
import app.astrosoft.util.Internalization;
import app.astrosoft.util.Mod;

public enum Rasi {
	// Sign Lord Polarity Cardinality Element
	Mesha("Ari", Planet.Mars, Polarity.Odd, Cardinality.Movable, Element.Fire),
	Vrishabha("Tau", Planet.Venus, Polarity.Even, Cardinality.Fixed, Element.Earth),
	Mithuna("Gem", Planet.Mercury, Polarity.Odd, Cardinality.Common, Element.Air),
	Kataka("Can", Planet.Moon, Polarity.Even, Cardinality.Movable, Element.Water),
	Simha("Leo", Planet.Sun, Polarity.Odd, Cardinality.Fixed, Element.Fire),
	Kanya("Vir", Planet.Mercury, Polarity.Even, Cardinality.Common, Element.Earth),
	Thula("Lib", Planet.Venus, Polarity.Odd, Cardinality.Movable, Element.Air),
	Vrichika("Sco", Planet.Mars, Polarity.Even, Cardinality.Fixed, Element.Water),
	Dhanus("Sag", Planet.Jupiter, Polarity.Odd, Cardinality.Common, Element.Fire),
	Makara("Cap", Planet.Saturn, Polarity.Even, Cardinality.Movable, Element.Earth),
	Kumbha("Aqu", Planet.Saturn, Polarity.Odd, Cardinality.Fixed, Element.Air),
	Meena("Pis", Planet.Jupiter, Polarity.Even, Cardinality.Common, Element.Water);

	public enum Polarity {
		Odd,
		Even;

		public boolean isOdd() {
			return this == Odd;
		}

		public boolean isEven() {
			return this == Even;
		}

		public Sex gender() {
			return this == Odd ? Sex.Male : Sex.Female;
		}

		@Override
		public String toString() {
			return name();
		}
	}

	public enum Cardinality {
		Movable("Movable", "Chara"),
		Fixed("Fixed", "Sthira"),
		Common("Common", "Dvisvabhava");

		private final String english;
		private final String sanskrit;

		Cardinality(String english, String sanskrit) {
			this.english = english;
			this.sanskrit = sanskrit;
		}

		public String english() {
			return english;
		}

		public String sanskrit() {
			return sanskrit;
		}

		public boolean isMovable() {
			return this == Movable;
		}

		public boolean isFixed() {
			return this == Fixed;
		}

		public boolean isCommon() {
			return this == Common;
		}

		public boolean isDual() {
			return isCommon();
		}

		@Override
		public String toString() {
			return english;
		}
	}

	public enum Element {
		Fire("Fire", "Agni"),
		Earth("Earth", "Prithvi"),
		Air("Air", "Vayu"),
		Water("Water", "Jala");

		private final String english;
		private final String sanskrit;

		Element(String english, String sanskrit) {
			this.english = english;
			this.sanskrit = sanskrit;
		}

		public String english() {
			return english;
		}

		public String sanskrit() {
			return sanskrit;
		}

		public boolean isFire() {
			return this == Fire;
		}

		public boolean isEarth() {
			return this == Earth;
		}

		public boolean isAir() {
			return this == Air;
		}

		public boolean isWater() {
			return this == Water;
		}

		@Override
		public String toString() {
			return english;
		}
	}

	private final String sym;
	private final Planet owner;
	private final Polarity polarity;
	private final Cardinality cardinality;
	private final Element element;

	private static Rasi[] vals = values();

	Rasi(String sym, Planet owner, Polarity polarity, Cardinality cardinality, Element element) {
		this.sym = sym;
		this.owner = owner;
		this.polarity = polarity;
		this.cardinality = cardinality;
		this.element = element;
	}

	public static Rasi ofIndex(int index) {
		return vals[Math.floorMod(index, vals.length)];
	}

	public static Rasi ofDeg(double deg) {
		return ofIndex((int) (deg / rasiLength));
	}

	public String sym() {
		return sym;
	}

	public static Interval longitudeForRasi(Rasi rasi) {
		double rasiStart = rasi.ordinal() * rasiLength;
		double rasiEnd = rasiStart + rasiLength;
		return new Interval(rasiStart, rasiEnd);
	}

	/** Returns rasi of absolute pos from current rasi */
	public Rasi absolute(int pos) {
		return Rasi.ofIndex(this.ordinal() + pos);
	}

	public Rasi next() {
		return absolute(1);
	}

	public Rasi previous() {
		return absolute(-1);
	}

	public Rasi[] trines() {
		return new Rasi[] {
				this,
				absolute(4),
				absolute(8)
		};
	}

	public Planet owner() {
		return owner;
	}

	public Planet lord() {
		return owner;
	}

	public Polarity polarity() {
		return polarity;
	}

	public Cardinality cardinality() {
		return cardinality;
	}

	public Element element() {
		return element;
	}

	public int bhava(Rasi ascendant) {
		Mod m = new Mod(12);
		return (m.sub(this.ordinal(), ascendant.ordinal()) + 1);
	}

	public boolean isOddSign() {
		return this.polarity == Polarity.Odd;
	}

	public boolean isEvenSign() {
		return this.polarity == Polarity.Even;
	}

	public boolean isMovableSign() {
		return this.cardinality == Cardinality.Movable;
	}

	public boolean isFixedSign() {
		return this.cardinality == Cardinality.Fixed;
	}

	public boolean isCommonSign() {
		return this.cardinality == Cardinality.Common;
	}

	public boolean isDualSign() {
		return isCommonSign();
	}

	public boolean isFireSign() {
		return this.element == Element.Fire;
	}

	public boolean isEarthSign() {
		return this.element == Element.Earth;
	}

	public boolean isAirSign() {
		return this.element == Element.Air;
	}

	public boolean isWaterSign() {
		return this.element == Element.Water;
	}

	public String classification() {
		return polarity + ", " + cardinality + ", " + element;
	}

	// Functional Planetary Nature delegations (delegates to PlanetCharacter baseline)
	public Set<Planet> functionalBenefics() {
		return PlanetCharacter.getFunctionalBenefics(this);
	}

	public Set<Planet> functionalMalefics() {
		return PlanetCharacter.getFunctionalMalefics(this);
	}

	public Set<Planet> functionalNeutrals() {
		return PlanetCharacter.getFunctionalNeutrals(this);
	}

	public Planet yogakaraka() {
		return PlanetCharacter.getYogakaraka(this);
	}

	public Set<Planet> marakas() {
		return PlanetCharacter.getMarakas(this);
	}

	public FunctionalRole functionalRole(Planet p) {
		return PlanetCharacter.getFunctionalRole(this, p);
	}

	public boolean isFunctionalBenefic(Planet p) {
		return PlanetCharacter.isFunctionalBenefic(this, p);
	}

	public boolean isFunctionalMalefic(Planet p) {
		return PlanetCharacter.isFunctionalMalefic(this, p);
	}

	public boolean isFunctionalNeutral(Planet p) {
		return PlanetCharacter.isFunctionalNeutral(this, p);
	}

	public boolean isYogakaraka(Planet p) {
		return PlanetCharacter.isYogakaraka(this, p);
	}

	public boolean isMaraka(Planet p) {
		return PlanetCharacter.isMaraka(this, p);
	}

	public String toString() {
		return Internalization.getString(this.name());
	}

	public String toString(Language lang) {
		return Internalization.getString(lang, this.name());
	}

	public String toString(DisplayFormat format) {
		return Internalization.getString(format, this.name(), this.sym());
	}
}
