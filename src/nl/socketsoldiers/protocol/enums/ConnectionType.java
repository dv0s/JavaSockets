package nl.socketsoldiers.protocol.enums;

import java.util.Arrays;
import java.util.Optional;

public enum ConnectionType {
    COMM(Constants.Integers.COMM_PORT.getValue()),
    DATA(Constants.Integers.DATA_PORT.getValue());

    private final int value;
    ConnectionType(int value) {
        this.value = value;
    }

    // Function to return the ConnectionType based on int.
    public static Optional<ConnectionType> valueOf(int value){
        return Arrays.stream(values())
                .filter(ConnectionType -> ConnectionType.value == value)
                .findFirst();
    }
}
