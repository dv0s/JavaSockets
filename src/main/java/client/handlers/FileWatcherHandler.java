package client.handlers;

import protocol.enums.Constants;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Scanner;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcherHandler implements Runnable {
    private static Path clientDir;

    private static WatchService watchService;

    private static ArrayList<File> changedFiles = new ArrayList<>();

    private static ArrayList<File> deletedFiles = new ArrayList<>();

    private static boolean busy;

    public FileWatcherHandler() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            clientDir = Paths.get(Constants.BASE_DIR + File.separator + "client");

            clientDir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

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
                WatchKey key = watchService.take();
                Path path = (Path) key.watchable();

                for (WatchEvent<?> event : key.pollEvents()) {
                    File file = path.resolve((Path) event.context()).toFile();

                    if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
                        changedFiles.add(file);
                    }

                    if (event.kind() == ENTRY_DELETE) {
                        deletedFiles.add(file);
                    }
                }

                poll = key.reset();
            }

            watchService.close();

            busy = true;
            System.out.println("Sending new files to server...");
            onPut(changedFiles);

            // TODO:: Deleting locally and remotely (server side)
            busy = true;
            System.out.println("Deleting files ...");
            onDelete(deletedFiles);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void onPut(ArrayList<File> files) {
        for (File file : files) {
            System.out.println("Send to server: " + file.getPath());
            // new Put(invoker, homeDirectory, socket, in, out).handle(params);
        }
        busy = false;
    }

    private static void onDelete(ArrayList<File> files) {
        for (File file : files) {
            if (file.delete()) {
                System.out.println("File deleted locally: " + file.getPath());
                System.out.println("Delete on server: " + file.getPath());
            }
            // new Put(invoker, homeDirectory, socket, in, out).handle(params);
        }
        busy = false;
    }

    public static boolean isBusy() {
        return busy;
    }
}
