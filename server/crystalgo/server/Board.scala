package crystalgo.server

sealed trait Stone {
  def opposite = this match {
    case Black => White; case White => Black
  }
}
case object Black extends Stone
case object White extends Stone

case class Board(stones: Map[(Int, Int), Stone], width: Int, height: Int) {
  def place(stone: Stone, x: Int, y: Int) = if (stones.contains((x, y))) None else Some {
    val stones1 = stones + ((x, y) -> stone)
    val neighs = Board.neighbors(stones1, x, y)
    val remove = neighs.filter {
      case (x_, y_) => Board.sq(Board.liberties(stones1, x_, y_), width, height).isEmpty
    }
    val removeGroups = remove.flatMap { case (x_, y_) => Board.neighbors(stones1, x_, y_) }
    val stones2 = stones1 -- removeGroups
    val removeSelf = Board.group(stones2, x, y)
    val stones3 =
      if (Board.sq(Board.liberties(stones2, x, y), width, height).isEmpty) stones2 -- removeSelf
      else stones2
    val score = stone match {
      case Black => Map(Black -> removeGroups.size, White -> removeSelf.size)
      case White => Map(Black -> removeSelf.size, White -> removeGroups.size)
    }
    (Board(stones3, width, height), score)
  }
}
object Board {
  def group[A](stones: Map[(Int, Int), A], x: Int, y: Int) = {
    var visited = Set[(Int, Int)]()
    var border = Set((x, y))
    val color = stones((x, y))
    while (!border.isEmpty) {
      visited ++= border
      border =
        border.flatMap { case (x, y) => Set((x + 1, y), (x, y + 1), (x - 1, y), (x, y - 1))}.
        filterNot(visited.contains _).
        filter(stones.get(_) == Some(color))
    }
    visited
  }
  def liberties[A](stones: Map[(Int, Int), A], x: Int, y: Int) =
    group(stones, x, y).flatMap{ case (x, y) => Set((x + 1, y), (x, y + 1), (x - 1, y), (x, y - 1))}.
    filterNot(stones.contains _)
  def sq(group: Set[(Int, Int)], width: Int, height: Int) =
    group.filter { case (x, y) => x >= 0 && y >= 0 && x < width && y < height }
  def neighbors(stones: Map[(Int, Int), Stone], x: Int, y: Int) = {
    val color = stones((x, y))
    val neighbors = 
      group(stones, x, y).flatMap{ case (x, y) => Set((x + 1, y), (x, y + 1), (x - 1, y), (x, y - 1))}.
      filter(stones.get(_) == Some(color.opposite))
    neighbors
  }
}
case class Game(state: Board, history: Vector[Board])