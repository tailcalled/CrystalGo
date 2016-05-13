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

    Optional<GameState> getPreviousState();
    int stateIndex();
    Optional<Move> getPreviousMove();

    /**
     * Performs the move in the argument.
     * @param move The move to perform.
     * @return The next GameState, or empty if the move is invalid.
     * @throws IllegalStateException If the GameState doesn't support performing further moves.
     */
    Optional<GameState> doMove(Move move) throws IllegalStateException;

}
