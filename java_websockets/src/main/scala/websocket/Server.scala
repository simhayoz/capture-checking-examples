package websocket

import java.io.{InputStream, OutputStream}
import java.net.{ServerSocket, Socket}
import java.util.Scanner
import Method.POST

import java.io.BufferedReader
import java.io.InputStreamReader
import scala.collection.mutable

class Server(pf: PartialFunction[Request, Response], port: Int) {
  val server: ServerSocket = ServerSocket(port)

  def _getContentLength(s: BufferedReader): Int = {
    var nLine = "."
    var contentLength = -1
    while(!nLine.isBlank) {
      nLine = s.readLine()
      if(nLine.startsWith("Content-Length: ")) {
        contentLength = nLine.substring(16).toInt
      }
    }
    contentLength
  }

  def listenOnNewRequests: Int =
    val client: Socket = server.accept()
    val in = client.getInputStream
    val out = client.getOutputStream
    val s: BufferedReader = new BufferedReader(new InputStreamReader(in))
    val firstLine = s.readLine().split(' ')
    val method = Method.valueOf(firstLine.head)
    val request = if(method == POST) {
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
        while(!nLine.isBlank) {
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

}