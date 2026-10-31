# Transit Chart Feature Specification

## 1. Purpose

This document specifies the **Transit Chart** feature for Astrosoft.

Transit Chart is a floating, modeless analysis window launched from **View -> Transit Chart**. It is intended for **Gochara / transit analysis** and behaves like a lightweight, independent tool window rather than a primary docked application view.

The feature must preserve Astrosoft's architectural layering:

- Presentation concerns remain in `src/app/astrosoft/ui/`
- Astrological orchestration remains in `src/app/astrosoft/core/`
- Immutable or DTO-style data transfer objects remain in `src/app/astrosoft/beans/`
- Preference access remains centralized and must not be duplicated in arbitrary UI classes
- Ephemeris/math helpers remain behind existing utility adapters rather than being called ad hoc from rendering code

The feature is intentionally **not** a new document type in v1 and must not introduce persistence, notes ownership, or save/dirty-state behavior.

## 2. Feature Summary

### 2.1 User Goal

The user opens a separate Transit Chart window and evaluates current or selected planetary transit positions for a chosen date, time, and location.

### 2.2 Launch Behavior

- Menu path: **View -> Transit Chart**
- Window type: **floating modeless dialog**
- Independence: must remain usable regardless of the currently active main application screen
- Re-entry behavior: repeated activation should focus the existing Transit Chart window if already open, rather than creating uncontrolled duplicates

### 2.3 Initial Defaults

The initial Transit Chart context is:

- Date/time: **current system date and time**
- Place: **default place from global preferences**
- Ayanamsa: **global preference value currently active in Astrosoft**
- Chart type: **Rasi only**

### 2.4 User Editing Model

The window supports both:

1. **Inline editing** for quick adjustments to date, time, and location-related values
2. An **Edit...** workflow, conceptually similar to `MuhurtaConfigDialog`, for structured editing of date, time, and place

This dual approach is a product decision for v1.

## 3. Non-Goals

The following are explicitly out of scope for v1:

- Navamsa or other Vargas
- Automatic ticking live chart updates every second/minute
- Notes integration
- File save/load
- Dedicated document type such as `.trn`
- Dirty-state prompts
- Printing/export workflow
- Integration with `ViewManager` as a docked main-screen view

## 4. Architectural Placement

### 4.1 Presentation Layer

The Transit Chart window belongs to the presentation layer and should live under:

- `src/app/astrosoft/ui/dlg/` for the floating dialog window
- `src/app/astrosoft/ui/comp/` only if a new reusable visual component is truly needed

Responsibilities of the presentation layer:

- Show and manage the modeless dialog
- Display the current transit context
- Accept user interaction for editing inputs
- Trigger recomputation through a narrowly defined interface
- Render the returned chart data using existing chart-rendering components
- Show validation and user-facing error messages

The presentation layer must **not**:

- Perform Swiss Ephemeris calculations directly
- Encode ayanamsa logic
- Construct low-level astronomical formulas
- Read scattered preference keys directly from multiple widgets
- Embed transit business rules in Swing event handlers

### 4.2 Core Layer

Transit computation/orchestration belongs in `src/app/astrosoft/core/`.

Responsibilities of the core layer:

- Accept a transit request context containing date, time, place, and ayanamsa
- Resolve the effective computation timestamp and geospatial context
- Invoke existing horoscope/planetary calculation infrastructure
- Produce a result suitable for Rasi chart display and optional textual summary
- Centralize any domain-specific gochara/transit calculation flow

The core layer must **not**:

- Reference Swing classes
- Show dialogs
- Depend on concrete UI controls
- Know whether the caller is `TransitChartDialog`, `MyMuhurtaView`, or a future API

### 4.3 Beans / DTO Layer

A small DTO-style context object is recommended.

Suggested objects:

- `TransitContext`
  - Inputs only: date, time, place, timezone, ayanamsa
- `TransitChartResult`
  - Computed output for rendering and summary display
  - May contain `PlanetaryInfo` or chart-ready structures, depending on what existing code already standardizes

These objects should be data carriers, not UI controllers and not computation engines.

### 4.4 Utility Layer

Existing utility and adapter code such as Swiss Ephemeris wrappers should remain behind established helper classes.

Utilities may:

- Convert time formats
- Resolve Julian day or related astronomical input values
- Adapt Astrosoft domain requests into Swiss Ephemeris calls

Utilities must not become feature-specific UI orchestrators.

## 5. Integration with Existing Framework

### 5.1 Main Shell Integration

`AstroSoft.java` is the correct entry point for launching the feature because it owns menu bars, lifecycle, and global actions.

Expected shell responsibilities:

- Register a new menu action under **View**
- Create or focus the modeless Transit Chart dialog
- Keep at most one active Transit Chart window per main application frame unless product requirements later change
- Dispose references correctly when the window closes

### 5.2 Not a ViewManager View

Transit Chart must **not** be implemented as a `ViewManager` docked view in v1.

Reason:

- The requested behavior is floating and screen-independent
- `ViewManager` is intended for main content views such as Horoscope, Panchang, Muhurtha, and My Muhurta
- Treating Transit Chart as a main view would violate the product interaction model and create avoidable coupling

### 5.3 Chart Rendering Reuse

Rendering should reuse the existing chart stack already used by Astrosoft:

- `Chart`
- `PlanetChartData`
- Existing chart sizing/layout conventions

Transit Chart should produce data in the format expected by the chart renderer, rather than introducing a separate parallel rendering implementation.

## 6. UI Design

### 6.1 Window Characteristics

- Type: modeless floating dialog
- Title: `Transit Chart`
- Default close behavior: dispose window, do not exit app
- Resizability: allowed, but chart readability must remain protected by preferred sizing and stable layout rules

### 6.2 Layout

