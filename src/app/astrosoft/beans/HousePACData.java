/**
 * HousePACData.java
 *
 * Immutable Data Transfer Object capturing the complete K.N. Rao PAC
 * (Placement, Aspect, Conjunction) analysis for a single astrological house.
 */
package app.astrosoft.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.astrosoft.consts.FunctionalRole;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.SignRelationship;

public class HousePACData {

	private final int houseNum;
	private final Rasi rasi;
	private final Planet lord;
	private final List<String> classifications;
	private final LordPlacement lordPlacement;
	private final List<OccupantInfo> occupants;
	private final List<AspectInfo> aspects;
	private final List<String> conjunctions;
	private final List<String> yogas;

	public HousePACData(int houseNum, Rasi rasi, Planet lord, List<String> classifications,
			LordPlacement lordPlacement, List<OccupantInfo> occupants, List<AspectInfo> aspects,
			List<String> conjunctions, List<String> yogas) {
		this.houseNum = houseNum;
		this.rasi = rasi;
		this.lord = lord;
		this.classifications = Collections.unmodifiableList(new ArrayList<>(classifications));
		this.lordPlacement = lordPlacement;
		this.occupants = Collections.unmodifiableList(new ArrayList<>(occupants));
		this.aspects = Collections.unmodifiableList(new ArrayList<>(aspects));
		this.conjunctions = Collections.unmodifiableList(new ArrayList<>(conjunctions));
		this.yogas = Collections.unmodifiableList(new ArrayList<>(yogas));
	}

	public HousePACData(int houseNum, Rasi rasi, Planet lord, List<String> classifications,
			LordPlacement lordPlacement, List<OccupantInfo> occupants, List<AspectInfo> aspects,
			List<String> occupantConjunctions, List<String> lordConjunctions, List<String> yogas) {
		this(houseNum, rasi, lord, classifications, lordPlacement, occupants, aspects,
				occupantConjunctions != null ? occupantConjunctions : Collections.emptyList(), yogas);
	}

	public int getHouseNum() { return houseNum; }
	public Rasi getRasi() { return rasi; }
	public Planet getLord() { return lord; }
	public List<String> getClassifications() { return classifications; }
	public LordPlacement getLordPlacement() { return lordPlacement; }
	public List<OccupantInfo> getOccupants() { return occupants; }
	public List<AspectInfo> getAspects() { return aspects; }
	public List<String> getConjunctions() { return conjunctions; }
	public List<String> getOccupantConjunctions() { return conjunctions; }
	public List<String> getLordConjunctions() { return Collections.emptyList(); }
	public List<String> getYogas() { return yogas; }

	public static class LordPlacement {
		private final Planet lord;
		private final int ruledHouse;
		private final int house;
		private final Rasi rasi;
		private final SignRelationship dignity;
		private final FunctionalRole functionalRole;
		private final boolean isYogakaraka;
		private final boolean isOwnHouse;
		private final boolean isCombust;
		private final boolean isRetrograde;

		public LordPlacement(Planet lord, int ruledHouse, int house, Rasi rasi, SignRelationship dignity,
				FunctionalRole functionalRole, boolean isYogakaraka, boolean isOwnHouse,
				boolean isCombust, boolean isRetrograde) {
			this.lord = lord;
			this.ruledHouse = ruledHouse;
			this.house = house;
			this.rasi = rasi;
			this.dignity = dignity;
			this.functionalRole = functionalRole;
			this.isYogakaraka = isYogakaraka;
			this.isOwnHouse = isOwnHouse;
			this.isCombust = isCombust;
			this.isRetrograde = isRetrograde;
		}

		public LordPlacement(Planet lord, int house, Rasi rasi, SignRelationship dignity,
				FunctionalRole functionalRole, boolean isYogakaraka, boolean isOwnHouse,
				boolean isCombust, boolean isRetrograde) {
			this(lord, house, house, rasi, dignity, functionalRole, isYogakaraka, isOwnHouse, isCombust, isRetrograde);
		}

