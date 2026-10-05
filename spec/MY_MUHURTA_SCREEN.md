# Specification: MyMuhurta Screen

## Overview
The **MyMuhurta** screen is a new view accessible via the menu (`View -> myMuhurta`). It serves as a personalized daily astrological dashboard. 
While the existing `Panchang` screen displays generalized daily planetary information (Tithi, Nakshatra, Auspicious Times), the `MyMuhurta` screen overlays this daily data with the **currently loaded user's natal profile**, calculating personalized compatibilities and displaying both transit and natal charts side-by-side.

## UI Layout & Components

### 1. Configuration Dialog (Edit Button)
Instead of cluttering the top of the table with input controls, the screen features an **Edit** button located at the bottom right of the table. Clicking this opens a dedicated `MuhurtaConfigDialog` to tweak event parameters:
* **Date & Time Selectors:** A date picker and a **24-hour Time Picker (HH:MM)**. The exact time is critical because the Muhurta chart acts as the "birth chart" for the event.
* **Place Chooser:** Reuses Astrosoft's existing `PlaceChooser` component allowing users to search for cities, input latitude/longitude, and select timezones. By default, it initializes to the user's global `AstroSoft.getPreferences().getPlace()`.
Changing these parameters immediately refreshes the view without overwriting the global application preferences.

### 2. Daily Panchang Data (Bottom Left)
Below the input controls, the screen will display the standard Panchang table:
* **Profile Name:** The very first row of the table displays the loaded user's name (`NAME_STR`) for clear context.
* **Standard Metrics:** Displays Nakshatra, Tithi, Yoga, Karana, Sunrise, Sunset, Auspicious Time (Subha Hora), Rahu Kalam, and Yama Gandam.

### 3. Personalized Muhurta Metrics (New Additions to Table)
Appended to the bottom of the standard Panchang table, a new section will display personalized metrics based on the currently loaded user profile:
* **Tara Balam (Star Strength):** Calculates the relationship between the User's birth Nakshatra (Janma Nakshatra) and the Transit Moon's Nakshatra for the selected date. 
  * *Calculation:* `(((Transit Nakshatra index - Birth Nakshatra index) + 27) % 9) + 1` (Outputs a 1-based index from 1 to 9).
  * *Paryaya (Row) Calculation:* Determines if the transit star is in the 1st, 2nd, or 3rd cycle (row) of 9 stars relative to the birth star.
  * *Ranking Hierarchy (1-based index):*
    * **Best:** 2 (Sampat), 6 (Sadhana), 9 (Parama Mitra)
    * **Second Best:** 4 (Kshema), 8 (Mitra)
    * **Acceptable (if nullified):** 1 (Janma - good for women/marriage), 5 (Pratyak)
    * **Conditional (Use Wisely):** 3 (Vipat) - No evil results if the day star falls in the 3rd row (Paryaya) from the birth star.
    * **Strictly Rejected:** 7 (Naidhana)
* **Chandra Balam (Moon Strength):** Calculates the relationship between the User's birth Moon sign (Janma Rasi) and the Transit Moon's sign for the selected date.
  * *Calculation:* Inclusive house count from the Natal Moon sign to the Transit Moon sign.
  * *Ranking Hierarchy:*
    * **Best / Approved:** 3rd, 10th, and 11th houses (Upachaya / growth houses). 
    * **Acceptable / Good:** 1st and 7th houses from Natal Moon.
    * **Mixed / Contextual:** 6th house (Upachaya growth, but sometimes avoided for general peace).
    * **Generally Avoided:** 12th house (loss).
    * **Strictly Rejected:** 8th house (Chandra Ashtama) - Must be strictly avoided as it causes mental anxiety and failure.

