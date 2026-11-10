/**
 * HouseContentModel.java
 *
 * Adapts {@link ChartData} ({@link PlanetChartData}, {@link AshtaVargaChartData}, or generic)
 * into per-rasi render models for North Indian chart rendering.
 */

package app.astrosoft.ui.comp.chart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import app.astrosoft.beans.AshtaVargaChartData;
import app.astrosoft.beans.ChartData;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.AshtavargaStrength;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.table.TableData;
import app.astrosoft.ui.table.TableRowData;
import app.astrosoft.ui.util.UIConsts;

public final class HouseContentModel {

	public static final class PlanetItem {
		private final Planet planet;
		private final String label;
		private final boolean retrograde;
		private final Color color;

		public PlanetItem(Planet planet, String label, boolean retrograde, Color color) {
			this.planet = planet;
			this.label = label;
			this.retrograde = retrograde;
			this.color = color;
		}

		public Planet getPlanet() {
			return planet;
		}

		public String getLabel() {
			return label;
		}

		public boolean isRetrograde() {
			return retrograde;
		}

		public Color getColor() {
			return color;
		}
	}

	public static final class HouseEntry {
		private final Rasi rasi;
		private final int signNumber;
		private final List<PlanetItem> planets;
		private final Integer binduScore;
		private final AshtavargaStrength strength;
		private final boolean ascMarker;
		private final boolean moonMarker;
		private final boolean natalHighlight;
		private final String natalToolTip;

		private HouseEntry(Rasi rasi, List<PlanetItem> planets,
				Integer binduScore, AshtavargaStrength strength,
				boolean ascMarker, boolean moonMarker, boolean natalHighlight, String natalToolTip) {
			this.rasi = rasi;
			this.signNumber = rasi.ordinal() + 1;
			this.planets = planets;
			this.binduScore = binduScore;
			this.strength = strength;
			this.ascMarker = ascMarker;
			this.moonMarker = moonMarker;
			this.natalHighlight = natalHighlight;
			this.natalToolTip = natalToolTip;
		}

		public Rasi getRasi() {
			return rasi;
		}

		public int getSignNumber() {
			return signNumber;
		}

		public List<PlanetItem> getPlanets() {
			return planets;
		}

		public Integer getBinduScore() {
			return binduScore;
		}

		public AshtavargaStrength getStrength() {
			return strength;
		}

		public boolean isAscMarker() {
			return ascMarker;
		}

		public boolean isMoonMarker() {
			return moonMarker;
		}

		public boolean isNatalHighlight() {
			return natalHighlight;
		}

		public String getNatalToolTip() {
			return natalToolTip;
		}
	}

	private final ChartData chartData;
	private final Rasi ascendant;
	private final boolean ashtavarga;
	private final boolean planetChart;
	private final int maxPlanetsInAnyHouse;
	private final EnumMap<Rasi, HouseEntry> entries = new EnumMap<>(Rasi.class);

	private HouseContentModel(ChartData chartData, Rasi ascendant, boolean ashtavarga,
			boolean planetChart, int maxPlanetsInAnyHouse) {
		this.chartData = chartData;
		this.ascendant = ascendant;
		this.ashtavarga = ashtavarga;
		this.planetChart = planetChart;
		this.maxPlanetsInAnyHouse = maxPlanetsInAnyHouse;
	}

	public static HouseContentModel from(ChartData chartData) {
		boolean isAv = chartData instanceof AshtaVargaChartData;
		boolean isPlanet = chartData instanceof PlanetChartData;
		Rasi asc = Rasi.Mesha;
		if (isAv) {
			Rasi orientAsc = ((AshtaVargaChartData) chartData).getOrientationAscendant();
			if (orientAsc != null) {
				asc = orientAsc;
			}
		} else if (chartData != null && chartData.getAscendant() != null) {
			asc = chartData.getAscendant();
		}

		int maxPlanets = 1;
		if (isPlanet) {
			PlanetChartData pcd = (PlanetChartData) chartData;
			if (pcd.getPlanetsInRasi() != null) {
				for (List<Planet> list : pcd.getPlanetsInRasi().values()) {
					if (list != null && list.size() > maxPlanets) {
						maxPlanets = list.size();
					}
				}
			}
		}

		HouseContentModel model = new HouseContentModel(chartData, asc, isAv, isPlanet, maxPlanets);
		model.populate();
		return model;
	}

