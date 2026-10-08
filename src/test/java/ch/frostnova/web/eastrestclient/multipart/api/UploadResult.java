package ch.frostnova.web.eastrestclient.multipart.api;

import java.util.List;
import java.util.Objects;

public class UploadResult {

    private String title;
    private String filename;
    private long fileSize;
    private String fileSha256;
    private List<String> tags;
    private String meta;

    public UploadResult() {
    }

    public UploadResult(String title, String filename, long fileSize, String fileSha256, List<String> tags) {
        this.title = title;
        this.filename = filename;
        this.fileSize = fileSize;
        this.fileSha256 = fileSha256;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSha256() {
        return fileSha256;
    }

    public void setFileSha256(String fileSha256) {
        this.fileSha256 = fileSha256;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UploadResult)) {
            return false;
        }
        UploadResult that = (UploadResult) o;
        return fileSize == that.fileSize && Objects.equals(title, that.title) && Objects.equals(filename, that.filename)
                && Objects.equals(fileSha256, that.fileSha256) && Objects.equals(tags, that.tags)
                && Objects.equals(meta, that.meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, filename, fileSize, fileSha256, tags, meta);
    }

    @Override
    public String toString() {
        return "UploadResult{title='" + title + "', filename='" + filename + "', fileSize=" + fileSize
                + ", fileSha256='" + fileSha256 + "', tags=" + tags + ", meta='" + meta + "'}";
    }
}
