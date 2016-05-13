package crystalgo.client.engine;

import crystalgo.client.Move;

import java.util.function.Consumer;

/**
 * Method for a player to interact with a game.
 * When doMove is called, either all the StateListeners or all the InvalidMoveListeners will be called
 * the new state.
 * Created by user on 13/05/16.
 */
public interface GoGame {

    void addStateListener(Consumer<GoState> listener);

    void addInvalidMoveListener(Consumer<GoState> listener);

    GoState getState();

    void doMove(Move move);

}
