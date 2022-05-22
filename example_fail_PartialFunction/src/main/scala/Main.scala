import foopackage.Foo
// Ran it without -Ycc -> fails as well -> not a bug from Ycc
class Main {
  def get: Foo = routes().notFound

  def routes(): Foo =
    Foo.of(List(scala.util.Random.nextInt.toString))
}

object Main {
  def main(args: Array[String]): Unit =
    println(Main().get)

  
}

class Test(val port: Int, val routes: Foo)

object Test {
  def apply(port: Int, routes: Foo): {routes} Test =
    new Test(port, routes)
}