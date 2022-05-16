package server

import server.Method.*
import server.websocket.*

import java.io.{BufferedReader, InputStream, InputStreamReader, OutputStream}
import java.net.{ServerSocket, Socket}
import java.util.Scanner
import scala.collection.mutable

/**
 * Server that handles request
 *
 * @param pf   the partial function to be applied when receiving a new request
 * @param port the port to listen from
 */
class Server(pf: Request => {*} Response, port: Int) {
  val server: ServerSocket = ServerSocket(port)

  /**
   * Block until a new request is received and handle it
   *
   * @return the return code of handling the new request
   */
  def listenOnNewRequests: Int =
    val client: Socket = server.accept()
    val in = client.getInputStream
    val out = client.getOutputStream
    val s: BufferedReader = new BufferedReader(new InputStreamReader(in))
    val firstLine = s.readLine().split(' ')
    val method = Method.valueOf(firstLine.head)
    val headers = _buildHeaders(s)
    val request = method match {
      case POST =>
        val len = _getContentLength(headers)
        val buf = new Array[Char](len)
        s.read(buf)
        Request(method, Uri(firstLine.tail.head), headers, Some(buf.mkString))
      case GET =>
        Request(method, Uri(firstLine.tail.head), headers, None)
    }
    pf(request) match { // TODO or else: , r => NotFound("Not Found: " + r.uri.path)
      case WebSocketResponsePipe(toClient, fromClient) =>
        WebSocketServerHandler(request, client, in, out, toClient, fromClient).handle()
        0
      case rep =>
        out.write(rep.createResponse)
        s.close()
        client.close()
        0
    }

  def _buildHeaders(s: BufferedReader): RequestHeader = {
    var mp: Map[String, String] = Map()
    var nLine = "."
    while (!nLine.isBlank) {
      nLine = s.readLine()
      val splt = nLine.split(':')
      if (splt.length > 1)
        mp += (splt(0) -> splt.tail.mkString.trim)
    }
    mp
  }

  def _getContentLength(rh: RequestHeader): Int =
    rh("Content-Length").toInt
}