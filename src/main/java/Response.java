public class Response
{
    String command = null;
    String[] args = null;
    int code = 0;
    State state = null;
    String message = null;

    public Response(String command, String[] args, int code, State state, String message)
    {
        super();
        this.command = command;
        this.args = args;
        this.code = code;
        this.state = state;
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResponse(int code, State state, String message)
    {
        setCode(code);
        setState(state);
        setMessage(message);
    }

    public String ToString()
    {
        return "command:" + command +
                ", args:" + args +
                ", code:" + code +
                ", state:" + state.toString() +
                ", message:" + message;
    }
}
