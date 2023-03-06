package server.handlers;

import protocol.returnobjects.Message;
import server.interfaces.CommandHandler;

import java.io.*;

public class Close implements CommandHandler {
    public final ObjectInputStream clientIn;
    public final ObjectOutputStream clientOut;

    public Close(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
    }

    @Override
    public void handle() throws IOException {
        this.clientOut.writeObject(new Message("END", true));
    }

    @Override
    public String output() {
        return "Disconnecting from server...";
    }
}
