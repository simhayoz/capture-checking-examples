package server

import annotation.capability

@capability class HttpRoutes(val pf: Request => {*} Response) {
  /**
   * Add default not found case for partial function
   *
   * @return the HttpRoutes
   */
  def orNotFound(): HttpRoutes = { // Only works with ()
    HttpRoutes.of(r => {
      try {
        this.pf(r)
      } catch {
        case _: MatchError => NotFound(f"Not Found: ${r.uri}")
      }
    })
  }
}

object HttpRoutes {
  /**
   * Update the HttpRoutes from a partial function
   *
   * @param pf a partial function from request to response
   * @return the HttpRoutes
   */
  def of(pf: Request => {*} Response): HttpRoutes =
    HttpRoutes(pf)
}
