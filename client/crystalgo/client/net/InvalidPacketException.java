package crystalgo.client.net;

import java.io.IOException;

/**
 * Represents an invalid packet sent by the server.
 * Created by user on 13/05/16.
 */
public class InvalidPacketException extends IOException {
    public InvalidPacketException() {
        super();
    }

    public InvalidPacketException(String s) {
        super(s);
    }

    public InvalidPacketException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InvalidPacketException(Throwable throwable) {
        super(throwable);
    }
}
