package protocol.commands;

import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.FileHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;

public class List {

    public final Invoker invoker;
    public final Path homeDirectory;
    public final Socket socket;
    public final BufferedReader in;
    public final PrintWriter out;

    public List(Invoker invoker, Path homeDirectory, Socket socket, BufferedReader in, PrintWriter out) {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    public void handle(ArrayList<String> args) {
        System.out.println(homeDirectory.toString());

        if (invoker == Invoker.SERVER) {
            out.println(Command.LS + output());
        } else {
            out.println(FileHandler.directoryListAsString(homeDirectory) + output());
        }
    }

    public String output() {
        return Constants.Strings.END_OF_TEXT.toString();
    }
}
