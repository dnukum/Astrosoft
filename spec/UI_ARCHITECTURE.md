# Astrosoft UI & Presentation Layer Architecture

**Document Version:** 1.0.0  
**Status:** Approved Architectural Standard  
**Target:** Astrosoft 2.0 (Java 21 LTS / Swing)  
**Primary Audience:** Core maintainers and autonomous AI agents modifying UI code.

---

## 1. Overview & Architectural Philosophy

Astrosoft's presentation layer is built exclusively with **Java Swing (Java 21 LTS)**. Unlike modern reactive web frameworks (e.g., React, Flutter) where state changes trigger automatic top-down re-renders across a unified virtual DOM, Swing is an **imperative, component-based desktop framework**.

In Astrosoft, the UI is divided into two fundamentally distinct operating environments:
1. **The Embedded View Container (`home` panel hierarchy)**: Panels mounted inside `AstroSoft`'s main split pane. Managed declaratively by `ViewManager`.
2. **Modeless Floating Companion Palettes (`JDialog` window peers)**: Detached top-level OS windows (`PlanetDetailsDialog`, `HouseDetailsDialog`, `AshtavargaDialog`, `TransitChartDialog`, `floatingNotesDialog`).

> [!IMPORTANT]
> **Swing Window Ownership vs. Event Propagation:**  
> Establishing a parent-child window relationship in Swing (`new JDialog(owner)`) only governs OS-level window mechanics (minimizing with owner, z-order stacking, and taskbar bundling). **It does NOT propagate domain events or preference updates.**  
> Whenever application preferences (Language, Ayanamsa, Rahu Node, Theme, Style) change, the orchestrator (`AstroSoft.java`) must explicitly push updates across **both** the embedded view tree and all active floating dialogs.

---

## 2. Window Tier Classification & Modality Rules

All windows in Astrosoft strictly belong to one of three tiers:

```
┌────────────────────────────────────────────────────────────────────────┐
│ Tier A: Main Application Shell (AstroSoft JFrame)                       │
│   • Single top-level application container                             │
│   • Global menu bar, action dispatchers, toolbars                      │
│   • Dynamic home container (JSplitPane holding ViewManager panels)     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
       ┌────────────────────────────┴───────────────────────────┐
       ▼                                                        ▼
┌────────────────────────────────────────┐   ┌─────────────────────────────────────────┐
│ Tier B: Modal Input & Alerts           │   │ Tier C: Modeless Companion Palettes     │
│ (`AstrosoftDialog` / APPLICATION_MODAL)│   │ (`JDialog` / MODELESS)                  │
│   • Birth Data Input / Place Options   │   │   • PlanetDetailsDialog                 │
│   • AboutDialog / OptionDialog         │   │   • HouseDetailsDialog                  │
│   • FileOps file chooser dialogs       │   │   • AshtavargaDialog                    │
│                                        │   │   • TransitChartDialog                  │
│   CONTRACTS:                           │   │   • floatingNotesDialog                 │
│   • ModalityType.APPLICATION_MODAL     │   │                                         │
│   • alwaysOnTop = true while active    │   │   CONTRACTS:                            │
│   • Must stay above all floating cards │   │   • alwaysOnTop = false strictly        │
│   • windowOpened elevation hooks       │   │   • Must NEVER occlude modal popups     │
└────────────────────────────────────────┘   └─────────────────────────────────────────┘
```

### Invariant Rules:
1. **No Raw JOptionPane Calls**: Prompts and alerts must route through `OptionDialog.showDialog(...)` which guarantees proper parent binding and topmost z-order.
2. **File Pickers**: Must use `FileOps.openFileDialog(...)` or `FileOps.saveFileDialog(...)` to guarantee that modal file choosers are elevated over floating cards.
3. **Modeless Palettes Keep `alwaysOnTop = false`**: Floating cards act as reference heads-up displays (HUDs). They must never have `alwaysOnTop=true`, ensuring modal popups naturally take precedence.

---

## 3. Scope & Lifetime Hierarchy

Floating and embedded UI components have three distinct lifetime scopes:

