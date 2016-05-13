package crystalgo.server

import java.net.Socket
import crystalgo.server.io.LineOut
import crystalgo.server.io.ByteOut
import crystalgo.server.io.ByteIn
import crystalgo.server.io.LineIn
import java.io.OutputStream
import java.io.InputStream
import scala.annotation.tailrec

class Client(input: InputStream, output: OutputStream) {
  val send = ServerProtocol.out(LineOut(new ByteOut(output)))
  val recv = ServerProtocol.in(LineIn(new ByteIn(input)))
  def close() = { send.close(); recv.close() }
}

class Server(accept: () => Option[Client], komiMinusHalf: Int = 7) {
  
  var running = true
  
  var state = Game(Black, _ => 0, Board(Map(), 19, 19), Vector())
  var conns = Vector[Client]()
  var clients = Vector[Client]()
  var playerBlack: Client = null
  var playerWhite: Client = null
  val lock = new Object
  
  val acceptThread = new Thread() {
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
    val joinThread = new Thread() {
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
      val goThread = new Thread() {
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
    c.send.put(ProS2C.Snapshot(state.board))
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
      Thread.sleep(10)
      currentPlayer.recv.poll() match {
        case Some(Msg(str)) => handleMessage(str, currentPlayer)
        case Some(ProC2S.Place(x, y)) =>
          state.place(x, y) match {
            case Some(newState) =>
              state = newState
              stateUpdated()
            case None => currentPlayer.send.put(ProS2C.No)
          }
        case Some(ProC2S.Pass) =>
          state = state.pass
          stateUpdated()
          if (state.history.take(3) == Vector(state.board, state.board, state.board)) {
            val scoreTaken = state.score
            val board = state.board
            val width = board.width
            val height = board.height
            val boardFilled = (0 until board.width).zip(0 until board.height).map(x => x -> board.stones.lift(x)).toMap
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
    }
  }
  def handleMessage(msg: String, sender: Client) = {
    // nyi
  }
  def stateUpdated() = {
    for (c <- clients) c.send.put(ProS2C.Snapshot(state.board))
  }
  def win(player: Stone) = {
    for (c <- clients) c.send.put(ProS2C.Win(player))
  }
  
  def stop() = {
    running = false
    threads.foreach(_.interrupt())
    conns.foreach(_.close())
  }
  
}