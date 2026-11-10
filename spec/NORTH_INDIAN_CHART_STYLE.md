# North Indian Chart Style Specification

**Status:** Implemented · **Scope:** Swing UI Layer · **Default:** South Indian (`ChartStyle.SOUTH_INDIAN`)

## 1. Overview
Astrosoft supports switching all on-screen chart components (`app.astrosoft.ui.comp.Chart`) globally between **South Indian** (default, fixed signs clockwise on a 4x4 grid) and **North Indian** (fixed houses counter-clockwise in the traditional diamond-and-triangle square).

- **UI-Only Scope:** Core astrology calculations and data models (`ChartData`, `PlanetChartData`, `AshtaVargaChartData`) remain unchanged.
- **PDF / Print Export:** XSL report templates remain South Indian.
- **Legacy Compatibility:** South Indian rendering and all 13 legacy screen structures remain unchanged.

## 2. User Preference, Dual-Chart Card & Zoom Controls
- **Menu:** `Options -> Chart Style` radio group with `South Indian` (`Command.STYLE_SOUTH_INDIAN`) and `North Indian` (`Command.STYLE_NORTH_INDIAN`).
- **Persistence:** `AstrosoftPref.Preference.ChartStyle` (default `SOUTH_INDIAN`) and `AstrosoftPref.Preference.ChartZoom` (`70..160%`, default `100`).
- **Live Refresh:** `AstroSoft.optionStyleChanged(ChartStyle)` updates `UIConsts.setChartStyle(...)` and invokes `refreshAllChartsAndCachedViews()`, rebuilding every `Chart` in place via `Chart.rebuild()`.
- **Dual-Chart Card & Right-Click Zoom:** On `HoroscopeDashboard`, Rasi and Navamsha charts share one rounded `ChartsCard` enclosure (`Chart.createDualChartsCard`), and fixed planet charts expose `Zoom In (+10%)`, `Zoom Out (-10%)`, and `Reset Zoom (100%)` via right-click context menu (`Chart.createPopups`), refreshing all active and cached chart views without toolbar clutter.

## 3. North Indian Geometry (`NorthIndianGeometry`)
Inside a bounding rectangle of width `W` and height `H` (square when `W == H`, or proportional rectangle up to `MAX_ASPECT_RATIO = 1.65` in `NorthIndianChartCanvas`) with origin `(ox, oy)`:
- **Houses 1..12** run **counter-clockwise** starting from **House 1 (Lagna)** at the top center diamond:
  - Diamonds (angular houses 1, 4, 7, 10): top, left, bottom, right.
  - Triangles: `2` (top-left upper), `3` (top-left left), `5` (bottom-left left), `6` (bottom-left lower), `8` (bottom-right lower), `9` (bottom-right right), `11` (top-right right), `12` (top-right upper).
- **Sign rotation:** House `h` (1..12) holds sign index `((ascendant.ordinal() + h - 1) mod 12)`, displaying sign number `1..12` at the house's inner corner pulled toward its centroid.

## 4. Visual Styling & Fitting (`NorthIndianChartCanvas`)
- **Header Strip:** Title (including `setTitleBadge` pill badge; multi-line HTML titles flattened to one crisp line) and optional subtitle render in a compact `NORTH` header strip above the chart square; the center vertex stays clear.
- **Theme Integration & Planet Colors:** Uses `UIConsts.getChartBackground()`, `getChartBorderColor()`, `getChartLabelColor()`, and `getChartScoreColor()`. Angular diamonds receive a subtle border-color tint (`getNorthIndianDiamondTint()`). Planet labels in North Indian mode use per-planet colors (`UIConsts.getNorthIndianPlanetColor(Planet)`), automatically contrast-adjusted per theme to WCAG AA (`>= 4.5:1`).
- **Clean Chart Parity & Retrogrades:** Matching the clean South Indian presentation, only planet symbols (`As` for English Ascendant, `Su..Ke`) are drawn inside the houses; longitude/orb, nakshatra, and functional nature details remain available via hover tooltips and the Planet Details popup. Retrograde planets render bold with an underline, matching `PlanetCellRenderer`.
- **Crowding Fallback:** Tries 1-column, 2-column, and 3-column layouts from base font size down to 8pt, and falls back to tapered base-to-apex triangle packing down to 6pt so labels never ellipsize or clip outside house boundaries.
- **Tooltips:** `NorthIndianChartCanvas.getToolTipText(MouseEvent)` hit-tests placed planet targets first (delegating to `ChartTooltips.planetToolTip`) and falls back to house polygon hit-testing (delegating to `ChartTooltips.houseToolTip`).

## 5. Ashtavarga Orientation & Proportional Resizing
- Ashtavarga BAV, SAV, Trikona, and Ekathipathya charts render bindu scores, `Asc`/`Mo` markers, strength fills (`UIConsts.getAshtavargaColor`), and natal highlight rings inside the North Indian houses.
- `AshtaVargaChartData` provides `setOrientationAscendant(Rasi)` / `getOrientationAscendant()` so `AshtavargaView` can orient Trikona and Ekathipathya charts in North Indian mode while leaving `getAscendant()` null so South Indian output is unchanged.
- Resizable Ashtavarga grids (e.g., `AshtavargaDialog`) scale score/marker fonts and highlight rings proportionally in both `South Indian` and `North Indian` styles.
