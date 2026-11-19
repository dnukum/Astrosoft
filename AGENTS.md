# Astrosoft Project Guidelines & Rules

## Documentation & Rule Ownership (Single Source of Truth)

Never duplicate rules across `AGENTS.md`, `spec/*.md`, and `.agents/skills/*/SKILL.md`. Place new rules only in their owning document, and reference other documents by link:

| File | Owns (Scope) |
|---|---|
| `AGENTS.md` | Global LLM guardrails (git/approval, RCA-first, iterative hygiene, logging, test fixtures), domain/runtime baselines, and pointers below. |
| [spec/ARCHITECTURE.md](file:///Users/mukunds100/Dev/Astrosoft2/spec/ARCHITECTURE.md) | 3-layer architecture (`Core`, `Beans`, `Presentation/UI`) and module boundaries. |
| [spec/UI_ARCHITECTURE.md](file:///Users/mukunds100/Dev/Astrosoft2/spec/UI_ARCHITECTURE.md) | All Swing UI rules: 13 legacy screens preservation, window tiers/modality, floating palettes, preference propagation, Tamil fonts, and Swing HTML/multiline policy. |
| [.agents/skills/astrosoft-regression-guardian/SKILL.md](file:///Users/mukunds100/Dev/Astrosoft2/.agents/skills/astrosoft-regression-guardian/SKILL.md) | UI verification & test-runner playbook (`run_legacy_regression.sh`, `run_ui_smoke.sh`, headless test gates). |
| [.agents/skills/astrosoft-release-publisher/SKILL.md](file:///Users/mukunds100/Dev/Astrosoft2/.agents/skills/astrosoft-release-publisher/SKILL.md) | Build, `bin/AstroSoft.jar` packaging, and GitHub release playbook (`verify_and_package.sh`, `publish_release.sh`, 2-release retention). |

## Git & Version Control Policy

- **Approval Before Commit or Push**: **NEVER** run `git commit`, `git push`, publish branches, or perform any remote modifications without explicit, prior user approval in the chat. Leave edits in the working tree for user review until asked to commit.
- **Commit Style**: When commits are approved, follow conventional commit style (e.g., `feat:`, `fix:`, `test:`, `refactor:`, `build:`, `docs:`).
- **Pre-Push Packaging**: Before pushing commits or publishing releases to remote, run the [astrosoft-release-publisher](file:///Users/mukunds100/Dev/Astrosoft2/.agents/skills/astrosoft-release-publisher/SKILL.md) workflow so `bin/AstroSoft.jar` and `resources/version.properties` stay synchronized with source.

## Architecture & Domain Baselines

- **Canonical Astrology Reference**: When classical texts or ephemeris calculations disagree, **B.V. Raman** (*Hindu Predictive Astrology*, *Graha and Bhava Balas*) is the authoritative baseline for Astrosoft. Note discrepancies against BPHS or PyJHora explicitly in specifications or commit notes.
- **Java 21 LTS Target**: Astrosoft compiles and runs on Java 21 (LTS) using Ant (`build.xml`). Preserve required JVM module flags (`--add-exports java.desktop/sun.awt=ALL-UNNAMED`, `--add-opens java.desktop/sun.awt=ALL-UNNAMED`).
- **Layer & UI Architecture**: Follow [spec/ARCHITECTURE.md](file:///Users/mukunds100/Dev/Astrosoft2/spec/ARCHITECTURE.md) for Core/Beans/UI separation and [spec/UI_ARCHITECTURE.md](file:///Users/mukunds100/Dev/Astrosoft2/spec/UI_ARCHITECTURE.md) before modifying any Swing UI, dialogs, fonts, or HTML components. Run [astrosoft-regression-guardian](file:///Users/mukunds100/Dev/Astrosoft2/.agents/skills/astrosoft-regression-guardian/SKILL.md) after UI changes.

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

## Iterative Hygiene & Refactoring Policy

- **Clean As You Iterate (Zero Leftover Scaffolding)**: Whenever an iterative change replaces or simplifies a previous approach (e.g., swapping a UI control, dropping a table column/field, or changing a calculation), immediately remove any superseded fields, getters, helper methods, and unused imports in the same turn.
- **No Copy-Paste UI/Logic Duplication**: When adding a second similar panel, card, grid, or calculation, generalize the existing helper (e.g., widen `JLabel[]` to `JComponent[]`, extract a shared factory method, or promote formatters to `static final` constants) rather than duplicating layout or domain code.
- **Leave Touched Files Cleaner**: When modernizing or modifying a legacy class, remove dead commented-out code blocks, debug `System.out.println` statements, and orphaned constants in the touched file before completing the turn.

## Logging & Console Policy

- **Developer Tracing at `FINE` Level**: Routine operational tracing, action handling, font registration, file dialog events, window close checks, pagination, and preference changes must strictly use `log.fine(...)` (or `log.finer(...)`), **never** `log.info(...)`.
- **Clean Terminal Output**: Because `ConsoleHandler.level` defaults to `INFO` in `bin/logging.properties`, using `log.info(...)` causes noisy terminal output during normal user interactions (clicking menus, opening files, resizing). Reserve `log.info(...)` solely for high-level application lifecycle events, and use `log.warning(...)` or `log.severe(...)` for actual error conditions.
