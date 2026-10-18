# Abhijit, Brahma and Balam Timing Windows

Reference: Computational specification (2026-10-06). Seasonal 15-part daylight/night division based on local sunrise and sunset, rather than a fixed 48-minute approximation.

## Core Definitions and Mathematical Formulas
Day duration $D = \text{sunset}_d - \text{sunrise}_d$.  
Night duration $R = \text{sunrise}_d - \text{sunset}_{d-1}$.

1. **Abhijit Muhurta**: The 8th of 15 equal divisions of the daytime period:
   $$\text{Abhijit} = \left[\text{sunrise}_d + \text{round}\left(\frac{7D}{15}\right), \text{sunrise}_d + \text{round}\left(\frac{8D}{15}\right)\right)$$
2. **Brahma Muhurta**: The 14th of 15 equal divisions of the preceding night period, ending one muhurta before sunrise:
   $$\text{Brahma} = \left[\text{sunrise}_d - \text{round}\left(\frac{2R}{15}\right), \text{sunrise}_d - \text{round}\left(\frac{R}{15}\right)\right)$$
3. **Subha Hora**: Auspicious time blocks per weekday, scaled to actual day length $D$ (for daytime blocks) and night length $R$ (for night blocks), based on `WeekDay.auspiciousTimeBlocks()`.

## Engine Implementation (`MuhurtaWindowsCalculator.java`)
- **Shared Source of Truth**: All calculation is consolidated in `MuhurtaWindowsCalculator.java`.
- **Solar Calculations**: Uses `com.web_tomorrow.utils.suntimes.SunTimes` with zenith at the selected `Place` (latitude/longitude), resolving UTC instants to the local civil date and handling DST transitions cleanly.
- **Fallbacks**: If solar ephemeris fails or coordinates are invalid, logs a warning and falls back to standard 06:00/18:00 local time, labelling the result with `[fallback solar time]`.
- **Wednesday Scrutiny**: On Wednesdays, Abhijit Muhurta coincides with Durmuhurtha under classical rules. The engine preserves the window but flags it with `caution=true`, formatted as `[Wednesday: additional scrutiny]`. It is not suppressed or automatically disqualified.
- **Window Clipping**: `Window.clip(start, end)` computes the intersection $[\max(s, \text{start}), \min(e, \text{end}))$. Empty intersections return null; partial overlaps are marked `partial=true`, formatted as `(partial overlap)`.

## Presentation Contexts and Consistency

### 1. Main Muhurta Table (`MyMuhurta.java`)
- Invokes `MuhurtaWindowsCalculator.forDay(timingDate, place)`.
- Presents the full timing of Abhijit Muhurta and Brahma Muhurta for the entire civil day of the selected event instant.
- Placed immediately following the "Auspicious Time" (`DisplayStrings.AUS_TIME_STR`) row.
- Formatted as `dd MMM yyyy, hh:mm a z – dd MMM yyyy, hh:mm a z` (e.g. `31 Mar 2026, 12:00 PM IST – 31 Mar 2026, 12:48 PM IST`), breaking at the en-dash in multi-line rendering.

### 2. Balam Calendar Window Details (`BalamCalendarPanel.java`)
- Invokes `MuhurtaWindowsCalculator.forWindow(start, end, tz, place)`.
- Evaluates intersections specifically against the half-open transit interval $[start, end)$ of the selected Balam Calendar row/cell.
- Rendered in the right-hand column of the two-column HTML details view for all three categories:
  - **Subha Hora**
  - **Abhijit Muhurta**
  - **Brahma Muhurta**
- If a timing window intersects the transit segment, its clipped interval is displayed (with `(partial overlap)` if bounded by the cell's transit boundary). If no intersection occurs within that specific transit interval, it displays `No overlap`.
- Both contexts consume the identical calculation engine; differences reflect the scope (whole-day horizon vs. selected transit window).

## Tests
- Equinox reference checks (11:36–12:24 Abhijit and 04:24–05:12 Brahma for 06:00–18:00 equinox).
- Seasonal scaling with varying day/night durations.
- Half-open full and partial intersections.
- Wednesday caution suffix and fallback solar time tags.
- Main table row ordering and multi-line wrap verification.
