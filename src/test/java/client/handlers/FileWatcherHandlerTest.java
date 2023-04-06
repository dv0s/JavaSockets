package client.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;


public class FileWatcherHandlerTest {
    private Thread monitor;
    private File file;
    private FileWatcherHandler fileWatcherHandler;

    @BeforeEach
    void setUp() {
        String[] args = {"localhost", "42069"};
        fileWatcherHandler = new FileWatcherHandler(args);

        monitor = new Thread(() -> fileWatcherHandler.monitorChanges());

        file = new File(fileWatcherHandler.getClientDir() + File.separator + "unitTestFile.txt");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void run() {
        try {
            monitor.start();
            file.createNewFile();

            int attempts = 0;

            // TODO:: Fix this
            while (monitor.isAlive()) {
                // This gives the fileWatcher enough time to process the input
                if (attempts >= 25) {
                    monitor.interrupt();
                }

                attempts++;
            }

            //file.delete();
            Assertions.assertEquals(1, fileWatcherHandler.getChangedFiles().size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}