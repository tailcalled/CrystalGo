package crystalgo.client.ui;

import crystalgo.client.Role;
import crystalgo.client.net.Connection;
import crystalgo.client.net.NetGame;

import javax.swing.*;
import java.awt.*;

/**
 * Main class for the go client.
 * Created by user on 15/05/16.
 */
public class GoMain {

    public static void main(String[] args) throws Exception {
        String ip = JOptionPane.showInputDialog("Please enter server ip.");
        if (ip == null)
            return;
        if (ip.equals(""))
            ip = "127.0.0.1";
        int port = 8448;
        if (ip.contains(":")) {
            int index = ip.indexOf(":");
            port = Integer.parseInt(ip.substring(index + 1));
            ip = ip.substring(0, index);
        }
        Connection connection = new Connection(ip, port);
        Role role = connection.getRole(null);
        JFrame window = new JFrame("Go");
        window.setLayout(new BorderLayout());
        NetGame game = new NetGame(connection);
        GoView view = new GoView(game, window);
        window.add(view);
        window.setPreferredSize(view.getPreferredSize());
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        game.startThread();
        while (game.getState().getWinner() == Role.spectate) {
            Thread.sleep(10);
            game.getState();
        }
    }

}
