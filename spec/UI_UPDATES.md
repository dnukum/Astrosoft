# UI Updates & Component Enhancements

Reference: Specification for cross-cutting UI components, main Horoscope screen enhancements, chart tooltips, and window management in Astrosoft.

---

## 1. Main Window Placement & Startup Geometry (`WindowPlacement.java`)

Manages top-level desktop window geometry across single- and multi-monitor setups.

### 1.1 Placement & Sizing Policy
- **First Launch or Topology Change**: Opens near the upper-left of the primary monitor's usable area (offset by 20px margin), sized proportionally to the display rather than centered or auto-maximized.
- **Subsequent Launches**: Restores the last normal outer bounds and maximized state, provided the monitor topology is identical and the saved rectangle fits within the current display's usable area.

### 1.2 Coordinate & Dimension Formulas
Let $W$ and $H$ represent the width and height of the startup monitor's usable rectangle (derived by subtracting OS taskbar and menu bar insets from screen bounds).
All calculations are in logical pixels:

$$\text{width} = \min(\max(1, \text{round}(0.9 \times W)), 1400, W)$$
$$\text{height} = \min(\max(1, \text{round}(0.9 \times H)), 900, H)$$
$$x = \text{usable.x} + \min(20, W - \text{width})$$
$$y = \text{usable.y} + \min(20, H - \text{height})$$

This produces a bounded layout (maximum $1400 \times 900$) that never clips on smaller screens or laptop displays.

### 1.3 Display Signature & Topology Detection
- **Signature**: A composite string encoding monitor device IDs, origins, usable dimensions, affine scale transforms, and the primary screen identifier.
- **Topology Invalidation**: Disconnecting monitors, changing resolutions, or moving between displays invalidates the signature and safely resets the window to defaults.

### 1.4 Persistence (`Preferences`)
- Stored under `AstrosoftPref.class.node("MainWindow")`.
- Resizing and movement events are debounced with a 500 ms swing timer to eliminate unnecessary disk I/O.
- Coordinates are also explicitly flushed during application shutdown.

---

## 2. Main Screen (Horoscope View) On-Demand Controls (`AstroSoft.java`)

Enhancements to the primary horoscope view (`createChartView`) providing on-demand controls for detailed astrological analysis without cluttering the initial display.

```
+-----------------------------------+---------------------------------------------------------+
|  Left Panel (BorderLayout.NORTH)  |  Right Container (Scrollable)                           |
|                                   |  +---------------------------+-----------------------+  |
|  [Birth Info (InfoView)]          |  | Rasi Chart (360x320)      | Navamsa Chart(360x320)|  |
|  -------------------------------  |  +---------------------------+-----------------------+  |
|  [x] Planet Offsets  [ ] AshtaVarga|                                                         |
|  -------------------------------  |  (When AshtaVarga Checked)                              |
|  (When Planet Offsets Checked)    |  +---------------------------------------------------+  |
|  [Relative Planet Offsets Table]  |  | AshtaVarga Tabbed Pane (Embedded below charts)    |  |
|  (Includes Astangata Combustion)  |  | - Bhinna, Sarva, Samudaya, Prasthara tabs         |  |
|                                   |  +---------------------------------------------------+  |
+-----------------------------------+---------------------------------------------------------+
```

### 2.1 On-Demand Controls Strip
Situated immediately below the birth info block (`InfoView`), styled with `DARK_BLUE` typography (Verdana 11pt):
- **"Planet Offsets" Checkbox** (Selected by default):
  - Toggles the display of `RelativePlanetView` below the birth info.
  - When unchecked, hides the relative positions table, keeping the left panel compact.
- **"AshtaVarga" Checkbox** (Unselected by default):
  - Toggles the embedding of `AshtavargaView.createTabbedPane(h.getAshtaVarga())` directly beneath the Rasi and Navamsa charts in the right container.
  - Dynamically resizes the right container from 320px to 725px height and enables vertical scrolling on demand (`VERTICAL_SCROLLBAR_AS_NEEDED`).

