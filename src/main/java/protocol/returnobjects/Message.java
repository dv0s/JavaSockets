package protocol.returnobjects;

import java.io.Serializable;

public class Message implements Serializable {
    public String message;
    public boolean messageEnd;

    public Message(String message, boolean messageEnd){
        super();
        this.message = message;
        this.messageEnd = messageEnd;
    }
}