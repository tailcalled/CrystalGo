package crystalgo.client;

/**
 * A role for a user in the go game.
 * Created by user on 12/05/16.
 */
public enum Role {
    black(SpotColor.black), white(SpotColor.white), spectate(SpotColor.empty);
    public final SpotColor spotcolor;
    Role(SpotColor sc) {
        this.spotcolor = sc;
    }
}