### 2.2 Vertical Layout Optimization
- To prevent awkward vertical stretching on tall monitors, the left panel is wrapped in a `BorderLayout.NORTH` container (`leftWrapper`).
- Birth info, controls strip, and planet offsets remain compactly anchored at the top regardless of window resizing or right-panel expansion.

---

## 3. Planetary Combustion / Astangata Evaluation (`PlanetaryInfo.java`, `RelativePlanetView.java`)

Calculates planetary combustion (Astangata) based on angular proximity to the Sun, using the classical orbs specified by Dr. B.V. Raman (*Muhurtha / Astrological Magazine*).

### 3.1 Combustion Orbs Matrix
A planet is classified as combust (*Astangata*) if its angular distance $|\lambda_{\text{planet}} - \lambda_{\text{Sun}}| \pmod{360} \le \theta_{\text{combust}}$:

| Planet | Direct Motion Orb ($\theta$) | Retrograde Motion Orb ($\theta$) |
| :--- | :---: | :---: |
| **Moon** | $12^\circ$ | — |
| **Mars** | $17^\circ$ | $17^\circ$ |
| **Mercury** | $14^\circ$ | $12^\circ$ |
| **Jupiter** | $11^\circ$ | $11^\circ$ |
| **Venus** | $10^\circ$ | $8^\circ$ |
| **Saturn** | $15^\circ$ | $15^\circ$ |
| **Rahu / Ketu** | Non-combust | Non-combust |

### 3.2 UI Presentation
- In `RelativePlanetView`, combust planets are annotated with combustion tags/indicators alongside house offsets, enabling immediate visual detection of Astangata dosha.

---

## 4. Chart Planetary Longitude & Hover Tooltips (`Chart.java`)

Enhances all astrological charts across the application (natal charts, divisional Varga charts, and My Muhurta transit charts) with detailed hover metadata.

### 4.1 Display Format
When hovering over an occupied planet glyph cell in any chart:
$$\text{Orb } \text{DDD:MM}, \text{DDD:MM}, \langle\text{Nakshatra}\rangle$$
Example outputs:
- `123:45, Magam`
- `Orb 004:12, 123:45, Magam`

### 4.2 Formatting & Normalization Rules
- **Absolute Longitude**: Always displays original absolute sidereal longitude $[0^\circ, 360^\circ)$, never sign-relative degrees ($[0^\circ, 30^\circ)$) or divisional chart remapping.
- **Normalization**:
  $$L_{\text{norm}} = ((L \pmod{360}) + 360) \pmod{360}$$
  $$\text{totalMinutes} = \lfloor 60 \times L_{\text{norm}} \rfloor$$
  $$\text{Degrees} = \lfloor \text{totalMinutes} / 60 \rfloor \quad (\%03d), \quad \text{Minutes} = \text{totalMinutes} \pmod{60} \quad (\%02d)$$
- **Seconds Truncation**: Seconds are truncated without rounding to prevent artificial minute carryover (e.g. $12.999^\circ \to \text{012:59}$, not $013:00$).
- **Context Preservation**: Hovering over empty spaces within a house cell displays the house tooltip (e.g., `<b>Mesha</b> (0° - 30°) <br>House: 1`), keeping house boundary information accessible.

---

## 5. Verification & Test Suite

| Test Class | Component Verified |
| :--- | :--- |
| `WindowPlacementTest` | 9 geometry checks: multi-monitor scaling, external screen cap ($1400 \times 900$), laptop usable bounds ($1296 \times 752$), topology changes, and negative origins. |
| `ChartLongitudeTooltipTest` | Absolute degree preservation (`123.75` $\to$ `123:45`), 360 wrap (`000:00`), negative wrap (`-0.001` $\to$ `359:59`), seconds truncation, and non-finite handling (`NaN` $\to$ `null`). |
| `ChartPlanetDetailsTooltipTest` | Integration test verifying JTable cell hover tooltip assembly with longitude, orb, and Nakshatra. |
