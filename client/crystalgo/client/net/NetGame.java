package crystalgo.client.net;

import crystalgo.client.Board;
import crystalgo.client.Move;
import crystalgo.client.Role;
import crystalgo.client.engine.GoGame;
import crystalgo.client.engine.GoState;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * An implementation of the GoGame interface which functions over the net and has one local player.
 * Created by user on 13/05/16.
 */
public class NetGame implements GoGame {

    private final ArrayList<Consumer<GoState>> stateListeners = new ArrayList<>(),
            invalidMoveListeners = new ArrayList<>();
    private final ArrayList<Consumer<String>> messageListeners = new ArrayList<>();

    private final Connection connection;
    private Thread thread = null;
    private IOException ioe = null;
    private GoState state;

    public NetGame(Connection connection) {
        this.connection = connection;
    }

    public synchronized void startThread() {
        if (thread != null)
            throw new IllegalStateException("thread already started");
        thread = new Thread(() -> {
            try (Closeable c = connection) {
                while (true) {
                    ServerPacket p = connection.nextPacket();
                    if (p == null)
                        return;
                    handlePacket(p);
                }
            } catch (IOException ioe) {
                NetGame.this.ioe = ioe;
            }
        });
        thread.start();
    }

    private void handlePacket(ServerPacket p) throws InvalidPacketException {
        if (p instanceof BoardPacket) {
            BoardPacket bp = (BoardPacket) p;
            GoState gostate;
            if (state == null || bp.move == null)
                gostate = new GoState(bp.turn, bp.board, null, 0, bp.move, Role.spectate);
            else
                gostate = new GoState(bp.turn, bp.board, state, state.stateIndex() + 1, bp.move, state.getWinner());
            this.state = gostate;
            stateListeners.forEach(listener -> listener.accept(gostate));
            return;
        }
        if (p instanceof InvalidMovePacket) {
            invalidMoveListeners.forEach(listener -> listener.accept(state));
            return;
        }
        if (p instanceof MessagePacket) {
            String msg = ((MessagePacket) p).msg;
            messageListeners.forEach(listener -> listener.accept(msg));
            return;
        }
        if (p instanceof WinnerPacket) {
            GoState gostate = state.setWinner(((WinnerPacket) p).winner);
            state = gostate;
            stateListeners.forEach(listener -> listener.accept(gostate));
            return;
        }
        throw new InvalidPacketException("Unsupported packet type " + p.getClass());
    }

    @Override
    public void addStateListener(Consumer<GoState> listener) {
        stateListeners.add(listener);
    }

    @Override
    public void addInvalidMoveListener(Consumer<GoState> listener) {
        invalidMoveListeners.add(listener);
    }

    public void addMessageListener(Consumer<String> listener) {
        messageListeners.add(listener);
    }

    @Override
    public GoState getState() {
        checkThread();
        if (state == null)
            return GoState.newGame(new Board(19, 19));
        return state;
    }

    @Override
    public void doMove(Move move) {
        checkThread();
        connection.doMove(move);
        try {
            connection.flush();
        } catch (IOException ioe) {
            this.ioe = ioe;
        }
    }

    @Override
    public void doPass() {
        checkThread();
        connection.doPass();
        try {
            connection.flush();
        } catch (IOException ioe) {
            this.ioe = ioe;
        }
    }

    private void checkThread() {
        if (ioe != null) {
            IOException ioe = this.ioe;
            this.ioe = null;
            throw new RuntimeException("Server thread threw exception.", ioe);
        }
    }
}
