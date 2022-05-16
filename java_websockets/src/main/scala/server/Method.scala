package server

enum Method(v: String):
  case GET extends Method("GET")
  case POST extends Method("POST")
end Method
