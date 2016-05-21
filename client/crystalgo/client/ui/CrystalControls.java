package crystalgo.client.ui;

import javax.swing.*;

/**
 * A part of the CrystalGo ui, which contains the controls for a game.
 * Created by user on 16/05/16.
 */
public class CrystalControls extends JPanel {

    public CrystalControls(Runnable onPass) {
        JButton button = new JButton("Pass");
        button.addActionListener(e -> onPass.run());
        this.add(button);
    }

}
