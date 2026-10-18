# My Muhurta Screen Specification

Reference: Unified design and computational specification for the **My Muhurta** feature in Astrosoft. Consolidates screen presentation, timing windows, Amirthathi Yoga, Balam Calendar, and matching-star suitability.

---

## 1. Screen Architecture & Visual Layout

The My Muhurta screen (`MyMuhurtaView.java`) presents an integrated dashboard pairing the event's Panchanga parameters with transit charts and an optional personal auspiciousness calendar.

```
+------------------------------------+---------------------------------------------------------------+
|  Panchanga Table (WEST)            |  Multi-Chart Dashboard (CENTER)                               |
|                                    |  +---------------------------------------------------------+  |
|  - Date, Time, Location            |  | Person 1 Toolbar | Person 2 Toolbar                     |  |
|  - Sunrise, Sunset, Length of Day  |  +---------------------------------------------------------+  |
|  - Day, Tithi, Nakshatra           |  | [Muhurta Rasi]   | [Person 1 Rasi]   | [Person 2 Rasi]  |  |
|  - Yoga, Amirthathi Yoga, Karana   |  +---------------------------------------------------------+  |
|  - Auspicious Time                 |  | [Show Balam Calendar] (aligned below Muhurta chart)     |  |
|  - Abhijit Muhurta, Brahma Muhurta |  +---------------------------------------------------------+  |
|  - Rahu Kalam, Yama Kandam         |                                                               |
|  - Subha Hora                      |  (When Expanded)                                              |
|  - Tara Balam, Chandra Balam       |  +---------------------------------------------------------+  |
|                                    |  | Balam Calendar Grid (Month/Year controls, Update)       |  |
|  Action Buttons (Bottom-Right):    |  +---------------------------------------------------------+  |
|  [Summarize]  [Edit Date/Time]     |  | Two-Column HTML Window Details (57% / 43% split)        |  |
+------------------------------------+---------------------------------------------------------------+
```

### 1.1 Left Column: Panchanga Table & Action Buttons
- **Layout & Positioning**: Anchored in `BorderLayout.WEST`. To avoid awkward vertical stretching on large monitors, the content is wrapped in a top-aligned `BorderLayout.NORTH` container.
- **Fixed Column Widths**:
  - Key column (`AstrosoftTableColumn.Key`): 130px.
  - Value column (`AstrosoftTableColumn.Value`): 270px (total table width: 400px).
- **Row Hierarchy & Panchanga Elements**:
  - Displays event metadata (`Date`, `Time`, `Location`) followed by solar events (`Sunrise`, `Sunset`).
  - Separates civil day from astrological day: **`Day`** displays the civil weekday (midnight-to-midnight), immediately followed by **`Vara (Solar Day)`** displaying the Vedic sunrise-to-sunrise solar day transition formatted as `prevDay -> (sunrise time) currentDay` (e.g. `Wednesday -> (06:42 AM) Thursday`).
  - Followed by `Nakshathra`, `Thithi`, `Paksha`, `Yoga`, `Amirthathi Yoga`, `Karana`, auspicious timings (`Auspicious Time`, `Abhijit Muhurta`, `Brahma Muhurta`), and inauspicious periods (`Rahu Kalam`, `Yama Kandam`).
  - Displays personal scores (`Tara Balam`, `Chandra Balam`) when a profile is selected.
- **Multi-Line Wrapping**:
  - Value column utilizes a custom `TableCellRenderer` (`multiLineRenderer`) that dynamically calculates line breaks and sets row heights based on font metrics.
  - Breaks naturally on comma delimiters (e.g. `Time`, `Location`), en-dash separators (e.g. `Abhijit Muhurta`), or transition arrows (`→` / `->`).
- **Action Buttons (`createBottomPanel`)**:
  - **"Summarize"**: Opens `MuhurtaSummaryDialog` displaying a formatted plain-text summary suitable for copying or exporting.
  - **"Edit Date/Time"**: Opens `MuhurtaConfigDialog` allowing interactive changes to event date, time, and geographic location without leaving the screen.

### 1.2 Right Column: Multi-Chart Dashboard
- **Chart Selection**: Rasi charts only (360x320 px each). Navamsa charts are deliberately omitted to preserve visual clarity.
  - **Muhurta Chart**: Transit planetary positions and Ascendant computed for the exact event instant and location.
  - **Person 1 Rasi Chart**: Natal horoscope of the primary person (loaded via Profile dialog or file).
  - **Person 2 Rasi Chart**: Natal horoscope of partner/secondary person.
