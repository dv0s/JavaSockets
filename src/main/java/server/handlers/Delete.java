package server.handlers;

import server.interfaces.CommandHandler;

public class Delete implements CommandHandler {
    @Override
    public void handle() {
        System.out.println(output());
    }

    @Override
    public String output() {
        return "Command 'DELETE' called";
    }
}
