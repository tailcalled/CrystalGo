package crystalgo.client.ui;

import crystalgo.client.Board;
import crystalgo.client.Move;
import crystalgo.client.SpotColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * A class for drawing a go board on the screen.
 * Created by user on 13/05/16.
 */
public final class JBoard extends JComponent {

    private Board board;
    private Color white = Color.white, black = Color.black,
            black_highlight_color = Color.green, white_highlight_color = Color.blue;
    private int cellPix = 33, borderPix = 1;
    private ArrayList<Consumer<Point>> pressListeners = new ArrayList<>();
    private Move highlight;

    public JBoard(Board board) {
        this.board = board;
        setOpaque(true);
        setBackground(new Color(0x44, 0x44, 0x44));
        setForeground(new Color(0x55, 0x55, 0x55));
        updateSize();
        addMouseListener(new JBML());
    }

    public Color getWhite() {
        return white;
    }

    public void setWhite(Color white) {
        this.white = white;
    }

    public Color getBlack() {
        return black;
    }

    public void setBlack(Color black) {
        this.black = black;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        Board b = this.board;
        this.board = board;
        this.highlight = null;
        if (b.height != board.height || b.width != board.width)
            updateSize();
        repaint();
    }
    public void highlight(Move move) {
        this.highlight = move;
    }

    public int getCellPix() {
        return cellPix;
    }

    public void setCellPix(int cellPix) {
        this.cellPix = cellPix;
        updateSize();
    }

    public int getBorderPix() {
        return borderPix;
    }

    public void setBorderPix(int borderPix) {
        this.borderPix = borderPix;
        updateSize();
    }

    private void updateSize() {
        Dimension dim = getPreferredSize();
        setSize(dim);
        setPreferredSize(dim);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = getPreferredSize();
        return new Dimension(dim.width * 16, dim.height * 16);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(getBoard().width * 2, getBoard().height * 2);
    }

    @Override
    public Dimension getPreferredSize() {
        int f = cellPix;
        return new Dimension(f * getBoard().width - f, f * getBoard().height - f);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        int w = getWidth();
        int h = getHeight();
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, w, h);
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getForeground());
        int bw = getBoard().width;
        int bh = getBoard().height;
        int lineHeight = ((bh-1)*h) / bh + 1;
        int lineLength = ((bw-1)*w) / bw + 1;
        for (int x = 0; x < bw; x++) {
            int pixx = ((2*x + 1) * w) / (2 * bw);
            g.fillRect(pixx, w / (2*bw), borderPix, lineHeight);
        }
        for (int y = 0; y < getBoard().height; y++) {
            int pixy = ((2*y + 1) * h) / (2 * bh);
            g.fillRect(h / (2*bh), pixy, lineLength, borderPix);
        }
        g.setStroke(thinStroke);
        int hx = highlight == null ? -1 : highlight.x;
        int hy = highlight == null ? -1 : highlight.y;
        int circr = Math.min((45*w) / (100*bw), (45*h) / (100*bh));
        for (int x = 0; x < getBoard().width; x++) {
            int pixx = ((2*x + 1) * w) / (2 * bw);
            for (int y = 0; y < getBoard().height; y++) {
                SpotColor sc = getBoard().get(x, y);
                int pixy = ((2*y + 1) * h) / (2 * bh);
                switch (sc) {
                    case black:
                        g.setColor(this.black);
                        fillCircle(pixx, pixy, circr, g);
                        if (hx == x && hy == y) {
                            g.setColor(this.black_highlight_color);
                            drawCircle(pixx, pixy, circr, g);
                        }
                        break;
                    case white:
                        g.setColor(this.white);
                        fillCircle(pixx, pixy, circr, g);
                        if (hx == x && hy == y)
                            g.setColor(this.white_highlight_color);
                        else
                            g.setColor(this.black);
                        drawCircle(pixx, pixy, circr, g);
                        break;
                }
            }
        }
        g.dispose();
    }
    private void drawCircle(int x, int y, int r, Graphics g) {
        g.drawOval(x - r, y - r, 2*r, 2*r);
    }
    private void fillCircle(int x, int y, int r, Graphics g) {
        g.fillOval(x - r, y - r, 2*r, 2*r);
    }
    private static final Stroke thinStroke = new BasicStroke(2);

    public void addPressListener(Consumer<Point> l) {
        pressListeners.add(l);
    }

    private void handlePress(int x, int y) {
        Point p = new Point(x, y);
        pressListeners.forEach(l -> l.accept(p));
    }

    private class JBML implements MouseListener {

        private int press_x = -1, press_y = -1;

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            press_x = getCell(mouseEvent.getX(), getBoard().width);
            press_y = getCell(mouseEvent.getY(), getBoard().height);
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            int px = getCell(mouseEvent.getX(), getBoard().width);
            int py = getCell(mouseEvent.getY(), getBoard().height);
            if (px != press_x || py != press_y)
                return;
            if (px == -1 || py == -1)
                return;
            handlePress(px, py);
        }

        private int getCell(int coord, int max) {
            int div = cellPix;
            int cell = coord / div;
            if (cell < 0 || cell >= max)
                return -1;
            return cell;
        }

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
        }
    }
}

