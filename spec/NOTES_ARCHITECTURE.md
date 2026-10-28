# Notes Architecture & Design Specification

## 1. Overview & Core Philosophy

The Notes subsystem in Astrosoft provides a persistent, rich-text scratchpad where users can record astrological observations, client consultation notes, muhurta rationales, and matching evaluations.

### The Fundamental Principle: "Document-Bound Notes"
Notes are not ephemeral UI states; **notes are always bound to a specific document entity**. 
- In Astrosoft, a **Document** is any entity that can be saved to or loaded from a persistent `.ash` file (e.g., a Natal Horoscope, a Muhurta Analysis session, or a Marriage Compatibility record).
- **Utility / Lookup views** (such as daily Panchang, Ephemeris tables, or Baby Name search) do not have a persistent document identity; therefore, Notes are **disabled** when viewing them.

---

## 2. Screen & View State Matrix

| Screen / View | Category | Notes State | Owning Entity | Target `.ash` Persistence |
| :--- | :--- | :---: | :--- | :--- |
| **Chart View** (Rasi / Navamsa) | Horoscope | **ENABLED** | `Horoscope` (`h.getNotes()`) | `<BirthData><Notes>` in Horoscope `.ash` |
| **Vimshottari Dasas** | Horoscope | **ENABLED** | `Horoscope` (`h.getNotes()`) | `<BirthData><Notes>` in Horoscope `.ash` |
| **Planetary Positions** | Horoscope | **ENABLED** | `Horoscope` (`h.getNotes()`) | `<BirthData><Notes>` in Horoscope `.ash` |
| **Bhava (House) Positions** | Horoscope | **ENABLED** | `Horoscope` (`h.getNotes()`) | `<BirthData><Notes>` in Horoscope `.ash` |
| **Divisional Charts (D2–D60)** | Horoscope | **ENABLED** | `Horoscope` (`h.getNotes()`) | `<BirthData><Notes>` in Horoscope `.ash` |
| **Ashtavarga** | Horoscope | **ENABLED** | `Horoscope` (`h.getNotes()`) | `<BirthData><Notes>` in Horoscope `.ash` |
| **Shadbala** | Horoscope | **ENABLED** | `Horoscope` (`h.getNotes()`) | `<BirthData><Notes>` in Horoscope `.ash` |
| **Yoga Combinations** | Horoscope | **ENABLED** | `Horoscope` (`h.getNotes()`) | `<BirthData><Notes>` in Horoscope `.ash` |
| **My Muhurta View** | Muhurta | **ENABLED** | `MyMuhurta` (`muhurta.getNotes()`) | `<Muhurta><Notes>` in Muhurta `.ash` |
| **Marriage Match View** *(Future)* | Compatibility | **ENABLED** | `Compactibility` (`comp.getNotes()`) | `<Compactibility><Notes>` in Match `.ash` |
| **Compatibility View** *(Future)* | Compatibility | **ENABLED** | `Compactibility` (`comp.getNotes()`) | `<Compactibility><Notes>` in Match `.ash` |
| **Daily Panchang** | Utility / Lookup | **DISABLED** | *None* | *None (Reference only)* |
| **Ephemeris View** | Utility / Lookup | **DISABLED** | *None* | *None (Reference only)* |
| **Find Baby Name** | Utility / Lookup | **DISABLED** | *None* | *None (Reference only)* |
| **Initial Empty State** | Application | **DISABLED** | *None* | *None* |

---

## 3. Architecture & Domain Model

### 3.1 The `NotesOwner` Interface
To decouple `ChartNotesPanel` from `Horoscope` and enable seamless scaling to **My Muhurta** and **Marriage Match**, document classes implement a lightweight interface:

```java
package app.astrosoft.beans;

public interface NotesOwner {
    /** Returns current notes content (HTML or plain text) */
    String getNotes();

    /** Sets notes content */
    void setNotes(String notes);

    /** Descriptive title for Notes header/tooltip (e.g. "Notes — Nikhil" or "Notes — House Warming") */
    String getNotesOwnerTitle();

    /** Returns whether this document has an associated file path */
    String getFilePath();

    /** Marks the underlying document as modified */
    void setModified(boolean modified);
}
```

### 3.2 Implemented By:
1. **`Horoscope` (via `BirthData`):**
   - Title: `h.getPersonName()` (e.g., `Notes — Nikhil`)
   - Storage: `<BirthData><Notes><![CDATA[...]]></Notes></BirthData>`
2. **`MyMuhurta`:**
   - Title: `Event Name / Date` (e.g., `Notes — House Warming [27 Oct 2026]`)
   - Storage: `<Muhurta><Notes><![CDATA[...]]></Notes></Muhurta>`
3. **`Compactibility` (Marriage Match - Planned):**
   - Title: `BoyName & GirlName` (e.g., `Notes — Rohit & Priya`)
   - Storage: `<Compactibility><Notes><![CDATA[...]]></Notes></Compactibility>`

