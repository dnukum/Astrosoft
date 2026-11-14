#!/bin/bash
set -euo pipefail

# ==============================================================================
# Astrosoft Release Publisher - Git & GitHub Release Publishing Script
# ==============================================================================
# Usage:
#   ./publish_release.sh [tag_name] [release_notes]
#
# Examples:
#   ./publish_release.sh v20261113 "Release v20261113: Diagnostics and Tamil font suite"
#   ./publish_release.sh --sync-only  (syncs and pushes master without tagging)
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel 2>/dev/null || true)"
if [ -z "$REPO_ROOT" ]; then
    REPO_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
fi
cd "$REPO_ROOT"

TAG="${1:-}"
NOTES="${2:-Astrosoft Distribution Release}"

echo "========================================================================"
echo " [Astrosoft Release Publisher] Automated Publishing Pipeline"
echo "========================================================================"

# Step 1: Run pre-flight verification
"$SCRIPT_DIR/verify_and_package.sh"

# Step 2: Ensure bin/AstroSoft.jar and version.properties are staged if modified
if ! git diff --quiet bin/AstroSoft.jar resources/version.properties; then
    echo "==> Staging refreshed bin/AstroSoft.jar and version.properties..."
    git add bin/AstroSoft.jar resources/version.properties
    git commit -m "build: package fresh bin/AstroSoft.jar and version stamp for release"
fi

# Step 3: Publish to remote
if [ "$TAG" = "--sync-only" ] || [ -z "$TAG" ]; then
    echo "==> Pushing verified branch to origin/master..."
    git push origin master
    echo "✅ Successfully synced master to origin!"
else
    echo "==> Tagging release: $TAG..."
    git tag -a "$TAG" -m "$NOTES"
    echo "==> Pushing master and tag $TAG to origin..."
    git push origin master
    git push origin "$TAG"
    echo "✅ Successfully published release tag $TAG to origin!"
    echo "   GitHub Actions (.github/workflows/release.yml) is now building and uploading the distribution asset."
fi

echo "========================================================================"
echo " ✅ PUBLISH PIPELINE COMPLETE"
echo "========================================================================"