### 4. Chart Visualizations (Right Side)
The right side of the screen replaces the standard single-chart view with a comprehensive, horizontal multi-chart dashboard. The charts are laid out horizontally side-by-side (`FlowLayout`) and use a rigid `350x300` dimension to prevent column squishing and symbol truncation (`S...`). The titles are injected cleanly into the center grid using HTML wrappers:
* **Muhurta Rasi Chart (Transit Chart):** A standard D1 Rasi chart showing the exact planetary positions for the selected date, time, and location.
* **User's D1 Rasi Chart:** The natal Rasi chart of the currently loaded user profile (for visual comparison).
* **User's D9 Navamsa Chart:** The natal Navamsa chart of the currently loaded user profile.

## Data Flow & Architecture
To comply with the MVC patterns specified in `ARCHITECTURE.md`, the MyMuhurta feature strictly isolates its logic:

1. **Core Domain Object (`app.astrosoft.core.MyMuhurta`):**
   * Acts as the stateful orchestrator. It accepts the `Date`, `Time`, `Place`, and the active `Horoscope`.
   * Encapsulates the `Panchang` Ephemeris engine, processes Timezone conversions to UT Julian Days, and evaluates Tara & Chandra Balam matrix logic.
   * Exposes `getTableData()` to output pure, pre-formatted `TableData<MapTableRow>` beans.
2. **Presentation Layer (`app.astrosoft.ui.view.MyMuhurtaView`):**
   * A "dumb" Swing component. When `updateView()` is triggered (either on load or via the Edit dialog), it instantiates the `MyMuhurta` core object and simply hands the output beans to the `JTable` and `Chart` UI components for rendering.
3. **User Profile Dependency:** The screen requires an active `Horoscope` to be loaded. If no user is loaded, the `myMuhurta` menu item is disabled.
4. **Menu Integration:** Added under the `View` menu, triggering the instantiation of `MyMuhurtaView`.

## Future Considerations
* **1/15th Division Muhurtas:** Implement the mathematical logic to divide the Dinamaana (daytime) and Ratrimaana (nighttime) into 15 equal parts to calculate and display highly auspicious classical windows, specifically **Abhijit Muhurta** (the 8th Muhurta at Solar Noon) and **Brahma Muhurta** (the 14th Muhurta of the night, Pre-Dawn).
* Highlighting or color-coding the Tara/Chandra Balam rows (Green for Auspicious, Red for Inauspicious).

### 5. Technical Constraints & Rendering Nuances
* **Chart Name Injection:** To natively render custom chart titles ("Muhurta Rasi", "Natal Rasi") in the center of the Vedic chart without breaking the core `Chart.java` renderer, the screen wraps the data by creating an anonymous subclass of `PlanetChartData`. Extending `PlanetChartData` specifically (rather than generic `ChartData`) ensures the engine doesn't fall back to the default `JTable` string renderer, which would cause planetary symbols to truncate (e.g., "Saturn" becoming "S...").
* **Ascendant Symbol Sizing:** The English translation property `Ascendant` in `AstrosoftBundle_en.properties` has been strictly set to `As` (instead of `Asc`) to ensure the Ascendant marker fits perfectly within the 20-pixel table cells alongside other 2-letter planets (`Su`, `Mo`) without triggering ellipsis truncation.
* **Layout Isolation:** The legacy screens in Astrosoft expect to be rigidly bounded to the laptop's original resolution (`getScreenSize()`) with hardcoded coordinate offsets (e.g., `100, 20`). The global window resizing logic explicitly filters for `MyMuhurtaView` to allow it to dynamically stretch and center horizontally across widescreen monitors, while strictly shielding the legacy views from being stretched, which prevents their long titles and tables from chopping.
* **Component Alignment (`BorderLayout`):** The Date UI controls are mapped to a `BorderLayout.CENTER` wrapper (instead of `EAST`), forcing them to dynamically stretch and seamlessly align their edges pixel-for-pixel with the `AstrosoftTable` rendering beneath them.
* **Dynamic Table Row Rendering:** Because rows (like "Name" or "Tara Balam") are dynamically injected, hardcoded table indices (e.g. `Panchang.AUS_TIME_ROW = 9`) can no longer be relied upon for formatting. Row modifications (like multi-line formatting for Subha Hora times or row height expansions) are resolved dynamically by querying the localized key names (e.g. scanning for `AUS_TIME_STR`) to guarantee they trigger on the correct row regardless of table shifts.
* **Scrollpane & Border Mechanics:** To maintain visual separation, an internal `EmptyBorder` (10px) is injected directly into the `chartsPanel` (content layer) rather than the parent `JScrollPane`. This ensures that when the user resizes the window, the scroll bars physically spawn on the outer perimeter and do not overlap or touch the padded charts inside.

