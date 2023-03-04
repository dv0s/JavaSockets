package protocol.returnobjects;

import protocol.data.ResponseBody;
import protocol.data.ResponseHeader;

public class Response {
    public ResponseHeader responseHeader;
    public ResponseBody responseBody;

    public Response(){
        super();
        this.responseHeader = null;
        this.responseBody = null;
    }
}
