object BugExample3 {
  val optToInt: Option[String => Int] = Some(s => s.toInt)
  optToInt match {
    case None => None
    case Some(toInt) => toInt.asInstanceOf[String -> Any]("1234")
  }
}
// minimize bug and report it as an issue
// write it down as email all error (markdown)
// - is putting the star the right way to track
// - two errors into cats
// - type inference of `openConnectionQueues.map((s`