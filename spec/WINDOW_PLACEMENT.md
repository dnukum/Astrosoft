# Main Window Placement Specification

Reference: Desktop display management specification for `Astrosoft`.

## Objectives and Product Behavior (`WindowPlacement.java`)
- **First Launch or Changed Topology**: Opens near the upper-left of the startup monitor usable area (offset by 20px margins), sized proportionally to the screen, rather than centered or automatically maximized.
- **Subsequent Launches**: Restores the last normal outer bounds and explicitly selected maximized state, provided the display topology signature is unchanged and the saved bounds fit within the current monitor's usable bounds.
- **Non-Interference**: Pure presentation layer policy; domain astrology calculations and view layouts remain untouched.

## Sizing and Coordinate Formulas
Let $W$ and $H$ be the width and height of the startup monitor's usable rectangle (accounting for OS taskbars and menu bars via screen insets).
All calculations are in logical pixels:

$$\text{width} = \min(\max(1, \text{round}(0.9 \times W)), 1400, W)$$
$$\text{height} = \min(\max(1, \text{round}(0.9 \times H)), 900, H)$$
$$x = \text{usable.x} + \min(20, W - \text{width})$$
$$y = \text{usable.y} + \min(20, H - \text{height})$$

This ensures a comfortable, bounded window ($1400 \times 900$ maximum) that never clips on smaller screens or laptop displays.

## Display Signature and State Validation
- **Display Signature**: A composite string encoding all screen device IDs, usable origins, dimensions, and affine scaling transforms, alongside the primary monitor device ID.
- **Topology Changes**: Any change to monitor resolution, arrangement, scaling, or disconnection invalidates saved placement and reverts safely to `defaults(usable)`.
- **Maximized vs. Normal Bounds**: Normal window dimensions are tracked separately from the maximized state; minimized/iconified bounds are never persisted as normal bounds.

## Persistence (`java.util.prefs.Preferences`)
- Stored under user package node `AstrosoftPref.class.node("MainWindow")`.
- **Debounced Writes**: Changes to window bounds or state during user movement/resizing are debounced with a 500 ms swing timer to avoid excessive I/O.
- **Application Exit Hook**: Window bounds are explicitly persisted on window closing / application exit.
- Preference storage failures log warnings and fall back gracefully without blocking application startup.

## Tests (`WindowPlacementTest.java`)
- Sizing on large external displays ($1940 \times 1080 \to 1400 \times 900$ max cap).
- Sizing on standard laptop usable areas ($1440 \times 835 \to 1296 \times 752$).
- Persistence and restoration on identical display signatures.
- Reversion to defaults on changed display topology or disconnected screens.
- Tiny and negative-origin multi-monitor topologies.
