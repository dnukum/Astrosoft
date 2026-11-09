/**
 * AshtavargaReduction.java
 * Created On 2006, Feb 4, 2006 3:48:48 PM
 * @author E. Rajasekar
 */

package app.astrosoft.consts;

import java.util.EnumSet;

import app.astrosoft.util.Internalization;

public enum AshtavargaName {
	
	Sun,
	Moon,
	Mars,
	Mercury,
	Jupiter,
	Venus,
	Saturn,
	SarvaAshtavarga,
	Ascendant,
	AshtaVarga, Trikona , Ekathipathya;
	
	public static EnumSet<AshtavargaName> ashtavargas(){
		return EnumSet.range(Sun, SarvaAshtavarga);
	}

	public static EnumSet<AshtavargaName> bhinnaAshtavargas(){
		return EnumSet.range(Sun, Saturn);
	}

	public static EnumSet<AshtavargaName> bhinnaAshtavargasWithAsc(){
		EnumSet<AshtavargaName> set = EnumSet.range(Sun, Saturn);
		set.add(Ascendant);
		return set;
	}

	public static EnumSet<AshtavargaName> allAshtavargas(){
		return EnumSet.range(Sun, Ascendant);
	}
	
	public static AshtavargaName ofPlanet(Planet p){
		return valueOf(p.name());
	}

	public Planet toPlanet() {
		try {
			return Planet.valueOf(name());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	public String toString(Language lang) {

		return Internalization.getString(lang, this.name());
	}
	
	@Override
	public String toString() {
		
		return Internalization.getString(this.name());
	}
}
