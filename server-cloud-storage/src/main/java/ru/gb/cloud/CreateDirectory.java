package ru.gb.cloud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateDirectory {
    private static Path SERVER_DIR = Paths.get("server-cloud-storage","Server-DATA");

    public Path createClientDir(String login) {
        Path clientDir = SERVER_DIR.resolve(createClientDir(login));
        if (!Files.exists(clientDir)) {
            try {
                Files.createDirectory(clientDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return clientDir;
    }
}
