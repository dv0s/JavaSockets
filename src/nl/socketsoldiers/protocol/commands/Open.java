package nl.socketsoldiers.protocol.commands;

import nl.socketsoldiers.protocol.enums.Constants;
import nl.socketsoldiers.protocol.enums.Invoker;
import nl.socketsoldiers.protocol.handlers.ConnectionHandler;
import nl.socketsoldiers.protocol.interfaces.ICommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;

public class Open implements ICommand {
    public final Invoker invoker;
    public final Path homeDirectory;
    public final ConnectionHandler connection;

    public Open(Invoker invoker, Path homeDirectory, ConnectionHandler connection) {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.connection = connection;
    }

    public void handle(ArrayList<String> args) throws IOException {
        PrintWriter out = new PrintWriter(connection.commSocket.getOutputStream(), true);;
        out.println(output());
    }

    public String output() {
        String output = "Command 'OPEN' called";
        return output + Constants.Strings.END_OF_TEXT;
    }
}
