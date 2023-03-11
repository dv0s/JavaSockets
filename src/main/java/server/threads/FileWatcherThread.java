package server.threads;

import server.events.FileEvent;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;

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
                WatchService watchService = FileSystems.getDefault().newWatchService()
        ) {
            Path path = Paths.get("E:\\IdeaProjects\\AvansELU33ServerFiles");
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            System.out.println("FileWatcher enabled");

            boolean poll = true;
            while (poll) {
                poll = pollEvents(watchService);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean pollEvents(WatchService watchService) throws InterruptedException {
        WatchKey key = watchService.take();
        Path path = (Path) key.watchable();

        for (WatchEvent<?> event : key.pollEvents()) {
            notifyListeners(event.kind(), path.resolve((Path) event.context()).toFile());
        }

        return key.reset();
    }

    private void notifyListeners(WatchEvent.Kind<?> kind, File file) {
        FileEvent event = new FileEvent(file);

        if (kind == ENTRY_CREATE) {
            System.out.println("ENTRY_CREATE - File : " + event.getFile());
        }

        if (kind == ENTRY_MODIFY) {
            System.out.println("ENTRY_MODIFY - File : " + event.getFile());
        }

        if (kind == ENTRY_DELETE) {
            System.out.println("ENTRY_DELETE - File : " + event.getFile());
        }
    }
}
