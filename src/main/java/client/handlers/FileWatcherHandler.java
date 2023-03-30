package client.handlers;

import protocol.commands.Put;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.ConnectionHandler;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Scanner;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcherHandler implements Runnable {
    private static Scanner scanner;
    private static Path clientDir;
    private final String[] args;
    private static ConnectionHandler connection;
    private static WatchService watchService;
    private static final ArrayList<File> changedFiles = new ArrayList<>();
    private static final ArrayList<File> deletedFiles = new ArrayList<>();

    public FileWatcherHandler(String[] args) {
        try {
            this.args = args;
            scanner = new Scanner(System.in);
            watchService = FileSystems.getDefault().newWatchService();
            clientDir = Paths.get(Constants.BASE_DIR + File.separator + "client");

            clientDir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            System.out.println("FileWatcher started...");
            run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Thread monitor = new Thread(() -> {
            try {
                System.out.println(clientDir + " is in monitoring state...");
                monitorChanges();
            } catch (InterruptedException | IOException e) {
                if (e instanceof InterruptedException) {
                    if (!Thread.currentThread().isInterrupted()) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println(clientDir + " is no longer in monitoring state...");
                } else {
                    throw new RuntimeException(e);
                }
            }
        });

        monitor.start();

        // User has sent data, so close the thread
        if (scanner.nextLine() != null) {
            monitor.interrupt();
        }

        if (monitor.isInterrupted()) {
            try {
                // Create a server connection
                connection = new ConnectionHandler(Invoker.CLIENT, clientDir).establish(args);

                System.out.println("Sending new files to server...");
                onPut();

                System.out.println("Deleting files ...");
                onDelete();

                // Close the server connection
                connection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void monitorChanges() throws InterruptedException, IOException {
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
    }

    private static void onPut() {
        for (File file : changedFiles) {
            System.out.println("Send to server: " + file.getPath());
            ArrayList<String> args = new ArrayList<>();
            args.add(file.getName());
            new Put(Invoker.CLIENT, clientDir, connection.socket, connection.in, connection.out).handle(args);
        }
    }

    private static void onDelete() {
        for (File file : deletedFiles) {
            System.out.println("Delete on server: " + file.getPath());
            // new Delete(invoker, homeDirectory, socket, in, out).handle(params);
        }
    }
}
