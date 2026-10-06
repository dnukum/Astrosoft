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
