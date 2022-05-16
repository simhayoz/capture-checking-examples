package server

import server.websocket.Method

enum Method(v: String):
  case GET extends Method("GET")
  case POST extends Method("POST")
end Method