## Testing & Validation Strategy

To guarantee the astrological accuracy of the MyMuhurta feature across complex edge cases, the engine relies on a dual-pronged testing approach combining exhaustive internal matrices with external Swiss Ephemeris golden data.

### 1. Exhaustive Balam Matrices (`MyMuhurtaTest.java`)
Because the domain of both Tara Balam and Chandra Balam is finite, we do not sample tests; we programmatically test the entire domain.
* **Tara Balam:** The JUnit suite iterates over all `27 * 27 = 729` combinations of Birth Nakshatra and Transit Nakshatra. It evaluates the 1-based Tara Index, the Paryaya offset, and asserts that the resulting `BalamRank` evaluates exactly to the hierarchical rules (Best, Second Best, Acceptable, Conditional, Strictly Rejected).
* **Chandra Balam:** The JUnit suite loops through all `12 * 12 = 144` combinations of Birth Rasi and Transit Rasi, verifying the inclusive house count and asserting proper categorizations (e.g. 8th house strictly triggering `STRICTLY_REJECTED` Chandra Ashtama).

### 2. Ephemeris Golden Validation (`PanchangGoldenTest.java`)
To ensure the `Panchang(Date, Place)` engine correctly isolates mathematical coordinates from the OS system timezone and dynamically computes planetary physics (Sunrise, Sunset, Tithis, and Nakshatras) for any location on Earth:
* **Generation via PyJhora:** A Python script utilizing `pyjhora` (and `pyswisseph`) evaluates 121 mathematically perfect timestamps. It feeds precise latitude, longitude, and timezone fractional offsets directly into the Swiss Ephemeris engine.
* **The Matrix:** The 121 golden cases evaluate 11 global edge-case locations against 11 critical dates.
  * *Location Edges:* Standard times (New York, London, Chennai), Fractional Indian offsets (Asia/Kolkata +05:30), DST anomalies (Phoenix ignoring DST, Sydney inverting DST seasons), and Extreme Latitudes (Anchorage, Punta Arenas).
  * *Date Edges:* Standard calendar spread, Exact DST boundary days (US Spring Forward, US Fall Back), Leap Years, and Solstices (Longest/Shortest days).
* **Java JUnit Validation:** `PanchangGoldenTest.java` loads this verified JSON data, programmatically constructs local `Calendar` objects offset to those extreme timezones, executes Astrosoft's `Panchang` engine, and asserts that the Java outputs perfectly match the Python Swiss Ephemeris golden data.

## Appendix: The 1/15th Division Muhurtas (Background Material)

To support future implementations of classical electional windows (like Abhijit and Brahma Muhurta), the following table details the 15 Daytime Muhurtas. In Vedic astrology, a full 24-hour cycle (*Ahoratra*) is divided into 30 Muhurtas (15 during the day, 15 during the night). Each Muhurta lasts exactly 2 *Ghatis* (roughly 48 minutes, scaling proportionally with the seasons).

### The 15 Daytime Muhurtas (From Sunrise to Sunset)

