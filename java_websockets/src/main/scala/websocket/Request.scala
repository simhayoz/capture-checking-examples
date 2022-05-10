package websocket

import scala.reflect.ClassTag

class Request(val method: Method, val uri: Uri, val postBody: Option[String]) {//extends Product with Serializable {
//  override def canEqual(that: Any): Boolean = that.isInstanceOf[Request]
//
//  override def productArity: Int = 2
//
//  override def productElement(n: Int): Any = {
//    if(n == 0)
//      method
//    else if(n == 1)
//      uri
//    else
//      throw new IndexOutOfBoundsException("")
//  }
  //  val Message_ : Class[Message] = classOf[Message]
  def as[A](implicit transf: String => A): A = transf(postBody.get)

  override def toString: String = f"Request($method, $uri, $postBody)"
}

object Request {
  def apply(method: Method, uri: Uri, postBody: Option[String]): Request = new Request(method, uri, postBody)
  def unapply(arg: Request): Option[(Method, Uri, Option[String])] = Some((arg.method, arg.uri, arg.postBody))
}