	private void populate() {
		if (ashtavarga) {
			AshtaVargaChartData avData = (AshtaVargaChartData) chartData;
			Rasi avAsc = avData.getAscendant();
			Rasi avMoon = avData.getMoonRasi();
			Rasi natal = avData.getNatalRasi();
			EnumMap<Rasi, Integer> varga = avData.getVarga();

			for (Rasi rasi : Rasi.values()) {
				Integer score = (varga != null) ? varga.get(rasi) : null;
				AshtavargaStrength strength = avData.getStrength(rasi);
				boolean isAsc = (avAsc == rasi);
				boolean isMoon = (avMoon == rasi);
				boolean isNatal = (natal == rasi);
				String tip = null;
				if (isNatal) {
					String valStr = (score != null) ? score.toString() : "";
					tip = "Natal " + (avData.getVargaName() != null ? avData.getVargaName().toString() : "Planet")
							+ " placement in " + rasi.toString(Language.ENGLISH) + " (" + valStr + " bindus)";
				}
				entries.put(rasi, new HouseEntry(
						rasi, Collections.<PlanetItem>emptyList(),
						score, strength, isAsc, isMoon, isNatal, tip));
			}
			return;
		}

		if (planetChart) {
			PlanetChartData pcd = (PlanetChartData) chartData;
			boolean highlightRetro = pcd.highlightRetrogrades();
			EnumMap<Planet, Boolean> planetDir = pcd.getPlanetDir();
			EnumMap<Rasi, List<Planet>> map = pcd.getPlanetsInRasi();

			for (Rasi rasi : Rasi.values()) {
				List<Planet> raw = (map != null) ? map.get(rasi) : null;
				List<PlanetItem> items;
				if (raw == null || raw.isEmpty()) {
					items = Collections.emptyList();
				} else {
					items = new ArrayList<>(raw.size());
					for (Planet p : raw) {
						items.add(buildPlanetItem(pcd, p, highlightRetro, planetDir));
					}
				}
				entries.put(rasi, new HouseEntry(
						rasi, items, null, AshtavargaStrength.NEUTRAL, false, false, false, null));
			}
			return;
		}

		// Generic fallback for any custom ChartData implementation
		Set<Rasi> dataHouses = (chartData != null && chartData.getHouses() != null)
				? chartData.getHouses()
				: Collections.<Rasi>emptySet();
		for (Rasi rasi : Rasi.values()) {
			List<PlanetItem> items = Collections.emptyList();
			if (dataHouses.contains(rasi)) {
				Table t = chartData.getChartHouseTable(rasi);
				if (t != null && t.getTableData() != null) {
					items = extractPlanetsFromTable(t.getTableData());
				}
			}
			entries.put(rasi, new HouseEntry(
					rasi, items, null, AshtavargaStrength.NEUTRAL, false, false, false, null));
		}
	}

	private static List<PlanetItem> extractPlanetsFromTable(TableData<? extends TableRowData> data) {
		List<PlanetItem> items = new ArrayList<>();
		for (int r = 0; r < data.getRowCount(); r++) {
			TableRowData row = data.getRow(r);
			if (row == null) continue;
			for (AstrosoftTableColumn col : AstrosoftTableColumn.chartHouseCols()) {
				Object v = row.getColumnData(col);
				if (v instanceof Planet) {
					Planet p = (Planet) v;
					String sym = (p == Planet.Ascendant && "Asc".equals(p.toString(DisplayFormat.SYMBOL)))
							? "As"
							: p.toString(DisplayFormat.SYMBOL);
					items.add(new PlanetItem(p, sym, false, UIConsts.getNorthIndianPlanetColor(p)));
				}
			}
		}
		return items;
	}

	private static PlanetItem buildPlanetItem(PlanetChartData pcd, Planet p,
			boolean highlightRetro, EnumMap<Planet, Boolean> planetDir) {
		String sym = p.toString(DisplayFormat.SYMBOL);
		if (p == Planet.Ascendant && "Asc".equals(sym)) {
			sym = "As";
		}
		boolean retro = highlightRetro && planetDir != null && Boolean.TRUE.equals(planetDir.get(p));
		Color color = UIConsts.getNorthIndianPlanetColor(p);
		return new PlanetItem(p, sym, retro, color);
	}

	public ChartData getChartData() {
		return chartData;
	}

	public Rasi getAscendant() {
		return ascendant;
	}

	public boolean isAshtavarga() {
		return ashtavarga;
	}

	public boolean isPlanetChart() {
		return planetChart;
	}

	public int getMaxPlanetsInAnyHouse() {
		return maxPlanetsInAnyHouse;
	}

	public HouseEntry getEntry(Rasi rasi) {
		return entries.get(rasi);
	}
}