---

## 4. Lifecycle & Context-Switching Protocol

When navigating across views in `AstroSoft.java`:

```mermaid
stateDiagram-v2
    [*] --> UnloadedState : Application Launch
    UnloadedState : Notes Disabled (Icon greyed out)

    UnloadedState --> HoroscopeView : Open/New Horoscope
    HoroscopeView : Notes Enabled
    HoroscopeView : Bound to Horoscope h

    HoroscopeView --> MuhurtaView : Switch to My Muhurta
    note right of MuhurtaView
      1. Flush edits to Horoscope h
      2. Bind NotesPanel to Muhurta session
      3. Update Header: Notes — [Muhurta Name]
    end note
    MuhurtaView : Notes Enabled
    MuhurtaView : Bound to MyMuhurta

    MuhurtaView --> UtilityView : View Panchang / Ephemeris
    UtilityView : Notes Disabled (Icon disabled)
    UtilityView : Notes Panel folded or read-only

    UtilityView --> MuhurtaView : Return to My Muhurta
    MuhurtaView : Notes Re-enabled
    MuhurtaView : Bound back to MyMuhurta
```

### Step-by-Step Context Switching Rules:
1. **Flush Before Switching (`flushCurrentNotes()`):**
   - Before leaving the current view, `AstroSoft` calls `chartNotesPanel.flushToOwner()`.
   - This writes any pending HTML text from the editor into the active `NotesOwner` memory object so edits are never lost.
2. **Bind New Owner (`bindNotesOwner(NotesOwner owner)`):**
   - When entering a document screen, `AstroSoft` passes the new `NotesOwner` to `chartNotesPanel`.
   - The editor loads `owner.getNotes()`, resets its undo manager, and sets the title/tooltip to `owner.getNotesOwnerTitle()`.
   - The toolbar `Command.NOTES` action is enabled (`true`).
3. **Handle Utility Views (`clearNotesOwner()`):**
   - When entering `PANCHANG_VIEW`, `EPHEMERIS_VIEW`, or `FIND_NAME_VIEW`, `AstroSoft` sets the active notes owner to `null`.
   - The toolbar `Command.NOTES` action is disabled (`false`).
   - If the Notes dock is currently open, it displays a disabled placeholder or safely folds without modifying any file.

---

## 5. Save & Dirty State Integration

1. **Typing Listener:**
   - When the user edits text in `ChartNotesPanel`, `NotesListener.onNotesModified()` fires.
   - This marks the active `NotesOwner` as modified and enables `Command.SAVE` and `Command.SAVE_AS` on the main toolbar.
2. **Toolbar Save Trigger (`saveHoroscope()` / `saveDocument()`):**
   - Clicking **Save**:
     - Flushes the editor to the active `NotesOwner`.
     - Writes to the active document's `.ash` file:
       - If active screen is a **Horoscope view**, saves the `Horoscope`.
       - If active screen is **My Muhurta**, saves the `MyMuhurta` `.ash` file.
       - If active screen is **Marriage Match**, saves the `Compactibility` `.ash` file.
   - If the active document has no file path yet, triggers **Save As** with the `.ash` file chooser.

---

## 6. XML Schema Specification (`.ash`)

All persistent records share the `.ash` file extension. The root XML element dictates document identity and parser routing:

### 6.1 Horoscope Document (`.ash`)
```xml
<BirthData>
  <Name>Nikhil</Name>
  <Sex>Male</Sex>
  <Date>15</Date>
  <Month>8</Month>
  <Year>1985</Year>
  <Hour>10</Hour>
  <Minute>30</Minute>
  <Place city="Chennai" latitude="13.0827" longitude="80.2707" timezone="5.5" />
  <Notes><![CDATA[<html><body><p>Consultation note for career...</p></body></html>]]></Notes>
</BirthData>
```

### 6.2 My Muhurta Document (`.ash`)
```xml
<Muhurta version="2.0">
  <Event>
    <Name>House Warming</Name>
    <Date>2026-10-27</Date>
    <Time>11:17:49</Time>
    <Place city="Ramachandrapuram" latitude="16.85" longitude="82.02" timezone="5.5" />
  </Event>
  <Person1 type="Horoscope">
    <!-- Standalone BirthData excluding person's private horoscope notes -->
    <BirthData>
      <Name>HouseWarming-Husband</Name>
      ...
    </BirthData>
  </Person1>
  <Person2 type="QuickEntry">
    <Name>HouseWarming-Wife</Name>
    <Rasi>Mesha</Rasi>
    <Nakshathra>Aswini</Nakshathra>
  </Person2>
  <Notes><![CDATA[<html><body><p>Recommended Subha Hora window 08:51 AM - 09:49 AM...</p></body></html>]]></Notes>
</Muhurta>
```

