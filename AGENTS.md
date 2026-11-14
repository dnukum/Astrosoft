# Astrosoft Project Guidelines & Rules

## Git & Version Control Policy

- **Local Commits**: You are encouraged to create local git commits for clean, incremental milestones, tests, bug fixes, and refactors. Follow the repository's conventional commit style (e.g., `feat:`, `fix:`, `test:`, `refactor:`, `build:`).
- **Remote Push Restriction**: **NEVER** run `git push`, publish branches, or perform any remote modifications without explicit, prior user approval in the chat.
- **Pre-Push Jar Synchronization Invariant**: Before pushing commits or publishing release tags to remote, `ant deploy` (or `./.agents/skills/astrosoft-release-publisher/scripts/verify_and_package.sh`) must always be executed to ensure `bin/AstroSoft.jar` and `resources/version.properties` are freshly compiled and synchronized with source. Windows and Mac standalone launchers (`RunAstroSoft20.bat`) execute directly from `bin/AstroSoft.jar`, so stale binaries must never be pushed.
- **Release Retention Policy (Keep Last 2 Versions)**: When publishing releases or tagging distributions, Astrosoft strictly enforces retaining only the **last 2 versions**. Older release distributions and their associated release tags must be pruned (matching the 2-distribution retention policy in `.github/workflows/release.yml`), ensuring at any time only the current and previous release exist.

## Architecture & Domain Guidelines

- **Canonical Astrology Reference**: When classical texts or ephemeris calculations disagree, **B.V. Raman** (*Hindu Predictive Astrology*, *Graha and Bhava Balas*) is the authoritative baseline for Astrosoft. Note discrepancies against BPHS or PyJHora explicitly in specifications or commit notes.
- **Java 21 LTS Target**: Astrosoft compiles and runs on Java 21 (LTS) using Ant (`build.xml`). Ensure required JVM module flags (`--add-exports java.desktop/sun.awt=ALL-UNNAMED`, `--add-opens java.desktop/sun.awt=ALL-UNNAMED`) are preserved.
- **13 Legacy Screens Preservation Invariant**:
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
    10. **Panchang** (`View.PANCHANG_VIEW` &rarr; `PanchangView`): `View -> Panchang` / Toolbar `Panchang`
    11. **Marriage Compatibility / Match** (`View.COMPATIBILITY_VIEW` / `View.MARRIAGE_MATCH_VIEW` &rarr; `CompatibilityView` / `MarriageMatchView`): `Compute -> Marriage Compatibility` / `Marriage Match`
    12. **Muhurtha** (`View.MUHURTHA_VIEW` &rarr; `MuhurthaPanel`): `Compute -> Muhurtha`
    13. **Find Name / Numerology** (`View.FIND_NAME_VIEW` &rarr; `FindNameView`): `Numerology -> Find Name`
  - **Verification Enforcement**: Every legacy screen must be verified both in isolation and via end-to-end container routing (`AstroSoft.viewChanged(view)` asserting the exact legacy class is mounted in `home`), executed via unattended test suites (`ant test -Dtestcase=...` / `./.agents/skills/astrosoft-regression-guardian/scripts/run_ui_smoke.sh`).
- **Dialog & Window Hierarchy Policy**: Follow [UI Architecture Spec](file:///Users/mukunds100/Dev/Astrosoft2/spec/UI_ARCHITECTURE.md) for window tiers, scopes, and event propagation:
  - **Modal Popups & Inputs**: All input, options, and confirmation popups must maintain topmost Z-order over floating reference windows. All dialogs must inherit `AstrosoftDialog` (with `ModalityType.APPLICATION_MODAL`, `setAlwaysOnTop(true)`, and `windowOpened` native elevation hooks). Prompts and alerts must use `OptionDialog` (never invoke raw `JOptionPane.show...` directly). File dialogs must use `FileOps.openFileDialog()`.
  - **Floating Inspection Palettes**: Auxiliary reference cards (`PlanetDetailsDialog`, `HouseDetailsDialog`, `AshtavargaDialog`, `TransitChartDialog`, `floatingNotesDialog`) are modeless palettes. They must strictly keep `alwaysOnTop = false` and adhere to `FloatingDialogsLifecycleSpec` (auto-hidden on utility screens, disposed on chart identity changes). Modeless dialogs must never occlude modal popups.
  - **Preference & Event Propagation**: Runtime preference switches (Language, Ayanamsa, Rahu Node, Theme, Style) must propagate to both embedded views (`refreshAllChartsAndCachedViews()`) AND active floating palettes (`refreshChartFloatingDialogs()`), preserving custom coordinates.

## Testing & Fixtures Policy

- **Anonymized Test Inputs**: All test input files (`.ash`, `.xml`, `.asc`, `.muh`, etc.) as well as test method names, assertions, and comments must use anonymized, non-personal identifiers (e.g., `sample_chart_1.ash`, `rahu_cusp_aries_taurus.ash`).
- **Self-Contained in `tests/`**: All unit and regression test inputs must live inside `tests/` (e.g., `tests/charts/`, `tests/options/`). Tests must never read from `samples/`, `Downloads/`, `MyDocs/`, or other external directories, and must never modify `samples/` directly.
- **Unattended & Headless Execution**: `ant test` runs with `-Dastrosoft.test.unattended=true` and `failonerror="true"` in `build.xml` so `JFrame`/`JDialog` suites and offscreen suites both run without human interaction. Any blocking modal dialog or prompt exercised by tests must guard `setVisible(true)` against `GraphicsEnvironment.isHeadless()` and `Boolean.getBoolean("astrosoft.test.unattended")`.

## Issue Reporting & Bug Fix Protocol

- **Root Cause Analysis First**: When the user reports an issue, bug, or regression, **NEVER** immediately modify codebase files or implement a fix unless the user explicitly instructs to fix it in their prompt.
- **Mandatory Approval Workflow**:
  1. Conduct a comprehensive root cause analysis (RCA) identifying the failing code paths, architectural causes, and affected lifecycle scopes.
  2. Explain the root cause clearly to the user.
  3. Outline the proposed fix strategy and verification plan.
  4. Wait for explicit user approval before touching code, running file edits, or deploying fixes.

## Logging & Console Policy

- **Developer Tracing at `FINE` Level**: Routine operational tracing, action handling, font registration, file dialog events, window close checks, pagination, and preference changes must strictly use `log.fine(...)` (or `log.finer(...)`), **never** `log.info(...)`.
- **Clean Terminal Output**: Because `ConsoleHandler.level` defaults to `INFO` in `bin/logging.properties`, using `log.info(...)` causes noisy terminal output during normal user interactions (clicking menus, opening files, resizing). Reserve `log.info(...)` solely for high-level application lifecycle events, and use `log.warning(...)` or `log.severe(...)` for actual error conditions.

