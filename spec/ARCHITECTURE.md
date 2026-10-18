# Astrosoft Architecture Blueprint

This document serves as a high-level architectural blueprint for the Astrosoft application. It is designed to onboard new maintainers and serve as context for autonomous AI agents extending the codebase.

## 1. Architectural Overview

Astrosoft is a monolithic Java Swing desktop application structured into distinct architectural layers. It separates presentation (UI) from astrological business logic and mathematical calculations. 

At its core, Astrosoft relies on the **Swiss Ephemeris** (via a Java port) for high-precision astronomical calculations, wrapping these complex mathematical operations in a domain-driven design using Java `enums` and POJOs.

### Directory Structure
- `src/app/astrosoft/ui/` - Presentation Layer (Swing UI frames, views, components, and dialogs)
- `src/app/astrosoft/core/` - Core Business Logic & Astrological Orchestrators
- `src/app/astrosoft/beans/` - Data Transfer Objects & Immutable POJOs
- `src/app/astrosoft/consts/` - System Constants & Strongly-Typed Domain Enums
- `src/app/astrosoft/util/` - Utilities, Ephemeris Adapters, and Versioning
- `src/app/astrosoft/export/` - Persistence, PDF Generation & XML Serialization
- `src/app/astrosoft/xps/` - Expert System (Rule-based Analysis)
- `resources/` - Localization bundles (`.properties` files), icons, and static XML configurations
- `ephe123/` - Astronomical data files for the Swiss Ephemeris
- `spec/` - Feature specifications and architectural blueprints

## 2. Core Layers

### 2.1 Presentation Layer (UI)
The presentation layer is built exclusively with Java Swing.
- **Main Shell:** `AstroSoft.java` acts as the primary application frame and orchestrator, managing the application lifecycle, menu bars, toolbars, and global view transitions.
- **Views:** The application is modularized into specialized view panels implementing or managed by `ViewManager`:
  - `HoroscopeView`: Rasi, Navamsa, and divisional charts.
  - `CompactibilityView`: Marriage compatibility analysis.
  - `MuhurthaView`: Classical electional astrology timings.
  - `MyMuhurtaView`: Personalized daily dashboard combining Muhurta Rasi, Person 1/2 natal charts, dynamic Panchang table, inline Balam Calendar flyout, and configuration/summary dialogs.
  - `PanchangView`: Daily Panchang table and charts.
  - `EphemerisView`: Planetary daily/monthly ephemeris tables.
  - `AshtavargaView`, `ShadBalaView`, `BhavaView`, `FindNameView`.
- **Components & Renderers:** Custom table models (`AstrosoftTableModel`), cell renderers (`LocalizedCellRenderer`, `ColoredCellRenderer`, `PlanetCellRenderer`), and specialized panels (`BalamCalendarPanel`, `BirthDataPanel`, `PlaceChooser`).
- **Dialogs:** Modal dialogs for user workflows:
  - `MuhurtaConfigDialog`: In-place date, time, and location adjustment for Muhurta events.
  - `MuhurtaProfileDialog`: Quick entry or file selection for Person 1 / Person 2 in Muhurta evaluation.
  - `MuhurtaSummaryDialog`: Plaintext summary modal designed for easy copying and feeding to LLMs.
  - `AboutDialog`: Accessible via `Help -> About AstroSoft`, displaying application name, description, build timestamp version, and Java runtime version.
  - `PrintDialog`: Multi-format report printing options.
  - `BirthInputDialog`, `CompactibilityInputDialog`, `PlaceOptionDialog`.
- **Window State Persistence:** Managed by `WindowPlacement.java`, persisting normal window bounds and maximized state across sessions in Java user preferences.
- **Localization:** 
  - Standard table keys and planetary concepts are localized through `DisplayStrings` and loaded dynamically from `AstrosoftBundle_en.properties` and `AstrosoftBundle_ta.properties`.
  - *Practical Note:* Dialog headers, top-level menus, buttons, and newer configuration panels currently use English string literals; comprehensive localization across all UI controls is slated for a future unified pass.

