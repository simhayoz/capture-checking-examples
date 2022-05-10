package websocket

import java.io.{InputStream, OutputStream}
import java.net.{ServerSocket, Socket}
import java.util.Scanner
import java.util.regex.Pattern

class RawServerRequest {

  val server: ServerSocket = ServerSocket(8080)
  var in: InputStream = null
  var out: OutputStream = null

  def build() =
    val client: Socket = _listenOnConnection
    println("A client connected.")
    in = client.getInputStream
    out = client.getOutputStream
    val s: Scanner = Scanner(in, "UTF-8")
    // Handshake with the client
    val data: String = s.useDelimiter("\\r\\n\\r\\n").next
    val get = Pattern.compile("^GET").matcher(data)
    println(data)
    if (get.find) {
      println("HERE")
      val av = in.available()
      if(av > 0)
        println(in.readAllBytes().mkString(" "))
      s.close()
      server.close()
    } else {
      throw RuntimeException("Could not complete handshake with client")
    }

  def getInputStream: InputStream = in

  def getOutputStream: OutputStream = out

  def _listenOnConnection: Socket =
    server.accept()

}
