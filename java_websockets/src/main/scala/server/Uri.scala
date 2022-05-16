package server

import server.{Method, Request}

class Uri(val path: String) {
  def combine(other: String): Uri = Uri(f"$path/$other")

  def combine(other: Uri): Uri = Uri(f"$path/${other.path}")

  override def toString: String = f"Uri($path)"
}

object Uri {
  val Root: Uri = new Uri("/")

  def apply(path: String): Uri = new Uri(path)

  def unapply(arg: Uri): Option[String] = Some(arg.path)
}

object / {
  def unapply(uri: Uri): Option[(Uri, String)] =
    val pth = if (uri.path != "/" && uri.path.startsWith("/")) {
      uri.path.substring(1)
    } else {
      uri.path
    }
    pth.split('/').toList match {
      case (lst: List[String]) :+ (last: String) if lst.isEmpty => Some(Uri.Root -> last)
      case (lst: List[String]) :+ (last: String) => Some(lst.foldLeft(Uri(""))((acc, s) => acc.combine(s)) -> last)
      case _ => None
    }
}

object -> {
  def unapply(req: Request): Some[(Method, Uri)] =
    if (req.uri.path == "/") {
      Some(req.method -> Uri.Root)
    } else {
      Some(req.method -> req.uri)
    }
}
