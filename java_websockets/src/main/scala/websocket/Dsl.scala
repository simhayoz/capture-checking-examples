package websocket

class Dsl {
  val Root: websocket.Uri = websocket.Uri.Root
  val / : websocket./.type = websocket./
  val -> : websocket.->.type = websocket.->
}
