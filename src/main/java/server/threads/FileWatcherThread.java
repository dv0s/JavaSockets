package server.threads;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcherThread extends Thread {
    private final Socket socket;
    private final WatchService watchService;

    public FileWatcherThread(Socket socket) {
        super();
        this.socket = socket;
        this.watchService = getWatchService();
    }

    public void run() {
        try {
            System.out.println("FileWatcher enabled");

            Path path = Paths.get("E:\\IdeaProjects\\AvansELU33ServerFiles");
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            boolean poll = true;
            while (poll) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    System.out.println("Event kind : " + event.kind() + " - File : " + event.context());
                }

                poll = key.reset();
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private WatchService getWatchService() {
        try {
            return FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
