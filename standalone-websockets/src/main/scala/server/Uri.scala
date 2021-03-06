package server

import server.{Method, Request}

/**
 * Represents the path to a request
 *
 * @param path string representation of the path
 */
class Uri(val path: String) {
  /**
   * Combine this uri with a new part of the path
   *
   * @param part the new part of the path to add
   * @return
   */
  def combine(part: String): Uri = Uri(f"$path/$part")

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
