package crystalgo.server

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
}

case class Msg(msg: String) extends ProC2S with ProS2C