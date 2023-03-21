package protocol.returnobjects;

import protocol.data.FileHeader;
import protocol.data.ResponseBody;

public class Response {
    public FileHeader fileHeader;
    public ResponseBody responseBody;

    public Response() {
        super();
        this.fileHeader = null;
        this.responseBody = null;
    }
}
