package websocket

import ContentType.*

class Response (val status: (Int, String), val httpVersion: String, val headers: Header, val body: String) {
  def createRequest: Array[Byte] =
    f"$httpVersion ${status._1} ${status._2}\r\n${headers.toString(body.length)}\r\n\r\n$body".getBytes
}

case class Ok(override val body: String, override val headers: Header) extends Response((200, "OK"), "HTTP/1.1", headers, body)

case class NotFound(override val body: String) extends Response((404, "Not Found"), "HTTP/1.1", Header(TextPlain), body)