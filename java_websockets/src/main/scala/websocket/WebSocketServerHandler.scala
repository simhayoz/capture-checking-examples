package websocket

import java.io.{InputStream, OutputStream}
import java.net.Socket
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern
import scala.concurrent.Future

class WebSocketServerHandler(header: String, client: Socket, in: InputStream, out: OutputStream, toClient: ConcurrentLinkedQueue[WebSocketFrame], fromClient: Pipe[WebSocketFrame, Unit]) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val isClosed: AtomicBoolean = new AtomicBoolean(false)
  val clientSubscriber: QueueSubscriber[WebSocketFrame] = QueueSubscriber(toClient)

  def handle(): Unit =
    val match_data = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(header)
    match_data.find
    val response = ("HTTP/1.1 101 Switching Protocols\r\n" + "Connection: Upgrade\r\n" + "Upgrade: websocket\r\n" + "Sec-WebSocket-Accept: " + Base64.getEncoder.encodeToString(MessageDigest.getInstance("SHA-1").digest((match_data.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8"))) + "\r\n\r\n").getBytes("UTF-8")
    out.write(response, 0, response.length)
    Future {
      clientSubscriber.onNewElement(e => sendToClient(e))
    }
    Future {
      _loopReceiveFromClient()
    }

  def _closeAndFreeResources(): Unit =
    clientSubscriber.unsubscribe()
    isClosed.set(true)
    in.close()
    out.close()
    client.close()

  def _loopReceiveFromClient(): Unit =
    while(!isClosed.get()) {
      fromClient.apply(receiveFromClient)
    }

  /**
   * Send a text message to the client
   * @param msg the message to send
   */
  def sendToClient(wsf: WebSocketFrame): Unit = wsf match {
    case Text(msg) =>
      val opcodeOut = Array(-127.toByte)
      val lenOut = if(msg.length <= 125) {
        Array(msg.length.toByte)
      } else if(msg.length <= 65535) { // FIXME longer message not working yet
        Array(-129.toByte) ++ BigInt(msg.length).toByteArray
      } else {
        Array(-128.toByte) ++ BigInt(msg.length).toByteArray
      }
      val res = opcodeOut ++ lenOut ++ msg.getBytes
      out.write(res, 0, res.length)
    case _ => throw RuntimeException(f"Unsupported frame to send: $wsf")
  }

  /**
   * Blocking method that wait for a message from the client
   *
   * @return the message received by the client
   */
  def receiveFromClient: WebSocketFrame = {
    val opcode = in.readNBytes(1).head
    if ((opcode & -128.toByte) != -128) {
      println("Cannot parse message on multiple frames (unsupported)")
      throw RuntimeException("Cannot parse message on multiple frames (unsupported)")
    }
    (opcode & 0xFF).toBinaryString.takeRight(4) match {
      case "0001" => val len = in.readNBytes(1).map(fromSignedByte).head
        val key = in.readNBytes(4)
        val encoded = in.readNBytes(len - 128)
        val decoded = new Array[Byte](len - 128)
        for (i <- encoded.indices) {
          decoded(i) = (encoded(i) ^ key(i & 0x3)).toByte
        }
        Text(decoded.map(_.toChar).mkString)
      case "1000" => {
        _closeAndFreeResources()
        Close(1000)
      }
    }

  }

  def fromSignedByte(b: Byte): Int = Integer.parseInt((b & 0xFF).toBinaryString, 2)
}
