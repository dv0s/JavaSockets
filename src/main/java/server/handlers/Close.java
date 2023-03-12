package server.handlers;

import protocol.enums.Constants;
import server.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class Close extends BaseHandler implements CommandHandler {
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;

    public Close(BufferedReader clientIn, PrintWriter clientOut) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
    }

    @Override
    public void handle() {
        this.clientOut.println(output());
    }

    @Override
    public String output() {
        return "Disconnecting from server..." + Constants.END_OF_TRANSMISSION;
    }
}
