# Astrosoft Project Guidelines & Rules

## Git & Version Control Policy

- **Local Commits**: You are encouraged to create local git commits for clean, incremental milestones, tests, bug fixes, and refactors. Follow the repository's conventional commit style (e.g., `feat:`, `fix:`, `test:`, `refactor:`, `build:`).
- **Remote Push Restriction**: **NEVER** run `git push`, publish branches, or perform any remote modifications without explicit, prior user approval in the chat.
- **Release Retention Policy (Keep Last 2 Versions)**: When publishing releases or tagging distributions, Astrosoft strictly enforces retaining only the **last 2 versions**. Older release distributions and their associated release tags must be pruned (matching the 2-distribution retention policy in `.github/workflows/release.yml`), ensuring at any time only the current and previous release exist.

## Architecture & Domain Guidelines

- **Canonical Astrology Reference**: When classical texts or ephemeris calculations disagree, **B.V. Raman** (*Hindu Predictive Astrology*, *Graha and Bhava Balas*) is the authoritative baseline for Astrosoft. Note discrepancies against BPHS or PyJHora explicitly in specifications or commit notes.
- **Java 21 LTS Target**: Astrosoft compiles and runs on Java 21 (LTS) using Ant (`build.xml`). Ensure required JVM module flags (`--add-exports java.desktop/sun.awt=ALL-UNNAMED`, `--add-opens java.desktop/sun.awt=ALL-UNNAMED`) are preserved.
- **UI Modernization & Legacy Compatibility**: Modern UI enhancements must never break or alter the 13 legacy screens. All UI changes must be verified using headless JUnit test suites (`-Djava.awt.headless=true`).
- **Dialog & Window Hierarchy Policy**:
  - **Modal Popups & Inputs**: All input, options, and confirmation popups must maintain topmost Z-order over floating reference windows. All dialogs must inherit `AstrosoftDialog` (with `ModalityType.APPLICATION_MODAL`, `setAlwaysOnTop(true)`, and `windowOpened` native elevation hooks). Prompts and alerts must use `OptionDialog` (never invoke raw `JOptionPane.show...` directly). File dialogs must use `FileOps.openFileDialog()`.
  - **Floating Inspection Palettes**: Auxiliary reference cards (`PlanetDetailsDialog`, `HouseDetailsDialog`, `AshtavargaDialog`, `floatingNotesDialog`) are modeless palettes. They must strictly keep `alwaysOnTop = false` and adhere to `FloatingDialogsLifecycleSpec` (auto-hidden on utility screens, disposed on chart identity changes). Modeless dialogs must never occlude modal popups.
