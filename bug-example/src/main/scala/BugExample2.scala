object BugExample2 {
  val optToInt: Option[{*} String -> Int] = Some(s => s.toInt)
  optToInt.get.asInstanceOf[String -> Any]("1234")
  // optToInt.map(_.asInstanceOf[String -> Any]("1234")) <-- Compiles without error
}