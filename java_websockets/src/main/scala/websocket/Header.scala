package websocket

class Header(contentType: ContentType, charset: String = "UTF-8") {
  def toString(contentLength: Int): String = f"Content-type: ${contentType.toString}; charset=$charset\r\nContent-Length: $contentLength"
}

enum ContentType(s: String):
  override def toString: String = s
  case TextHtml extends ContentType("text/html")
  case TextPlain extends ContentType("text/plain")
  case ApplicationJavascript extends ContentType("application/javascript")
  case ApplicationJson extends ContentType("application/json")
end ContentType

