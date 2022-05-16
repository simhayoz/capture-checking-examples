package server

import server.websocket.{ContentType, Header}

class Header(contentType: ContentType, charset: String = "UTF-8") {
  def toString(contentLength: Int): String = f"Content-type: ${contentType.toString}; charset=$charset\r\nContent-Length: $contentLength"
}

class NoHeader extends Header(ContentType.NoContentType, "") {
  override def toString(contentLength: Int): String = ""
}

enum ContentType(s: String):
  override def toString: String = s
  case TextHtml extends ContentType("text/html")
  case TextPlain extends ContentType("text/plain")
  case ApplicationJavascript extends ContentType("application/javascript")
  case ApplicationJson extends ContentType("application/json")
  case NoContentType extends ContentType("")
end ContentType

