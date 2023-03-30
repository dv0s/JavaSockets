package protocol.interfaces;

import java.util.ArrayList;

public interface ICommand {
    void handle(ArrayList<String> args);

    String output();
}
