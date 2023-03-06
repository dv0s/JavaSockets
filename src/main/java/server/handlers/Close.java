package server.handlers;

import server.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class Close implements CommandHandler {
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;

    public Close(BufferedReader clientIn, PrintWriter clientOut) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
    }

    @Override
    public void handle() {
        this.clientOut.println("END");
    }

    @Override
    public String output() {
        return "Disconnecting from server...";
    }
}
