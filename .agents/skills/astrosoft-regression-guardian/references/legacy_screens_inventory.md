# Astrosoft Legacy Screens Inventory & Structural Contracts

Astrosoft adheres to a strict backward-compatibility invariant: **Modern UI enhancements must never break or alter the 13 original legacy screens.**

Any change to shared components (`AstrosoftTable`, `Chart`, `AstrosoftTabbedPane`, renderers, color palettes, fonts, or data models) must be verified against this inventory.

---

## The 13 Legacy Screens

| # | Screen Name | View Class | Structural Invariants / Contract |
|---|---|---|---|
| 1 | **Legacy Ashtavarga** | `app.astrosoft.ui.view.AshtavargaView` | Exactly 8 tabs (`Sun`, `Moon`, `Mars`, `Mercury`, `Jupiter`, `Venus`, `Saturn`, `SarvaAshtavarga`). Each tab contains 3 charts: Main (BAV/SAV), Trikona, Ekathipathya. Planetary tabs contain 1 Guna table. Natal Rasi score circle only on planetary BAV charts. |
| 2 | **Vimshottari Dasa** | `app.astrosoft.ui.view.VimDasaView` | Hierarchical Dasa tree (`JTree`). Collapsed or expanded Mahadasa nodes down to Antardasa and Pratyantardasa levels with date periods. |
| 3 | **Bhava Positions** | `app.astrosoft.ui.view.BhavaView` | Two distinct table presentations: Bhava summary table and detailed Bhava planet table. Offscreen layout bounds check. |
| 4 | **Planetary Positions** | `app.astrosoft.ui.view.PlanetView` | Planet longitude tables with columns for Planet, Sign, Longitude, Nakshatra, Pada, and Retrograde status. Rendered text must stay free of clipping. |
| 5 | **Divisional Charts** | `app.astrosoft.ui.view.VargaChartView` | Pagination support (`AbstractPagination`) traversing 16 classical Varga division charts (`D1` through `D60`). Renderable offscreen. |
| 6 | **Shadbala Summary & Bar** | `app.astrosoft.ui.view.ShadBalaView` / `ShadBalaBarView` | Multi-category strength tables (Sthana, Dik, Kaala, Chesta, Naisargika, Drik) and relative graphical bar chart with requirement thresholds. |
| 7 | **Yoga Combinations** | `app.astrosoft.ui.view.YogaCombinationsView` | Master combination list (`JList`) on left; definition and results HTML render view (`JEditorPane`) on right. Must not throw reflection or module errors on Java 21. |
| 8 | **Panchang View** | `app.astrosoft.ui.view.PanchangView` | Calendar-based five limbs of time: Tithi, Vara, Nakshatra, Yoga, Karana, plus Rahu Kalam and Yamagandam. |
| 9 | **Ephemeris View** | `app.astrosoft.ui.view.EphemerisView` | Daily/Monthly planetary longitude table with pagination and date stepping controls. |
| 10 | **Marriage Compatibility** | `app.astrosoft.ui.view.CompatibilityView` | Porutham / Kuta evaluation table (10 or 12 kutas) with individual scores, maximum points, and final recommendation. Dual-read compatibility for legacy tags. |
| 11 | **Muhurtha Panel** | `app.astrosoft.ui.view.MuhurthaPanel` | Candidate electional timings table with Gowri Panchangam and Subha Hora indicators. |
| 12 | **Find Name View** | `app.astrosoft.ui.view.FindNameView` | Numerology and alphabet sound table based on Nakshatra pada sounds. |
| 13 | **Horoscope Dashboard** | `Horoscope` / `MainFrame` Legacy Layout | Classic South Indian natal chart layout (Rasi and Navamsa side-by-side with basic details table). |

---

## Common Regression Scenarios to Guard Against

1. **Shared Cell Renderer Pollution:**
   - Modifying `PlanetCellRenderer` or `AstrosoftTableCellRenderer` to add modern features (e.g., status suffix badges) must be opt-in so legacy screens retain their unpolluted standard text.
2. **Layout Manager Clipping:**
   - Changes in default font metrics, border insets, or table row heights can clip table text or push action buttons outside visible panel bounds.
3. **Headless Execution Environment:**
   - CI and test suites run with `-Djava.awt.headless=true`. Code must never call `comp.getLocationOnScreen()` or create unmocked native dialogs during component construction.
4. **Timezone & Locale Drift:**
   - Avoid calls that depend on host machine timezone or date format without explicit Astrosoft `TimeZone` or `Calendar` context.