| # | Muhurta Name | Nature | Primary Purpose & Usage |
| :--- | :--- | :--- | :--- |
| **1** | **Rudra / Shiva** | 🔴 Inauspicious | Starts at Sunrise. Good for aggressive actions or confronting enemies; strictly avoided for positive beginnings. |
| **2** | **Ahi / Sarpa** | 🔴 Inauspicious | Ruled by serpents. Generally avoided for all auspicious work. |
| **3** | **Mitra** | 🟢 Auspicious | Excellent for forming alliances, signing contracts, negotiations, and making friends. |
| **4** | **Pitri / Aryaman** | 🔴 Inauspicious | Ruled by ancestors. Excellent for rituals for the deceased (*Shraddha*), but strictly avoided for new beginnings. |
| **5** | **Vasu** | 🟢 Auspicious | Highly favorable for financial transactions, buying property, and expanding businesses. |
| **6** | **Varaha / Jala** | 🟢 Auspicious | Good for travel, agriculture, planting seeds, and tasks related to liquids or sea voyages. |
| **7** | **Viswedeva** | 🟢 Auspicious | A universally safe and positive period for almost all standard auspicious actions. |
| **8** | **Abhijit / Vidhi** | 🌟 **Highly Auspicious** | Occurs at **Solar Noon**. Capable of destroying countless astrological flaws. Used for highly important events. *(Avoided on Wednesdays)*. |
| **9** | **Brahma / Virinchi** | 🟢 Auspicious | Excellent for studying, learning, starting education, and intellectual pursuits. |
| **10** | **Indra** | 🟢 Auspicious | Excellent for taking charge, political actions, assuming leadership, or seeking favors from authorities. |
| **11** | **Indragni / Puruhuta** | 🟡 Mixed / Neutral | Good for tasks requiring intense energy, heat, or engineering; avoided for peaceful events. |
| **12** | **Nairrutya / Rakshasa**| 🔴 Inauspicious | Ruled by demons. A highly negative period; strictly avoided for any auspicious work. |
| **13** | **Varuna** | 🟢 Auspicious | Good for justice, legal matters, resolving disputes, and ocean-related activities. |
| **14** | **Vivaswan / Aryaman** | 🟡 Neutral | A transition period; generally treated as neutral or slightly inauspicious for major events. |
| **15** | **Bhaga** | 🟢 Auspicious | Ends at Sunset. Excellent for romance, weddings, pleasure, and artistic pursuits. |

### The Notable Nighttime Muhurta

| # | Muhurta Name | Nature | Primary Purpose & Usage |
| :--- | :--- | :--- | :--- |
| **14** | **Brahma Muhurta** | 🌟 **Highly Auspicious** | Occurs roughly **1.5 hours before Sunrise**. Dominated by pure *Sattva* (stillness). Exclusively reserved for waking up, meditation, and spiritual practices. |


## Inline Balam calendar lifecycle

Product/design decision: the calendar is inline. The main Show/Hide toggle and calendar close action dismiss the same panel. Reopening uses current shared person selections. Editing date/time/place refreshes an open calendar and resets its month. With neither person selected, the calendar is disabled and any open panel closes.

## Shared person selection

Product/design decision: Person 1 and Person 2 selectors above the charts load or replace horoscope files. Adjacent clear buttons remove selections; canceling or an unsuccessful load leaves existing selections unchanged. Entry starts with Person 1 and Person 2 unselected regardless of the main-screen horoscope; no role-selection prompt. MyMuhurta remains accessible with no main-screen horoscope.

Product/design decision: the information table excludes Name, Tara Balam and Chandra Balam. The chart sequence is Muhurta Rasi, optional Person 1 Rasi, optional Person 2 Rasi, with role/name titles; no Navamsa. The calendar uses these same selections, with role/name headers and detail labels; it has no independent partner picker.

Implementation detail: when both persons are selected, Person 1 supplies the existing primary model slot and Person 2 the partner slot; either alone supplies the primary slot. Selection changes refresh an open panel. No astrology formulas or domain rules change.

Regression expectations: zero selections gives one chart and disabled calendar; either single selection gives two charts and three calendar columns; both give three charts and five columns. Clear last selection closes calendar. Existing toggle/close/month-reset behavior remains covered by the headless lifecycle harness. Source-contract tests cover shared selectors and menu gating; visual/file-dialog behavior requires interactive verification.


