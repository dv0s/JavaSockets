package Models;

import Enums.MyState;

public class Response
{
    String command = null;
    String[] args = null;
    int code = 0;
    public MyState myState = null;
    public String message = null;

    public Response(String command, String[] args, int code, MyState myState, String message)
    {
        super();
        this.command = command;
        this.args = args;
        this.code = code;
        this.myState = myState;
        this.message = message;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public MyState getState() {
        return myState;
    }

    public void setState(MyState myState) {
        this.myState = myState;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResponse(int code, MyState myState, String message)
    {
        setCode(code);
        setState(myState);
        setMessage(message);
    }

    public String ToString()
    {
        return "command:" + command +
                ", args:" + args +
                ", code:" + code +
                ", state:" + myState.toString() +
                ", message:" + message;
    }
}