Recommended v1 layout:

- Top section: compact inline context row or stacked context panel
  - Date
  - Time
  - Place
- Secondary controls:
  - `Edit...`
  - `Now`
  - `Refresh` or `Recompute`
  - `Close`
- Main section:
  - Single **Rasi** chart only
- Optional footer:
  - Timezone
  - last computed timestamp
  - validation or status text

### 6.3 Similarity to My Muhurta

The feature should be visually and behaviorally similar to the My Muhurta workflow in the following ways:

- explicit date/time/place context
- chart-first analysis flow
- structured edit dialog option
- stable chart footprint

The feature should differ from My Muhurta in the following ways:

- no personal profiles
- no Panchanga-specific dashboard tables unless added later
- no Balam calendar
- only one chart
- not document-bound

## 7. State Model

### 7.1 Required State

The dialog needs a local in-memory state object representing the currently selected transit context:

Inputs:

- local date
- local time
- place name
- latitude
- longitude
- timezone offset or zone representation used by existing Astrosoft models
- ayanamsa from preferences

Derived values:

- normalized timestamp used for calculation
- computed planetary positions
- chart-ready display data
- optional summary text

### 7.2 Source of Truth

For v1, the **TransitContext** in memory is the source of truth for the dialog.

Inline controls must update this state in a controlled way. The Edit dialog must also update this same state object or a validated replacement object. The chart redraw must consume this state only after validation succeeds.

### 7.3 Preference Usage

Preferences are only used to initialize or refresh global defaults such as:

- default place
- active ayanamsa

Preferences must not be treated as the continuously mutable source of every keystroke in the dialog.

## 8. Computation Contract

### 8.1 Inputs

The transit computation service must accept:

- date
- time
- place
- ayanamsa

### 8.2 Output

The service must return enough information to populate:

- a Rasi chart with all relevant graha placements
- any existing tooltip/hover metadata expected by the chart component
- optional compact text summary if needed later

### 8.3 Formula / Computation Path

This specification intentionally does **not** redefine planetary mathematics. The feature must reuse Astrosoft's existing astronomical pipeline through its core/util abstractions and active ayanamsa setting.

This is a **software implementation constraint**, not a new textual astrology rule.

### 8.4 Error Handling

The computation contract must define behavior for:

- invalid or missing place
- invalid date/time entry
- unsupported timezone data
- Swiss Ephemeris failure or unavailable ephemeris data files

Expected UI result:

- preserve the last successfully rendered chart where practical
- show a user-readable error message
- never leave the dialog in a partially corrupted UI state

## 9. Validation Rules

### 9.1 Required Fields

At minimum, valid computation requires:

- valid date
- valid time
- valid place coordinates or a valid place object resolvable by existing place-selection workflows

### 9.2 Inline Editing Rules

Inline fields may temporarily contain incomplete text while the user is editing. Recompute must occur only after commit/validation, not on every intermediate invalid keystroke.

### 9.3 Edit Dialog Rules

The structured Edit workflow should validate all fields before applying changes back to the Transit Chart dialog.

## 10. Lifecycle and Window Management

### 10.1 Modeless Behavior

The dialog must remain open while the user continues interacting with the main Astrosoft window.

### 10.2 Reopen Behavior

If the user selects **View -> Transit Chart** while the dialog is already open:

- bring the existing dialog to front
- request focus
- optionally refresh defaults only when explicitly commanded, not implicitly overwriting user edits

### 10.3 Close Behavior

Closing the dialog should:

- dispose the floating window
- release shell references so it can be reopened cleanly
- not trigger save prompts

## 11. Clean-Layering Rules

The following rules are mandatory.

### 11.1 Allowed Dependencies

- `ui` may depend on `core`, `beans`, `consts`, and `util`
- `core` may depend on `beans`, `consts`, and `util`
- `beans` must remain lightweight and dependency-poor
- `util` must not depend on `ui`

### 11.2 Forbidden Leakage

The following are forbidden:

- Swing classes in `core`
- business logic in anonymous action listeners beyond simple orchestration
- preference key lookups duplicated throughout field widgets
- chart rendering code buried inside computation services
- direct Swiss Ephemeris calls spread across multiple dialog classes
- making `TransitChartDialog` itself responsible for astronomical computation

### 11.3 Narrow Interfaces

The dialog should depend on a small computation-facing interface or service entry point rather than many unrelated helper calls. This reduces leakage and makes test-driven development easier.

## 12. Proposed Class Responsibilities

This section is a design recommendation, not a finalized implementation mandate.

| Class / Area | Responsibility | Must Not Do |
|---|---|---|
| `AstroSoft` | menu wiring, open/focus lifecycle | perform transit math |
| `TransitChartDialog` | own modeless window and interaction flow | compute planets directly |
| `TransitContext` | hold validated input state | render UI |
| `TransitChartResult` | hold computed output | query Swing controls |
| Transit computation service | create chart-ready transit output | know about dialog layout |
| `Chart` / `PlanetChartData` | render chart | fetch preferences or compute ephemeris |

## 13. Edge Cases

### 13.1 Timezone and Place Edge Cases

Inputs and expected behavior:

- missing default place in preferences -> prompt user to choose or require explicit entry
- edited place without resolved coordinates -> reject recompute with clear message
- date/time near day rollover -> compute exactly from validated timestamp
- DST boundary or ambiguous local time -> use existing Astrosoft time interpretation rules; if ambiguity is unresolved by current infrastructure, surface the limitation explicitly

### 13.2 Window Edge Cases

- user opens Transit Chart repeatedly -> one managed instance
- user closes main screen view underneath -> Transit Chart remains independent until explicitly closed or application exits
- user changes global preferences while Transit Chart is open -> v1 behavior should be explicit; recommended default is to preserve current dialog context until the user presses `Now` or `Refresh Defaults` if such a control is introduced later

