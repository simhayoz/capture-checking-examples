package websocket

import ContentType.*
import scala.io.Source
object StaticFile {
  def fromPath(path: String): Response = {
    val f = Source.fromFile(path, "UTF-8")
    val res = Ok(f.mkString, Header(ApplicationJavascript))
    f.close()
    res
  }
}
