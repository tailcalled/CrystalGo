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
        int f = cellPix + borderPix;
        Dimension dim = new Dimension(borderPix + f * getBoard().width, borderPix + f * getBoard().height);
        setSize(dim);
        setPreferredSize(dim);
    }

    @Override
    public Dimension getPreferredSize() {
        return getSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getSize();
    }

    @Override
    public Dimension getSize() {
        int f = cellPix;
        return new Dimension(f * getBoard().width, f * getBoard().height);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        Rectangle bounds = g.getClipBounds();
        double factor1 = bounds.getWidth() / (double)getWidth();
        double factor2 = bounds.getHeight() / (double)getHeight();
        double factor = Math.min(factor1, factor2);
        g.scale(factor, factor);
        if (isOpaque()) {
            g.setColor(getBackground());
            int w = getWidth();
            int h = getHeight();
            g.fillRect(0, 0, w, h);
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int f = cellPix;
        g.setColor(getForeground());
        int offset = cellPix / 2;
        for (int x = 0; x < getBoard().width; x++) {
            g.fillRect(offset + x * f, offset, borderPix, getHeight() - 2 * offset);
        }
        for (int y = 0; y < getBoard().height; y++) {
            g.fillRect(offset, offset + y * f, getWidth() - 2 * offset, borderPix);
        }
        g.setStroke(thinStroke);
        int hx = highlight == null ? -1 : highlight.x;
        int hy = highlight == null ? -1 : highlight.y;
        for (int x = 0; x < getBoard().width; x++) {
            for (int y = 0; y < getBoard().height; y++) {
                SpotColor sc = getBoard().get(x, y);
                switch (sc) {
                    case black:
                        g.setColor(this.black);
                        fillCircle(x*f + offset, y*f + offset, cellPix/2 - 4, g);
                        if (hx == x && hy == y) {
                            g.setColor(this.black_highlight_color);
                            drawCircle(x*f + offset, y*f + offset, offset - 4, g);
                        }
                        break;
                    case white:
                        g.setColor(this.white);
                        fillCircle(x*f + offset, y*f + offset, offset - 4, g);
                        if (hx == x && hy == y)
                            g.setColor(this.white_highlight_color);
                        else
                            g.setColor(this.black);
                        drawCircle(x*f + offset, y*f + offset, offset - 4, g);
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

