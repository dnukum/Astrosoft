package app.astrosoft.beans;

/**
 * NotesOwner.java
 *
 * Common contract for any document entity (Horoscope, Muhurta, MarriageMatch)
 * that owns persistent notes.
 */
public interface NotesOwner {

    String getNotes();

    void setNotes(String notes);

    String getNotesOwnerTitle();

    String getFilePath();

    void setFilePath(String path);

    boolean isModified();

    void setModified(boolean modified);
}
