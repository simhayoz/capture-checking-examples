package server

class HttpRoutes {
  var pf: PartialFunction[Request, Response] = null

  def of(pf: PartialFunction[Request, Response]): HttpRoutes = {
    this.pf = pf
    this
  }

  def orNotFound: HttpRoutes = {
    this.pf.orElse({
      case _ => NotFound("Not Found")
    })
    this
  }
}