## 14. Testing Strategy

The project direction is test-driven development. The feature should therefore be designed for testability before implementation.

### 14.1 Unit-Testable Inputs

Inputs:

- date
- time
- place
- ayanamsa

Expected outputs:

- successful transit result with chart-ready planet placements
- validation errors for invalid input combinations

### 14.2 Suggested Test Areas

1. **Menu lifecycle tests**
   - opening Transit Chart creates one dialog
   - reopening focuses existing dialog instead of duplicating it

2. **Default initialization tests**
   - defaults come from current time + preferred place + active ayanamsa

3. **Validation tests**
   - invalid date rejected
   - invalid time rejected
   - place without coordinates rejected

4. **Computation orchestration tests**
   - valid context invokes transit computation service once
   - result maps correctly into chart-rendering data structures

5. **Modeless behavior tests**
   - dialog does not block main application interaction

6. **Close/reopen tests**
   - dialog disposes cleanly and can be reopened

### 14.3 Golden / Snapshot-Oriented UI Tests

If existing UI test patterns support it, add focused tests for:

- title and key buttons present
- Rasi chart displayed
- inline fields synchronize with dialog state after successful edit

## 15. Acceptance Criteria

The v1 Transit Chart feature is complete when all of the following are true:

- A `View -> Transit Chart` action exists
- The action opens a **modeless floating dialog**
- The dialog is independent of the currently active main view
- The dialog defaults to **now + preferred place + global ayanamsa**
- The dialog displays **Rasi only**
- The dialog supports both **inline editing** and an **Edit...** flow for date/time/place
- Computation and UI remain architecturally separated
- No new document type or dirty/save workflow is introduced
- Repeated menu invocation does not create uncontrolled duplicate windows
- Tests cover lifecycle, initialization, validation, and computation orchestration

## 16. Deferred Enhancements

Potential future enhancements, intentionally deferred:

- additional Vargas
- optional planet-position side panel
- compact textual ephemeris summary
- export/print support
- persisted transit sessions
- comparison mode against natal chart
- transit overlays inside other chart workflows

---

## 17. Planet Status Markers (Addendum, 2026-10-31)

This addendum adds dignity and Vargottama markers to planet cells in the Transit Chart. Each rule below is labelled by type, as required by the project instructions.

### 17.1 Domain Rules (textual rule)

Source: B.V. Raman, *Graha and Bhava Balas*, Table II (as encoded in `PlanetDignityTest`, hand-written and independent of production code).

- Exaltation: Sun-Aries, Moon-Taurus, Mars-Capricorn, Mercury-Virgo, Jupiter-Cancer, Venus-Pisces, Saturn-Libra.
- Debilitation: the sign opposite the exaltation sign.
- Moolatrikona (whole sign): Sun-Leo, Mars-Aries, Jupiter-Sagittarius, Venus-Libra, Saturn-Aquarius. Moon-Taurus and Mercury-Virgo are exaltation signs and do not report Moolatrikona.
- Own sign: sign lordship of the seven classical planets.
- Precedence when several apply: Exalted, then Moolatrikona, then Own. Debilitated stands alone.
- Vargottama: same sign in Rasi (D1) and Navamsa (D9).
- Not applicable to Rahu, Ketu, Ascendant or the combined node.

### 17.2 Interpretation and Convention

- Moolatrikona is applied by whole sign, not by degree range. Degree-limited Moolatrikona is out of scope.
- Dignity is shown for the planet's sign in the displayed Rasi chart only.
- Vargottama needs the planet's Navamsa sign, which comes from the existing `VargaCharts` pipeline. It is not recomputed by the UI.

### 17.3 Product Decisions (reversible)

- (Superseded by 17.6) Markers are shown only in the Transit Chart. Other charts that share `Chart` and `PlanetCellRenderer` are unchanged (opt-in).
- Marker suffixes appended to the planet label: Exalted U+25B2, Debilitated U+25BC, Moolatrikona U+25C6, Own U+25A0, Vargottama U+2021. Vargottama follows any dignity marker.
- Glyph choices are provisional pending a visual review in narrow (3-planet) cells.

### 17.4 Layering and Implementation

| Layer | Item | Rule |
|---|---|---|
| `consts` | `Dignity` enum | Pure enum |
| `beans` | `PlanetStatus` | Immutable pair: `Dignity`, `vargottama` |
| `core` | `PlanetStatusCalculator.dignity`, `isVargottama`, `statusOf` | Pure functions, no Swing, no preferences, no I/O |
| `ui.util` | `PlanetStatusMarkers.suffix(PlanetStatus)` | Pure string mapping, no Swing |
| `beans` | `PlanetChartData` status map | Optional, default empty |
| `ui.table` | `PlanetCellRenderer` | Appends the suffix only if a status exists for the planet; contains no dignity logic |
| `ui.dlg` | `TransitChartDialog` | Orchestrates only: asks the computation layer for statuses and passes them to the chart data |

Forbidden: dignity logic in the renderer or the dialog; calling `Planet.toString()` in domain code or tests (it reaches `AstroSoft` through `Internalization`); changing existing chart behaviour when no status is supplied.

### 17.5 Test Plan

| Test | Covers |
|---|---|
| `PlanetDignityTest` | Full 7 by 12 table, precedence, non-classical planets, nulls |
| `PlanetVargottamaTest` | 84 same-sign cases, 924 different-sign cases, non-classical planets, nulls |
| `PlanetStatusTest` | `statusOf` combines dignity and Vargottama |
| `PlanetStatusMarkersTest` | Suffix strings for every combination |
| Pending | `VargaCharts` degree-window sweep for Navamsa sign; renderer opt-in regression; transit wiring |

