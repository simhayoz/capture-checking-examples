package server

/**
 * Represents a header in the HTTP protocol
 *
 * @param contentType the type of the content
 * @param charset     the type of the charset
 */
class Header(contentType: ContentType, charset: String = "UTF-8") {
  /**
   * Get the header string value to be sent through the connection
   *
   * @param contentLength length of the content
   * @return the string representation of the header
   */
  def toString(contentLength: Int): String = f"Content-type: ${contentType.toString}; charset=$charset\r\nContent-Length: $contentLength"
}

/**
 * Represents an HTTP request with no header
 */
class NoHeader extends Header(ContentType.NoContentType, "") {
  override def toString(contentLength: Int): String = ""
}

/**
 * Represents the different type of content supported
 */
enum ContentType(s: String):
  override def toString: String = s

  case TextHtml extends ContentType("text/html")
  case TextPlain extends ContentType("text/plain")
  case ApplicationJavascript extends ContentType("application/javascript")
  case ApplicationJson extends ContentType("application/json")
  case NoContentType extends ContentType("")
end ContentType

