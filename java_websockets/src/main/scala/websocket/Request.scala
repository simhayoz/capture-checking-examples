package websocket

class Request(val method: Method, val uri: Uri, val postBody: Option[String]) {
  def as[A](implicit transf: String => A): A = transf(postBody.get)

  override def toString: String = f"Request($method, $uri, $postBody)"
}

object Request {
  def apply(method: Method, uri: Uri, postBody: Option[String]): Request = new Request(method, uri, postBody)
  def unapply(arg: Request): Option[(Method, Uri, Option[String])] = Some((arg.method, arg.uri, arg.postBody))
}