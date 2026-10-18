# Amirthathi Yoga mechanical port

Reference: supplied Amirthathi-Yoga-Java.docx, E. Rajasekar, 2020 Apr 14. Table and algorithm preserved exactly; not an independently verified classical textual rule.

## Inputs and output
Inputs: WeekDay, Nakshathra, isNextNakshatraSameday. Output: Tuple<AmirthathiYoga>, accessed by getFirst()/getSecond().
Formula: first=T[weekday][star.ordinal()]. If true, second=T[weekday][next star]; otherwise second=T[next weekday][same star].
No timing, boundary convention, interval generation or UI integration is added.

## Mechanical changes
Repository packages, Nakshathra spelling, added cyclic next() helpers, immutable local Tuple, omitted unused imports. Codes, 189 table cells and branching preserved. Source did not provide Tuple implementation; local support supplies constructor and getters required to consume the results.

## Edge cases and tests
Saturday wraps to Sunday; Revathi wraps to Ashwini. Null weekday/star preserves NullPointerException. Unknown code preserves null. No simultaneous weekday/star advancement.
Test all 189 cells with both boolean branches (378 ordered pairs), cyclic helpers, code round trips and invalid inputs.
Examples: Wednesday/Ashwini/true => Marana,Siddha; false => Marana,Amirtha. Saturday/Revathi/true => Prabalarshta,Siddha; false => Prabalarshta,Amirtha.

## Selected-row UI integration
Core attaches current classification to each details Entry using its local civil weekday and actual transit Moon Nakshathra at segment start, taking getFirst() from the exact supplied method. No speculative second result is displayed. UI adds Amirthathi Yoga below Varam, suppressing consecutive repeated values. Null classifications in legacy constructors are omitted. No warning/color meanings added.
Selected Balam calendar rows already stop at Nakshathra boundaries; existing details segments split at Tithi and civil-midnight boundaries. This integration relies on that row contract: arbitrary windows spanning Nakshathra transitions are not newly supported. Civil weekday is existing application behavior, not a newly established classical rule. Main table not changed.
Tests: bean accessor, real HTML renderer sequence/repeat suppression, Core against supplied lookup at actual transit instant; full existing regressions. Inputs, table formula and edge behavior remain as above.

## Timed transitions and Panchanga fields
Latest amendment supersedes earlier arbitrary-window limitation: PanchangaWindowEngine now splits at Nakshatra, Tithi, Karana, Nitya Yoga and civil midnight boundaries. Input: half-open window, selected timezone, sidereal Sun/Moon positions from SwissHelper. Normalize degrees to [0,360). Tithi=floor((Moon-Sun mod 360)/12); Karana slot=floor(elongation/6); Nitya Yoga=floor((Sun+Moon mod 360)/(360/27)); Nakshatra=floor(Moon/(360/27)).
Karana slots: 0 Kimstughna, 1..56 repeating Bava..Visti, 57 Sakuna, 58 Chatushpada, 59 Naga. New window path avoids existing Karana.ofDeg exact-boundary inconsistencies without altering legacy API. These are implementation conventions consistent with existing interior-sector calculations, not newly sourced classical text.
Boundary search: hourly bracketing and millisecond bisection, assuming continuous positions advancing less than one sector per hour. Output: contiguous [start,end) classified entries. Empty window => empty; null/reversed/nonfinite inputs rejected; exact endpoint belongs to next interval; coincident boundaries split once; local Calendar midnight handles DST. Transition labels use new entry start in selected timezone, dd MMM HH:mm z; initial value has no timestamp, adjacent repeats omitted. Tests cover Karana half-Tithi, all 60 exact sectors, cycle wrap, Yoga changes, midnight and renderer timestamps/fields. Amirthathi table unchanged. UI adds Yoga (Nitya Yoga) and Karana after Amirthathi Yoga, without new auspiciousness colors.

