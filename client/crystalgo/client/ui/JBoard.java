package crystalgo.client.ui;

import crystalgo.client.Board;
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
    private Color white = new Color(60, 60, 60), black = Color.black;
    private int cellPix = 32, borderPix = 4;
    private ArrayList<Consumer<Point>> pressListeners = new ArrayList<>();

    public JBoard(Board board) {
        this.board = board;
        setOpaque(true);
        setBackground(Color.white);
        setForeground(Color.black);
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
        if (b.height != board.height || b.width != board.width)
            updateSize();
        repaint();
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
        Dimension dim = new Dimension(borderPix + f * board.width, borderPix + f * board.height);
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
        return new Dimension(f * board.width, f * board.height);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        if (isOpaque()) {
            g.setColor(getBackground());
            int w = getWidth();
            int h = getHeight();
            g.fillRect(0, 0, w, h);
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int f = cellPix;
        g.setColor(getForeground());
        int offset = (cellPix - borderPix) / 2;
        for (int x = 0; x < board.width; x++) {
            g.fillRect(offset + x * f, offset, borderPix, getHeight() - 2 * offset);
        }
        for (int y = 0; y < board.height; y++) {
            g.fillRect(offset, offset + y * f, getWidth() - 2 * offset, borderPix);
        }
        for (int x = 0; x < board.width; x++) {
            for (int y = 0; y < board.height; y++) {
                SpotColor sc = board.get(x, y);
                switch (sc) {
                    case black:
                        g.setColor(this.black);
                        break;
                    case white:
                        g.setColor(this.white);
                        break;
                    case empty:
                        continue;
                }
                g.fillOval(2 + f * x, 2 + f * y, cellPix - 4, cellPix - 4);
            }
        }
        g.dispose();
    }

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
            press_x = getCell(mouseEvent.getX(), board.width);
            press_y = getCell(mouseEvent.getY(), board.height);
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            int px = getCell(mouseEvent.getX(), board.width);
            int py = getCell(mouseEvent.getY(), board.height);
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

