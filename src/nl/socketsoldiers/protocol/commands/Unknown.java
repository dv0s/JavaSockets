package nl.socketsoldiers.protocol.commands;

import nl.socketsoldiers.protocol.enums.Constants;
import nl.socketsoldiers.protocol.enums.Invoker;
import nl.socketsoldiers.protocol.utils.ConnectionSockets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Unknown {

    private final Invoker invoker;
    private final ConnectionSockets connectionSockets;

    public Unknown(Invoker invoker, ConnectionSockets connectionSockets){
        this.invoker = invoker;
        this.connectionSockets = connectionSockets;
    }

    public void handle() throws IOException {
        PrintWriter out = new PrintWriter(connectionSockets.commSocket.getOutputStream(), true);
        out.println(output());

    }

    public String output(){
        return Constants.Strings.END_OF_TEXT.toString();
    }
}
