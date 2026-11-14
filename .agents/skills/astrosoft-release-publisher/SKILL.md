---
name: astrosoft-release-publisher
description: >-
  Packages, verifies, and publishes Astrosoft distribution packages and release tags to GitHub.
  Enforces pre-flight compilation, bytecode verification of bin/AstroSoft.jar, headless smoke tests,
  version timestamp consistency, and 2-release retention policies.
---

# Astrosoft Release Publisher

The **Astrosoft Release Publisher** skill provides automated verification and release publishing workflows. It ensures that the tracked binary archive `bin/AstroSoft.jar` is always freshly compiled from source, verified against regression tests, and properly synchronized before code or release tags are pushed to GitHub.

## Why This Skill Exists

In Astrosoft:
- Windows launch scripts (`RunAstroSoft20.bat`) execute directly from `-jar bin/AstroSoft.jar`.
- Standard development (`ant test`, `ant compile`) compiles classes to `deploy/` without updating `bin/AstroSoft.jar`.
- This skill bridges that gap by guaranteeing that `bin/AstroSoft.jar` is freshly packaged, tested, and validated before any push to remote.

---

## Quick Start Runbooks

### 1. Pre-Flight Packaging & Verification (Dry Run)

To verify that all classes compile, `bin/AstroSoft.jar` packages without errors, version timestamps match, and all UI smoke tests pass:

```bash
./.agents/skills/astrosoft-release-publisher/scripts/verify_and_package.sh
```

**Verification Gates Enforced**:
1. Clean compilation of all 429+ source files via `ant clean deploy`.
2. Existence and timestamp parity between `bin/AstroSoft.jar` and `resources/version.properties`.
3. Bytecode presence check for core modern classes (`DiagnosticsDialog`, `SystemDiagnostics`, `PlanetDetailsDialog`, `AshtavargaDialog`, etc.).
4. 100% pass across all 7 test classes in `run_ui_smoke.sh`.

---

### 2. Publishing a Release to GitHub

Whenever publishing a new release version or syncing verified binaries to GitHub:

```bash
# To publish a tagged release:
./.agents/skills/astrosoft-release-publisher/scripts/publish_release.sh v20261113 "Release v20261113: Diagnostics and Tamil font suite"

# Or to sync master with fresh binaries without creating a new tag:
./.agents/skills/astrosoft-release-publisher/scripts/publish_release.sh --sync-only
```

This triggers the GitHub Actions pipeline ([.github/workflows/release.yml](../../../../.github/workflows/release.yml)) to:
1. Run headless regression tests in CI.
2. Build `release/Astrosoft.zip` from source.
3. Create the GitHub Release and upload the asset.
4. Automatically enforce the **2-release retention policy** (pruning older releases and tags).

---

## Mandatory Release Invariant Checklist

Before pushing any commits or tags to `origin/master`, verify:
- [ ] **Pre-Flight Verified**: `./.agents/skills/astrosoft-release-publisher/scripts/verify_and_package.sh` passes completely with zero errors.
- [ ] **Bytecode Freshness**: `bin/AstroSoft.jar` is rebuilt from current `src/` and committed to git.
- [ ] **Version Parity**: `unzip -p bin/AstroSoft.jar resources/version.properties` matches `resources/version.properties`.
- [ ] **Prior User Approval**: Explicit approval has been granted by the user before running any remote `git push`.
- [ ] **Release Retention**: When tagging releases, only the last 2 versions exist on GitHub (enforced by release CI).
