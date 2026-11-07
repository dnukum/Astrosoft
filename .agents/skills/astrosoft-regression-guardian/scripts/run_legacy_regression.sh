#!/bin/bash
# run_legacy_regression.sh - Executes the 13 legacy screens headless regression suite

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../../.." && pwd)"

echo "============================================================"
echo " Running Astrosoft 13 Legacy Screens Regression Suite"
echo " Target: app.astrosoft.ui.test.LegacyScreensRegressionTest"
echo "============================================================"

cd "${PROJECT_ROOT}"

# Run headless legacy regression test via Ant
ant test -Dtestcase=app.astrosoft.ui.test.LegacyScreensRegressionTest

echo "============================================================"
echo " Legacy screens regression tests passed successfully!"
echo "============================================================"
