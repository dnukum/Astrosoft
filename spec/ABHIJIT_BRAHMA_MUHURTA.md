# Abhijit, Brahma and Balam timing windows

Contract: attached abhijit-brahma-muhurta-specification.md (2026-10-06). Seasonal 15-part daylight/night division, not fixed 48 minutes. Brahma on civil date d uses sunset d-1 and sunrise d. Abhijit uses sunrise/set d. Wednesday shows caution, not suppression or automatic dosha override.

Implementation: MuhurtaWindowsCalculator, immutable half-open Window values. Inputs are instant sunrise/set, local Calendar and Place. Abhijit [rise+7D/15,rise+8D/15), Brahma [rise-2R/15,rise-R/15). Day/night Subha Hora uses defensive copies of existing WeekDay offsets. Window overlap [max(start),min(end)); empty intersections discarded, clipped intervals labelled partial. Calendar iterates local dates; elapsed instant durations handle DST. Solar failures log and mark local 06:00/18:00 fallbacks. Invalid ordering rejected.

UI: MyMuhurta adds both rows after existing auspicious time; Balam completed background result supplies all three categories in right-side HTML column. Empty category says No overlap; dates/timezones explicit. Existing Rahu/Yama/main Subha Hora methods unchanged.

Tests: equinox 11:36–12:24 and 04:24–05:12; seasonal scaling; half-open, full/partial intersections; ordering; Wednesday; fallback; date/window integration; core/UI regressions. Classical attribution not fully verified: uploaded Raman excerpts indicate 48-minute terminology and Wednesday Durmuhurtha; attachment is controlling computational specification.