## Default timezone initialization

Entry uses the current instant and global default place/timezone from application preferences. Display and calculations use the selected-place zone, not the host clock; selected person birth zones do not affect Muhurta. Editing date/time interprets local fields in the chosen place timezone. Tests verify next-day rollover in India and summer DST roundtrip. The legacy host-zone chooser Date encoding can normalize host-zone DST-gap clock fields; resolving that limitation requires a timezone-aware chooser.


## Table column sizing

The Panchanga table and its action buttons are anchored together at the top of the left column. The column may fill the available height, but its content uses preferred height, keeping Show/Hide Balam Calendar and Edit Date/Time immediately below the table. Headless geometry checks cover 700, 1200 and 1600 pixel column heights.


## Screen spacing

Product/design decision: content starts 15 pixels from top/left and ends 20 pixels from right/bottom. The Personal horoscopes titled selector panel remains at preferred width instead of drawing a full-width border. Calendar Update sits beside month navigation in a centered group. Chart viewport sizing uses the padded right-column width on resize and selection changes. Geometry checks cover widths 1000, 1500 and 1900 pixels.


## Calendar height bound

Combined calendar controls, table and details are capped at 360 pixels high, shrinking to available height in shorter windows. Unused vertical space remains outside the panel. Table and details retain scrollbars and draggable divider. Production layout checks cover host heights 600, 900 and 1400 pixels.


## Manual calendar resizing

Product decision: initial calendar fills available column width and is 360 pixels high. Right, bottom and corner grips support horizontal, vertical and combined dragging. Requested size persists during month/context updates while the panel remains open, constrained to available host area, preserving existing 20 pixel outer margins. Minimum requested size is 400 by 200, overridden by a smaller available area. Reopening restores initial sizing.

Implementation: size is a panel client property; host layout clamps actual bounds. JTable AUTO_RESIZE_ALL_COLUMNS adjusts columns with the viewport. Tests cover initial/manual sizes, available-space clamping, grip installation and column-width totals at two viewport widths. GUI dragging requires interactive verification.


## Planet Nakshatra hover

Presentation: occupied planet hover shows only Nakshatra name, no planet name or Pada. Shared Chart queries PlanetChartData precomputed results without astronomical calculations. Natal results come from PlanetaryInfo; Panchang records Nakshatra using existing Nakshathra.ofDeg on the same longitude map used for chart positions, with no new formula. Missing results suppress planet tooltip; other cells retain house tooltips. Ascendant is shown only if its longitude is supplied by domain, never inferred from Rasi. Muhurta refresh creates fresh data. Tests cover lookup, null/missing values, defensive copies; visual hover verification remains required.


## Tara Chandra inverse candidates

User-supplied rule overrides unspecified traditions: fixed Navatara rows have indices q, q+9, q+18; count inclusively from birth sign for Chandra and birth star for Tara. Core MyMuhurta.find_tara_chandra_match takes birth Nakshathra/Rasi, Tara 1-9, Chandra 1-12, returns TaraChandraMatchResult with defensive EnumSet copies of Tara Set, Chandra Set, Match. Chandra is calculated first using (r+C-1)%12 and existing Nakshathra.ofRasi; Tara primary is (n+T-1)%27 and row is primary%9. Intersection uses retainAll in Core; TaraChandraMatchResult only defensively copies and exposes the three computed sets. Null birth inputs/out-of-range targets throw IllegalArgumentException; no match is empty, not guessed. Overlapping stars count in both signs; these are candidates, not complete transit windows.

Selection details retain existing information and append separate three-column Tara Set / Chandra Set / Match tables for each loaded person. UI only invokes core and formats names; an empty match displays exactly Null: Astronomically impossible combination. Person-2-only uses Person 2 as primary. No natal data suppresses candidates. Normal row changes recalculate from row numbers, Update behavior remains unchanged.