		public Planet getLord() { return lord; }
		public int getRuledHouse() { return ruledHouse; }
		public int getHouse() { return house; }
		public Rasi getRasi() { return rasi; }
		public SignRelationship getDignity() { return dignity; }
		public FunctionalRole getFunctionalRole() { return functionalRole; }
		public boolean isYogakaraka() { return isYogakaraka; }
		public boolean isOwnHouse() { return isOwnHouse; }
		public boolean isCombust() { return isCombust; }
		public boolean isRetrograde() { return isRetrograde; }

		public String formatText() {
			StringBuilder sb = new StringBuilder();
			sb.append(lord.name());
			if (isRetrograde) sb.append(" [R]");
			if (isCombust) sb.append(" [C]");
			sb.append(", ").append(app.astrosoft.core.HousePACCalculator.ordinal(ruledHouse)).append(" lord, is placed in ")
			  .append(app.astrosoft.core.HousePACCalculator.ordinal(house)).append(" house");
			if (rasi != null) {
				sb.append(" (").append(rasi.englishName());
				if (dignity != null) {
					sb.append(", ").append(dignity.label()).append(" sign");
				}
				sb.append(")");
			}
			if (isYogakaraka) {
				sb.append(" — Yogakaraka");
			} else if (functionalRole != null) {
				sb.append(" — ").append(functionalRole.displayName());
			}
			return sb.toString();
		}
	}

	public static class OccupantInfo {
		private final Planet planet;
		private final SignRelationship dignity;
		private final FunctionalRole functionalRole;
		private final boolean isYogakaraka;
		private final boolean isCombust;
		private final boolean isRetrograde;

		public OccupantInfo(Planet planet, SignRelationship dignity, FunctionalRole functionalRole,
				boolean isYogakaraka, boolean isCombust, boolean isRetrograde) {
			this.planet = planet;
			this.dignity = dignity;
			this.functionalRole = functionalRole;
			this.isYogakaraka = isYogakaraka;
			this.isCombust = isCombust;
			this.isRetrograde = isRetrograde;
		}

		public Planet getPlanet() { return planet; }
		public SignRelationship getDignity() { return dignity; }
		public FunctionalRole getFunctionalRole() { return functionalRole; }
		public boolean isYogakaraka() { return isYogakaraka; }
		public boolean isCombust() { return isCombust; }
		public boolean isRetrograde() { return isRetrograde; }

		public String formatText() {
			StringBuilder sb = new StringBuilder();
			sb.append(planet.name());
			if (isRetrograde) sb.append(" [R]");
			if (isCombust) sb.append(" [C]");
			sb.append(": ");
			if (isYogakaraka) {
				sb.append("Yogakaraka");
			} else if (functionalRole != null) {
				sb.append(functionalRole.displayName());
			}
			if (dignity != null) {
				sb.append(" in ").append(dignity.label().toLowerCase()).append(" sign");
			}
			return sb.toString();
		}
	}

	public static class AspectInfo implements Comparable<AspectInfo> {
		private final Planet planet;
		private final int fromHouse;
		private final Rasi fromRasi;
		private final String aspectName;
		private final FunctionalRole functionalRole;
		private final boolean isYogakaraka;
		private final boolean isOwnLord;
		private final int sortPriority; // 0: Benefic, 1: Neutral, 2: Malefic

		public AspectInfo(Planet planet, int fromHouse, Rasi fromRasi, String aspectName,
				FunctionalRole functionalRole, boolean isYogakaraka, boolean isOwnLord) {
			this.planet = planet;
			this.fromHouse = fromHouse;
			this.fromRasi = fromRasi;
			this.aspectName = aspectName;
			this.functionalRole = functionalRole;
			this.isYogakaraka = isYogakaraka;
			this.isOwnLord = isOwnLord;

			if (isYogakaraka || functionalRole == FunctionalRole.BENEFIC) {
				this.sortPriority = 0;
			} else if (functionalRole == FunctionalRole.NEUTRAL) {
				this.sortPriority = 1;
			} else {
				this.sortPriority = 2;
			}
		}

