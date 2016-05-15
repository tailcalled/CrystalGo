package crystalgo.client.net;

import crystalgo.client.Board;
import crystalgo.client.Move;
import crystalgo.client.Role;

import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Stack;

/**
 * A connection to a go server
 * Created by user on 12/05/16.
 */
public class Connection implements Closeable, Flushable {
    private final BufferedReader in;
    private final PrintWriter out;
    private final ArrayDeque<String> messages = new ArrayDeque<>();
    private final Stack<String> pushback = new Stack<>();
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
            String role = nextLine();
            if (role.equals("no")) {
                return null;
            }
            return wish;
        }
    }
    public ServerPacket nextPacket() throws IOException {
        try {
            if (!messages.isEmpty())
                return new MessagePacket(messages.pollFirst());
            String nextLine = readLine();
            if (nextLine == null)
                return null;
            if (nextLine.startsWith("msg ")) {
                return new MessagePacket(nextLine.substring(4));
            }
            String[] split = nextLine.split(" ");
            if (split.length == 1) {
                if (nextLine.equals("no")) {
                    return new InvalidMovePacket();
                }
            }
            if (split.length != 2)
                throw new InvalidPacketException();
            if (split[1].equals("wins"))
                return new WinnerPacket(Role.valueOf(split[0]));
            pushback.push(nextLine);
            return fetchBoard();
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw e;
            }
            throw new InvalidPacketException(e);
        }
    }

    private BoardPacket fetchBoard() throws IOException {
        String l = nextLine();
        String[] line = l.split(" ");
        int h = Integer.parseInt(line[1]);
        String[] board = new String[h];
        for (int y = 0; y < h; y++) {
            board[y] = nextLine();
        }
        Board b = Board.parse(board);
        String[] points = nextLine().split(" ");
        int black_points = Integer.parseInt(points[0]);
        int white_points = Integer.parseInt(points[1]);
        Role turn = Role.valueOf(points[2]);
        Move move;
        if (points.length > 3)
            move = new Move(Integer.parseInt(points[3]), Integer.parseInt(points[4]));
        else
            move = null;
        return new BoardPacket(b, move, turn, white_points, black_points);
    }
    private String nextLine() throws IOException {
        String l = readLine();
        while (l.startsWith("msg ")) {
            messages.addLast(l.substring(4));
            l = readLine();
        }
        return l;
    }
    private String readLine() throws IOException {
        if (pushback.isEmpty())
            return in.readLine();
        return pushback.pop();
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }
}

