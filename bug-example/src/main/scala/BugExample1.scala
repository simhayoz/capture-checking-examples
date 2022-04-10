object BugExample1 {
  val optToInt: Option[{*} String -> Int] = Some(s => s.toInt)
  optToInt match {
    case None => None
    case Some(toInt) => toInt.asInstanceOf[String -> Any]("1234")
  }
}