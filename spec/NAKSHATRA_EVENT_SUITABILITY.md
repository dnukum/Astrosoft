# Matching-star event suitability

Approved source: Excel screenshot pasted_image_1791252565_0.jpeg, supplied 5 Oct 2026. User amendment: use the Excel exactly; ignore fallback interpretation. This is a user-approved domain mapping, not independently asserted classical provenance. Nine categories in spreadsheet column order. General only for Magha/Chitra/Swati, alongside specific activities. Empty rows stay empty.

Inputs: existing last-column BalamMatchCell matching stars, NOT the separate transit Nakshatra or a natal star. Lookup E(n)={event | checked in Excel row n}. Output: immutable ordered star/event entries on BalamWindowDetails. Core catalog owns rules; MyMuhurta orchestrates; bean carries data; UI only formats before Transit Moon, one line per matching star. No ranking/filtering/time window changes or new astronomical calculation.

Edge cases: reject null catalog star/matches; empty set produces no suitability lines; star with empty row displays Not recommended in red. Enum order determines stars and spreadsheet column order determines events. Snapshot matches before worker starts; preserve request cancellation/version guarding against stale selection.

Tests: all 27 rows against nine checked columns, all eight blank rows, General exactly three explicit rows, defensive copies, immutable bean list, Core orchestration and backward-compatible details method, multiple matches distinct from transit star, renderer column order/before Transit Moon, cleared results without stale categories; existing regressions.

Presentation amendment: blank line between matching-star entries and Transit Moon; transit Nakshatra lord displayed inline in parentheses, no separate Lord line. Domain mapping unchanged.

Calendar layout: toggle below chart, Update on right. Noneditable month/year choices synchronized with arrows/context using event guard. Rolling year list +/-50 around displayed year; arrows recenter when exceeded.

Toggle alignment amendment: place Show/Hide inside the Muhurta chart column, centered within that individual chart width, not across the multi-chart area. Reattach the same button when charts refresh.

Latest toggle amendment: outside the bordered chart scroll pane, in an independent 350px-wide centered footer aligned with the Muhurta chart. Footer and button hidden until Person 1 or Person 2 horoscope loads successfully; built-in Muhurta chart alone does not qualify. No hidden footer space.
