package server.handlers;

import server.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class Open implements CommandHandler {
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;

    public Open(BufferedReader clientIn, PrintWriter clientOut){
        this.clientIn = clientIn;
        this.clientOut = clientOut;
    }
    @Override
    public void handle() {
        System.out.println(output());
    }

    @Override
    public String output() {
        return "Command 'OPEN' called";
    }
}
