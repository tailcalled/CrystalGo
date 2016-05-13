package crystalgo.server

import java.io.OutputStream
import java.io.InputStream
import crystalgo.server.io.Out
import crystalgo.server.io.In
import scala.util.Try
import scala.util.Success

sealed trait Role
object Role {
  case class Player(stone: Stone) extends Role
  case object Spectator extends Role
}

sealed trait ProC2S
object ProC2S {
  case class Place(column: Int, row: Int) extends ProC2S
  case object Pass extends ProC2S
  case class Pick(role: Role) extends ProC2S
}

sealed trait ProS2C
object ProS2C {
  case class Snapshot(board: Board) extends ProS2C
  case class Win(color: Stone) extends ProS2C
  case object No extends ProS2C
  case object Ok extends ProS2C
}

case class Msg(msg: String) extends ProC2S with ProS2C

object ServerProtocol {
  def out(lines: Out[String]) = lines.unfold[ProS2C] {
    case ProS2C.No => Vector("no")
    case ProS2C.Ok => Vector("ok")
    case ProS2C.Win(Black) => Vector("black wins")
    case ProS2C.Win(White) => Vector("white wins")
    case ProS2C.Snapshot(board) =>
      (board.width + " " + board.height) +:
      (0 until board.height).map(y =>
        (0 until board.width).map(x =>
          board.stones.get((x, y)) match {
            case Some(Black) => "x"
            case Some(White) => "o"
            case None => "."
          }
        ).mkString
      ).toVector
    case Msg(msg) => Vector(msg)
  }
  def in(lines: In[String]): In[ProC2S] = lines.fold[ProC2S] {
    case "pass" => Some(ProC2S.Pass)
    case "black" => Some(ProC2S.Pick(Role.Player(Black)))
    case "white" => Some(ProC2S.Pick(Role.Player(White)))
    case "spectator" => Some(ProC2S.Pick(Role.Spectator))
    case s if s.startsWith("msg ") => Some(Msg(s.substring(4)))
    case s =>
      s.split(" ") match {
        case Array(x, y) =>
          (Try(x.toInt), Try(y.toInt)) match {
            case (Success(x_), Success(y_)) => Some(ProC2S.Place(x_, y_))
            case _ => None
          }
        case _ => None
      }
  }
}