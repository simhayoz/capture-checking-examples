package foopackage

import annotation.capability

@capability class Foo(val s: List[{*} String]) {
  def notFound: Foo = Foo.of(s ++ List("-1"))
}

object Foo {
  def of(s: List[{*} String]): Foo =
    Foo(s)
}
