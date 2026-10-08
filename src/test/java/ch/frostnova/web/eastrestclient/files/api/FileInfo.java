package ch.frostnova.web.eastrestclient.files.api;

import java.util.Objects;

public class FileInfo {

    private String filename;
    private long size;
    private String sha256;

    public FileInfo() {
    }

    public FileInfo(String filename, long size, String sha256) {
        this.filename = filename;
        this.size = size;
        this.sha256 = sha256;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileInfo)) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) o;
        return size == fileInfo.size && Objects.equals(filename, fileInfo.filename) && Objects.equals(sha256, fileInfo.sha256);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, size, sha256);
    }

    @Override
    public String toString() {
        return "FileInfo{filename='" + filename + "', size=" + size + ", sha256='" + sha256 + "'}";
    }
}
