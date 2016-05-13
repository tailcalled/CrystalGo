package crystalgo.client.engine;

import crystalgo.client.Role;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Controls the players.
 * Created by user on 13/05/16.
 */
public class GoGame {

    private final ArrayList<Player> spectators = new ArrayList<>();
    private Player white, black;
    private GoState state;
    private Role waitingFor = Role.spectate;

    public void startGame() throws IllegalStateException {
        if (waitingFor != Role.spectate)
            throw new IllegalStateException("Game already started");
        poll(Role.white);
    }

    public void addPlayer(Player player) {
        if (player.getRole() == Role.white) {
            if (white != null)
                throw new IllegalStateException("this game already has a white player");
            white = player;
            return;
        }
        if (player.getRole() == Role.black) {
            if (black != null)
                throw new IllegalStateException("this game already has a black player");
            black = player;
            return;
        }
        spectators.add(player);
    }

    /**
     * Makes a game where all the moves come from a single source.
     * Usually used for things like spectating a net game.
     *
     * @param spectator The source of all moves.
     */
    public void spectatorGame(Player spectator) {
        if (white != null) {
            if (black != null)
                throw new IllegalStateException("this game already has a white and a black player");
            throw new IllegalStateException("this game already has a white player");
        }
        if (black != null)
            throw new IllegalStateException("this game already has a black player");
        white = spectator;
        black = spectator;
    }

    private void poll(final Role role) {
        if (waitingFor == Role.spectate)
            throw new IllegalArgumentException("poll a spectator");
        waitingFor = role;
        final Player play = role == Role.white ? white : black;
        play.readyForNextMove(move -> {
            state = state.doMove(move);
            updateState();
            poll(role.inverse());
        });
    }

    private void updateState() {
        white.newGoState(state);
        black.newGoState(state);
        for (Player player : spectators) {
            if (player == white || player == black)
                continue;
            player.newGoState(state);
        }
    }

    public Iterable<GoState> getStates() {
        return () -> new Iterator<GoState>() {
            private GoState state = GoGame.this.state;

            @Override
            public boolean hasNext() {
                return state != null;
            }

            @Override
            public GoState next() {
                GoState r = state;
                state = state.getPreviousState().orElse(null);
                return r;
            }
        };
    }

}
