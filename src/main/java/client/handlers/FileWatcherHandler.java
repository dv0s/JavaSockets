package client.handlers;

import protocol.enums.Constants;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Scanner;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcherHandler implements Runnable {
    private final Scanner scanner;
    private final WatchService watchService;

    private ArrayList<File> changedFiles;

    private ArrayList<File> deletedFiles;

    private boolean busy;

    public FileWatcherHandler() {
        try {
            this.scanner = new Scanner(System.in);
            this.watchService = FileSystems.getDefault().newWatchService();

            getClientDir().register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            System.out.println("FileWatcher started ...");
            run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            boolean poll = true;

            while (poll) {
                String fromUser;
                fromUser = scanner.nextLine().toUpperCase();

                if (fromUser.equals("CLOSE")) {
                    break;
                }

                WatchKey key = watchService.take();
                Path path = (Path) key.watchable();

                for (WatchEvent<?> event : key.pollEvents()) {
                    File file = path.resolve((Path) event.context()).toFile();

                    if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
                        onPut(event.kind().toString(), file);
                    }

                    if (event.kind() == ENTRY_DELETE) {
                        onDelete(event.kind().toString(), file);
                    }
                }

                poll = key.reset();
            }

            scanner.close();
            watchService.close();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void onPut(String kind, File file) {
        System.out.println("FILE-WATCHER : " + kind + " - File :" + file.getName());
        System.out.println("Do you want to send this file to the server? (Y/N): ");
        String fromUser = scanner.nextLine().toUpperCase();

        if (fromUser.equals("Y")) {
            System.out.println("Sending file to user...");
            // new Put(invoker, homeDirectory, socket, in, out).handle(params);
        }
    }

    private void onDelete(String kind, File file) {
        System.out.println("FILE-WATCHER : " + kind + " - File :" + file.getName());
        System.out.println("Do you want to delete this file to the server? (Y/N): ");
        String fromUser = scanner.nextLine().toUpperCase();

        if (fromUser.equals("Y")) {
            System.out.println("Sending file to user...");
            // new Delete(invoker, homeDirectory, socket, in, out).handle(params);
        }
    }

    private Path getClientDir() throws IOException {
        String directoryPath = Constants.BASE_DIR + File.separator + "client";
        return Paths.get(directoryPath);
    }
}
