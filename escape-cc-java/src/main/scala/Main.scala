// Working
object Main {
  def takeWithoutCC(s: String): String=
    s + " should fail"
  def main(args: Array[String]): Unit =
    val s: {*} String = "a string with capability"
    println(takeWithoutCC(Escaper.escape(s).asInstanceOf[String]))
}

// Not working
//object Main {
//  def takeWithoutCC(s: String): String =
//    s + " should fail"
//  def main(args: Array[String]): Unit =
//    val s: {*} String = "a string with capability"
//    println(takeWithoutCC(s))
//}
