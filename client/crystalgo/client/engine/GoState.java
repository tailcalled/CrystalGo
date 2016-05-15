package crystalgo.client.engine;

import crystalgo.client.Board;
import crystalgo.client.Move;
import crystalgo.client.Role;

import java.util.Optional;

/**
 * A class which represents the state of a game of go. This class is immutable.
 * Created by user on 13/05/16.
 */
public final class GoState {

    private final Role turn;
    private final Board board;
    private final GoState parent;
    private final int depth;
    private final Move move;
    private final boolean over;

    public GoState(Role turn, Board board, GoState parent, int depth, Move move, boolean over) {
        this.turn = turn;
        this.board = board;
        this.parent = parent;
        this.depth = depth;
        this.move = move;
        this.over = over;
    }

    public static GoState newGame(Board board) {
        return new GoState(Role.black, board, null, 0, null, false);
    }

    public Role getTurn() {
        return turn;
    }

    public Board getBoard() {
        return board;
    }

    public Optional<GoState> getPreviousState() {
        return Optional.ofNullable(parent);
    }

    public int stateIndex() {
        return depth;
    }

    public Optional<Move> getPreviousMove() {
        return Optional.ofNullable(move);
    }

    public GoState gameIsOver() {
        return new GoState(turn, board, parent, depth, move, true);
    }

    /**
     * Performs the move in the argument. This function does not check if a move is invalid.
     * @param move The move to perform.
     * @return The next GoState.
     */
    public GoState doMove(Move move) {
        Board b = board.setSpot(move.x, move.y, getTurn().spotcolor);
        return new GoState(turn.inverse(), b, this, depth + 1, move, over);
    }

    public boolean isGameOver() {
        return over;
    }

}

