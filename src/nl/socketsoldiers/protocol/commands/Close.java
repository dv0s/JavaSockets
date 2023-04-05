package nl.socketsoldiers.protocol.commands;

import nl.socketsoldiers.protocol.enums.Command;
import nl.socketsoldiers.protocol.enums.Constants;
import nl.socketsoldiers.protocol.enums.Invoker;
import nl.socketsoldiers.protocol.interfaces.ICommand;
import nl.socketsoldiers.protocol.utils.ConnectionSockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Close implements ICommand {
    public final Invoker invoker;
    public final ConnectionSockets connectionSockets;

    public BufferedReader in;
    public PrintWriter out;

    public Close(Invoker invoker, ConnectionSockets connectionSockets) throws IOException {
        this.invoker = invoker;
        this.connectionSockets = connectionSockets;
        this.in = new BufferedReader(new InputStreamReader(connectionSockets.commSocket.getInputStream()));
        this.out = new PrintWriter(connectionSockets.commSocket.getOutputStream(), true);
    }

    public void handle(ArrayList<String> args) {
        if (invoker == Invoker.SERVER) {
            out.println("Disconnecting from server...");
            out.println(output());
            out.close();

            try {
                in.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            out.println(Command.CLOSE);
        }
    }

    public String output() {
        return Constants.Strings.END_OF_TRANSMISSION.toString();
    }
}
