#!/bin/bash
# run_ui_smoke.sh - Runs the headless UI smoke suite covering legacy screens, dirty-tracking, themes, and dialogs.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../../.." && pwd)"

cd "${PROJECT_ROOT}"

TEST_CLASSES=(
    "app.astrosoft.ui.test.LegacyScreensRegressionTest"
    "app.astrosoft.test.DocumentDirtyAndPromptIntegrationTest"
    "app.astrosoft.ui.test.ChartThemeTest"
    "app.astrosoft.ui.test.AshtavargaMultiGridTest"
    "app.astrosoft.ui.test.TransitChartDialogTest"
    "app.astrosoft.ui.test.ModernBirthDataPanelTest"
    "app.astrosoft.ui.test.FloatingDialogsLifecycleTest"
)

echo "============================================================"
echo " Running Astrosoft Full UI Smoke Suite (${#TEST_CLASSES[@]} test classes)"
echo "============================================================"

FAILED=0
for test_class in "${TEST_CLASSES[@]}"; do
    echo "--- Running: ${test_class} ---"
    if ! ant test -Dtestcase="${test_class}"; then
        echo "FAILED: ${test_class}"
        FAILED=$((FAILED + 1))
    fi
done

echo "============================================================"
if [ "$FAILED" -eq 0 ]; then
    echo " SUCCESS: All UI smoke tests passed without regressions!"
    exit 0
else
    echo " FAILURE: ${FAILED} test suite(s) failed!"
    exit 1
fi
