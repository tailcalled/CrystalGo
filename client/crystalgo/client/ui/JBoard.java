package crystalgo.client.ui;

import crystalgo.client.Board;
import crystalgo.client.SpotColor;

import javax.swing.*;
import java.awt.*;

/**
 * A class for drawing a go board on the screen.
 * Created by user on 13/05/16.
 */
public final class JBoard extends JComponent {

    private Board board;
    private Color white = new Color(60, 60, 60), black = Color.black;
    private int cellPix = 32, borderPix = 4;

    public JBoard(Board board) {
        this.board = board;
        setOpaque(true);
        setBackground(Color.white);
        setForeground(Color.black);
        updateSize();
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
        setSize(borderPix + f * board.width, borderPix + f * board.height);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics g = graphics.create();
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        int f = cellPix + borderPix;
        for (int x = 0; x < board.width; x++) {
            g.fillRect(x * f, 0, borderPix, getHeight());
        }
        for (int y = 0; y < board.height; y++) {
            g.fillRect(0, y * f, getWidth(), borderPix);
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
                g.fillOval(2 + f * x + borderPix, 2 + f * y + borderPix, cellPix - 4, cellPix - 4);
            }
        }
        g.dispose();
    }
}

