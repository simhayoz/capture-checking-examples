package server

class ServerBuilder {

  var port: Int = -1
  var routes: HttpRoutes = null

  /**
   * Bind to http
   *
   * @param port the port of the http connection
   * @return this
   */
  def bindHttp(port: Int): ServerBuilder = {
    this.port = port
    this
  }

  /**
   * Add an http websocket app
   *
   * @param routes the http/websocket routes
   * @return this
   */
  def withHttpWebSocketApp(routes: HttpRoutes): ServerBuilder = {
    this.routes = routes
    this
  }

  /**
   * Build the server and run it
   *
   * @return a list of exit code for each request
   */
  def serve: LazyList[Int] = // TODO adapt list type
    val server = Server(this.routes.pf, port)
    while (true)
      server.listenOnNewRequests
    LazyList(0)
}
