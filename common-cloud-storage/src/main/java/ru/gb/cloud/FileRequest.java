package ru.gb.cloud;

public class FileRequest extends Message {
    private String filename;
    private long size;

    public FileRequest(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }
}
