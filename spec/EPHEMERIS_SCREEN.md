# Ephemeris Screen — Basic & Advanced Mode Specification

**Document Version:** 2.1.0  
**Status:** Approved (Post-Goldfish v2 Audit)  
**Target:** Astrosoft 2.0 (Java 21 LTS / Swing)  
**Primary Files:**
- `src/app/astrosoft/core/Ephemeris.java`
- `src/app/astrosoft/util/SwissHelper.java`
- `src/app/astrosoft/ui/view/EphemerisView.java`
- `src/app/astrosoft/ui/table/EphemerisCellRenderer.java`
- `src/app/astrosoft/ui/table/AstrosoftTable.java`
- `src/app/astrosoft/ui/comp/Chart.java` / `src/app/astrosoft/beans/PlanetChartData.java`
- `src/app/astrosoft/ui/dlg/EphemerisConfigDialog.java`
- `src/app/astrosoft/ui/AstroSoft.java`

---

## 1. Purpose & Legacy Screen Contract

Preserve the existing **Basic** Ephemeris screen unchanged as the default experience (protected by `spec/UI_ARCHITECTURE.md` §6 and `LegacyScreensRegressionTest`), while modernizing **Advanced** mode into an expandable, chart-themed screen with a live **D1 (Rasi) Chart** reflecting the selected row.

| Capability | Basic Mode (Default) | Advanced Mode (Modernized) |
|---|---|---|
| **Layout & Sizing** | Legacy fixed `TitledTable` (`860×500`) anchored top-left | **Expandable** responsive layout (`BorderLayout.CENTER`) filling `AstroSoft.home` |
| **Theme** | Legacy default table styling | **Inherits `UIConsts.getChartTheme()`** (`CANVAS_BACKGROUND`, themed cards, table, header, `ModernScrollBarUI`) |
| **Date Picker** | Legacy `CalendarSpinner` | `ModernButton` opening `DatePickerDialog` |
| **Time & Location** | Static header label from `AstrosoftPref` | Clickable pill opening `EphemerisConfigDialog` (`TimePickerPanel` + `PlaceSearchPanel`) |
| **Time Stepping** | None | Inline **Hour** (`<< [step] >>`) and **Minute** (`<< [step] >>`) controls in Daily mode only |
| **Rollover** | None | Stepping past midnight/month/year rolls over full `Calendar` across days, months, and years |
| **Ascendant (`As`) Column** | Omitted | Included in **Daily** mode immediately after `Date` (omitted in Monthly mode) |
| **Live D1 Chart** | None | **D1 (`Varga.Rasi`) `Chart` card** on the right showing all 9 planets + Ascendant for the **chosen row** |
| **Preference Scope** | Reads global `AstrosoftPref` | **Session-only** in-memory `Calendar` + `Place` (never overwrites `AstrosoftPref`) |

---

## 2. Mode & State Model

### 2.1 UI Mode (`UiMode`)
- `UiMode.Basic` (default on construction) and `UiMode.Advanced`.
- **On-Screen Switcher:** Slim themed strip (`Basic` | `Advanced`) at `BorderLayout.NORTH` of `EphemerisView`.
- **State Preservation on Toggle:**
  - `Basic -> Advanced`: Inherits `ephDate`, initializes `sessionPlace` / time from `AstrosoftPref` if not yet customized, switches `bodyCardPanel` to `"Advanced"`, updates `advTable` + `D1 Chart` for the active row.
  - `Advanced -> Basic`: Switches `bodyCardPanel` to `"Basic"`, syncs `CalendarSpinner` and `Daily`/`Monthly` radio buttons, and recalculates `ephTable` using `AstrosoftPref`.

### 2.2 Session State (Advanced Mode)
- `Ephemeris currentEphemeris`: Retained on `EphemerisView` after each `updateEphTable()` so row clicks can build the D1 chart without recomputing the whole month/year.
- `Calendar activeDateTime` (year, month, day-of-month, hour `0..23`, minute `0..59`, `second = 0`).
- `Place sessionPlace` (session-only; never mutates `AstrosoftPref`).
- `Ephemeris.Mode ephMode` (`Daily` or `Monthly`).
- `int selectedMonthRow` (`0..11`, tracks selected row in Monthly mode for `Monthly -> Daily` carry-over, D1 chart, and `restoreSessionState`).

---

## 3. Core Calculation & D1 Chart Data (`Ephemeris.java`)