### 17.6 Scope Revision: Planetary Positions Suffix Design (2026-10-31)

This section supersedes the earlier glyph-in-Transit-Chart proposal (17.3–17.5) and the interim "Status column" proposal.

#### 17.6.1 Purpose and Non-Goals

- **Purpose** *(3) product/design decision*: Display concise planetary status suffixes—retrograde (`R`), combust (`C`), whole-sign dignity (`Ex`, `Db`, `Mt`, `Own`), and Vargottama (`V`)—directly inside the planet name cell of the **Planetary Positions** panels:
  1. Compact Planetary Positions panel on the Profile tab ([`RelativePlanetView.java:83-96`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/view/RelativePlanetView.java#L83-L96)).
  2. Full Planetary Positions table view ([`PlanetView.java:49-51`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/view/PlanetView.java#L49-L51), `ViewManager.PLANET_POS_VIEW`).
- **Non-Goals** *(3) product/design decision*:
  - No status suffixes in 12-house grid charts ([`Chart.java`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/comp/Chart.java)) or the Transit Chart dialog ([`TransitChartDialog.java`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/dlg/TransitChartDialog.java)).
  - View-only: horoscope XML, Markdown, and print/PDF exports remain unchanged.
  - Does **not** modify [`PlanetaryInfo.getPlanetRasi(Varga)`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/beans/PlanetaryInfo.java#L193-L206), which has a known defect *(4) software implementation detail*: line 202 mutates `this.planetRasi` in-place (`planetRasi.put(...)`) while returning the empty local map `planetInRasi`. Callers must read Navamsa signs from `PlanetaryInfo.getDivChart(Varga.Navamsa)` instead.

#### 17.6.2 Data Flow and Layering

| Component | Symbol / Location | Responsibility | Rule Category |
|---|---|---|---|
| `PlanetaryInfo` | [`PlanetaryInfo.java:125-188`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/beans/PlanetaryInfo.java#L125-L188) | Supplies D1 signs (`getPlanetRasi()`), 1-based D9 sign numbers (`getDivChart(Varga.Navamsa)`), retrograde map (`getPlanetDirection()`), and combustion map (`getPlanetCombustion()`). | *(4) software implementation detail* |
| `PlanetStatusCalculator` | [`PlanetStatusCalculator.java:28-103`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/core/PlanetStatusCalculator.java#L28-L103) | Pure domain functions: `dignity(Planet, Rasi)`, `isVargottama(Planet, Rasi, Rasi)`, `statusOf(Planet, Rasi, Rasi)`, `signsOf(Map<Planet, Integer>)`, `statusMap(Map<Planet, Rasi>, Map<Planet, Rasi>)`. No Swing, preferences, or I/O. | *(4) software implementation detail* |
| `PlanetStatus` / `Dignity` | [`PlanetStatus.java`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/beans/PlanetStatus.java), [`Dignity.java`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/consts/Dignity.java) | Immutable value object pairing `Dignity` (`EXALTED`, `DEBILITATED`, `MOOLATRIKONA`, `OWN`, `NONE`) with `boolean vargottama`. | *(4) software implementation detail* |
| `PlanetStatusCodes` | [`PlanetStatusCodes.java:17-56`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/util/PlanetStatusCodes.java#L17-L56) | Maps lead token (`"R"`, `"C"`, `"R,C"`, or `null`) and `PlanetStatus` to `" (R,C,Ex,V)"` or `""`. Pure string utility. | *(3) product/design decision* |
| `PlanetLabels` | [`PlanetLabels.java:21-41`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/util/PlanetLabels.java#L21-L41) | Shared facade for Planetary Positions views: `label(name, retro, combust, status)` and `statuses(PlanetaryInfo)`. | *(4) software implementation detail* |
| `RelativePlanetView` | [`RelativePlanetView.java:83-95`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/view/RelativePlanetView.java#L83-L95) | Compact Profile panel: populates `AstrosoftTableColumn.Planet` string cell via `PlanetLabels.label(...)`. | *(4) software implementation detail* |
| `PlanetView` & `PlanetCellRenderer` | [`PlanetView.java:49-51`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/view/PlanetView.java#L49-L51), [`PlanetCellRenderer.java:38-40, 119-125`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/table/PlanetCellRenderer.java#L119-L125) | Full table view: passes `PlanetLabels.statuses(planetaryInfo)` to `PlanetCellRenderer`, which formats the `Planet` cell text on render. | *(4) software implementation detail* |

#### 17.6.3 Formatter Contract (`PlanetLabels.label` & `PlanetStatusCodes.suffixText`)

- **Signature**: `PlanetLabels.label(String name, boolean retro, boolean combust, PlanetStatus status)` ([`PlanetLabels.java:21-24`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/util/PlanetLabels.java#L21-L24)).
- **Token Ordering** *(3) product/design decision*:
  1. `R` — if `retro == true`
  2. `C` — if `combust == true`
  3. At most one dignity code from `status.getDignity()`: `Ex` (`EXALTED`), `Db` (`DEBILITATED`), `Mt` (`MOOLATRIKONA`), or `Own` (`OWN`); omitted when `NONE`
  4. `V` — if `status != null && status.isVargottama()`
- **Formatting**:
  - When at least one token is present: `name + " (" + String.join(",", tokens) + ")"` (comma-separated, no spaces inside parentheses, single space before `(`). Example: `"Mercury (R,C,Db,V)"`.
  - When no tokens are present: returns `name` unchanged (`"Venus"`, `"Ascendant"`).
- **Null & Edge Handling** *(4) software implementation detail*:
  - `status == null` (always passed for `Ascendant`, or when status map entry is absent): dignity and `V` are omitted; `retro` and `combust` still format if true.
  - In `PlanetStatusCodes.suffixText(String retroSym, PlanetStatus status)` ([`PlanetStatusCodes.java:22-25`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/util/PlanetStatusCodes.java#L22-L25)): null or empty `retroSym` is ignored.

#### 17.6.4 Computational Rules

##### Rule 1: Retrograde (`R`)
- **Category**: *(1) direct textual rule* (apparent backward motion in ecliptic longitude) + *(4) software implementation detail* (Swiss Ephemeris speed sign / node handling).
- **Inputs**: Planet $p \in \{\text{Sun}..\text{Ascendant}\}$, daily longitudinal speed $v_\lambda$ (` degrees/day` from Swiss Ephemeris `swe_calc` index 3 in [`SwissHelper.java:108-115`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/core/SwissHelper.java#L108-L115)).
- **Formula**:
  - In `SwissHelper`: `isRetro = (speed < 0.0)` for planets; for `Rahu` and `Ketu`, `planetDir` is hardcoded to `false` ([`SwissHelper.java:114, 122`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/core/SwissHelper.java#L114-L122)) so nodes do not display `(R)`.
  - Sun and Moon never have $v_\lambda < 0$; [`PlanetCellRenderer.java:120`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/table/PlanetCellRenderer.java#L120) also explicitly guards `p != Planet.Moon`.
- **Edge cases**: Stationary point ($v_\lambda = 0.0 \implies \text{false}$); Rahu/Ketu true node retrograde motion suppressed (`false` in `planetDir`); `Ascendant` not in `planetDir` (`false`).
- **Expected output**: `boolean` (`true` $\implies$ token `"R"`).
- **Test cases**: `PlanetLabelsTest.retroThenCombustThenStatus`, `PlanetaryPositionsViewTest.compactPanelKeepsRetrogradeAndLeavesNeutralPlanetsPlain`.

##### Rule 2: Combustion (`C`)
- **Category**: *(1) direct textual rule* (planet within specified angular orb of Sun) + *(2) tradition-specific convention* (degree thresholds per planet and retrograde reduction for inner planets).
- **Inputs**: Sidereal longitudes $\lambda_p, \lambda_{\odot} \in [0^\circ, 360^\circ)$, retrograde flag `isRetro` for planet $p$ ([`PlanetaryInfo.java:125-161`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/beans/PlanetaryInfo.java#L125-L161)).
- **Formula**:
  $$\Delta = \min\left(|\lambda_p - \lambda_{\odot}|,\; 360^\circ - |\lambda_p - \lambda_{\odot}|\right)$$
  Planet $p$ is combust iff $p \in \{\text{Mars}, \text{Mercury}, \text{Jupiter}, \text{Venus}, \text{Saturn}\}$ and $\Delta \le \text{orb}(p, \text{isRetro})$:
  - **Astrosoft orbs** ([`PlanetaryInfo.java:146-153`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/beans/PlanetaryInfo.java#L146-L153)):
    - Moon: excluded (`false` at line 132)
    - Mars: $17^\circ$
    - Mercury: $12^\circ$ (retrograde), $14^\circ$ (direct)
    - Jupiter: $11^\circ$
    - Venus: $8^\circ$ (retrograde), $10^\circ$ (direct)
    - Saturn: $5^\circ$
    - Sun, Rahu, Ketu, Ascendant: `false`
  - **Source conflict**: Classical texts (Surya Siddhanta / B.V. Raman, *Hindu Predictive Astrology* & *Graha and Bhava Balas*) and PyJHora ([`const.py:635-636`](file:///Users/mukunds100/Dev/PyJHora/src/jhora/const.py#L635-L636)) define combustion for **Moon ($12^\circ$)** and **Saturn ($15^\circ$)** (and in PyJHora, direct/retrograde arrays `[12, 17, 14, 10, 11, 15]` / `[12, 8, 12, 11, 8, 16]` for `[Moon, Mars, Mercury, Jupiter, Venus, Saturn]`, swapping Jupiter/Venus indices). See Open Decision #4.
- **Edge cases**: Wrap-around across $0^\circ$ Aries ($\lambda_{\odot} = 355^\circ, \lambda_p = 2^\circ \implies \Delta = 7^\circ$); exact boundary $\Delta = \text{orb}$ (`<=` evaluates `true`); $\text{orb} \pm 1'$ ($0.0167^\circ$).
- **Expected output**: `boolean` (`true` $\implies$ token `"C"`).
- **Test cases**: `PlanetLabelsTest.combustComesBeforeDignity`, `PlanetaryPositionsViewTest.compactPanelShowsJupiterCombustAndOwnSign`, plus $\pm 1'$ boundary sweep in Test Matrix.

##### Rule 3: Whole-Sign Dignity (`Ex`, `Db`, `Mt`, `Own`, `NONE`)
- **Category**: *(1) direct textual rule* (sign tables for exaltation, debilitation, Moolatrikona, own sign from B.V. Raman, *Graha and Bhava Balas*, Table II) + *(2) tradition-specific convention* (whole-sign evaluation vs degree ranges; precedence order; nodes excluded).
- **Inputs**: `Planet planet`, D1 `Rasi sign` ([`PlanetStatusCalculator.java:28-48`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/core/PlanetStatusCalculator.java#L28-L48), [`Planet.java:126-180`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/consts/Planet.java#L126-L180)).
- **Formula** (evaluated in strict precedence order for $p \in \{\text{Sun}, \text{Moon}, \text{Mars}, \text{Mercury}, \text{Jupiter}, \text{Venus}, \text{Saturn}\}$):
  1. Non-classical ($p \in \{\text{Rahu}, \text{Ketu}, \text{Ascendant}, \text{Ra\_Ke}, \text{NullPlanet}\}$) $\implies \text{NONE}$.
  2. `planet.isExaltedRasi(sign)` $\implies \text{EXALTED}$ (`Ex`): Sun–Mesha, Moon–Vrishabha, Mars–Makara, Mercury–Kanya, Jupiter–Kataka, Venus–Meena, Saturn–Thula.
  3. `planet.isDebilitatedRasi(sign)` $\implies \text{DEBILITATED}$ (`Db`): Sun–Thula, Moon–Vrichika, Mars–Kataka, Mercury–Meena, Jupiter–Makara, Saturn–Mesha, Venus–Kanya.
  4. `planet.isMoolaTrikona(sign)` $\implies \text{MOOLATRIKONA}$ (`Mt`): Sun–Simha, (Moon–Vrishabha shadowed by Exalted), Mars–Mesha, (Mercury–Kanya shadowed by Exalted), Jupiter–Dhanus, Venus–Thula, Saturn–Kumbha.
  5. `planet.isOwnerOf(sign)` $\implies \text{OWN}$ (`Own`): Moon–Kataka, Mars–Vrichika, Mercury–Mithuna, Jupiter–Meena, Venus–Vrishabha, Saturn–Makara (all other own signs are shadowed by `Mt` or `Ex` under whole-sign rules).
  6. Otherwise $\implies \text{NONE}$.
- **Source conflict**:
  - Under whole-sign evaluation, Sun in Simha is always `Mt` (never `Own`), Moon in Vrishabha is always `Ex` (never `Mt` or `Own`), and Mercury in Kanya is always `Ex` (never `Mt` or `Own`).
  - Under degree-based Moolatrikona rules (B.V. Raman / BPHS / PyJHora [`const.py:432-440`](file:///Users/mukunds100/Dev/PyJHora/src/jhora/const.py#L432-L440)), Sun in Leo $0^\circ\text{–}20^\circ$ is `Mt` and $20^\circ\text{–}30^\circ$ is `Own`; Moon in Taurus $0^\circ\text{–}3^\circ$ is `Ex` and $3^\circ\text{–}30^\circ$ (BPHS/PyJHora) or $4^\circ\text{–}30^\circ$ (Raman) or $4^\circ\text{–}20^\circ$ (`Moolatrikona_Summary_Table.md`) is `Mt`; Mercury in Virgo $0^\circ\text{–}15^\circ$ is `Ex`, $15^\circ/16^\circ\text{–}20^\circ$ is `Mt`, and $20^\circ\text{–}30^\circ$ is `Own`. See Open Decisions #2 and #3.
- **Edge cases**: `planet == null` or `sign == null` throws `IllegalArgumentException`; sign boundaries at $29^\circ59'59''$ vs $0^\circ00'00''$.
- **Expected output**: `Dignity` enum value.
- **Test cases**: `PlanetDignityTest` (84 planet-sign pairs + non-classical + nulls), `PlanetMoolaTrikonaTest` (11 tests).

##### Rule 4: Navamsa Sign (`D9`) and Vargottama (`V`)
- **Category**: *(1) direct textual rule* (Parashari Navamsa division and Vargottama = same sign in D1 and D9) + *(2) tradition-specific convention* (classical 7 planets only) + *(4) software implementation detail* (reading 1-based sign integers from `getDivChart(Varga.Navamsa)`).
- **Inputs**: Planet $p$, D1 sign $r_1 \in \{0..11\}$ ($1\text{-based } s_1 = r_1 + 1$), longitude in sign $\theta = \lambda_p \bmod 30^\circ \in [0^\circ, 30^\circ)$, Navamsa pada index $k = \lfloor \theta / (30^\circ/9) \rfloor = \lfloor \theta / 3^\circ20' \rfloor \in \{0..8\}$ ([`VargaCharts.java`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/core/VargaCharts.java), [`PlanetStatusCalculator.java:53-83`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/core/PlanetStatusCalculator.java#L53-L83)).
- **Formula**:
  - Parashari D9 sign (0-based):
    $$r_9 = (\text{seed}(r_1) + k) \bmod 12, \quad \text{where } \text{seed}(r_1) = \begin{cases} 0 \text{ (Mesha)} & r_1 \in \{0, 4, 8\} \text{ (Fire)} \\ 9 \text{ (Makara)} & r_1 \in \{1, 5, 9\} \text{ (Earth)} \\ 6 \text{ (Thula)} & r_1 \in \{2, 6, 10\} \text{ (Air)} \\ 3 \text{ (Kataka)} & r_1 \in \{3, 7, 11\} \text{ (Water)} \end{cases}$$
  - Vargottama condition:
    $$\text{isVargottama}(p, r_1, r_9) \iff p \in \text{CLASSICAL} \;\land\; r_1 = r_9$$
    This occurs in the **1st Navamsa** ($0^\circ00'\text{–}3^\circ20'$, $k=0$) of **Movable (Chara)** signs ($r_1 \in \{0, 3, 6, 9\}$), the **5th Navamsa** ($13^\circ20'\text{–}16^\circ40'$, $k=4$) of **Fixed (Sthira)** signs ($r_1 \in \{1, 4, 7, 10\}$), and the **9th Navamsa** ($26^\circ40'\text{–}30^\circ00'$, $k=8$) of **Dual (Ubhaya)** signs ($r_1 \in \{2, 5, 8, 11\}$).
- **Edge cases**:
  - Pada boundaries at $3^\circ20'00''$ ($3.3333^\circ$) and $6^\circ40'00''$ ($6.6667^\circ$) and $1'$ below ($3^\circ19'59''$).
  - Missing or malformed Navamsa map (`getDivChart(Varga.Navamsa)` is `null`, missing a planet key, or contains a sign number $\notin [1..12]$): [`PlanetLabels.statuses`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/util/PlanetLabels.java#L30-L41) catches `IllegalArgumentException` and falls back to an empty D9 map so dignity is still displayed and `isVargottama` is `false`.
- **Expected output**: `boolean` (`true` $\implies$ token `"V"`).
- **Test cases**: `PlanetVargottamaTest`, `PlanetStatusMapTest`.

#### 17.6.5 Verification History

1. **Initial attempt (`aa76e3e`)**: Wired `PlanetCellRenderer.setPlanetStatus(...)` inside `PlanetView` only and tested `PlanetCellRendererStatusTest` in isolation.
2. **Gap identified**: The Profile screen's Planetary Positions panel uses [`RelativePlanetView`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/view/RelativePlanetView.java), which constructs a standalone `JTable` with `MapTableRow` strings and a custom `DefaultTableCellRenderer` rather than `PlanetCellRenderer`. Because no test instantiated the Swing views, the missing suffixes in `RelativePlanetView` went undetected.
3. **Fix (`f505dc0` & `PlanetaryPositionsViewTest`)**: Extracted [`PlanetLabels`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/util/PlanetLabels.java) to unify label formatting across `RelativePlanetView` and `PlanetView`, and added [`PlanetaryPositionsViewTest.java`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/test/PlanetaryPositionsViewTest.java) to instantiate both Swing views and assert the rendered `JLabel` text in every `JTable`.
4. **Discrepancies found during audit (pending user decision before code change)**:
   - In [`PlanetView.java:49-51`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/view/PlanetView.java#L49-L51) and [`PlanetCellRenderer.java:119-125`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/table/PlanetCellRenderer.java#L119-L125), `planetCombustion` is never passed to `PlanetCellRenderer`. Consequently, a combust planet shows `"Jupiter (C,Own)"` in `RelativePlanetView` but `"Jupiter (Own)"` (missing `C`) in `PlanetView`.

#### 17.6.6 Test Matrix

| # | Matrix Scenario | Target / Boundary | Rule Categories | Expected Behaviour (Current Code) |
|---|---|---|---|---|
| 1 | Sign boundary | $29^\circ59'59''$ vs $0^\circ00'00''$ | *(1)*, *(4)* | Sign transitions cleanly at $0^\circ00'00''$; dignity and D9 recompute for new sign. |
| 2 | Navamsa boundary | $3^\circ20'00''$, $6^\circ40'00''$, and just under ($3^\circ19'59''$, $6^\circ39'59''$) | *(1)*, *(4)* | Pada $k = \lfloor \theta / 3^\circ20' \rfloor$ steps from 0 $\rightarrow$ 1 at $3^\circ20'$ and 1 $\rightarrow$ 2 at $6^\circ40'$. |
| 3 | Vargottama by sign modality | Movable 1st pada ($0^\circ\text{–}3^\circ20'$), Fixed 5th pada ($13^\circ20'\text{–}16^\circ40'$), Dual 9th pada ($26^\circ40'\text{–}30^\circ$) | *(1)* | Classical planet gets `V` iff in the matching pada for its sign modality. |
| 4 | Moolatrikona edges | Sun Leo $20^\circ$, Moon Taurus $3^\circ/4^\circ$, Mars Aries $12^\circ$, Mercury Virgo $16^\circ/20^\circ$, Jupiter Sag $10^\circ$, Venus Libra $15^\circ$, Saturn Aq $20^\circ$ | *(1)*, *(2)*, *(3)* | Whole-sign code: Sun Leo=`Mt`, Moon Taurus=`Ex`, Mars Aries=`Mt`, Mercury Virgo=`Ex`, Jupiter Sag=`Mt`, Venus Libra=`Mt`, Saturn Aq=`Mt` across all degrees of the sign (see Open Decisions #2, #3). |
| 5 | Combined suffix permutations | `R+Ex`, `R+Db`, `C+Own`, `R+C+Db+V` | *(3)*, *(4)* | Formatted in strict order `R,C,<Dignity>,V`: e.g. `(R,Ex)`, `(R,Db)`, `(C,Own)`, `(R,C,Db,V)`. |
| 6 | Moon states | `Ex` (Taurus), `Own` (Cancer), `Mt` (shadowed by `Ex` in Taurus), never `R` | *(1)*, *(2)*, *(4)* | Moon shows `(Ex)` in Taurus, `(Own)` in Cancer, never `(R)`. (Combust excluded in Astrosoft; see Open Decision #4). |
| 7 | Rahu and Ketu | All signs and padas | *(2)*, *(3)* | Always plain `"Rahu"` and `"Ketu"` (no `R`, `C`, dignity, or `V`). |
| 8 | Ascendant | All signs and padas (including Vargottama padas) | *(1)*, *(3)* | Always plain `"Ascendant"` (never has a suffix). |
| 9 | Combustion limit $\pm 1'$ per planet | Mars $17^\circ$, Mercury $14^\circ$ (direct) / $12^\circ$ (retro), Jupiter $11^\circ$, Venus $10^\circ$ (direct) / $8^\circ$ (retro), Saturn $5^\circ$ (Astrosoft) vs $15^\circ$ (PyJHora) | *(1)*, *(2)*, *(4)* | At $\text{orb} - 1'$: `C` shown; at $\text{orb} + 1'$: `C` omitted. |
| 10 | Retrograde + Combust inner planets | Mercury (`R,C` at $\Delta \le 12^\circ$, `R` only at $12^\circ < \Delta \le 14^\circ$); Venus (`R,C` at $\Delta \le 8^\circ$, `R` only at $8^\circ < \Delta \le 10^\circ$) | *(1)*, *(2)* | Retrograde orb applies when planet is retrograde; produces `(R,C,...)`. |
| 11 | Neutral chart (no statuses) | All 7 classical planets direct, non-combust, in neutral/friendly/enemy signs, non-Vargottama | *(3)*, *(4)* | Every row renders plain planet name with no parentheses. |
| 12 | Ayanamsa Lahiri vs Raman near sign edge | Planet within $\sim 1^\circ20'$ of a sign boundary evaluated under Lahiri vs Raman | *(2)*, *(4)* | Sign shift between Lahiri and Raman changes D1/D9 sign and resulting dignity/`V` suffix. |
| 13 | Bad or missing Navamsa data | `getDivChart(Varga.Navamsa)` returns `null`, omits a planet, or has out-of-range sign number ($0$ or $13$) | *(4)* | Fallback in `PlanetLabels.statuses` preserves D1 dignity and sets `V = false` without throwing. |

#### 17.6.7 Resolved Decisions (2026-10-31)

1. **Rahu/Ketu Dignity and Vargottama** *(2) tradition-specific convention / (3) product/design decision*:
   - **Decision**: Rahu and Ketu receive **no** dignity (`Dignity.NONE`) and **no** Vargottama (`false`). They render as plain `"Rahu"` and `"Ketu"`.
   - **Noted discrepancy**: B.V. Raman (*Graha and Bhava Balas*) excludes Rahu/Ketu from Shad-Bala dignity tables, whereas PyJHora ([`const.py:414-415, 430`](file:///Users/mukunds100/Dev/PyJHora/src/jhora/const.py#L414-L415)) assigns Rahu Exalted in Taurus/Gemini, Debilitated in Scorpio/Sagittarius, Moolatrikona in Virgo (`5`), and Ketu Exalted in Scorpio/Sagittarius, Debilitated in Taurus/Gemini, Moolatrikona in Pisces (`11`). Astrosoft follows B.V. Raman.
2. **Whole-Sign Dignity System and Precedence (`Ex > Db > Mt > Own`)** *(2) tradition-specific convention / (3) product/design decision*:
   - **Decision**: Use the **whole-sign** system with precedence `Ex > Db > Mt > Own` as implemented in [`PlanetStatusCalculator.java:28-48`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/core/PlanetStatusCalculator.java#L28-L48).
   - **Noted discrepancy**: Under whole-sign rules, Moon in Taurus and Mercury in Virgo always report `Ex` (shadowing `Mt` and `Own`), and Sun in Leo, Mars in Aries, Jupiter in Sagittarius, Venus in Libra, and Saturn in Aquarius always report `Mt` across $0^\circ\text{–}30^\circ$ (shadowing `Own`). In degree-based texts:
     - **Moon in Taurus**: B.V. Raman (*Hindu Predictive Astrology* / *Graha and Bhava Balas*) gives $0^\circ\text{–}3^\circ$ Exaltation and **$4^\circ\text{–}30^\circ$** Moolatrikona; BPHS & PyJHora ([`const.py:434`](file:///Users/mukunds100/Dev/PyJHora/src/jhora/const.py#L434)) give **$3^\circ\text{–}30^\circ$** Moolatrikona; workspace summary [`Moolatrikona_Summary_Table.md:15`](file:///Users/mukunds100/Downloads/Moolatrikona_Summary_Table.md#L15) lists **$4^\circ\text{–}20^\circ$**.
     - **Mercury in Virgo**: B.V. Raman / [`Moolatrikona_Summary_Table.md:17`](file:///Users/mukunds100/Downloads/Moolatrikona_Summary_Table.md#L17) gives $0^\circ\text{–}15^\circ$ Exaltation, **$16^\circ\text{–}20^\circ$** Moolatrikona, and $21^\circ\text{–}30^\circ$ Own; PyJHora ([`const.py:436`](file:///Users/mukunds100/Dev/PyJHora/src/jhora/const.py#L436)) uses **$15^\circ\text{–}20^\circ$** Moolatrikona.
3. **Authoritative Source on Discrepancies (B.V. Raman)** *(1) direct textual rule / (2) tradition-specific convention*:
   - **Decision**: Always follow **B.V. Raman** when sources disagree, while noting the discrepancy in this spec.
   - **Combustion (`C`) discrepancy**:
     | Planet | Astrosoft Current (`PlanetaryInfo.java:132-153`) | B.V. Raman (*Hindu Predictive Astrology*) | PyJHora (`const.py:635-636`) |
     |---|---|---|---|
     | Moon | Excluded (`false`) | **$12^\circ$** | $12^\circ$ (direct & retro) |
     | Mars | $17^\circ$ | **$17^\circ$** | $17^\circ$ (direct), $8^\circ$ (retro) |
     | Mercury | $14^\circ$ (direct), $12^\circ$ (retro) | **$14^\circ$ (direct), $12^\circ$ (retro)** | $14^\circ$ (direct), $12^\circ$ (retro) |
     | Jupiter | $11^\circ$ | **$11^\circ$** | $10^\circ$ (direct), $11^\circ$ (retro) *(swapped with Venus)* |
     | Venus | $10^\circ$ (direct), $8^\circ$ (retro) | **$10^\circ$ (direct), $8^\circ$ (retro)** | $11^\circ$ (direct), $8^\circ$ (retro) *(swapped with Jupiter)* |
     | Saturn | **$5^\circ$** *(code defect)* | **$15^\circ$** | $15^\circ$ (direct), $16^\circ$ (retro) |
4. **Tooltips for Status Codes** *(3) product/design decision*:
   - **Decision**: Deferred (no tooltip changes in this scope).
5. **Bad or Missing Navamsa Data Handling** *(4) software implementation detail*:
   - **Decision**: Silent UI fallback to dignity-only (`V = false`), while logging the reason via `java.util.logging.Logger` in [`PlanetLabels.statuses`](file:///Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/util/PlanetLabels.java#L30-L41).
