package server.handlers;

import server.interfaces.CommandHandler;

public class Put implements CommandHandler {
    @Override
    public void handle() {
        System.out.println(output());
    }

    @Override
    public String output() {
        return "Command 'PUT' called";
    }
}
