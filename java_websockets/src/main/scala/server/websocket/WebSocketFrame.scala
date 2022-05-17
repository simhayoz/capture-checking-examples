package server.websocket

class WebSocketFrame

/**
 * Represents a text frame in the websocket protocol
 *
 * @param msg the text value of this frame
 */
case class Text(msg: String) extends WebSocketFrame

/**
 * Represents a close frame in the websocket protocol
 *
 * @param code the exit code of this frame
 */
case class Close(code: Int) extends WebSocketFrame
