package server

class HttpRoutes(val pf: Request => {*} Response) {
  /**
   * Add default not found case for partial function
   *
   * @return the HttpRoutes
   */
  def orNotFound: {pf} HttpRoutes = {
    // TODO find a way to fix this
//    this.pf = this.pf.orElse({
//      case _ => NotFound("Not Found")
//    })
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
  def of(pf: Request => {*} Response): {pf} HttpRoutes =
    HttpRoutes(pf)
}
