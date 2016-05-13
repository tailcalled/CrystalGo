package crystalgo.server

import java.net.Socket
import crystalgo.server.io.LineOut
import crystalgo.server.io.ByteOut
import crystalgo.server.io.ByteIn
import crystalgo.server.io.LineIn
import java.io.OutputStream
import java.io.InputStream
import scala.annotation.tailrec
import crystalgo.server.io.In
import crystalgo.server.io.Out

class Client(val send: Out[ProS2C], val recv: In[ProC2S]) {
  def this(input: InputStream, output: OutputStream) =
    this(
      ServerProtocol.out(LineOut(new ByteOut(output))),
      ServerProtocol.in(LineIn(new ByteIn(input)))
    )
  def close() = { send.close(); recv.close() }
}

class Server(accept: () => Option[Client], closeSocket: () => Unit,
    message: (String, Client) => Unit = (s, c) => {}, size: Int = 19, komiMinusHalf: Int = 7) {
  
  var running = true
  
  var state = Game(Black, _ => 0, Board(Map(), size, size), Vector())
  var conns = Vector[Client]()
  var clients = Vector[Client]()
  var playerBlack: Client = null
  var playerWhite: Client = null
  val lock = new Object
  var prevMove: Option[Move] = None
  
  val acceptThread = new Thread("Accept Thread") {
    override def run() = {
      while (running) {
        accept() match {
          case None =>
          case Some(c) => connectClient(c)
        }
      }
    }
  }
  acceptThread.start()
  var threads: Vector[Thread] = Vector[Thread](acceptThread)
  
  private def connectClient(c: Client) = {
    conns :+= c
    val joinThread = new Thread("Joining Player") {
      override def run() = {
        joinClient(c)
      }
    }
    joinThread.start()
    lock.synchronized { threads :+= joinThread }
  }
  @tailrec private def joinClient(c: Client): Unit = {
    var go = false
    var ok = true
    c.recv.await() match {
      case ProC2S.Pick(Role.Player(Black)) =>
        lock.synchronized {
          if (playerBlack == null) {
            playerBlack = c
            addClient(c)
            if (playerWhite != null) go = true
          }
          else {
            c.send.put(ProS2C.No)
            ok = false
          }
        }
      case ProC2S.Pick(Role.Player(White)) =>
        lock.synchronized {
          if (playerWhite == null) {
            playerWhite = c
            addClient(c)
            if (playerBlack != null) go = true
          }
          else {
            c.send.put(ProS2C.No)
            ok = false
          }
        }
      case ProC2S.Pick(Role.Spectator) =>
        addClient(c)
      case _ =>
        c.send.put(ProS2C.No)
        ok = false
    }
    if (go) {
      val goThread = new Thread("Game Thread") {
        override def run() = {
          runGame()
        }
      }
      goThread.start()
      lock.synchronized { threads :+= goThread }
    }
    if (!ok) joinClient(c)
  }
  def addClient(c: Client) = lock.synchronized {
    c.send.put(ProS2C.Ok)
    c.send.put(ProS2C.Snapshot(state.board, state.score, state.turn, prevMove))
    clients :+= c
  }
  def runGame() = {
    while (running) {
      val currentPlayer = state.turn match {
        case Black => playerBlack
        case White => playerWhite
      }
      for (c <- clients; if c != currentPlayer) {
        c.recv.poll() match {
          case Some(Msg(str)) => handleMessage(str, c)
          case Some(_) => c.send.put(ProS2C.No)
          case None => // do nothing
        }
      }
      currentPlayer.recv.poll() match {
        case Some(Msg(str)) => handleMessage(str, currentPlayer)
        case Some(ProC2S.Place(x, y)) =>
          state.place(x, y) match {
            case Some(newState) =>
              prevMove = Some(ProC2S.Place(x, y))
              state = newState
              stateUpdated()
            case None => currentPlayer.send.put(ProS2C.No)
          }
        case Some(ProC2S.Pass) =>
          prevMove = Some(ProC2S.Pass)
          state = state.pass
          stateUpdated()
          if (state.history.take(2) == Vector(state.board, state.board)) {
            val scoreTaken = state.score
            val board = state.board
            val width = board.width
            val height = board.height
            val boardFilled =
              (0 until board.width).flatMap(x =>
                (0 until board.height).map(y =>
                  (x, y) -> board.stones.lift((x, y))
                )
              ).toMap
            val playerStones = Vector[Stone](Black, White).map(owner =>
              owner -> board.stones.toSet[((Int, Int), Stone)].collect { case (p, `owner`) => p }
            ).toMap
            val territory = playerStones.map { case (player, stones) =>
              val libs = stones.flatMap { case (x, y) => Board.sq(Board.liberties(board.stones, x, y), width, height) }
              val area = libs.flatMap { case (x, y) => Board.group(boardFilled, x, y) }
              val owned = area.filter { case (x, y) =>
                Board.neighbors(boardFilled, x, y).forall(p => board.stones.get(p) != Some(player.opposite))
              }
              player -> owned
            }
            val score = (p: Stone) => scoreTaken(p) + playerStones(p).size + territory(p).size
            val black = score(Black) * 2
            val white = (score(White) + komiMinusHalf) * 2 + 1
            if (black < white) win(White)
            else win(Black)
          }
        case Some(_) => currentPlayer.send.put(ProS2C.No)
        case None => // do nothing
      }
      Thread.sleep(10)
    }
  }
  def handleMessage(msg: String, sender: Client) = {
    message(msg, sender)
  }
  def stateUpdated() = {
    for (c <- clients) c.send.put(ProS2C.Snapshot(state.board, state.score, state.turn, prevMove))
  }
  def win(player: Stone) = {
    for (c <- clients) c.send.put(ProS2C.Win(player))
    stop()
  }
  
  def stop() = {
    running = false
    conns.foreach(_.close())
    closeSocket()
    threads.foreach(_.interrupt())
  }
  
}