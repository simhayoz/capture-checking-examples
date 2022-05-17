package server

import java.util.concurrent.ConcurrentLinkedDeque

class ServerBuilder(val port: Int, val routes: HttpRoutes) {

  /**
   * Bind to http
   *
   * @param port the port of the http connection
   * @return this
   */
  def bindHttp(port: Int): {routes} ServerBuilder = ServerBuilder(port, this.routes)

  /**
   * Add an http websocket app
   *
   * @param routes the http/websocket routes
   * @return this
   */
  def withHttpWebSocketApp(routes: HttpRoutes): {routes} ServerBuilder = ServerBuilder(this.port, routes)

  /**
   * Build the server and run it
   *
   * @return a list of exit code for each request
   */
  def serve: ConcurrentLinkedDeque[Int] =
    val queue: ConcurrentLinkedDeque[Int] = new ConcurrentLinkedDeque()
    val server = Server(this.routes.pf, port)
    while (true)
      queue.add(server.listenOnNewRequests)
    queue
}

object ServerBuilder {
  def apply(port: Int, routes: HttpRoutes): {routes} ServerBuilder = new ServerBuilder(port, routes)
}
