package crystalgo.client.net;

import crystalgo.client.Board;
import crystalgo.client.Role;

/**
 * Contains a ServerPacket
 * Created by user on 13/05/16.
 */
public interface ServerPacket {}

class BoardPacket implements ServerPacket {
    public final Board board;

    public BoardPacket(Board board) {
        this.board = board;
    }
}

class InvalidMovePacket implements ServerPacket {
}

class WinnerPacket implements ServerPacket {
    public final Role winner;

    public WinnerPacket(Role winner) {
        this.winner = winner;
    }
}

class MessagePacket implements ServerPacket {
    public final String msg;

    public MessagePacket(String msg) {
        this.msg = msg;
    }
}


