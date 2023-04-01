package client.handlers;

import protocol.commands.Delete;
import protocol.commands.Put;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.ConnectionHandler;

import java.io.*;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
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
        this.args = args;
        scanner = new Scanner(System.in);
        clientDir = Paths.get(Constants.BASE_DIR + File.separator + "client");
        run();
    }

    @Override
    public void run() {
        startFileWatcher();

        Thread monitor = new Thread(() -> {
            System.out.println(clientDir + " is in monitoring state...");
            monitorChanges();
        });

        monitor.start();

        // User has sent data, so close the thread
        if (scanner.nextLine().equalsIgnoreCase("CLOSE")) {
            monitor.interrupt();
        }

        if (monitor.isInterrupted()) {
            try {
                // Create a server connection
                connection = new ConnectionHandler(Invoker.CLIENT, clientDir).establish(args);

                System.out.println("Sending new files to server...");
                putToServer();

                System.out.println("Deleting files ...");
                deleteOnServer();

                // Close the server connection
                connection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void monitorChanges() {
        try {
            boolean poll = true;

            while (poll) {
                WatchKey key = watchService.take();
                Path path = (Path) key.watchable();

                for (WatchEvent<?> event : key.pollEvents()) {
                    handleWatchEvent(event, path);
                }

                poll = key.reset();
            }
        } catch (InterruptedException | NoSuchAlgorithmException | IOException e) {
            if (e instanceof InterruptedException) {
                closeFileWatcher();
                Thread.currentThread().interrupt();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private static void handleWatchEvent(WatchEvent<?> event, Path path) throws NoSuchAlgorithmException, IOException {
        File file = path.resolve((Path) event.context()).toFile();

        if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
            // Remove from deletedFiles array
            deletedFiles.remove(file);

            // Add only if not already exist.
            if (!changedFiles.contains(file)) changedFiles.add(file);
        }

        if (event.kind() == ENTRY_DELETE) {
            // Remove from changedFiles array
            changedFiles.remove(file);

            // Add only if not already exist.
            if (!deletedFiles.contains(file)) deletedFiles.add(file);
        }
    }

    private static void putToServer() {
        // We are creating one object in memory
        ArrayList<String> args = new ArrayList<>();

        for (File file : changedFiles) {
            // Add file to the arraylist
            args.add(file.getName());

            System.out.println("Send to server: " + file.getName());
            new Put(Invoker.CLIENT, clientDir, connection.socket, connection.in, connection.out).handle(args);

            // Remove file from the arraylist
            args.remove(0);
        }
    }

    private static void deleteOnServer() {
        // We are creating one object in memory
        ArrayList<String> args = new ArrayList<>();

        for (File file : deletedFiles) {
            // Add file to the arraylist
            args.add(file.getName());

            System.out.println("Delete on server: " + file.getName());
            //new Delete(Invoker.CLIENT, clientDir, connection.socket, connection.in, connection.out).handle(args);

            // Remove file from the arraylist
            args.remove(0);
        }
    }

    private static void startFileWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            clientDir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void closeFileWatcher() {
        try {
            if (watchService != null) {
                watchService.close();
                System.out.println("FileWatcher is no longer in monitor state...");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
