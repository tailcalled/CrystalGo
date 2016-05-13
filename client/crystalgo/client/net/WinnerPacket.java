package crystalgo.client.net;

import crystalgo.client.Role;

public class WinnerPacket implements ServerPacket {
    public final Role winner;

    public WinnerPacket(Role winner) {
        this.winner = winner;
    }
}
