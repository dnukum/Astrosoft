# Specification: MyMuhurta Screen

## 1. Overview
The **MyMuhurta** screen is an interactive personalized astrological dashboard accessible via the menu (`View -> My Muhurta`). 
While the traditional `Panchang` view displays generalized daily planetary information (Tithi, Nakshatra, Auspicious Times), the `MyMuhurta` screen overlays this daily data with the **currently loaded user's natal profile** (or up to two individuals for event/marriage evaluation), calculating personalized compatibilities, displaying transit and natal charts side-by-side, and providing an inline transit calendar (Balam Calendar).

---

## 2. UI Layout & Components

The screen uses a two-column responsive split layout designed for standard and widescreen displays:

```
+------------------------------------+---------------------------------------------------+
|  Main Panchanga & Muhurta Table    |  Personal Horoscopes Selectors (Person 1 / 2)     |
|  - Date, Time (with TimeZone)      +---------------------------------------------------+
|  - Location                        |  Side-by-Side Charts (350x300 each):              |
|  - Sunrise, Sunset                 |  [ Muhurta Rasi ] [ Person 1 Rasi ] [ Person 2 ]  |
|  - Nakshatra, Thithi (Transitions) +---------------------------------------------------+
|  - Amirthathi Yoga, Nitya Yoga     |  [ Show/Hide Balam Calendar ] Button              |
|  - Auspicious Time (Subha Hora)    +---------------------------------------------------+
|  - Abhijit Muhurta & Brahma Muhurta|  Inline Balam Calendar Panel (When Open):         |
|  - Rahu Kala, Yama Kanda           |  - Navigation (< Prev, Month, Year, Next >)       |
|  - Tara Balam: Score (Name)        |  - Transit Table (Date, Tara, Chandra, Match)     |
|  - Chandra Balam Score             |  - Update Button (Applies window to Muhurta)      |
|                                    |  - Two-Column Window Details Panel:               |
|  [ Summarize ] [ Edit Date/Time ]  |    Left: Thithi, Varam, Amirthathi, Suitability   |
|                                    |    Right: Subha Hora, Abhijit, Brahma Muhurtas    |
+------------------------------------+---------------------------------------------------+
```

### 2.1 Main Panchanga & Muhurta Table (Left Column)
Anchored at the top left with dynamic row heights and multi-line cell rendering to prevent text clipping:
* **Date & Time:** Event instant displayed in the chosen location's timezone (e.g. `MMMM d, yyyy` and `hh:mm:ss a, <TimeZone> (GMT +/-HH:MM)`).
* **Location:** Displays selected city, state, country, or default place.
* **Sunrise & Sunset:** Calculated via Swiss Ephemeris for the local event date and coordinates.
* **Named Transitions:** For Nakshathra, Thithi, Nitya Yoga, and Karana, displays current value until transition time followed by the arrow (`→`) and the named successor (e.g., `Rohini until 10:41 PM → Mrigasira`).
* **Amirthathi Yoga:** Evaluates Amirthathi Yoga based on sunrise Vara and transit Moon Nakshatra, displaying transitions through the civil day.
* **Auspicious Time Windows:**
  * **Subha Hora:** Weekday auspicious blocks scaled to actual sunrise/sunset intervals.
  * **Abhijit Muhurta:** 8th of 15 daytime divisions. Flagged with `[Wednesday: additional scrutiny]` on Wednesdays due to classical Durmuhurta/Rahu afflictions.
  * **Brahma Muhurta:** 14th of 15 nighttime divisions preceding local sunrise.
* **Inauspicious Windows:** Rahu Kala and Yama Kanda.
* **Personalized Scores (when Profile Loaded):**
  * **Tara Balam:** Displays raw score (1–9) with classical Tara name in parentheses, e.g. `2 (Sampat)`, `6 (Sadhana)`.
  * **Chandra Balam:** Displays raw inclusive house count (1–12) from natal Moon to transit Moon.

### 2.2 Table Action Buttons
Located immediately below the main table:
1. **`[ Summarize ]` Button:** Opens the modal `MuhurtaSummaryDialog`, generating a structured plain-text report intended for easy copying and feeding to LLMs:
   * 1) Muhurta Date
   * 2) Muhurta Time
   * 3) Location with timezone
   * 4) Paksha and Thithi
   * 5) Vara
   * 6) Nakshatra (with Pada)
   * 7) Amirthathi Yoga
   * 8) Tara and Chandra Balam (with score, name, and evaluative rank for Person 1 and Person 2)
   * 9) Panchanga Yoga
   * 10) Karana
   * Clockwise Planetary Positions starting from Ascendant (one line per planet: `Planet, Zodiac Sign, Longitude within Sign (DD° MM' SS"), Nakshatra (Pada N)`).
   * Features a one-click **Copy to Clipboard** button with temporary confirmation feedback and `Escape`/`Enter` dismissal.
2. **`[ Edit Date/Time ]` Button:** Opens `MuhurtaConfigDialog` allowing immediate adjustment of:
   * Event Date (JCalendar combo picker)
   * Event Time (24-hour hour and minute spinners)
   * Location (`PlaceChooser` integration)
   * Refreshes the view and Balam calendar immediately upon OK without overwriting application global preferences.

