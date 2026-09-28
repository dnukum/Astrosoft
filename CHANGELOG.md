# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased] - 2026-09-28

### Added
- **Global Geocoding:** Integrated a robust geocoding search to resolve exact latitude, longitude, and timezone configurations for cities worldwide.
- **DST Indicators:** The application now actively evaluates historical Daylight Saving Time rules based on the specific Date of Birth, appending a `(DST)` tag to the Time of Birth field and Horoscope window title when applicable.
- **Coordinate Display:** Added a new "Lat/Long." row to the main Horoscope Info table and Compactibility tables, formatting coordinates elegantly (e.g., `47N36  122W12  (GMT-07)`).
- **Planetary Sign Indicators:** The "Rasi Chart Planet Offsets" table now explicitly includes the localized Sign (Rasi) name in parentheses next to the longitude orb.

### Fixed
- **Historical DST Bug:** Fixed a critical bug where birth-time-to-GMT conversions evaluated daylight saving time based on the *current* date rather than the historical birth date.
- **XML Serialization Data Loss:** Fixed a long-standing `.ash` save bug where `<State>` and `<Country>` tags were silently dropped, resolving issues where locations would truncate to just the City upon reload.
- **Location Rendering:** Corrected `BirthData` to display the fully formatted location string (City, State, Country) in charts instead of unexpectedly truncating to just the city name.
- **Legacy TimeZone Parsing:** Legacy `.ash` files containing malformed or suffixed TimeZone IDs (e.g., `America/Los_Angeles (GMT-07:00)`) are now safely parsed defensively.
- **Coordinate UI:** Restored missing minutes and direction dropdowns for latitude and longitude fields in the Birth Details dialog.
- **Negative Formats:** Prevented double negative signs (`--`) from appearing in formatted TimeZone offset strings.
- **Time Periods:** Corrected Rahu Kalam and Yama Kandam calculations to properly respect local time.

### Improved
- **UI Layouts & Scaling:**
  - Expanded the `InfoView` table width to 800px to prevent long data strings (such as Dasas) from clipping.
  - Constrained the Place dropdown width to prevent exceptionally long location names from pushing the search button off the screen.
  - Overhauled the main chart layout to display relative planet offsets and retrograde visibility much more cleanly.
- **Quality of Life:**
  - Coordinate numbers in text fields are now correctly zero-padded.
  - Enabled native Mac OS keyboard shortcuts (Command+C, Command+V) inside application text fields.

