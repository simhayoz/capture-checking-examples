package server.websocket

import server.{Pipe, Response, WebSocketResponsePipe}

import java.util.concurrent.ConcurrentLinkedQueue

class WebSocketBuilder {
  /**
   * Build a new websocket response
   *
   * @param toClient   queue which will be updated with element to send to client
   * @param fromClient pipe to be called when new element are received from client
   * @return the websocket response
   */
  def build(toClient: {*} ConcurrentLinkedQueue[WebSocketFrame], fromClient: {*} Pipe[WebSocketFrame, Unit]): {toClient, fromClient} Response =
    WebSocketResponsePipe(toClient, fromClient)
}
