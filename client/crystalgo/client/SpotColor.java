package crystalgo.client;

/**
 * Either black, white or empty
 * Created by user on 13/05/16.
 */
public enum SpotColor {
    black(2, Role.black), white(1, Role.white), empty(0, Role.spectate);
    public final byte color;
    public final Role role;
    SpotColor(int color, Role role) {
        this.color = (byte) color;
        this.role = role;
    }
    public static SpotColor fromChar(char c) {
        switch (c) {
            case ' ':
                return empty;
            case 'o':
                return white;
            case 'x':
                return black;
        }
        throw new IllegalArgumentException("Not a spotcolor: " + c);
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
        throw new IllegalArgumentException("Not a spotcolor: " + b);
    }
}
