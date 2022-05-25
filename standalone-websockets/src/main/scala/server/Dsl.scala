package server

class Dsl {
  val Root: Uri = server.Uri.Root
  val / : server./.type = server./
  val -> : server.->.type = server.->
}
