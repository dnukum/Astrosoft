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