### 3.1 `Ephemeris.getPlanetChartData(int rowIndex)`
- Bounds check: if `ephemeris == null || ephemeris.isEmpty()`, return `null`; clamp `rowIndex` to `[0, ephemeris.size() - 1]`.
- **Daily mode:** Uses `ephemeris.get(rowIndex)` (which already includes `Planet.Ascendant` + all 9 planets for day `rowIndex + 1` at `calcTimeHours` and `place`).
- **Monthly mode:** Uses `ephemeris.get(rowIndex)` for the 9 planets on the 1st of month `rowIndex`, and explicitly sets `swissHelper.setSweDate(new SweDate(cal.get(Calendar.YEAR), rowIndex + 1, 1, calcTimeHours - place.timeZone()))` before calling `swissHelper.getAscendantEphData(place.longitude(), place.latitude())` so the D1 chart always has a valid Ascendant even though the Monthly table omits the `As` column.
- **Mapping `EphData` → `PlanetChartData`:**
  - `positions`: `ephData.getHouse().ordinal() + 1` (`1..12` Rasi index) for each `Planet` in `Planet.planetsAsc()`.
  - `longitudes`: `ephData.getHouse().ordinal() * 30.0 + ephData.getPosition()` (`0..360°`) for chart tooltips.
  - `stars`: `Nakshathra.ofDeg(longitude)` for chart tooltips.
  - `directions`: `ephData.isReverse()` (`true` = **retrograde**, `false` = **direct**, matching `SwissHelper.getPlanetDirection()`, `Panchang.getPlanetChartData()`, and `PlanetCellRenderer`).
  - Returns `new PlanetChartData(Varga.Rasi, positions, directions, stars, longitudes)`.

---

## 4. Expandable Layout, Dual Tables & Chart Theme Inheritance

### 4.1 Window Expandability (`AstroSoft.java` + `EphemerisView.java`)
- Add `view == View.EPHEMERIS_VIEW` to `AstroSoft.isModernScreen(View)` so `AstroSoft` (`home` has `null` layout) resizes `EphemerisView` to `(0, 0, home.getWidth(), home.getHeight())` on mount and window resize.
- `EphemerisView` retains `super(new Dimension(860, 500), loc)` (preserving `LegacyScreensRegressionTest` offscreen dimensions) and expands seamlessly when `AstroSoft` resizes it to `home`'s bounds.
- `EphemerisView` uses `BorderLayout`:
  - `NORTH`: Slim `Basic | Advanced` mode switcher strip.
  - `CENTER`: `CardLayout` (`bodyCardPanel`) holding:
    1. `"Basic"` card: Wraps the legacy `TitledTable` in a top-left-anchored panel (`FlowLayout(FlowLayout.LEFT, 0, 0)`) so Basic mode stays at its exact legacy `860×500` footprint and is never stretched.
    2. `"Advanced"` card: Expandable `JPanel(new BorderLayout(0, 10))` with `UIConsts.CANVAS_BACKGROUND` and `EmptyBorder(8, 10, 10, 10)`:
       - `NORTH`: Themed rounded toolbar card (`DatePickerDialog` button, `Time & Location` pill, `Hr` & `Min` steppers, `Daily`/`Monthly` toggle).
       - `CENTER`: `JSplitPane(JSplitPane.HORIZONTAL_SPLIT)` with `resizeWeight = 1.0`, `setContinuousLayout(true)`, `setBorder(null)`, `setDividerSize(8)`:
         - **Left (`CENTER`):** Themed Ephemeris table card (`advTable` inside a themed `JScrollPane` with `ModernScrollBarUI`), expanding both horizontally and vertically with the window.
         - **Right:** Themed **D1 Chart Card** (`preferredSize ~ 340×380`, `minimumSize ~ 320×340`) containing:
           - Card header (`"D1 Rasi Chart"` + selected date/time subtitle, e.g., `"Dec 5, 2026 • 03:00 AM"`).
           - Live `Chart(planetChartData, new Dimension(300, 300))` centered in the card (supports South Indian & North Indian `ChartStyle` and `rebuildChartsInComponent`).

### 4.2 Separate Tables (`basicTable` vs `advTable`)
- Keep two `AstrosoftTable` instances sharing `tableModel` (`AstrosoftTableModel`):
  - `ephTable` (Basic): Mounted inside legacy `TitledTable`, using legacy defaults.
  - `advTable` (Advanced): Mounted inside the Advanced themed `JScrollPane` with `ModernScrollBarUI` and themed colors.
