# Matching-Star Event Suitability and Balam Calendar Details

Reference: Event suitability rules approved from domain matrix (5 Oct 2026). Nine suitability categories in spreadsheet column order: General, Travel, Medication, Property, Trade, Study, Farming, Marriage, Construction. General applies strictly to Magha, Chitra, and Swati alongside specific activities. Empty rows stay empty.

## Core Rules and Mapping (`NakshatraEventSuitability.java`)
- **Inputs**: The set of matching stars from the selected Balam Calendar cell's matching stars (NOT the separate transit Nakshatra or a natal star).
- **Lookup**: $E(n) = \{ \text{event} \mid \text{checked for star } n \}$.
- **Empty Rows**: Any star with no checked events produces an empty event set, which formats as `<font color='red'>Not recommended</font>`.
- **Immutability**: Suitability entries are packaged into immutable `BalamWindowDetails.Suitability` beans attached to `BalamWindowDetails`.

## UI Integration and Presentation (`BalamCalendarPanel.java`)

### 1. Two-Column Layout
The details pane beneath the Balam Calendar grid is rendered using HTML in a responsive two-column table (57% / 43% split):

#### Left Column (57% width)
1. **Window Interval**:
   - `<b>Start:</b> dd MMM yyyy HH:mm z`
   - `<b>End:</b> dd MMM yyyy HH:mm z`
2. **Matching-Star Event Suitability**:
   - One line per matching star in enum order: `<b><Star>:</b> <Comma-separated events>`
   - Stars with no permitted activities display `<font color='red'>Not recommended</font>`.
   - Blank line following suitability entries.
3. **Transit Moon**:
   - `<b>Transit Moon:</b> <Nakshathra> in <Rasi> (<Lord>)`
   - Followed by a blank line.
4. **Panchanga Elements and Timed Transitions**:
   - **Thithi**: Transitions split at tithi boundaries with transition timestamps (`&rarr; (dd MMM, HH:mm z)`). Amavasya, Prathamai, Ashtami, Navami, and Riktha/warning thithis are highlighted in `<font color='red'>`.
   - **Varam**: Local weekday. Tuesday and Saturday are highlighted in `<font color='red'>`.
   - **Amirthathi Yoga**: Evaluated using sunrise-to-sunrise Vara (`SunriseVara`). `Marana` yoga is wrapped in `<font color='red'>Marana</font>`. In transitions, only the word `Marana` is red; arrows and timestamps remain standard font.
   - **Yoga (Nitya Yoga)**: Sourced from `PanchangaWindowEngine` transit intervals.
   - **Karana**: 60-slot half-tithi cyclic sequence.

#### Right Column (43% width)
- **Subha Hora**: Timing window intersections within $[start, end)$, or `No overlap`.
- **Abhijit Muhurta**: Timing window intersections within $[start, end)$, or `No overlap`. Flagged with `[Wednesday: additional scrutiny]` if falling on Wednesday.
- **Brahma Muhurta**: Timing window intersections within $[start, end)$, or `No overlap`.
- Clipped intervals are marked `(partial overlap)`.

### 2. Calendar Toggle Button and Controls (`MyMuhurtaView.java`)
- **Toggle Button**: "Show Balam Calendar" / "Hide Balam Calendar", styled with rounded corners (160x30 px).
- **Placement**: Situated in `chartControlStrip` immediately below the first (Muhurta) chart, centered within the chart's column width (`chartSize.width`).
- **Visibility**: Disabled and hidden until at least one person (Person 1 or Person 2) horoscope is configured and loaded (`hasPeople == true`).
- **Mounting**: Toggling expands/collapses the calendar inside `flyoutHost` directly under the chart scroll pane, auto-fitting the calendar dimensions.
- **Header Controls**: Month/year selectors with rolling +/-50 year span and synchronized navigation arrows; "Update" button on right.
- **Row Selection**: Clicking a row immediately applies its start instant to the Muhurta view (`applyCalendarDate`), updating the Muhurta chart and Panchanga table synchronously, and asynchronously queries window details with request cancellation to prevent stale display.

## Tests
- Comprehensive unit tests covering all 27 nakshatras against the nine suitability categories.
- Verification of `<font color='red'>Not recommended</font>` for empty rows.
- Defensive copying and immutability of `BalamWindowDetails`.
- Background worker cancellation and race condition prevention on fast row selection.
- Multi-line rendering and HTML escaping for all text fields.
