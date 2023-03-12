package protocol.returnobjects;

import protocol.data.ResponseBody;
import protocol.data.FileHeader;

public class Response {
    public FileHeader fileHeader;
    public ResponseBody responseBody;

    public Response(){
        super();
        this.fileHeader = null;
        this.responseBody = null;
    }
}
