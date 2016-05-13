package crystalgo.client;

/**
 * Either black, white or empty
 * Created by user on 13/05/16.
 */
public enum SpotColor {
    black(2), white(1), empty(0);
    public final byte color;
    SpotColor(int color) {
        this.color = (byte) color;
    }
    public static SpotColor fromByte(byte b) {
        switch (b) {
            case 0:
                return empty;
            case 1:
                return white;
            case 2:
                return black;
        }
        throw new IllegalArgumentException("Not a color: " + b);
    }
}
