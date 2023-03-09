package server.handlers;

public class BaseHandler {
    public String endOfText(){
        return "\u0003";
    };

    public String endOfTransmission(){
        return "\u0004";
    }
}
