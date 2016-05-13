package crystalgo.client.net;

import crystalgo.client.Role;
import crystalgo.client.engine.GoState;
import crystalgo.client.engine.Player;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * A player over the net.
 * An instance of this class manages a thread for reading from the server.
 * Created by user on 13/05/16.
 */
public class NetPlayer implements Player {

    private Thread thread = null;
    private Connection connection;
    private Consumer<GoState> consumer;
    private Consumer<String> messageHandler = str -> {
    };
    private Queue<ServerPacket> packets = new ConcurrentLinkedQueue<>();
    private boolean stop;
    private IOException threadFail = null;

    public void startThread() {
        if (thread != null)
            throw new IllegalStateException("double start server");
        stop = false;
        Runnable run = () -> {
            try {
                while (!stop) {
                    ServerPacket packet = connection.nextPacket();
                    packets.add(packet);
                    handlePackets();
                }
            } catch (IOException io) {
            }
            thread = null;
        };
        thread = new Thread(run);
        thread.start();
    }

    @Override
    public void readyForNextMove(Consumer<GoState> consumer) {
        this.consumer = consumer;
        handlePackets();
    }

    private void handlePackets() {
        while (!packets.isEmpty()) {
            ServerPacket packet = packets.poll();

        }
    }

    @Override
    public Role getRole() {
        return null;
    }

    @Override
    public void newGoState(GoState state) {

    }
}
