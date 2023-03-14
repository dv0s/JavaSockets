package protocol.interfaces;

import java.util.ArrayList;

public interface CommandHandler {
    void handle(ArrayList<String> args);
    String output();
}
