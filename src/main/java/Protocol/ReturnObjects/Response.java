package Protocol.ReturnObjects;

import Protocol.Data.ResponseBody;
import Protocol.Data.ResponseHeader;
import Protocol.Enums.ResponseType;

public class Response {
    public ResponseHeader responseHeader;
    public ResponseBody responseBody;

    public Response(){
        super();
        this.responseHeader = null;
        this.responseBody = null;
    }
}
