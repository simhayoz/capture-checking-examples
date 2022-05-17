package server

import server.ContentType.*
import server.websocket.WebSocketFrame

import annotation.capability

import java.util.concurrent.ConcurrentLinkedQueue

type Pipe[A, B] = PartialFunction[A, B]

/**
 * Represents a response sent by this server to the client
 *
 * @param status      the status code of the response
 * @param httpVersion the HTTP version of the response
 * @param header      the header of the response
 * @param body        the body of the response
 */
@capability class Response(val status: (Int, String), val httpVersion: String, val header: Header, val body: String) {
  /**
   * Create a new response to be sent through the connection
   *
   * @return an array of bytes to be sent through the connection
   */
  def createResponse: Array[Byte] =
    f"$httpVersion ${status._1} ${status._2}\r\n${header.toString(body.length)}\r\n\r\n$body".getBytes
}

case class Ok(override val body: String, override val header: Header) extends Response((200, "OK"), "HTTP/1.1", header, body)

case class NotFound(override val body: String) extends Response((404, "Not Found"), "HTTP/1.1", Header(TextPlain), body)

case class WebSocketResponsePipe(toClient: {*} ConcurrentLinkedQueue[WebSocketFrame], fromClient: {*} Pipe[WebSocketFrame, Unit]) extends Response((200, "OK"), "HTTP/1.1", NoHeader(), "")