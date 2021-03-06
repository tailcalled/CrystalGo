package crystalgo.client;

/**
 * Represents a move on a go board. This class is immutable.
 * Created by user on 13/05/16.
 */
public final class Move {

    public final int x, y;
    public static final Move pass = new Move(-1, -1);

    public Move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isPass() {
        return x == -1;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = x; i > 0; i /= 'Z'-'A'+1) {
            sb.append(i % ('Z'-'A'+1) + 'A');
        }
        return sb + " " + y;
    }

}
