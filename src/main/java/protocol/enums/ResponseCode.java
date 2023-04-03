package protocol.enums;

public enum ResponseCode {
    NONE(0),
    PROCESSING(100),
    SUCCESS(200),
    ERROR(400),
    FAILURE(500),
    UNKNOWN(-1);

    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String toString(){ return String.valueOf(code); }
}
