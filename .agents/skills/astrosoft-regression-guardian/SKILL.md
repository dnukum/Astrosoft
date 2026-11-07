---
name: astrosoft-regression-guardian
description: >-
  Runs and enforces headless UI regressions, structural contracts across all 13 legacy screens,
  and document dirty-state transitions in Astrosoft. Use whenever modifying Swing UI components,
  renderers, themes, layouts, or shared models to prevent breaking legacy compatibility.
---

# Astrosoft Regression Guardian

The **Astrosoft Regression Guardian** skill guarantees that modern UI enhancements, theme restyling, or calculation adjustments never compromise the 13 original legacy screens or core document contracts.

## Quick Start Runbooks

### 1. Run the 13 Legacy Screens Regression Suite

Whenever touching shared UI views, table renderers, charts, or fonts:

```bash
./.agents/skills/astrosoft-regression-guardian/scripts/run_legacy_regression.sh
```
Or directly via Ant:
```bash
ant test -Dtestcase=app.astrosoft.ui.test.LegacyScreensRegressionTest
```

Expected result: `OK (13 tests)`. All 13 legacy screens must construct, layout, and render offscreen without throwing `NullPointerException` or `HeadlessException`.

### 2. Run the Full UI Smoke Suite

Before creating any commit or finalizing UI refactors:

```bash
./.agents/skills/astrosoft-regression-guardian/scripts/run_ui_smoke.sh
```

This validates:
1. `LegacyScreensRegressionTest` (13 legacy views intact)
2. `DocumentDirtyAndPromptIntegrationTest` (Dirty-tracking across screen transitions)
3. `ChartThemeTest` (Palette switching without breaking contrasts)
4. `AshtavargaMultiGridTest` (Modern 4x2 grid vs. legacy 8-tab compatibility)
5. `TransitChartDialogTest` (Transit calculations and sign-change steppers)
6. `ModernBirthDataPanelTest` (Modern inputs and validation)

---

## Detailed References

- [Legacy Screens Inventory & Structural Contracts](./references/legacy_screens_inventory.md): Details each of the 13 legacy views and their invariants.
- [Headless Swing Testing Guide](./references/headless_testing_guide.md): Patterns for `renderOffscreen`, deep component traversal, and headless safety rules.

---

## Invariant Checklist

Before finalizing any UI changes, verify:
- [ ] No changes to the 8-tab structure or 3-chart layout of legacy `AshtavargaView`.
- [ ] Shared cell renderers (`PlanetCellRenderer`, `AstrosoftTableCellRenderer`) do not force modern status badges into legacy views unless explicitly enabled.
- [ ] Font size changes auto-scale or respect cell boundaries without clipping text into ellipses (`...`).
- [ ] Headless tests pass completely with `BUILD SUCCESSFUL` and zero failures.