- **People Toolbars**: Mounted above the chart area. Contains load/edit/clear buttons for Person 1 and Person 2.
- **Calendar Toggle Button**:
  - Rounded button labeled "Show Balam Calendar" / "Hide Balam Calendar" (160x30 px).
  - Centered inside a control strip aligned directly with the first (Muhurta) chart column (`chartSize.width`).
  - Disabled and hidden when no personal profile is loaded; visible and enabled when Person 1 or Person 2 is present.

---

## 2. Core Calculations & Domain Engines

All mathematical and astrological calculations reside in `app.astrosoft.core.*`, completely isolated from Swing components.

### 2.1 Seasonal Timing Windows (`MuhurtaWindowsCalculator.java`)
Calculates seasonal daylight and nighttime muhurta divisions based on local sunrise and sunset instants, rather than a fixed 48-minute approximation.

Let $D = \text{sunset}_d - \text{sunrise}_d$ (daylight length) and $R = \text{sunrise}_d - \text{sunset}_{d-1}$ (preceding night length).

1. **Abhijit Muhurta**: The 8th of 15 equal daytime divisions:
   $$\text{Abhijit} = \left[\text{sunrise}_d + \text{round}\left(\frac{7D}{15}\right), \text{sunrise}_d + \text{round}\left(\frac{8D}{15}\right)\right)$$
   - *Wednesday Scrutiny*: On Wednesdays, Abhijit Muhurta coincides with Durmuhurtha under classical rules. The engine preserves the window but appends the note `[Wednesday: additional scrutiny]`. It is not suppressed.
2. **Brahma Muhurta**: The 14th of 15 equal night divisions, ending one muhurta before sunrise:
   $$\text{Brahma} = \left[\text{sunrise}_d - \text{round}\left(\frac{2R}{15}\right), \text{sunrise}_d - \text{round}\left(\frac{R}{15}\right)\right)$$
3. **Subha Hora**: Scaled auspicious daytime/nighttime blocks derived from `WeekDay.auspiciousTimeBlocks()`.
4. **Main Table vs. Balam Calendar Scopes**:
   - *Main Table*: Evaluates `forDay(...)` for the full civil day of the event instant. Placed immediately after "Auspicious Time".
   - *Balam Calendar Details*: Evaluates `forWindow(...)` for the selected cell's half-open transit window $[start, end)$. Intersecting intervals are clipped and marked `(partial overlap)`. If no intersection exists within that interval, it displays `No overlap`.

### 2.2 Amirthathi Yoga & Sunrise Vara (`AmirthathiYoga.java`, `SunriseVara.java`)
- **Civil Day vs. Vedic Solar Day (Vara)**:
  - *Civil Day* (`Day` row): Governed by standard midnight-to-midnight ($00:00 \to 00:00$) calendar boundaries.
  - *Vedic Solar Day* (`Vara (Solar Day)` row): Governed strictly by local sunrise-to-sunrise boundaries. Transitions from previous day's Vara to current day's Vara at local sunrise.
  - *Main Table Presentation*: Displayed as `prevDay -> (sunrise time) currentDay` (e.g. `Wednesday -> (06:42 AM) Thursday`).
- **Lookup Formula**: $Y(t) = T[V(t)][N(t)]$, where $N(t)$ is the Moon's transit Nakshatra and $V(t)$ is the **Sunrise Vara** (the weekday of the latest local sunrise at or before $t$, resolved via `SunriseVara`).
- **Table Dimensions**: 189 cells (7 weekdays $\times$ 27 stars) mapping to `Amirtha`, `Siddha`, `Marana`, or `Prabalarshta`.
- **Cyclic Progression**: Saturday wraps to Sunday; Revathi wraps to Ashwini.
- **Main Table Presentation**: Evaluated at event instant with transitions displayed through the next civil midnight (e.g. `Siddha → (15 Oct, 04:30 IST) Amirtha`).
- **Inauspicious Warning**: `Marana` yoga is displayed in red font (`<font color='red'>Marana</font>`). When transitions occur, only the keyword `Marana` is wrapped in red tags; transition arrows and timestamps remain in standard font.

