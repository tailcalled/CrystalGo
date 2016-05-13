package crystalgo.client;

import java.io.*;
import java.net.Socket;

/**
 * A connection to a go server
 * Created by user on 12/05/16.
 */
public class Connection {
    private final BufferedReader in;
    private final PrintWriter out;
    public Connection(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }
    public Connection(InputStream in, OutputStream out) {
        this(new BufferedReader(new InputStreamReader(in)), new PrintWriter(out));
    }
    public Connection(Socket sock) throws IOException {
        this(sock.getInputStream(), sock.getOutputStream());
    }
    public Connection(String ip, int port) throws IOException {
        this(new Socket(ip, port));
    }

    public Role getRole(Role wish) throws IOException {
        if (wish == null) {
            Role r = getRole(Role.black);
            if (r == null)
                r = getRole(Role.white);
            if (r == null)
                r = getRole(Role.spectate);
            return r;
        } else {
            out.println(wish.toString());
            String role = in.readLine();
            if (role.equals("no")) {
                return null;
            }
            return wish;
        }
    }

}

