package client.handlers;

import protocol.enums.Constants;

import java.io.*;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher extends Thread {
    private final WatchService watchService;

    public FileWatcher() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            getClientDir().register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            StringBuilder backspaces = new StringBuilder();
            boolean poll = true;

            String clientCommandTrigger = "Command: ";

            // TODO:: Deze nog fixen
            backspaces.append("\b".repeat(clientCommandTrigger.length() + 1));

            while (poll) {
                if (currentThread().isInterrupted()) {
                    break;
                }

                WatchKey key = watchService.take();
                Path path = (Path) key.watchable();

                for (WatchEvent<?> event : key.pollEvents()) {
                    File file = path.resolve((Path) event.context()).toFile();

                    // Send the file to the client
                    if (event.kind() == ENTRY_CREATE) {
                        // Send information to the client
                        System.out.println(backspaces + "FILE-WATCHER : ENTRY_CREATE - File : " + file.getName());

                        // Client can give a new command
                        System.out.print(clientCommandTrigger);
                    }

                    if (event.kind() == ENTRY_MODIFY) {
                        // Send information to the client
                        System.out.println(backspaces + "FILE-WATCHER : ENTRY_MODIFY - File : " + file.getName());

                        // Client can give a new command
                        System.out.print(clientCommandTrigger);
                    }

                    // Delete file from the client
                    if (event.kind() == ENTRY_DELETE) {
                        // Send information to the client
                        System.out.println(backspaces + "FILE-WATCHER : ENTRY_DELETE - File : " + file.getName());

                        // Client can give a new command
                        System.out.print(clientCommandTrigger);
                    }
                }

                poll = key.reset();
            }

            watchService.close();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                currentThread().interrupt();
            } else {
                throw new RuntimeException(e);
            }
        }

    }

    private Path getClientDir() throws IOException {
        String directoryPath = Constants.BASE_DIR + File.separator + "client";
        return Paths.get(directoryPath);
    }
}
