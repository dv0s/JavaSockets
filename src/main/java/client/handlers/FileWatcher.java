package client.handlers;

import java.io.*;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {
    public static Path clientDir() throws IOException {
        // Client folder is defined here
        String directoryPath = System.getProperty("user.home") + File.separator + "Avans33FileSync";
        Path directory = Paths.get(directoryPath);

        // Create dir if not exists
        if (!dirExists(directory)) {
            createDir(directory);
        }

        return directory;
    }

    private static boolean dirExists(Path path) {
        return Files.exists(path);
    }

    private static void createDir(Path path) throws IOException {
        Files.createDirectory(path);
    }
}
