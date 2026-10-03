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
