package crystalgo.client.ui;

import crystalgo.client.Move;
import crystalgo.client.Role;
import crystalgo.client.engine.GoGame;

import javax.swing.*;
import java.awt.*;

/**
 * A JComponent which
 * Created by user on 15/05/16.
 */
public final class GoView extends JPanel {

    public final JBoard jBoard;
    private final GoGame game;

    public GoView(GoGame game, Container comp) {
        this.game = game;
        jBoard = new JBoard(game.getState().getBoard());
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        this.add(jBoard, c);
        setLayout(layout);
        game.addInvalidMoveListener(state ->
                JOptionPane.showMessageDialog(jBoard, "Invalid move.", "Invalid move.", JOptionPane.ERROR_MESSAGE)
        );
        game.addStateListener(state -> {
            jBoard.setBoard(state.getBoard());
            jBoard.highlight(state.getPreviousMove().orElse(null));
            if (state.getWinner() != Role.spectate) {
                Role role = state.getWinner();
                JOptionPane.showMessageDialog(jBoard, role + " has won.", role + " has won.",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        jBoard.addPressListener(point -> {
            Move move = new Move(point.x, point.y);
            game.doMove(move);
        });
    }

}
