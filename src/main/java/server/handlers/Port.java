package server.handlers;

import protocol.returnobjects.Message;
import server.interfaces.CommandHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Port implements CommandHandler {
    public final ObjectInputStream clientIn;
    public final ObjectOutputStream clientOut;
    public final ArrayList<String> params;

    public Port(ObjectInputStream clientIn, ObjectOutputStream clientOut, ArrayList<String> params) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
        this.params = params;
    }

    @Override
    public void handle() throws IOException {
        System.out.println(output());
        clientOut.writeObject(new Message(output(), true));
    }

    @Override
    public String output() {
        return "Command 'PORT' called";
    }
}
