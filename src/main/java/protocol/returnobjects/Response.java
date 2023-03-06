package protocol.returnobjects;

import protocol.data.ResponseBody;
import protocol.data.ResponseHeader;

public class Response {
    public ResponseHeader responseHeader;
    public ResponseBody responseBody;

    public Response(ResponseHeader responseHeader, ResponseBody responseBody){
        super();
        this.responseHeader = responseHeader;
        this.responseBody = responseBody;
    }
}