- Both tables call `localizeColumns()` and `setEphemerisColumnWidths(...)` after `tableModel.updateColumns(...)`. Selection listeners on both tables are guarded by `isUpdatingTable`.

### 4.3 Chart Theme Inheritance (`UIConsts.getChartTheme()`)
In **Advanced mode**, all surfaces use existing `UIConsts.ChartTheme` APIs:
- **Outer Canvas:** `UIConsts.CANVAS_BACKGROUND`
- **Toolbar & Cards:** Rounded card panels painted with `theme.getBackground()` and `theme.getBorderColor()` (12px arc).
- **`advTable` & Header:**
  - `advTable.setBackground(theme.getBackground())`, `advTable.setForeground(theme.getPlanetForeground())`, `advTable.setGridColor(theme.getBorderColor())`, `advTable.setSelectionBackground(theme.getPrimaryAccentColor())`, `advTable.setSelectionForeground(Color.WHITE)`.
  - `advTable.getTableHeader()`: `setBackground(theme.getBackground())`, `setForeground(theme.getLabelColor())`, bottom matte border `theme.getBorderColor()`.
- **`EphemerisCellRenderer` (Advanced instance):**
  - Parameterized (e.g., `new EphemerisCellRenderer(true)`) so in Advanced mode Rasi symbols use `theme.getScoreColor()` (or `theme.getPrimaryAccentColor()`) and retrograde `[R]` uses a high-contrast red/accent, switching to `getForeground()` when `rowSelected == true`. Basic mode keeps `new EphemerisCellRenderer()` unchanged.

---

## 5. Live D1 Chart Synchronization & `AstroSoft.java` Propagation

### 5.1 Chart Update Triggers
`updateD1Chart(int rowIndex)` rebuilds the `Chart` inside `chartContainer` whenever:
1. User clicks/selects a row in `advTable` (Daily: `day = row + 1`; Monthly: `selectedMonthRow = row`).
2. User steps time with `Hr` or `Min` `<<` / `>>` buttons (including midnight/month/year rollovers).
3. User confirms a new date in `DatePickerDialog` or time/location in `EphemerisConfigDialog`.
4. User toggles `Basic -> Advanced` or switches `Daily <-> Monthly`.

### 5.2 Preference & Theme Propagation (`AstroSoft.java`)
- **`ChartTheme`, `Language`, `Ayanamsa`, `RahuNode` changes (`refreshActiveView` / `optionThemeChanged`):**
  - Capture `(uiMode, activeDateTime, sessionPlace, ephMode, selectedMonthRow)` before rebuilding `EphemerisView`, and restore all 5 fields via `restoreSessionState(...)`.
- **`ChartStyle` change (`optionStyleChanged`):**
  - `refreshAllChartsAndCachedViews()` -> `rebuildChartsInComponent(EphemerisView)` automatically rebuilds the embedded `Chart` (South ↔ North Indian) while preserving `baseChartSize` (`300×300`) and all session state.

---

## 6. Verification & Test Plan

1. **Core (`EphemerisAdvancedTest`):**
   - `Ephemeris.getPlanetChartData(rowIndex)` returns non-null `PlanetChartData` with `Varga.Rasi`, valid `Ascendant` (in both Daily and Monthly modes), all 9 planets, `longitudes` (`0..360`), and `isReverse()` retrograde flags (`true` = retrograde).
2. **UI, Expandability & Theme (`EphemerisAdvancedTest`):**
   - `Basic` mode remains default and passes `LegacyScreensRegressionTest` unchanged.
   - `Advanced` mode builds expandable `JSplitPane`, applies `UIConsts.getChartTheme()` colors to `advTable`, renders a `Chart` component reflecting the selected row, and updates `Chart` on `stepActiveMinutes(...)` and row selection.
   - `selectedMonthRow` is preserved across `restoreSessionState(...)`.
3. **Regression Gates:**
   - `ant test -Dtestcase=app.astrosoft.ui.test.EphemerisAdvancedTest`
   - `./.agents/skills/astrosoft-regression-guardian/scripts/run_legacy_regression.sh`
   - `./.agents/skills/astrosoft-regression-guardian/scripts/run_ui_smoke.sh`
   - `ant test -Dtestcase=app.astrosoft.ui.test.LocalizationAuditTest`
