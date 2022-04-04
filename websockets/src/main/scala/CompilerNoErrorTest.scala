object CompilerNoErrorTest {
  @main def testNoError() = {
    val default: Option[{*} String -> Int] = Some(s => s.toInt)
    val clazz: String = "1234"
    val testValue: Option[Any] = default match {
      case None => None
      case Some(getter) =>
        val value: Any = getter.asInstanceOf[String -> Any](clazz)
        Some(value)
    }
    println(testValue)
  }
}