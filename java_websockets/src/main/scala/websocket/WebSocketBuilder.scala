package websocket

import java.util.concurrent.ConcurrentLinkedQueue

class WebSocketBuilder {
  def build(toClient: ConcurrentLinkedQueue[WebSocketFrame], fromClient: Pipe[WebSocketFrame, Unit]): Response =
    WebSocketResponsePipe(toClient, fromClient)
}
