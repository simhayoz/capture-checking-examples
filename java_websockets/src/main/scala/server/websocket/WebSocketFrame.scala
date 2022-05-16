package server.websocket

class WebSocketFrame
case class Text(msg: String) extends WebSocketFrame
case class Close(code: Int) extends WebSocketFrame
