package websocket

import java.io.{InputStream, OutputStream}
import java.net.{ServerSocket, Socket}
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.{Base64, Scanner}
import java.util.regex.{Matcher, Pattern}

class RawWebSocket {

  val server: ServerSocket = ServerSocket(2000)
  var in: InputStream = null
  var out: OutputStream = null

  class Message
  case class Text(msg: String) extends Message
  case class Close(code: Int) extends Message

  def build() =
    println("Server has started on 127.0.0.1:80.\r\nWaiting for a connection...")
    val client: Socket = _listenOnConnection
    println("A client connected.")
    in = client.getInputStream
    out = client.getOutputStream
    val s: Scanner = Scanner(in, "UTF-8")
    // Handshake with the client
    val data: String = s.useDelimiter("\\r\\n\\r\\n").next
    val get = Pattern.compile("^GET").matcher(data)
    if (get.find) {
      val match_data = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data)
      match_data.find
      val response = ("HTTP/1.1 101 Switching Protocols\r\n" + "Connection: Upgrade\r\n" + "Upgrade: websocket\r\n" + "Sec-WebSocket-Accept: " + Base64.getEncoder.encodeToString(MessageDigest.getInstance("SHA-1").digest((match_data.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8"))) + "\r\n\r\n").getBytes("UTF-8")
      out.write(response, 0, response.length)
      println("Completed handshake")
      println(receiveFromClient)
      sendToClient("TEST MESSAGE")
      println(receiveFromClient)
      s.close()
      server.close()
    } else {
      throw RuntimeException("Could not complete handshake with client")
    }

  /**
   * Send a text message to the client
   * @param msg the message to send
   */
  def sendToClient(msg: String): Unit = {
    if(msg.length > 125)
      throw RuntimeException("Message of length more than 125 are not supported (yet)")
    val opcodeOut = Array(-127.toByte)
    val lenOut = Array(msg.length.toByte)
    val res = opcodeOut ++ lenOut ++ msg.getBytes
    out.write(res, 0, res.length)
  }

  /**
   * Blocking method that wait for a message from the client
   * @return the message received by the client
   */
  def receiveFromClient: Message = {
    val opcode = in.readNBytes(1).head
    if((opcode & -128.toByte) != -128) {
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
      case "1000" => Close(1000)
    }

  }

  def getInputStream: InputStream = in

  def getOutputStream: OutputStream = out

  def fromSignedByte(b: Byte): Int = Integer.parseInt((b & 0xFF).toBinaryString, 2)

  def _listenOnConnection: Socket =
    server.accept()
}
