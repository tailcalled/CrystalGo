package crystalgo.client;

/**
 * Represents a move on a go board.
 * Immutable.
 * Created by user on 13/05/16.
 */
public class Move {

    public final int x, y;

    public Move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = x; i > 0; i /= 'Z'-'A'+1) {
            sb.append(i % ('Z'-'A'+1) + 'A');
        }
        return sb + " " + y;
    }

}
