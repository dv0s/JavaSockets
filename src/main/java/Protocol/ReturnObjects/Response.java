package Protocol.ReturnObjects;

import Protocol.Data.ResponseBody;
import Protocol.Data.ResponseHeader;
import Protocol.Enums.ResponseType;

public class Response {
    public ResponseType responseType;
    public ResponseHeader responseHeader;
    public ResponseBody responseBody;

    public Response(){
        super();
        this.responseType = null;
        this.responseHeader = null;
        this.responseBody = null;
    }
}
