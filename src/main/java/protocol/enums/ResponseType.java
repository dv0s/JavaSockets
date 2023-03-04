package protocol.enums;

public enum ResponseType {

    // Will use status code 2xx
    SUCCESS{
        @Override
        public String toString() { return "SUCCESS"; }
    },
    // Will use status code 4xx
    ERROR{
        @Override
        public String toString() { return "ERROR"; }
    },
    // Will use status code 5xx
    FAILURE{
        @Override
        public String toString() { return "FAILURE"; }
    };

    public static ResponseType fromInt(int x){
        return switch(x){
            case 0 -> SUCCESS;
            case 1 -> ERROR;
            case 2 -> FAILURE;
            default -> null;
        };
    }

    public static ResponseType fromString(String responseType){
        String str = responseType.trim().toUpperCase();

        return switch(str){
            case "SUCCESS" -> SUCCESS;
            case "ERROR" -> ERROR;
            case "FAILURE" -> FAILURE;
            default -> null;
        };
    }
}
