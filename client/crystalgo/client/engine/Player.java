package crystalgo.client.engine;

import crystalgo.client.Role;

import java.util.function.Consumer;

/**
 * A player is a source of moves.
 * This is the method with which new GameStates are created.
 * Created by user on 13/05/16.
 */
public interface Player {

    /**
     * Tells the player that the game is ready for the next move.
     * The argument performer can only be called once.
     *
     * @param performer This function receives the new state that the player wishes to perform.
     */
    void readyForNextMove(Consumer<GoState> performer);

    Role getRole();

    /**
     * This function is called when the GoState changes, including when the player itself has changed the board.
     *
     * @param state The new GoState.
     */
    void newGoState(GoState state);

}
