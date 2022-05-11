package websocket

class Uri(val path: String) {
  def combine(other: String): Uri = Uri(f"$path/$other")
  def combine(other: Uri): Uri = Uri(f"$path/${other.path}")

  override def toString: String = f"Uri($path)"
}

object Uri {
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
      case (lst: List[String]) :+ (last: String)  if lst.isEmpty => Some(Uri("/") -> last)
      case (lst: List[String]) :+ (last: String) => Some(lst.foldLeft(Uri(""))((acc, s) => acc.combine(s)) -> last)
      case _ => Some(Uri("/") -> "")
    }
}

object -> {
  def unapply(req: Request): Some[(Method, Uri)] =
    Some((req.method, req.uri))
}
