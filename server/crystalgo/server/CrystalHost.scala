package crystalgo.server

import java.net.ServerSocket
import scala.util.Try

object CrystalHost {
  
  case class User(name: String, id: Int)
  
  def main(args: Array[String]) = {
    val server = new CrystalServer()
    val chat = new CrystalModule.Chat
    server.install("chat", chat)
    chat.install("nick", new CrystalModule.Nick)
  }
  
}