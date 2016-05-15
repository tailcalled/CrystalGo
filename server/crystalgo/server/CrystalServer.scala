package crystalgo.server

import java.net.ServerSocket
import scala.util.Try

trait CrystalModule {
  
  def join(client: Client, id: Int): Unit
  def message(sender: Client, id: Int, message: String): Unit
  def installHooks(mirror: Mirror): Unit
  
}

object CrystalModule {
  
  class Chat extends CrystalModule {
    
    private var clients = Vector[Client]()
    val lock = new Object
    var commands = Map[String, CrystalModule]()
    
    def join(client: Client, id: Int) = {
      lock.synchronized {
        clients :+= client
        for ((name, command) <- commands) {
          command.join(CrystalServer.localClient(client, "/" + name), id)
        }
      }
    }
    def message(sender: Client, id: Int, message: String) = {
      if (message.startsWith("/")) {
        val space = message.indexOf(" ")
        val command = if (space == -1) message.substring(1) else message.substring(1, space)
        val param = if (space == -1) "" else message.substring(space + 1)
        lock.synchronized {
          commands.get(command) match {
            case Some(c) => c.message(CrystalServer.localClient(sender, "/" + command), id, param)
            case None => sender.send.put(Msg("invalid command " + command))
          }
        }
      }
      else for (reciever <- clients) {
        reciever.send.put(Msg(id + " " + message))
      }
    }
    def installHooks(mirror: Mirror) = {}
    
    def install(name: String, command: CrystalModule) = {
      lock.synchronized {
        commands += name -> command
      }
    }
    
  }
  class Nick extends CrystalModule {
    
    var nicks = Map[Int, String]()
    var clients = Vector[Client]()
    
    def join(client: Client, id: Int) = {
      clients :+= client
      nicks += id -> ("Anon #" + id)
      for (other <- clients) other.send.put(Msg(id + " " + nicks(id)))
    }
    def message(client: Client, id: Int, message: String) = {
      if (message.forall(c => c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_')
          && !nicks.values.toSet.contains(message)) {
        nicks += id -> message
        for (other <- clients) other.send.put(Msg(id + " " + nicks(id)))
      }
      else client.send.put(Msg("invalid name"))
    }
    def installHooks(mirror: Mirror) = {}
    
  }
  
}

object CrystalServer {
  def localClient(client: Client, module: String): Client = new Client(
    client.send.unfold[ProS2C] {
      case Msg(str) => Vector(Msg(module + " " + str))
      case x => Vector(x)
    },
    client.recv
  )
}

trait Mirror {
  
  def recieveHook(f: (Client, Int, ProC2S) => Unit): Unit
  def sendHook(f: (Client, Int, ProS2C) => Unit): Unit
  
}

class CrystalServer(private var modules: Map[String, CrystalModule] = Map(),
    port: Int = 8448, size: Int = 19, komiMinusHalf: Int = 7) {
  
  val socket = new ServerSocket(port)
  var ids = Map[Client, Int]()
  val lock = new Object
  var id = 0
  val server = new Server(() => {
    Try {
      val client = socket.accept()
      val cUnhooked = new Client(client.getInputStream, client.getOutputStream)
      val cid = id
      id += 1
      lazy val c: Client = new Client(
          cUnhooked.send.unfold { (x: ProS2C) =>
            for (f <- sendHooks) f(c, cid, x)
            Vector(x)
          },
          cUnhooked.recv.fold { x =>
            for (f <- recieveHooks) f(c, cid, x)
            Some(x)
          }
      )
      lock.synchronized {
        ids += c -> cid
        for ((name, module) <- modules) {
          module.join(CrystalServer.localClient(c, name), ids(c))
        }
      }
      c
    }.toOption
  }, socket.close _, (message, sender) => {
    lock.synchronized {
      for ((name, module) <- modules) {
        if (message.startsWith(name + " ")) {
          module.message(CrystalServer.localClient(sender, name), ids(sender), message.substring(name.length() + 1))
        }
      }
    }
  })
  
  var recieveHooks = Vector[(Client, Int, ProC2S) => Unit]()
  var sendHooks = Vector[(Client, Int, ProS2C) => Unit]()
  
  object mirror extends Mirror {
    
    def recieveHook(f: (Client, Int, ProC2S) => Unit): Unit = { recieveHooks :+= f }
    def sendHook(f: (Client, Int, ProS2C) => Unit): Unit = { sendHooks :+= f }
  
  }
  
  def install(name: String, module: CrystalModule) = {
    lock.synchronized {
      modules += name -> module
      for ((client, id) <- ids) {
        module.join(CrystalServer.localClient(client, name), id)
      }
      module.installHooks(mirror)
    }
  }
  
  def close() = {
    server.stop()
  }
  
}