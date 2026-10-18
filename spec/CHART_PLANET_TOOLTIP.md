# Planet Longitude and Chart Tooltips

Reference: Chart tooltip specification for astrological glyphs and houses.

## Specification and Formatting (`Chart.java`)
- **Display Format**: Absolute sidereal longitude in `DDD:MM` format (degrees and arcminutes) followed by Nakshatra name, e.g.:
  - `123:45, Magam`
  - If planet orb data is available: `Orb 004:12, 123:45, Magam`
- **Absolute vs. Sign-Relative**: Always uses original absolute sidereal longitude $[0^\circ, 360^\circ)$, never sign-relative degrees ($[0^\circ, 30^\circ)$) or divisional chart remapping.
- **Normalization and Truncation**:
  - $L_{\text{norm}} = ((L \pmod{360}) + 360) \pmod{360}$.
  - $\text{totalMinutes} = \lfloor 60 \times L_{\text{norm}} \rfloor$.
  - Degrees = $\lfloor \text{totalMinutes} / 60 \rfloor$, formatted as `%03d`.
  - Minutes = $\text{totalMinutes} \pmod{60}$, formatted as `%02d`.
  - Seconds are truncated without rounding to prevent artificial minute carryover (e.g. $12.999^\circ \to \text{012:59}$).
- **Fallback / Null Behavior**:
  - `Double.NaN` or null longitude yields null (omitted).
  - Missing longitude preserves Nakshatra-only tooltip; missing star displays longitude only; neither present yields no tooltip.
  - Hovering outside occupied planet cells preserves the existing house tooltip (e.g., `<b>Mesha</b> (0° - 30°) <br>House: 1`).

## Scopes
- Applies to all natal charts, Varga divisional charts, and My Muhurta transit charts.
- Planetary coordinates are encapsulated in `PlanetChartData` via defensive copies.

## Tests (`ChartLongitudeTooltipTest.java`)
- `123.75` $\to$ `123:45` (absolute degree preservation).
- `360.0` $\to$ `000:00` (wrap around).
- `-0.001` $\to$ `359:59` (negative wrap and minute floor).
- `12.999` $\to$ `012:59` (truncate seconds, no carry).
- `Double.NaN` $\to$ `null`.
