package crystalgo.server.io

import java.io.{InputStream, OutputStream}
import java.nio.charset.Charset
import java.util.concurrent.LinkedBlockingQueue

import scala.annotation.tailrec

trait In[+A] { self =>
  def poll(): Option[A]
  def await(): A
  def close(): Unit
  def fold[B](f: A => Option[B]): In[B] = new In[B] {
    def poll(): Option[B] = {
      while (true) {
        self.poll() match {
          case None => return None
          case Some(x) => f(x) match {
            case None =>
            case Some(y) => return Some(y)
          }
        }
      }
      throw new Exception("not reached")
    }
    @tailrec final def await(): B = {
      f(self.await()) match {
        case None => await()
        case Some(x) => x
      }
    }
    def close() = self.close()
  }
}
trait Out[-A] { self =>
  def unfold[B](f: B => Vector[A]): Out[B] = new Out[B] {
    def put(b: B) = {
      for (a <- f(b)) self.put(a)
    }
    def close() = self.close()
  }
  def put(a: A): Unit
  def close(): Unit
}

object LineIn {
  def apply(bytes: In[Byte]) = {
    var line = Vector[Byte]()
    bytes.fold { b =>
      if (b == '\n') {
        val s = new String(line.toArray, "UTF-8")
        line = Vector()
        Some(s)
      }
      else if (b != '\r') {
        line :+= b
        None
      }
      else None
    }
  }
}
object LineOut {
  val utf8 = Charset.forName("UTF-8")
  def apply(bytes: Out[Byte]) = {
    bytes.unfold { (line: String) =>
      if (line.contains("\n")) throw new Exception
      (line + "\n").getBytes(utf8).toVector
    }
  }
}

class ByteIn(stream: InputStream) extends In[Byte] {
  val queue = new LinkedBlockingQueue[Byte]()
  val thread = new Thread("Stream IO Thread") {
    override def run(): Unit = {
      while (open) {
        val i = stream.read()
        if (i == -1) return;
        queue.add(i.toByte)
      }
    }
  }
  var open = true
  thread.start()

  def poll() = Option(queue.poll())
  def await() = queue.take()
  def close() = {
    open = false
    stream.close()
    thread.interrupt()
  }
}
class ByteOut(stream: OutputStream) extends Out[Byte] {
  val queue = new LinkedBlockingQueue[Byte]()
  val thread = new Thread("Stream IO Thread") {
    override def run(): Unit = {
      while (open) {
        val byte = queue.take()
        stream.write(byte)
        stream.flush()
      }
    }
  }
  var open = true
  thread.start()

  def put(b: Byte) = { queue.put(b) }
  def close() = {
    open = false
    stream.close()
    thread.interrupt()
  }
}