### 2.2 Domain Business Logic
The `core` package contains the primary astrological orchestrators encapsulating astrological computation and rules:
- **`Horoscope`**: Generates birth charts, planetary longitudes, divisional charts (Vargas), and Vimshottari Dasa.
- **`Compactibility`**: Calculates marriage matching (Kutas) and Doshas between two `BirthData` instances.
- **`Muhurtha`**: Computes auspicious timings based on Tara Balam, Chandra Balam, and planetary transits.
- **`MyMuhurta`**: Orchestrator for personalized Muhurta evaluation. Computes Tara Balam (index, Paryaya, grade), Chandra Balam (house count and rank), dynamic Panchanga transitions, Amirthathi Yoga, and exports structured plain-text summaries (`getSummaryText`).
- **`Panchang`**: Calculates daily Tithi, Nakshatra, Yoga, Karana, and time periods (Rahu Kalam, Yama Kandam).
- **`PanchangaWindowEngine`**: Bisects planetary time windows down to millisecond precision at Nakshatra, Tithi, Karana, Yoga, civil midnight, and sunrise boundaries.
- **`MuhurtaWindowsCalculator`**: Calculates seasonal 1/15th daytime/nighttime divisions for **Abhijit Muhurta**, **Brahma Muhurta**, and scaled **Subha Hora**, with caution flags for Wednesday and polar fallback handling.
- **`BalamCalendarEngine` & `BalamCalendarModel`**: Generates full-month transit calendars mapping matching stars and multi-person Tara/Chandra scores.
- **`NakshatraEventSuitability`**: Domain mapping of Nakshatras to auspicious life activities (marriage, vehicle purchase, travel, etc.).

### 2.3 Mathematical Engine & Utilities
- **Swiss Ephemeris Adapter:** `swisseph` library performs celestial physics calculations. `SwissHelper` adapts requests into Swiss Ephemeris API calls (sidereal longitudes, house cusps, planetary speeds).
- **Astrological Utilities:** `AstroUtil` provides helper methods for DMS degree formatting, time conversions, Julian day arithmetic, and mathematical modulo operations.
- **Build Versioning:** `Version.java` dynamically retrieves the build version from `/resources/version.properties` or the JAR `MANIFEST.MF` `Implementation-Version`.

### 2.4 Data Models (Beans & Consts)
- **Beans:** Clean, immutable POJOs carrying state across layers:
  - `BirthData`, `Place`, `PlanetaryInfo`, `Interval`.
  - `MuhurtaProfile`: Represents a person via full `Horoscope` or quick-entry Janma Rasi/Nakshatra.
  - `BalamWindowDetails`: Immutable snapshot of transit window segments, suitabilities, and timing windows.
  - `TaraBalamResult`, `ChandraBalamResult`, `BalamRowBean`, `BalamDateCell`.
- **Enums:** Strongly typed domain concepts with embedded astrological logic:
  - `Planet`, `Rasi`, `Nakshathra`, `Tara`, `AmirthathiYoga`, `Thithi`, `Paksha`, `Karana`, `Yoga`, `WeekDay`, `ChandraHouse`, `BalamRank`.

### 2.5 Persistence & State
- **File Storage:** Horoscopes serialized to XML files (`.ash`) via `XMLHelper`.
- **User Preferences:** Stored persistently via `java.util.prefs.Preferences` (`AstrosoftPref`), capturing default location, Ayanamsa, language, and window placement (`WindowPlacement`).

### 2.6 Build & Versioning Pipeline
- **Ant Build (`build.xml`)**:
  - `compile`: Compiles sources with JDK 8 target.
  - `generate-version`: Invokes Ant `<tstamp>` to generate a 24-hour timestamp in `yyyyMMdd:HHmmss` format and writes `version.properties` to `resources/` and `deploy/resources/`.
  - `deploy`: Packages `bin/AstroSoft.jar` embedding `Implementation-Version: ${build.version}` and `Create-Date` in `MANIFEST.MF`.

## 3. Extensibility Points

### 3.1 Expert System (XPS) Expansion
The `src/app/astrosoft/xps` package provides a rule-based expert system for detecting Yogas and Doshas without altering charting logic.

### 3.2 View Plug-ins
New UI views extend `JPanel` and register with `ViewManager` and `AstroSoft.java` actions, completely isolated from existing view code.

### 3.3 Ayanamsa & Calculation Strategies
Additional Ayanamsas can be added to the `Ayanamsa` enum and configured in `SwissHelper`, propagating instantly across all calculations.

### 3.4 Localization
`DisplayStrings` and `.properties` bundles dictate UI table keys and astronomical terms. A future unified sweep will extend this to all dialogs, menus, and buttons.

### 3.5 External Data Providers
Geocoding in `PlaceFinder` hooks into Nominatim and can be extended with offline databases or alternative APIs transparently to the UI layer.
