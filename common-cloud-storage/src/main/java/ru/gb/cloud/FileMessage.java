package ru.gb.cloud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends Message{
    private String filename;
    private byte[] buffer;
    private long size;

    //todo BIG FILE & Directory
    public FileMessage(Path path) throws IOException {
        filename = path.getFileName().toString();
        buffer = Files.readAllBytes(path);
        size = Files.size(path);

    }

    public byte[] getBuffer() {
        return buffer;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }
}