```mermaid
flowchart TD
    subgraph AppScope["1. Application Scope (JVM Lifetime)"]
        AS["AstroSoft Frame"]
        Pref["AstrosoftPref"]
        WP["WindowPlacement"]
        TD["TransitChartDialog"]
    end

    subgraph DocScope["2. Document / Horoscope Scope (Active h)"]
        H["Horoscope (h)"]
        HDash["HoroscopeDashboard"]
        PD["PlanetDetailsDialog"]
        HD["HouseDetailsDialog"]
        AD["AshtavargaDialog"]
        Notes["ChartNotesPanel (floatingNotesDialog)"]
    end

    subgraph ViewScope["3. View Container Scope (home panel)"]
        V1["HoroscopeDashboard (D1/D9)"]
        V2["VimDasaView"]
        V3["DivChartView"]
        V4["ShadBalaView / BhavaView"]
        U1["EphemerisView"]
        U2["PanchangView"]
        U3["MarriageMatchView"]
    end

    AS --> Pref
    AS --> H
    H --> DocScope
    AS --> ViewScope
    DocScope -. "Stays visible while browsing within horoscope" .-> ViewScope
    U1 -. "Temporarily suppresses (hide)" .-> DocScope
```

### Scope Summary Matrix

| Scope | Lifetime Trigger | Components | Lifecycle Management |
|---|---|---|---|
| **1. Application Scope** | Lifetime of application process | `AstroSoft`, `preferences`, `WindowPlacement`, `TransitChartDialog` | Survives opening and closing charts. Must refresh whenever global options (Language, Theme, Style, Ephemeris settings) change. |
| **2. Document / Horoscope Scope** | Lifetime of currently loaded `Horoscope (h)` | `HoroscopeDashboard`, `PlanetDetailsDialog`, `HouseDetailsDialog`, `AshtavargaDialog`, `floatingNotesDialog` | • **Teardown**: Completely disposed (`cleanupChartFloatingDialogs()`) on `File -> New`, `File -> Open`, or birth data edits.<br/>• **Suppression**: Auto-hidden (`setVisible(false)`) when entering non-chart utility screens (Ephemeris, Panchang, Marriage Match) and restored upon returning.<br/>• **Navigation**: Remains visible when switching between horoscope views (Dasa, Divisional, Shadbala). |
| **3. View Container Scope** | Lifetime of active panel in `home` | Embedded screens (`VimDasaView`, `DivChartView`, etc.) | Reconstructed or swapped by `ViewManager.showView()`. |

---

## 4. Preference & Domain Event Propagation Contract

When a user modifies runtime options via the `Options` menu, `AstroSoft.optionChanged(...)` is the central coordinator.

### The Two-Pronged Propagation Rule:
Whenever any preference changes:
1. **Prong 1: Embedded Views**: Must update the current document and rebuild the mounted view:
   - `viewManager.showView(...)`
   - `refreshAllChartsAndCachedViews()` (iterates child charts across `home`).
2. **Prong 2: Floating Palettes**: Must rebuild all active modeless dialogs while preserving their exact user-adjusted screen bounds:
   - `refreshChartFloatingDialogs()`

```java
// Central dispatcher in AstroSoft.java
public void optionChanged(Preference preference, Object value) {
    preferences.preferenceChanged(preference, value);
    switch (preference) {
        case Language:    optionLangChanged(((Language) value).name()); break;
        case Ayanamsa:    optionAyanamsaChanged((Ayanamsa) value); break;
        case RahuNode:    optionRahuNodeChanged((RahuNode) value); break;
        case ChartTheme:  optionThemeChanged((ChartTheme) value); break;
        case ChartStyle:  optionStyleChanged((ChartStyle) value); break;
    }
}
```

