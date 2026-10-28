# Changelog

All notable changes to this project will be documented in this file.

## [2.0.0] - 2026-10-27

### Added
- **Chart Notes Subsystem:** Persistent rich-text dockable editor (`ChartNotesPanel`) attached to chart metadata with bold, italic, underline, bullets, numbers, smart enter, and atomic undo/redo (`NotesUndoManager`).
- **Spreadsheet Table Ingestion:** Seamless copy/paste preservation for Microsoft Excel, Google Sheets (`text/html`), and TSV tab-delimited plain-text tables into structured HTML notes with XML CDATA persistence.
- **Universal NotesOwner Abstraction:** Unified notes contract across Horoscope (`.ash`), My Muhurta (`.muh`), and Marriage Match / Compatibility (`.asc`) documents.
- **Direct Save & Save As:** Toolbar and menu support for direct save (`saveHoroscopeDirect()`, `saveMuhurtaDirect()`, `saveCompatibility()`) to overwrite open files directly with automatic `.bak` backup generation, or invoke Save As for new documents.
- **Distinct Marriage Print Options:** Separated report exporter types into "Marriage Match" (`Type.MarriageMatch` for full charts view) and "Marriage Compatibility" (`Type.Compatibility` for star-only matching) with customized default file names.
- **Notes in PDF Export:** Rendered notes as an appended page in exported PDF reports using Astrosoft's standard table header format with proper vertical clearance from running headers.
- **Dynamic Cross-Platform Accelerators:** Smart keyboard accelerator mapping resolving to `Command` on macOS and `Ctrl` on Windows/Linux (`KeyMaskHelper`), plus `ESC` key modal dialog dismissal.
- **High-DPI Vector Icons:** Replaced font-dependent Unicode characters with Java2D anti-aliased vector stars (`StarIcon`) and ASCII transit transitions, resolving missing glyph boxes (`□`) on Windows.

### Fixed
- **Compatibility Typo & Dual-Read Compatibility:** Modernized XML tag from `<Compactibility>` to `<Compatibility>`, while maintaining seamless dual-format reading and backward compatibility for older `.asc` files.
- **PDF Export Missing Templates:** Fixed an issue where modern `<Compatibility>` tags produced 15-byte corrupted PDFs by updating `compactibility2pdf.xsl` to match both `<Compatibility | Compactibility>`.
- **PDF Notes Header Overlap:** Resolved visual overlap where the Notes section title collided directly with the running page header by formatting it with `<fo:table width="500pt" margin-top="40">`.
- **Window Title Transitions:** Fixed an issue where switching to My Muhurta retained stale window titles (such as "Marriage Match") by implementing `updateMuhurtaTitle()` to dynamically display candidates or active file names.
- **Dirty State Matrix & Unsaved Prompts:** Added comprehensive dirty-tracking (`promptSaveIfModified`) and verified across all 225 pairwise view transitions.

---

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

