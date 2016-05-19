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
public final class CrystalView extends JPanel {

    public final JBoard jBoard;
    public final CrystalControls crystalControls;
    public final JTextArea msgs;
    public final JTextField msgin;
    private final GoGame game;

    public CrystalView(GoGame game, Container comp) {
        this.game = game;
        this.crystalControls = new CrystalControls();
        this.msgs = new JTextArea(20, 20);
        this.msgin = new JTextField(20);
        jBoard = new JBoard(game.getState().getBoard());
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(new GridBagLayout());
        //c.anchor = GridBagConstraints.CENTER;
        c.ipadx = 30;
        c.ipady = 30;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0f;
        c.gridheight = 3;
        this.add(jBoard, c);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0f / 3.0f;
        c.weighty = 0.0f;
        this.add(crystalControls, c);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.0f / 3.0f;
        c.weighty = 1.0f;
        c.fill = GridBagConstraints.VERTICAL;
        this.add(msgs, c);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0f / 3.0f;
        c.weighty = 0.0f;
        this.add(msgin, c);


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
