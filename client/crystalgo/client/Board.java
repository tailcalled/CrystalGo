package crystalgo.client;

/**
 * Represents a go board
 * Created by user on 13/05/16.
 */
public class Board {
    final int width, height;
    byte[][] data;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        data = new byte[height][width];
    }

    public void setBlack(int x, int y) {
        data[y][x] = 2;
    }
    public void setWhite(int x, int y) {
        data[y][x] = 1;
    }
    public void setNone(int x, int y) {
        data[y][x] = 0;
    }
    public void setSpot(int x, int y, SpotColor sc) {
        data[y][x] = sc.color;
    }
    public SpotColor get(int x, int y) {
        return SpotColor.fromByte(data[y][x]);
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

}
/*
f(' ') = 0
f('o') = 1
f('x') = 2
 */


