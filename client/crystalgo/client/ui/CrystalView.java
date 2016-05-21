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
        this.crystalControls = new CrystalControls(game::doPass);
        this.msgs = new JTextArea(20, 20);
        this.msgin = new JTextField(20);
        jBoard = new JBoard(game.getState().getBoard());
        setLayout(new BorderLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        JPanel jBoardPanel = new JPanel();
        jBoardPanel.setLayout(new GridBagLayout());
        jBoardPanel.add(jBoard, c);
        this.add(jBoardPanel, BorderLayout.CENTER);
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.add(crystalControls, BorderLayout.NORTH);
        sidebar.add(msgs, BorderLayout.CENTER);
        sidebar.add(msgin, BorderLayout.SOUTH);
        msgs.setEditable(false);
        this.add(sidebar, BorderLayout.EAST);


        game.addInvalidMoveListener(state -> {
            jBoard.setHoverColor(Color.red);
            jBoard.repaint();
        });
        game.addStateListener(state -> {
            jBoard.setBoard(state.getBoard());
            Move prev = state.getPreviousMove().orElse(null);
            jBoard.highlight(prev);
            if (prev != null && prev.isPass()) {
                msgs.append(state.getTurn().inverse() + " has passed!\n");
            }
            if (state.getWinner() != Role.spectate) {
                msgs.append(state.getWinner() + " wins!\n");
            }
        });
        jBoard.addPressListener(point -> {
            Move move = new Move(point.x, point.y);
            game.doMove(move);
        });
    }

}
