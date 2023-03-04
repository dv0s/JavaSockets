package server.handlers;

import server.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class Put implements CommandHandler {

    public final BufferedReader clientIn;
    public final PrintWriter clientOut;

    public Put(BufferedReader clientIn, PrintWriter clientOut){
        this.clientIn = clientIn;
        this.clientOut = clientOut;
    }
    @Override
    public void handle() {
        System.out.println(output());
    }

    @Override
    public String output() {
        return "Command 'PUT' called";
    }
}
