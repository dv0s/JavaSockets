package nl.socketsoldiers.protocol.commands;

import nl.socketsoldiers.protocol.data.FileMetaData;
import nl.socketsoldiers.protocol.enums.Command;
import nl.socketsoldiers.protocol.enums.Constants;
import nl.socketsoldiers.protocol.enums.Invoker;
import nl.socketsoldiers.protocol.handlers.ConnectionHandler;
import nl.socketsoldiers.protocol.handlers.FileHandler;
import nl.socketsoldiers.protocol.interfaces.ICommand;
import nl.socketsoldiers.protocol.utils.ConnectionSockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.sql.Array;
import java.util.ArrayList;

public class List implements ICommand {

    public final Invoker invoker;
    public final Path homeDirectory;
    public final ConnectionSockets connectionSockets;

    public Socket socket;
    public BufferedReader in;
    public PrintWriter out;

    public List(Invoker invoker, Path homeDirectory, ConnectionSockets connectionSockets) throws IOException {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.connectionSockets = connectionSockets;

        this.socket = connectionSockets.commSocket;
        this.in = new BufferedReader(new InputStreamReader(connectionSockets.commSocket.getInputStream()));
        this.out = new PrintWriter(connectionSockets.commSocket.getOutputStream(), true);
    }


    public void handle(ArrayList<String> args) {
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
