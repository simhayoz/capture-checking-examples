package server

class Request(val method: Method, val uri: Uri, val postBody: Option[String]) {
  /**
   * Transform the json string representation of an object to the object
   *
   * @param transf the implicit transformation function
   * @tparam A the type of the object to be transformed
   * @return the object transformed
   */
  def as[A](implicit transf: String => A): A = transf(postBody.get)

  override def toString: String = f"Request($method, $uri, $postBody)"
}

object Request {
  def apply(method: Method, uri: Uri, postBody: Option[String]): Request = new Request(method, uri, postBody)

  def unapply(arg: Request): Option[(Method, Uri, Option[String])] = Some((arg.method, arg.uri, arg.postBody))
}