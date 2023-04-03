package protocol.enums;

public enum ConnectionType {
    COMM(Constants.Integers.COMM_PORT.getValue()),
    DATA(Constants.Integers.DATA_PORT.getValue());

    ConnectionType(int value) {

    }
}
