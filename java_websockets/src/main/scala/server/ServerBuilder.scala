package server

class ServerBuilder {

  var port: Int = -1
  var routes: HttpRoutes = null

  def bindHttp(port: Int): ServerBuilder = {
    this.port = port
    this
  }

  def withHttpWebSocketApp(routes: HttpRoutes): ServerBuilder = {
    this.routes = routes
    this
  }

  def serve: LazyList[Int] =
    val server = Server(this.routes.pf, port)
    while (true)
      server.listenOnNewRequests
    LazyList(0)
}
