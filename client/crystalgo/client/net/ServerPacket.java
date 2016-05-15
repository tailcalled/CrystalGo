package crystalgo.client.net;

import crystalgo.client.Board;
import crystalgo.client.Move;
import crystalgo.client.Role;

/**
 * Contains a ServerPacket
 * Created by user on 13/05/16.
 */
public interface ServerPacket {}

class BoardPacket implements ServerPacket {
    public final Board board;
    public final Move move;
    public final Role turn;
    public final int white_points, black_points;

    public BoardPacket(Board board, Move move, Role turn, int white_points, int black_points) {
        this.board = board;
        this.move = move;
        this.turn = turn;
        this.white_points = white_points;
        this.black_points = black_points;
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