## Main Muhurta table
Product layout: Date, Time, Location, Sunrise, Sunset, then existing Panchanga rows; Amirthathi Yoga immediately after Yoga (End). Standalone Panchang table unchanged. Domain adds main-table classification at eventInstant and actual changes through the next civil midnight (inclusive via 1 ms final segment), timestamped dd MMM HH:mm z. Inputs: event instant, selected timezone and existing Swiss sidereal positions; formula T[weekday,transit star] unchanged. Adjacent identical classifications omitted; exact-midnight events use new day. No fixed assumption that a star change always changes classification. Table layout test verifies rise/set location and Amirthathi placement/value. This horizon is a product decision, not a traditional day definition.


## Sunrise Vara integration (2026-10-06)
Domain convention: expert clarification confirms Vara runs local sunrise to next sunrise.
Product decision: the table Day and details weekday remain civil; Amirthathi alone uses sunrise Vara.
Legacy lookup table, tuple branches, and null-input behavior remain unchanged.
Inputs: window instants, selected/default geographic place, location timezone, sidereal nakshatra.
Formula: Y(t)=T[V(t)][N(t)]; V(t) is the weekday of the latest local sunrise at or before t.
Implementation: SunriseVara resolves/caches SunTimes sunrise per local date; interval engine splits at sunrise as well as existing nakshatra/civil boundaries. Both MyMuhurta callers consume segment.vara and getFirst().
Output: real classifications through the requested half-open window; main row remains current instant to civil midnight, without including tomorrow at the endpoint.
Fallback product policy: calculation failure or invalid date-bound sunrise logs a warning and uses 06:00 local. Before fallback sunrise use previous Vara; equality uses new Vara.
Tests: before/exact/after sunrise, weekend wrap, thrown/invalid sunrise fallback, midnight civil/Vara separation, simultaneous sunrise/star change, Revathi wrap, DST and empty windows; existing table 378 pairs unchanged.
This supersedes the civil-weekday calculation described in the earlier integration section; it does not independently validate the lookup table.


## Named successors and tooltip presentation (2026-10-06)
Product decision: MyMuhurta shows current until local end time → named successor for Nakshatra, Thithi, Nitya Yoga and successive Karanas. Date is explicit when local end hours cross midnight; year is shown on year rollover. Other Panchang table consumers retain legacy formatting.
Implementation: uses existing PanEvent values and end hours; no ephemeris or reference-time changes. Nakshatra/yoga wrap cyclically, thithi uses the 30-slot lunar phase (including Pournami/Amavasya), Karana uses the 60 half-thithi slots rather than enum cycling.
Row help is available over both columns, explains civil Day versus sunrise Vara, transitions, daily Panchang reference time, and Subha Hora versus Nitya Yoga Subha. Rahu, Yama and Subha Hora calculations remain unchanged.
Inputs: existing event names/end hours, Sun/Moon phase and local Calendar. Output: presentation strings. Tests: successor names, Karana special slots, next-day/multiple-day/year rollover, help text and unchanged non-transition rows, plus full core/UI suite.


## Auspiciousness and warning color coding (2026-10)
Product decision: Balam Calendar window details highlights inauspicious and warning elements in red to allow instant visual assessment of quality:
- **Amirthathi Yoga**: `Marana` yoga is displayed in red font (`<font color='red'>Marana</font>`). When transitions occur across the window, only the word `Marana` is wrapped in red tags; transition arrows (`&rarr;`) and timestamp intervals remain in standard text color (e.g., `<font color='red'>Marana</font> &rarr; (01 Jan, 00:00 UTC) Amirtha`).
- **Varam**: Inauspicious weekdays `Tuesday` and `Saturday` are highlighted in red font.
- **Thithi**: Warning thithis (`Amavasya`, `Prathamai`, `Ashtami`, `Navami`, or segments marked `isWarning()`) are highlighted in red font.
- **Suitability**: In the event suitability section, stars with no recommended activities display `<font color='red'>Not recommended</font>`.
