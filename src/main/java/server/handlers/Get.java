package server.handlers;

import server.interfaces.CommandHandler;

// Get Handler for handling the get command
public class Get implements CommandHandler {

    public void handle(){
        System.out.println(output());
    }

    public String output(){
        return "GET CALLED";
    }
}
