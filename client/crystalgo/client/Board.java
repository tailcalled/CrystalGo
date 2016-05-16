package crystalgo.client;

/**
 * Represents a go board. This class is immutable.
 * Created by user on 13/05/16.
 */
public final class Board implements Cloneable {
    public final int width, height;
    private final byte[][] data;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        data = new byte[height][width];
    }

    public static Board parse(String[] lines) {
        Board b = new Board(lines[0].length(), lines.length);
        for (int j = 0; j < lines.length; j++) {
            char[] chars = lines[j].toCharArray();
            byte[] dat = b.data[j];
            for (int i = 0; i < chars.length; i++) {
                dat[i] = SpotColor.fromChar(chars[i]).color;
            }
        }
        return b;
    }

    public Board setSpot(int x, int y, SpotColor sc) {
        Board clone = clone();
        clone.data[y][x] = sc.color;
        return clone;
    }

    public SpotColor get(int x, int y) {
        return SpotColor.fromByte(data[y][x]);
    }

    @Override
    public Board clone() {
        Board b = new Board(width, height);
        for (int i = 0; i < height; i++)
            System.arraycopy(b.data[i], 0, data[i], 0, width);
        return b;
    }

}

