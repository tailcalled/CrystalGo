package crystalgo.server

import java.net.ServerSocket
import scala.util.Try

object CrystalHost {
  
  def main(args: Array[String]) = {
    val port = 8448
    val socket = new ServerSocket(8448)
    val server = new Server(() => {
      Try {
        val client = socket.accept()
        new Client(client.getInputStream, client.getOutputStream)
      }.toOption
    }, socket.close _)
  }
  
}