package crystalgo.client.engine;

import crystalgo.client.Board;
import crystalgo.client.Move;
import crystalgo.client.Role;

import java.util.Optional;

/**
 * A class which represents the state of a game of go. This class is immutable.
 * Created by user on 13/05/16.
 */
public interface GameState {

    Role getTurn();
    Board getBoard();

    /**
     * Returns the previous state, or null if no previous state.
     * @return The previous state, or null if no previous state.
     */
    GameState getPreviousState();
    int stateIndex();
    /**
     * Returns the previous move, or null if no previous move.
     *
     * This function returns null if and only if getPreviousState() returns null.
     * @return The previous move, or null if no previous move.
     */
    Move getMove();

    /**
     * Performs the move in the argument.
     * @param move The move to perform.
     * @return The next GameState, or empty if the move is invalid.
     * @throws IllegalStateException If the GameState doesn't support performing further moves.
     */
    Optional<GameState> doMove(Move move) throws IllegalStateException;

}
