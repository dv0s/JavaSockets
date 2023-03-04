package protocol.enums;

// Constants for this application
public enum Constants {
    // Values
    CHARSET("UTF-8"),
    HASHING_ALGORITHM("MD5");

    public final String value;

    private Constants(String value) {
        this.value = value;
    }
}
