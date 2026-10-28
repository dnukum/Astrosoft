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
