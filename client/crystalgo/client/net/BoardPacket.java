package crystalgo.client.net;

import crystalgo.client.Board;

public class BoardPacket implements ServerPacket {
    public final Board board;

    public BoardPacket(Board board) {
        this.board = board;
    }
}
