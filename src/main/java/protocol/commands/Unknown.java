package protocol.commands;

import protocol.enums.Constants;
import protocol.enums.Invoker;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Unknown {
    public final Invoker invoker;
    public final Socket socket;

    public Unknown(Invoker invoker, Socket socket) {
        this.invoker = invoker;
        this.socket = socket;
    }

    public void handle() throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(output());
    }

    public String output() {
        return Constants.END_OF_TEXT.toString();
    }
}