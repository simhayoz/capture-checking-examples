package server

class HttpRoutes(var pf: PartialFunction[Request, Response]) {
  /**
   * Add default not found case for partial function
   *
   * @return the HttpRoutes
   */
  def orNotFound: HttpRoutes = {
    this.pf = this.pf.orElse({
      case _ => NotFound("Not Found")
    })
    this
  }
}

object HttpRoutes {
  /**
   * Update the HttpRoutes from a partial function
   *
   * @param pf a partial function from request to response
   * @return the HttpRoutes
   */
  def of(pf: PartialFunction[Request, Response]): HttpRoutes =
    HttpRoutes(pf)
}
