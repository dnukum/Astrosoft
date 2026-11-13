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
7. `FloatingDialogsLifecycleTest` (Window z-order, dialog modality, and floating palette scopes)

---

## Detailed References

- [Legacy Screens Inventory & Structural Contracts](./references/legacy_screens_inventory.md): Details each of the 13 legacy views and their invariants.
- [Headless Swing Testing Guide](./references/headless_testing_guide.md): Patterns for `renderOffscreen`, deep component traversal, and headless safety rules.
- [Floating Windows Lifecycle Spec](../../../../spec/FLOATING_WINDOWS_LIFECYCLE_SPEC.md): Scope-based lifecycle and z-order contracts for companion palettes.

---

## Invariant Checklist

Before finalizing any UI changes, verify:
- [ ] **Bug Investigation First**: Never start coding fixes immediately when an issue is reported; provide thorough RCA and obtain explicit user approval first.
- [ ] No changes to the 8-tab structure or 3-chart layout of legacy `AshtavargaView`.
- [ ] Shared cell renderers (`PlanetCellRenderer`, `AstrosoftTableCellRenderer`) do not force modern status badges into legacy views unless explicitly enabled.
- [ ] Font size changes auto-scale or respect cell boundaries without clipping text into ellipses (`...`).
- [ ] All modal dialogs inherit `AstrosoftDialog` (`ModalityType.APPLICATION_MODAL`, `alwaysOnTop=true`, `windowOpened` toFront/requestFocus hooks).
- [ ] Confirmation and warning prompts route through `OptionDialog` (never raw `JOptionPane.show...`).
- [ ] All file pickers use `FileOps.openFileDialog()` (guaranteeing `createDialog` sets `alwaysOnTop=true`).
- [ ] Modeless inspection palettes (`PlanetDetails`, `HouseDetails`, `Ashtavarga`, `Chart Notes`) keep `alwaysOnTop=false` to avoid occluding modal popups.
- [ ] All test input files (`.ash`, `.xml`, etc.) are anonymized (no personal names) and live inside `tests/` (never `samples/` or external user directories).
- [ ] Any blocking modal `setVisible(true)` exercised by tests is guarded for `GraphicsEnvironment.isHeadless()` and `Boolean.getBoolean("astrosoft.test.unattended")` so `ant test` runs completely unattended.
- [ ] Unattended/headless tests pass completely with `BUILD SUCCESSFUL` and zero failures.
