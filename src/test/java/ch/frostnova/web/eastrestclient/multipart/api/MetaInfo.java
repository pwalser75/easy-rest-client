package ch.frostnova.web.eastrestclient.multipart.api;

import java.util.Objects;

/**
 * A structured (JSON-encoded) multipart part.
 */
public class MetaInfo {

    private String author;
    private int revision;

    public MetaInfo() {
    }

    public MetaInfo(String author, int revision) {
        this.author = author;
        this.revision = revision;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaInfo)) return false;
        MetaInfo metaInfo = (MetaInfo) o;
        return revision == metaInfo.revision && Objects.equals(author, metaInfo.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, revision);
    }

    @Override
    public String toString() {
        return "MetaInfo{author='" + author + "', revision=" + revision + '}';
    }
}
