package protocol.commands;

import protocol.enums.Constants;
import protocol.enums.Invoker;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;

public class Open{
    public final Invoker invoker;
    public final Path homeDirectory;
    public final Socket socket;
    public final BufferedReader in;
    public final PrintWriter out;

    public Open(Invoker invoker, Path homeDirectory, Socket socket, BufferedReader clientIn, PrintWriter clientOut) {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.socket = socket;
        this.in = clientIn;
        this.out = clientOut;
    }

    public void handle(ArrayList<String> args) {
        out.println(output());
    }

    public String output() {
        String output = "Command 'OPEN' called";
        return output + Constants.Strings.END_OF_TEXT;
    }
}