### 2.3 Matching-Star Event Suitability (`NakshatraEventSuitability.java`)
Evaluates permitted activities for the selected Balam cell's matching stars (the intersection of personal Tara and Chandra balam candidates, distinct from the transit star).
- **Nine Categories**: General, Travel, Medication, Property, Trade, Study, Farming, Marriage, Construction.
- **Rule Source**: Domain mapping matrix. "General" applies strictly to Magha, Chitra, and Swati alongside specific activities.
- **Empty Set Behavior**: Stars with no recommended activities display `<font color='red'>Not recommended</font>`.

### 2.4 Personal Balams (`MyMuhurta.java`)
- **Tara Balam**: Count from natal star to transit star modulo 9. Main table displays raw score and name: `score (Name)`, e.g. `2 (Sampat)`.
- **Chandra Balam**: Count from natal Moon rasi to transit Moon rasi (1 to 12). Main table displays raw house index: `score`, e.g. `11`.

### 2.5 Panchanga Engine Transitions (`PanchangaWindowEngine.java`)
Subdivides arbitrary time windows into contiguous segments $[start, end)$ classified by sidereal positions:
- Hourly bracketing and millisecond bisection across Nakshatra, Tithi, Karana, Nitya Yoga, and civil midnight boundaries.
- Karana uses the continuous 60 half-tithi cyclic sequence rather than enum wrapping.

---

## 3. Balam Calendar & Window Details Presentation

### 3.1 Inline Calendar Panel (`BalamCalendarPanel.java`)
- Embedded directly below the charts inside `flyoutHost` when toggled.
- **Navigation Controls**: Non-editable month/year selectors with synchronized arrow buttons (rolling span $\pm 50$ years) in the top center/left panel.
- **Filter / Show All Toggle**:
  - Positioned in the top right control panel.
  - **Default State**: Label `Filter`, displaying all raw monthly transit segments.
  - **Filtered State**: Label `Show All`, filtering out inauspicious segments based on:
    - **Tara Balam = 7** (*Naidhana*) for Person 1 or Person 2.
    - **Chandra Balam = 8** (*Ashtama*) for Person 1 or Person 2.
  - Surviving segments remain intact with their exact original start and end timestamps.
  - **Stickiness**: Toggle state persists across month and year navigation controls (`Prev Month`, `Next Month`, and combo choices).
- **Row Selection**: Clicking a row immediately synchronizes the main screen via `applyCalendarDate(startInstant, endInstant)`, updating the Muhurta chart and Panchanga table synchronously, and triggers asynchronous loading of window details.

### 3.2 Two-Column Details View (50% / 50% Horizontal Split with Wrapping)

Divided by a subtle horizontal split pane with 50/50 resize weighting. Both panels enforce horizontal viewport width tracking so all text wraps cleanly without squeezing the opposite column.

#### Left Column (50% width)
1. **Window Boundaries**:
   - `<b>Start:</b> dd MMM yyyy, hh:mm a z`
   - `<b>End:</b> dd MMM yyyy, hh:mm a z`
   - `<b>Sunrise on <MMM d>:</b> hh:mm a z` (included immediately below `End` if and only if the time window straddles a local sunrise; omitted if the window does not straddle a sunrise).
2. **Matching-Star Event Suitability**:
   - `<b><Star>:</b> <Activities>` (or `<font color='red'>Not recommended</font>`)
   - Blank separator line.
3. **Transit Moon**:
   - `<b>Transit Moon:</b> <Nakshathra> in <Rasi> (<Lord>)`
   - Blank separator line.
4. **Panchanga & Transitions**:
   - **Thithi**: Timed transitions (`&rarr; (dd MMM, hh:mm a z)`). `Amavasya`, `Prathamai`, `Ashtami`, `Navami`, and Riktha thithis in red.
   - **Varam**: Local weekday. `Tuesday` and `Saturday` in red.
   - **Amirthathi Yoga**: Evaluated via sunrise Vara. `Marana` in red.
   - **Yoga (Nitya Yoga)** & **Karana**: Classified transition sequences (`&rarr; (dd MMM, hh:mm a z)`).

#### Right Column (50% width)
- **Subha Hora Selection & Update Action**:
  - Horizontal control row containing `<b>Subha Hora:</b>` label, interactive dropdown combo box (`horaCombo`), and the **Update** button.
  - The dropdown is populated with all overlapping Subha Horas in that transit window, formatted in 12-hour AM/PM time (`hh:mm a` or `dd MMM, hh:mm a`), plus an option for `Window Start (<time>)`.
  - The first Subha Hora is selected by default.
  - When the user clicks **Update**, the chosen Subha Hora start instant (or window start) is applied to the main Muhurta event time.
