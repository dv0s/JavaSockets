package client.threads;

import protocol.enums.Constants;

import java.io.*;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcherThread extends Thread {
    private final WatchService watchService;

    public FileWatcherThread() {
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
                WatchKey key = watchService.take();
                Path path = (Path) key.watchable();

                for (WatchEvent<?> event : key.pollEvents()) {
                    String fileWatcherState = null;
                    File file = path.resolve((Path) event.context()).toFile();

                    // Send the file to the client
                    if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
                        fileWatcherState = event.kind() == ENTRY_CREATE ? "ENTRY_CREATE" : "ENTRY_MODIFY";
                    }

                    // Delete file from the client
                    if (event.kind() == ENTRY_DELETE) {
                        fileWatcherState = "ENTRY_DELETE";
                        // TODO:: implement command when available
                    }

                    System.out.println(backspaces + "FILE-WATCHER : " + fileWatcherState + " - File :" + file.getName());

                    // TODO:: implement server
                    // new Put(invoker, homeDirectory, socket, in, out).handle(params);
                    // new Delete(in, out, params).handle(params);

                    // Client can give a new command
                    System.out.print(clientCommandTrigger);
                }

                poll = key.reset();
            }

            watchService.close();
            interrupt();
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
