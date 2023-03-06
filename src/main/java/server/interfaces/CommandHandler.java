package server.interfaces;

import java.io.IOException;

public interface CommandHandler {
    void handle() throws IOException;
    String output();
}