### 6.3 Marriage Compatibility Document (`.ash` / planned)
```xml
<Compactibility version="2.0" hasHoroscope="true">
  <BoyData>...</BoyData>
  <GirlData>...</GirlData>
  <Notes><![CDATA[<html><body><p>Guna Milan score 28/36. Excellent match for marriage...</p></body></html>]]></Notes>
</Compactibility>
```

---

## 7. Auto-Routing on `File -> Open`

When the user invokes `File -> Open` (or clicks the Open toolbar icon) and selects any `.ash` file:
1. `AstroSoft` reads the root element via `XMLHelper.parseXML(...)`:
   - Root is `<BirthData>`: Instantiates `Horoscope`, binds notes, routes to `CHART_VIEW`.
   - Root is `<Muhurta>`: Instantiates `MyMuhurta`, binds notes, routes to `MY_MUHURTA_VIEW`.
   - Root is `<Compactibility>`: Instantiates `Compactibility`, binds notes, routes to `MARRIAGE_MATCH_VIEW`.
2. Title bar updates accordingly:
   - `Astrosoft 2.0 - Nikhil.ash`
   - `Astrosoft 2.0 - HouseWarming.ash [Muhurta]`
   - `Astrosoft 2.0 - Rohit-Priya.ash [Marriage Match]`

---

## 8. Dirty-State Tracking & Prompt Lifecycle

To protect user work, every document implementing `NotesOwner` tracks its in-memory modification state (`isModified()`).

### 8.1 Mutation Triggers
- **Notes Edits**: When the user edits content in `ChartNotesPanel`, `setNotes(...)` detects text changes and marks `modified = true`.
- **Document Edits**: In `MyMuhurtaView`, changing date, time, place, or profile invokes `document.setModified(true)`. Creating or editing a chart via `computeHoroscope` marks `h.setModified(true)`.
- **Save Reset**: Whenever a document is loaded from disk (`createFromFile`) or successfully written to disk (`saveToFile`), `modified` is reset to `false`.

### 8.2 Guarded Transitions
Before replacing or closing an active document with unsaved changes, `promptSaveIfModified(NotesOwner)` is triggered during:
1. **File $\rightarrow$ Open**: Opening another `.ash` horoscope or `.asm` Muhurta profile.
2. **File $\rightarrow$ New Horoscope**: Creating a new chart.
3. **Application Exit**: Window close button ('x'), `Cmd+Q` / macOS QuitHandler, or File $\rightarrow$ Exit.

### 8.3 Dialog Action Matrix
```
"Do you want to save changes to '<Document Name>'?"
[ Save ]  [ Don't Save ]  [ Cancel ]
```
| Selection | Action | Result |
| :--- | :--- | :--- |
| **`Save`** | Saves directly if `filePath` exists; prompts Save As file chooser if new. | If save succeeds $\rightarrow$ proceeds with open/exit. If user cancels file picker $\rightarrow$ aborts. |
| **`Don't Save`** | Discards unsaved in-memory changes (`setModified(false)`). | Proceeds immediately with opening the new document or closing the app. |
| **`Cancel` / Close [X]** | Aborts the requested operation entirely. | Window stays open; new file is not opened; editor and in-memory chart remain intact. |

---

## 9. Spreadsheet & Table Preservation Architecture

### 9.1 Supported Ingestion Formats
1. **Microsoft Excel (`text/html`)**:
   - Sanitizes Excel Office markup (`MsoNormalTable`, XML namespaces, Office styles) into standard HTML `<table>`.
   - Normalizes cells to `<table border="1" cellpadding="4" cellspacing="0" style="border-collapse: collapse; margin-top: 4px; margin-bottom: 6px;">`.
   - Shades header rows (`#f2f2f2` / `#f0f4f8`) and formats column titles with bold tags (`<b>`).
2. **Google Sheets (`text/html`)**:
   - Strips `<google-sheets-html-origin>` wrappers while preserving table structure, cell values, and column dimensions.
3. **Tab-Delimited TSV (`text/plain`)**:
   - `isTabDelimitedTable(rawText)` automatically detects multi-line spreadsheet pastes with consistent tab count ($\ge 2$ rows with tabs).
   - `convertTabDelimitedToHtmlTable(rawText)` generates structured HTML tables dynamically from plain-text clipboard data.
4. **Mixed Text & Tables**:
   - Preserves introductory paragraphs, embedded tables, and conclusion paragraphs seamlessly in document order.

### 9.2 XML Serialization & Reload
- All table markup (`<table>`, `<tr>`, `<th>`, `<td>`) is wrapped in `<Notes><![CDATA[...]]></Notes>`.
- `normalizeHtmlContent()` preserves table structures intact on reload without paragraph flattening.
- Comprehensive automated test suite in `ChartNotesXmlTest.java`:
  - `testExcelHtmlTablePasteAndXmlRoundTrip()`
  - `testGoogleSheetsHtmlTablePasteAndXmlRoundTrip()`
  - `testTabDelimitedPlainTextSpreadsheetTableConversion()`
  - `testMixedNotesWithParagraphsAndSpreadsheetTables()`


