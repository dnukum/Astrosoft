# Astrosoft Architecture Blueprint

This document serves as a high-level architectural blueprint for the Astrosoft application. It is designed to onboard new maintainers and serve as context for autonomous AI agents extending the codebase.

## 1. Architectural Overview

Astrosoft is a monolithic Java Swing desktop application structured into distinct architectural layers. It separates the presentation (UI) from the astrological business logic and mathematical calculations. 

At its core, Astrosoft relies on the **Swiss Ephemeris** (via a Java port) for high-precision astronomical calculations, wrapping these complex mathematical operations in a domain-driven design using Java `enums` and POJOs.

### Directory Structure
- `src/app/astrosoft/ui/` - Presentation Layer (Swing UI)
- `src/app/astrosoft/core/` - Core Business Logic (Domain Models)
- `src/app/astrosoft/beans/` - Data Transfer Objects / POJOs
- `src/app/astrosoft/consts/` - System Constants & Enums
- `src/app/astrosoft/util/` - Utilities & Ephemeris Adapters
- `src/app/astrosoft/export/` - Persistence & XML Serialization
- `src/app/astrosoft/xps/` - Expert System (Rule-based Analysis)
- `resources/` - Localization bundles (`.properties` files)
- `ephe123/` - Astronomical data files for the Swiss Ephemeris

## 2. Core Layers

### 2.1 Presentation Layer (UI)
The presentation layer is built exclusively with Java Swing.
- **Main Shell:** `AstroSoft.java` acts as the primary application frame and orchestrator, managing the lifecycle of the application and the main menu bar.
- **Views:** The application is divided into specialized view panels (e.g., `HoroscopeView`, `CompactibilityView`, `MuhurthaView`, `PanchangView`). Each view is responsible for rendering data from its corresponding core domain object.
- **Components:** Extensive use of custom table models (`AstrosoftTableModel`) and cell renderers (`LocalizedCellRenderer`) to display tabular data dynamically.
- **Localization:** UI strings are not hardcoded. They are loaded dynamically from `AstrosoftBundle_en.properties` and `AstrosoftBundle_ta.properties` using the `DisplayStrings` enum and `Internalization` utility.

### 2.2 Domain Business Logic
The `core` package contains the primary astrological orchestrators. These classes encapsulate the business rules for specific astrological functions:
- **`Horoscope`**: Generates birth charts, planetary longitudes, divisional charts (Vargas), and Vimshottari Dasa.
- **`Compactibility`**: Calculates marriage matching (Kutas) and Doshas between two `BirthData` instances.
- **`Muhurtha`**: Computes auspicious timings based on Tara Balam, Chandra Balam, and planetary transits.
- **`Panchang`**: Calculates daily Tithi, Nakshatra, Yoga, and time periods (Rahu Kalam, Yama Kandam).

### 2.3 Mathematical Engine & Utilities
- **Swiss Ephemeris Adapter:** The `swisseph` library performs the heavy lifting. The `SwissHelper` class acts as the adapter, translating Astrosoft's requests (e.g., "Where is the Moon at this Julian day?") into Swiss Ephemeris API calls.
- **Astrological Utilities:** `AstroUtil` provides helper methods for degree formatting, Julian day conversions, and mathematical modulo operations essential for astrology.

### 2.4 Data Models (Beans & Consts)
- **Beans:** `BirthData`, `Place`, `PlanetaryInfo`, and `Interval` act as the primary data carriers across the application layers.
- **Enums:** The application heavily leverages Java Enums (`Planet`, `Rasi`, `Nakshathra`) to strictly type astrological concepts. These enums often contain embedded logic (e.g., ruling planets, longitude bounds) rather than being simple constants.

### 2.5 Persistence & State
- **File Storage:** Horoscopes are serialized and deserialized to XML files (`.ash`) using `XMLHelper`.
- **User Preferences:** Stored persistently using the standard `java.util.prefs.Preferences` API (managed by `AstrosoftPref`), capturing the user's default location, Ayanamsa, and language.

## 3. Extensibility Points

Astrosoft's architecture provides several natural seams for extending functionality:

### 3.1 Expert System (XPS) Expansion
The `src/app/astrosoft/xps` package is designed as a rule-based expert system. 
- **Extension:** New Yogas, Doshas, or analytical rules (e.g., Shadbala analysis, predictive astrology) can be added here without modifying the core charting logic. By implementing new `YogaCombination` rules, the expert system can automatically scan birth charts for new astrological formations.

### 3.2 View Plug-ins
The UI is modularized into distinct `*View` panels.
- **Extension:** Adding a new feature (e.g., "Transit Analysis" or "Prashna/Horary Chart") simply requires creating a new `JPanel` view and registering it in the main `AstroSoft.java` sidebar/menu, isolated from existing views.

### 3.3 Ayanamsa & Calculation Strategies
The core logic relies on the `Ayanamsa` enum to offset planetary positions.
- **Extension:** Additional Ayanamsas (e.g., Fagan/Bradley, Pushya Paksha) can be introduced by simply adding them to the `Ayanamsa` enum and hooking them into the `SwissHelper` configuration, instantly propagating the new calculation standard across all charts.

### 3.4 Localization
The `DisplayStrings` enum and `.properties` bundles dictate the UI language.
- **Extension:** Adding a new language (e.g., Hindi, Telugu) requires zero code changes to the UI layer. It only requires dropping in a new `AstrosoftBundle_hi.properties` file and registering the language in the application preferences.

### 3.5 External Data Providers
Locations are currently fetched via the `PlaceFinder` which hooks into the OpenStreetMap Nominatim API.
- **Extension:** The `PlaceFinder` can easily be subclassed or interfaced to support fallback APIs (e.g., Google Maps API, offline SQLite databases) for geocoding, completely transparent to the UI layer.