### 2.3 Horoscopes & Multi-Chart Panel (Top Right)
* **Person Selectors:** Person 1 and Person 2 selector buttons open `MuhurtaProfileDialog` to load an existing `.ash` horoscope file or perform Quick Entry (specifying Name, Janma Rasi, and Janma Nakshatra). Clear buttons (`X`) remove individual profiles.
* **Side-by-Side Charts (`350x300`):**
  * **Muhurta Rasi Chart:** Transit D1 chart for the event instant and location.
  * **Person 1 Rasi Chart:** Natal chart for Person 1 (if loaded).
  * **Person 2 Rasi Chart:** Natal chart for Person 2 (if loaded).
* Charts maintain rigid cell sizing to prevent symbol truncation (`Su`, `Mo`, `As`, etc.) and support hover tooltips showing original sidereal longitude and Nakshatra.

### 2.4 Inline Balam Calendar (Bottom Right)
Toggled via the **Show/Hide Balam Calendar** button located below the charts:
* **Month Navigation:** `< Prev Month`, month/year dropdowns (synchronized rolling +/-50 year range), and `Next Month >`.
* **Table Columns:**
  * `Date`: Transit transition start date/time (e.g. `Oct 28, 10:41 PM`).
  * `Person 1 Tara`: Raw score and star rating (e.g. `6 (★★)`).
  * `Person 1 Chandra`: Raw house and star rating (e.g. `3 (★★★)`).
  * `Person 2 Tara` & `Person 2 Chandra`: Added dynamically if Person 2 is loaded.
  * `Match`: Matching stars between natal profiles and transit window.
* **`Update` Button:** Applies the selected calendar row's start date and time directly to the Muhurta event and main table. Chosen table row selection persists after the update.
* **Two-Column Details Panel:**
  * **Left Column:**
    * Start and End timestamps.
    * Matching Star Event Suitability (activities like Marriage, Vehicle Buying, Travel, etc., or "Not recommended" in red).
    * Transit Moon sign and Nakshatra lord.
    * **Thithi:** Warning thithis (Amavasya, Prathamai, Ashtami, Navami) styled in `<font color='red'>`. Transitions isolate red formatting to warning items only.
    * **Varam:** Tuesday and Saturday styled in `<font color='red'>`.
    * **Amirthathi Yoga:** Marana Yoga styled in `<font color='red'>`.
    * **Yoga (Nitya Yoga)** and **Karana**.
  * **Right Column:**
    * **Subha Hora:** Overlapping auspicious hours within the transit window.
    * **Abhijit Muhurta:** Overlapping midday window, or `No overlap` if window falls outside midday.
    * **Brahma Muhurta:** Overlapping pre-dawn window, or `No overlap`.

---

## 3. Data Flow & Architecture

The feature strictly adheres to the layered architecture outlined in `spec/ARCHITECTURE.md`:

```
+-----------------------------------------------------------------------------+
|                            PRESENTATION LAYER                               |
|  MyMuhurtaView  <--->  BalamCalendarPanel  <--->  MuhurtaSummaryDialog      |
|         |                      |                                            |
|  (User Input/Swing)      (SwingWorker)           (Displays text from core)  |
+---------|----------------------|--------------------------------------------+
          |                      |
          v                      v
+-----------------------------------------------------------------------------+
|                            DOMAIN CORE LAYER                                |
|  MyMuhurta.java                                                             |
|  ├── getTableData()                 --> TableData<MapTableRow>              |
|  ├── getSummaryText(...)            --> Plain text report for LLMs          |
|  ├── getBalamCalendarTable(...)     --> BalamCalendarModel / Engine         |
|  └── getBalamWindowDetails(...)     --> BalamWindowDetails                  |
|                                                                             |
|  Supporting Core Engines:                                                   |
|  ├── PanchangaWindowEngine          --> Exact boundary bisection            |
|  ├── MuhurtaWindowsCalculator       --> Abhijit, Brahma, Subha Hora windows |
|  └── NakshatraEventSuitability      --> Activity suitability mappings       |
+-----------------------------------------------------------------------------+
          |
          v
+-----------------------------------------------------------------------------+
|                      MATHEMATICAL & DATA LAYER                              |
|  SwissHelper / swisseph             --> Planetary physics & coordinates     |
|  Beans: MuhurtaProfile, BalamWindowDetails, TaraBalamResult, etc.           |
|  Enums: Nakshathra, Rasi, Planet, Tara, AmirthathiYoga, Thithi, Karana      |
+-----------------------------------------------------------------------------+
```

---

## 4. Testing & Verification

1. **Exhaustive Balam Domain Coverage (`MyMuhurtaTest.java`):**
   * All 729 combinations of birth and transit Nakshatras for Tara index, Paryaya, and rank.
   * All 144 combinations of birth and transit Rasi for Chandra house counting and Chandra Ashtama rejection.
2. **Panchanga Golden Validation (`PanchangGoldenTest.java`):**
   * Validated against Swiss Ephemeris golden data across 11 global coordinates and critical DST boundary dates.
3. **Table Layout & Multi-Line Wrapping (`MyMuhurtaTableLayoutTest.java`):**
   * Verifies table ordering, Yoga details, and automatic `<br>` injection for Time and Location strings.
4. **Summary Report Verification (`MuhurtaSummaryTest.java`):**
   * Asserts all 10 summary fields, clockwise planetary ordering starting with Ascendant, degree formatting, and Person 1/2 score integration.
5. **Window Details & Red Color-Coding (`BalamWindowDetailsTest.java`):**
   * Validates HTML generation, bean immutability, repeat suppression, and red color-coding for Tuesday/Saturday, warning thithis, and Marana yoga.
