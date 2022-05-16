package server

import server.websocket

class Dsl {
  val Root: Uri = server.Uri.Root
  val / : /.type = server./
  val -> : ->.type = server.->
}
