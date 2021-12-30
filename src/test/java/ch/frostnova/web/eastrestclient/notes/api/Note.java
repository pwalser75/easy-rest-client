package ch.frostnova.web.eastrestclient.notes.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * DTO for Note.
 */
public class Note {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("created")
    private OffsetDateTime created;

    @JsonProperty("updated")
    private OffsetDateTime updated;

    @JsonProperty("text")
    private String text;

    public Note() {
        // default constructor
    }

    public Note(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + id + ": " + text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Objects.equals(id, note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}