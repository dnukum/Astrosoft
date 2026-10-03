package app.astrosoft.beans;

import java.text.SimpleDateFormat;

/**
 * Table cell value for the Date column of the Balam Calendar.
 * Renders as a formatted start date/time in the table, while carrying the
 * underlying {@link BalamRowBean} so the UI can show full transit details
 * (transit Nakshatra, Rasi, start/end times) without re-querying the engine.
 */
public class BalamDateCell {
    private final BalamRowBean row;
    private final SimpleDateFormat format;

    public BalamDateCell(BalamRowBean row, SimpleDateFormat format) {
        this.row = row;
        this.format = format;
    }

    public BalamRowBean getRow() { return row; }

    public String formatStart() { return format.format(row.getStartTime()); }

    public String formatEnd() { return format.format(row.getEndTime()); }

    @Override
    public String toString() { return formatStart(); }
}
