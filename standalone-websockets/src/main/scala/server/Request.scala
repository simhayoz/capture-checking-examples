package server

/**
 * Represents a request done by the client to this server
 *
 * @param method   the method type
 * @param uri      the uri of the request
 * @param headers  the headers of the request
 * @param postBody the body of the request in case of POST method
 */
class Request(val method: Method, val uri: Uri, val headers: RequestHeader, val postBody: Option[String]) {
  /**
   * Transform the json string representation of an object to the object
   *
   * @param transf the implicit transformation function
   * @tparam A the type of the object to be transformed
   * @return the object transformed
   */
  def as[A](implicit transf: String => A): A = transf(postBody.get)

  override def toString: String = f"Request($method, $uri, $headers, $postBody)"
}

object Request {
  def apply(method: Method, uri: Uri, headers: RequestHeader, postBody: Option[String]): Request = new Request(method, uri, headers, postBody)

  def unapply(arg: Request): Option[(Method, Uri, Option[String])] = Some((arg.method, arg.uri, arg.postBody))
}

type RequestHeader = Map[String, String]