- **Available Subha Horas**:
  - Heading `<b>Available:</b>` followed by bulleted list of all individual Subha Hora time intervals overlapping the transit window formatted in 12-hour AM/PM time (`dd MMM yyyy, hh:mm a z`), or `Not Applicable` if none.
- **Abhijit Muhurta**: Intersecting intervals (with `[Wednesday: additional scrutiny]` if applicable) or `Not Applicable`.
- **Brahma Muhurta**: Intersecting intervals or `Not Applicable`.
- Partial intersections are labelled `(partial overlap)`.
- **Typography & Formatting Consistency**: Both panes use 13px base font (`SansSerif`) with generous vertical spacing and line-height (1.45 - 1.5). Standardized 12-hour AM/PM formatting (`hh:mm a` / `hh:mm a z`) is applied consistently across all panels (Balam Calendar dates, detail view boundaries, sunrises, timed transitions, Subha Hora choices, and timing blocks).

---

## 4. Dialogs & Utilities

### 4.1 Edit Date/Time/Place (`MuhurtaConfigDialog.java`)
- Modal dialog allowing instant adjustments to event date, time, and geographic location.
- Uses `PlaceChooser` with country/state/city auto-complete and custom coordinates.
- On confirmation, triggers `updateView()` to recompute the entire dashboard synchronously.

### 4.2 Profile Dialogs (`MuhurtaProfileDialog.java`)
- Supports loading saved horoscopes or quick manual entry of Janma Rasi and Nakshatra.
- Dynamically filters permitted Nakshatra options based on selected Rasi.

### 4.3 Plain-Text Summary (`MuhurtaSummaryDialog.java`, `MyMuhurta.getSummaryText`)
- Generates a cleanly structured plain-text report including:
  - Header with event instant, location, and coordinates.
  - Sunrise, sunset, and daylight duration.
  - Complete Panchanga parameters (with transition times).
  - Auspicious timing windows (Abhijit, Brahma, Subha Hora) and inauspicious periods (Rahu Kalam, Yama Kandam).
  - Personal compatibility analysis (Tara Balam, Chandra Balam) for Person 1 and Person 2.
- Clean MVC design: `MyMuhurta.getSummaryText(...)` handles formatting in domain core; `MuhurtaSummaryDialog` handles Swing display and clipboard copying.

---

## 5. Verification & Test Suite

| Test Class | Focus & Verification |
| :--- | :--- |
| `MyMuhurtaTest` | Core domain instantiation, table data structure, Panchang retrieval, and Tara/Chandra candidate matching. |
| `MuhurtaSummaryTest` | Plain-text summary output, header formatting, solar calculations, and multi-profile compatibility text. |
| `MuhurtaWindowsCalculatorTest` | Seasonal 15-part formulas, equinox timings, DST shifts, year rollover, Wednesday caution, and fallback solar times. |
| `AmirthathiYogaTest` | All 189 table cells, both boolean branches (378 ordered pairs), cyclic wrapping, and null handling. |
| `SunriseVaraTest` | Sunrise-to-sunrise Vara determination, midnight civil separation, Revathi wrap, and fallback handling. |
| `PanchangaWindowEngineTest` | Continuous bisection, Karana 60-slot cycle, transition boundary isolation, and DST handling. |
| `NakshatraEventSuitabilityTest` | All 27 nakshatras against 9 suitability categories, empty row red warning, and bean immutability. |
| `BalamCalendarFilterTest` | Phase 1 whole-row filtering on Tara 7 and Chandra 8 across single-person and partner profiles, row invariant counts, surviving segment timing preservation, and UI toggle button stickiness (Chennai Oct 2026). |
| `BalamWindowDetailsTest` | HTML two-column rendering, repeat suppression, Marana red font isolation, and worker thread safety. |
| `MyMuhurtaTableLayoutTest` | Key/Value column widths, value wrapping, en-dash line breaks, row order including `Vara (Solar Day)` immediately after `Day`. |
| `MyMuhurtaEventTimeTest` | Exact event instant preservation, defensive copies, and edit synchronization. |
| `MyMuhurtaTimezoneLabelTest` | Localized timezone strings across major global timezones and DST boundaries. |
