package server

import server.ContentType.ApplicationJavascript

import scala.io.Source

object StaticFile {
  /**
   * Create a new response with the content of the file @ path
   * <p>
   * Note: For now only support JavaScript files
   * </p>
   *
   * @param path the path to a file
   * @return the response containing the content of the file @ path
   */
  def fromPath(path: String): Response = {
    val f = Source.fromFile(path, "UTF-8")
    val res = Ok(f.mkString, Header(ApplicationJavascript))
    f.close()
    res
  }
}