JUnit tests cover examples, wraparound, all three cycles, boundary-spanning Krittika, invalid arguments, defensive copies of constructor inputs and getters, and all 34,992 birth-star/sign/target combinations against forward Tara counting plus independent integer interval-overlap oracle (4 units/star, 9/sign). UI integration regression checks both person paths and headings.


## Calendar Match column

Presentation decision: append Match after all person score columns. Core calendar model invokes existing inverse matcher with each loaded person's birth inputs and the row's Tara/Chandra numbers. With both people intersect their matches; one person uses their result. No birth context displays -, empty candidates use the existing exact Null message. BalamMatchCell carries defensive copies and formats names only. Existing score column indexes and selected-row details stay unchanged. Tests check no-context Match value and match-cell copy/empty behavior, alongside existing exhaustive matcher coverage.


## Tara star display override

User-approved presentation rule: Tara 4 (Kshema) and 8 (Mitra) use one green star (#1E9E3A). Tara 2/6/9 retain two green stars; 7 one red (#D32F2F); 1/3/5 one yellow (#E0A800). This overrides the prior simplified display mapping, not the detailed Tara evaluation or candidate formulas. Tara.getThumbRuleGrade adds GOOD for 4/8; shared calendar renderer formats one green star. Actual Swing-renderer unit test checks all nine numbers, counts and colors with selection both on and off. No changes to Chandra display.


## Selected-window Panchang details

Product rule: the panel below the Balam Calendar displays explicit Start and End labels and replaces the personalized Balam/candidate tables with two plain lines: Thithi (including Paksham), then Varam. Ordered changes use arrows; consecutive duplicates are suppressed. Start/End times appear only in the existing header, not repeated per segment. Prathamai, Ashtami, Navami and Amavasya are red in either Paksham; this is a user-specified warning, not a universal classical rejection rule.

Implementation: inputs are the selected absolute start/end instants and event-place timezone. Normalize Moon minus Sun longitude into [0,360); the zero-based tithi index is floor(elongation / 12). Indices 0–14 are Shukla, 15–29 Krishna; index 29 is Amavasya. Find transitions by hourly bracketing and millisecond bisection using the existing Swiss ephemeris helper. Return positive-duration half-open segments covering exactly the selected window, split additionally at local civil midnight. Varam follows the existing Panchang civil weekday convention, not a newly introduced sunrise rule. All dates use the selected event timezone.

Edge cases and tests: one/multiple tithis, both Paksha transitions, transition at start/end, all warning indices, empty/reversed windows, DST midnight, and real-ephemeris interval coverage with no gaps. Expected output is ordered, contiguous, clipped segments with Paksham, Thithi, Varam and warning status.


### Window-detail layering
Core MyMuhurta orchestrates the selected absolute start/end window and event-place timezone, invoking BalamWindowEngine. BalamWindowDetails is an immutable bean snapshot (defensive Date/TimeZone copies and unmodifiable entry list); it performs no domain calculations. The Swing controller snapshots selection inputs and calls Core through SwingWorker. Selection/context refresh and panel removal invalidate/cancel prior requests; stale completions cannot replace the current result. The renderer accepts only completed results, formats the Core-provided weekday, deduplicates consecutive display values, and adds HTML/colors. Core retains all existing phase formulas, clipping and midnight conventions. Tests cover bean immutability, actual rendered HTML, Core window orchestration, source-level background/stale-result safeguards, and existing boundary/ephemeris cases. No new astrology rule is introduced.


### Event time versus daily Panchang time
Core preserves the composed event instant before constructing Panchang. The main table Date/Time use that event instant in the selected event-place timezone, not Panchang's mutable daily-reference Calendar. Inputs: selected event date, selected event clock time and place timezone. Composition copies local Y/M/D from date and H/M/S from time with milliseconds zeroed. Expected: editing event time changes the table; Calendar Update shows the exact selected window start (to seconds). Tests cover two different clock times on one date, next-date window-start update and defensive result copies. Daily Panchang calculations remain unchanged.