### Dialog Refresh Implementation Pattern:
```java
private void refreshChartFloatingDialogs() {
    // 1. Refresh Horoscope-scoped dialogs if a horoscope is loaded
    if (h != null) {
        PlanetDetailsDialog pd = PlanetDetailsDialog.getActiveDialog();
        Rectangle pdBounds = (pd != null && pd.isDisplayable() && pd.isVisible()) ? pd.getBounds() : null;
        if (pdBounds != null) {
            PlanetDetailsDialog.closeActiveDialog();
            PlanetDetailsDialog reopened = PlanetDetailsDialog.showDialog(this, h, () -> syncCheckbox(false));
            reopened.setBounds(pdBounds);
        }
        // Repeat for HouseDetailsDialog and AshtavargaDialog...
    }

    // 2. Refresh Application-scoped dialogs (independent of h)
    if (transitChartDialog != null && transitChartDialog.isDisplayable() && transitChartDialog.isVisible()) {
        Rectangle tdBounds = transitChartDialog.getBounds();
        Date instant = transitChartDialog.getSelectedInstant();
        Place place = transitChartDialog.getCurrentPlace();
        boolean expanded = transitChartDialog.isExpanded();

        transitChartDialog.dispose();
        transitChartDialog = new TransitChartDialog(this, instant, place);
        if (expanded) transitChartDialog.setExpanded(true);
        transitChartDialog.setBounds(tdBounds);
        transitChartDialog.setVisible(true);
    }
}
```

---

## 5. Localization & Dynamic Font Rendering Architecture

### 5.1 Resource Bundles & Domain Lookups
- Astrological constants (`Planet`, `Rasi`, `Nakshathra`, `Thithi`, `Yoga`, etc.) use strongly-typed enums whose `toString()` delegates to `Internalization.getString(key)`.
- Localization bundles live in `resources/AstrosoftBundle_en.properties` and `resources/AstrosoftBundle_ta.properties`.
- **Missing Translation Safeguard**: Any lookup failing in `Internalization.java` logs to `logs/missing_translations.log` for offline dictionary updates.

### 5.2 Dual-Script System Font Prioritization
Tamil and Sanskrit terms often appear side-by-side with Latin digits, English abbreviations, and degree glyphs (e.g., `12° Cp 35' (AK)`). Astrosoft resolves system fonts via `UIUtil.getTamilFontName()` using OS-aware dual-script prioritization:
* **macOS**: `Tamil Sangam MN` or `Tamil MN` (high-quality native dual-script fonts that render Latin numerals and Tamil ligatures cleanly without vertical displacement).
* **Windows**: `Nirmala UI` (native Microsoft Indic dual-script font introduced in Windows 8/10/11) with fallback to bundled `Noto Sans Tamil`.
* **Linux / Cross-Platform Fallback**: Bundled `Noto Sans Tamil` registered at runtime.

### 5.3 Rich-Text Notes Localization
In `ChartNotesPanel`, HTML notes rendering employs an explicit CSS fallback stack:
```css
font-family: Arial, 'Nirmala UI', 'Noto Sans Tamil', 'Tamil Sangam MN', Helvetica, sans-serif;
```
This guarantees that pasting or typing Tamil text into consultation notes renders without glyph tofu or font substitution errors on all supported OSes.

### 5.4 Swing HTML & Plain Multi-Line Text Policy
Swing's `BasicHTML` / `HTMLEditorKit` defaults to `Dialog` / `SansSerif` and ignores `JComponent.getFont()` inside `<table>`, `<td>`, or styled blocks unless explicitly configured. On Windows 11, `SansSerif` and `Verdana` lack Tamil glyph fallback, producing tofu boxes (`□□□`).

1. **No HTML for Plain Multi-Line Text**: Never use `<html>...<br>...</html>` or `<html><table>...` solely for line wrapping, centering, or line breaks on `JLabel` or `JTable` cell renderers. Use `UIUtil.createWrappingTextArea(font, fg)` (`JTextArea` with `setLineWrap(true)` / `setWrapStyleWord(true)`), `TitleLabel` (supports `\n`), or stacked `JLabel`s so text renders directly via `UIUtil.getFont(...)`.
2. **HTML Reserved for Rich Formatting**: Only use `<html>` when rich styling is genuinely required (mixed colors, bold/italic spans, structured cards, or rich notes).
3. **Mandatory Font Binding on Rich HTML**: Prefer `UIUtil.createHtmlLabel(html, font)` and `UIUtil.createHtmlPane(html, font)`, which automatically enable `UIUtil.enableHtmlDisplayProperties(comp)`. Any manual `<html>` component (`JLabel`, `JEditorPane`, `JTextPane`, tooltips) must call `UIUtil.enableHtmlDisplayProperties(comp)` and inject `UIUtil.getHtmlFontFamily()` into its CSS/style (never hardcode `sans-serif` or `Verdana`). `UIUtil.syncHtmlDefaultStyleSheet()` keeps `HTMLEditorKit`'s shared default stylesheet aligned with the active language.

