package websocket

class Dsl {
  val Root = new Uri("")
  val / : websocket./.type = websocket./
  val -> : websocket.->.type = websocket.->
}
