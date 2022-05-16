package server

import server.Method.POST
import server.websocket.*

import java.io.{BufferedReader, InputStream, InputStreamReader, OutputStream}
import java.net.{ServerSocket, Socket}
import java.util.Scanner
import scala.collection.mutable

class Server(pf: PartialFunction[Request, Response], port: Int) {
  val server: ServerSocket = ServerSocket(port)

  /**
   * Block until a new request is received and handle it
   *
   * @return the return code of handling the new request
   */
  def listenOnNewRequests: Int = // TODO handle more cleanly
    val client: Socket = server.accept()
    val in = client.getInputStream
    val out = client.getOutputStream
    val s: BufferedReader = new BufferedReader(new InputStreamReader(in))
    val firstLine = s.readLine().split(' ')
    val method = Method.valueOf(firstLine.head)
    val request = if (method == POST) {
      val len = _getContentLength(s)
      val buf = new Array[Char](len)
      s.read(buf)
      Request(method, Uri(firstLine.tail.head), Some(buf.mkString))
    } else {
      Request(method, Uri(firstLine.tail.head), None)
    }
    val rep = pf.applyOrElse(request, r => NotFound("Not Found: " + r.uri.path))
    rep match {
      case WebSocketResponsePipe(toClient, fromClient) =>
        val headerBuilder: mutable.StringBuilder = mutable.StringBuilder()
        var nLine = "."
        while (!nLine.isBlank) {
          nLine = s.readLine()
          headerBuilder.append(nLine).append("\n")
        }
        WebSocketServerHandler(headerBuilder.mkString, client, in, out, toClient, fromClient).handle()
        0
      case _ =>
        out.write(rep.createResponse)
        s.close()
        client.close()
        0
    }

  def _getContentLength(s: BufferedReader): Int = {
    var nLine = "."
    var contentLength = -1
    while (!nLine.isBlank) {
      nLine = s.readLine()
      if (nLine.startsWith("Content-Length: ")) {
        contentLength = nLine.substring(16).toInt
      }
    }
    contentLength
  }
}