---

## 6. 13 Legacy Screens Preservation Invariant

- **Zero Replacement Policy**: Modern UI enhancements must **never** replace, alias, hijack, or alter the 13 original full-screen legacy views. Modern features (`PlanetDetailsDialog`, `HouseDetailsDialog`, `AshtavargaDialog`, `TransitChartDialog`, `ChartNotesPanel`) are strictly modeless auxiliary inspection palettes (HUDs). They exist *in addition to*, never *in place of*, the legacy screens.
- **Explicit 13-Screen Enumeration & Routing**: Menu items and toolbar buttons for the 13 screens must always instantiate and mount the original full-screen legacy component into the main container (`home`):
  1. **Chart View** (`View.CHART_VIEW` &rarr; `HoroscopeDashboard`): `View -> Chart` / Toolbar `Chart`
  2. **Vimshottari Dasa** (`View.DASAS_VIEW` &rarr; `VimDasaView`): `View -> Dasa` / Toolbar `Dasa`
  3. **Planetary Position** (`View.PLANET_POS_VIEW` &rarr; `PlanetView`): `View -> Planetary Position` / Toolbar `Planet`
  4. **House Position** (`View.BHAVA_POS_VIEW` &rarr; `BhavaView`): `View -> House Position` / Toolbar `House`
  5. **Divisional Charts** (`View.DIV_CHART_VIEW` &rarr; `VargaChartView`): `View -> Divisional Charts` / Toolbar `Div Charts`
  6. **Ashtakavarga** (`View.ASHTAVARGA_VIEW` &rarr; `AshtavargaView` with 8 tabs & 24 charts): `View -> Ashtavarga` / Toolbar `Ashtavarga`
  7. **Shadbala** (`View.SHADBALA_VIEW` &rarr; `ShadBalaView` with 4 tabs): `View -> Shadbala` / Toolbar `Shadbala`
  8. **Yoga Combinations** (`View.YOGA_COMBINATIONS_VIEW` &rarr; `YogaCombinationsView`): `View -> Yoga Combinations`
  9. **Daily Ephemeris** (`View.EPHEMERIS_VIEW` &rarr; `EphemerisView`): `View -> Ephemeris` / Toolbar `Ephemeris`
  10. **Marriage Compatibility / Match** (`View.COMPATIBILITY_VIEW` / `View.MARRIAGE_MATCH_VIEW` &rarr; `CompatibilityView` / `MarriageMatchView`): `Compute -> Marriage Compatibility` / `Marriage Match`
  11. **Muhurtha** (`View.MUHURTHA_VIEW` &rarr; `MuhurtaPanel`): `Compute -> Muhurtha`
  12. **Find Name / Numerology** (`View.FIND_NAME_VIEW` &rarr; `FindNameView`): `Numerology -> Find Name`

- **Graduated / Modernized Views** (covered by dedicated feature tests rather than `LegacyScreensRegressionTest`):
  - **Panchang** (`View.PANCHANG_VIEW` &rarr; `PanchangView`): `View -> Panchang` / Toolbar `Panchang` (modernized card dashboard with live Today mode, `.ash` birth-chart instant Panchang, and theme-aware cards; covered by `PanchangChartAndTodayTest`)

## 7. Verification

Whenever touching UI components, layouts, dialogs, fonts, or themes, run the verification runbooks defined in [astrosoft-regression-guardian/SKILL.md](file:///Users/mukunds100/Dev/Astrosoft2/.agents/skills/astrosoft-regression-guardian/SKILL.md).