		public Planet getPlanet() { return planet; }
		public int getFromHouse() { return fromHouse; }
		public Rasi getFromRasi() { return fromRasi; }
		public String getAspectName() { return aspectName; }
		public FunctionalRole getFunctionalRole() { return functionalRole; }
		public boolean isYogakaraka() { return isYogakaraka; }
		public boolean isOwnLord() { return isOwnLord; }
		public int getSortPriority() { return sortPriority; }

		public String formatText() {
			StringBuilder sb = new StringBuilder();
			sb.append("Aspected by ").append(planet.name());
			sb.append(" from House ").append(fromHouse);
			if (aspectName != null) {
				sb.append(" via ").append(aspectName);
			}
			String roleLabel = isYogakaraka ? "Yogakaraka" : (functionalRole != null ? functionalRole.displayName() : null);
			if (isOwnLord) {
				sb.append(" (").append(roleLabel != null ? roleLabel : "Lord").append(" - Own House Lord)");
			} else if (roleLabel != null) {
				sb.append(" (").append(roleLabel).append(")");
			}
			return sb.toString();
		}

		@Override
		public int compareTo(AspectInfo other) {
			int pDiff = Integer.compare(this.sortPriority, other.sortPriority);
			if (pDiff != 0) return pDiff;
			return this.planet.name().compareTo(other.planet.name());
		}
	}

	/**
	 * Formats this House PAC analysis into plain text for clipboard copying and notes.
	 */
	public String toPlainText() {
		StringBuilder sb = new StringBuilder();
		sb.append("--------------------------------------------------\n");
		sb.append("HOUSE ").append(houseNum).append(": ").append(rasi.englishName().toUpperCase()).append(" (").append(rasi.name()).append(")");
		if (!classifications.isEmpty()) {
			sb.append(" — ").append(String.join(", ", classifications));
		}
		sb.append("\nLord: ").append(lord.name()).append("\n");
		sb.append("--------------------------------------------------\n\n");

		// [P] Placement
		sb.append("[P] PLACEMENT\n");
		if (lordPlacement != null) {
			sb.append("  • Lord Placement: ").append(lordPlacement.formatText()).append("\n");
		}
		if (occupants.isEmpty()) {
			sb.append("  • Occupants: None (Empty house)\n");
		} else {
			sb.append("  • Occupants:\n");
			for (OccupantInfo occ : occupants) {
				sb.append("    • ").append(occ.formatText()).append("\n");
			}
		}
		sb.append("\n- - - - - - - - - - - - - - - - - - - - - - - - - \n\n");

		// [A] Aspects
		sb.append("[A] ASPECTS\n");
		if (aspects.isEmpty()) {
			sb.append("  • None (No planetary aspects on this house)\n");
		} else {
			for (AspectInfo asp : aspects) {
				sb.append("  • ").append(asp.formatText()).append("\n");
			}
		}
		sb.append("\n- - - - - - - - - - - - - - - - - - - - - - - - - \n\n");

		// [C] Conjunctions & Yogas
		sb.append("[C] CONJUNCTIONS & YOGAS\n");
		if (conjunctions.isEmpty() && yogas.isEmpty()) {
			sb.append("  • None\n");
		} else {
			if (!conjunctions.isEmpty()) {
				for (String c : conjunctions) {
					sb.append("  • ").append(c).append("\n");
				}
			}
			if (!yogas.isEmpty()) {
				for (String y : yogas) {
					sb.append("    ★ ").append(y).append("\n");
				}
			}
		}
		sb.append("--------------------------------------------------\n");
		return sb.toString();
	}
}
