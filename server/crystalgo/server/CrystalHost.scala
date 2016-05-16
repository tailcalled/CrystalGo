package crystalgo.server

import java.net.ServerSocket
import scala.util.Try
import javafx.application.Application

object CrystalHost {
  
  case class User(name: String, id: Int)
  
  def main(args: Array[String]) = {
    if (args.contains("-headless")) {
      val server = new CrystalServer()
      val chat = new CrystalModule.Chat
      server.install("chat", chat)
      chat.install("nick", new CrystalModule.Nick)
    }
    else Application.launch(classOf[CrystalUI], args:_*)
  }
  
}