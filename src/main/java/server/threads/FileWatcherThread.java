package server.threads;

import server.handlers.Delete;
import server.handlers.Get;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.ArrayList;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcherThread extends Thread {
    private final Socket socket;

    public FileWatcherThread(Socket socket) {
        super();
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                WatchService watchService = FileSystems.getDefault().newWatchService();
                PrintWriter clientOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // TODO:: Houd de folder in de gaten. Zodra een bestand is gewijzigd connect met de communicationThread
            // Client folder is defined here
            String directoryPath = System.getProperty("user.home") + File.separator + "Avans33FileSync";
            Path directory = Paths.get(directoryPath);

            // Create dir if not exists
            if (!dirExists(directory)) {
                createDir(directory);
            }

            directory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            System.out.println("FileWatcher enabled");

            boolean poll = true;
            while (poll) {
                WatchKey key = watchService.take();
                Path path = (Path) key.watchable();

                for (WatchEvent<?> event : key.pollEvents()) {
                    File file = path.resolve((Path) event.context()).toFile();

                    ArrayList<String> params = new ArrayList<>();
                    params.add(file.getName());

                    // Send the file to the client
                    if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
                        new Get(clientIn, clientOut, params);
                        System.out.println("ENTRY_CREATE - File : " + file.getName());
                    }

                    // Delete file from the client
                    if (event.kind() == ENTRY_DELETE) {
                        new Delete(clientIn, clientOut, params);
                        System.out.println("ENTRY_DELETE - File : " + file.getName());
                    }
                }

                poll = key.reset();
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean dirExists(Path path) {
        return Files.exists(path);
    }

    private void createDir(Path path) throws IOException {
        Files.createDirectory(path);
    }
}
