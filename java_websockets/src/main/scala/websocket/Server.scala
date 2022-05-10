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

  def _getContentLength(s: Scanner): Int = {
    while(s.hasNextLine) {
      val nLine: String = s.nextLine()
      if(nLine.startsWith("Content-Length: ")) {
        return nLine.substring(16).toInt
      }
    }
    -1
  }

  def listenOnNewRequests: Int =
    val client: Socket = server.accept()
    val in = client.getInputStream
    val out = client.getOutputStream
    val s: Scanner = Scanner(in, "UTF-8")
    val firstLine = s.nextLine().split(" ")
    val method = Method.valueOf(firstLine.head)
    if(method == POST) { // TODO fix this
      val len = _getContentLength(s)
      println("LENGTH " + len)
      var nxt = "."
      while(nxt.map(_.toByte).nonEmpty && s.hasNextLine) {
        println("before")
        nxt = s.nextLine()
        println("|" + nxt + "|" + nxt.map(_.toByte))
        println("after")
      }
      println("...")
      println(s.nextByte())
//      val arr: Array[Byte] = Array.fill(len){0}
//      in.read(arr, 0, len)
//      println(arr.map(_.toChar).mkString)
      val request = Request(method, Uri(firstLine.tail.head), Some("???")) // TODO should not always be None
      println(request)
      val rep = pf.applyOrElse(request, r => NotFound("Not Found: " + r.uri.path))
      out.write(rep.createRequest)
      s.close()
      client.close()
      return 0
    }
    val request = Request(method, Uri(firstLine.tail.head), None) // TODO should not always be None
    println(request)
    val rep = pf.applyOrElse(request, r => NotFound("Not Found: " + r.uri.path))
    out.write(rep.createRequest)
    s.close()
    client.close()
    0
}