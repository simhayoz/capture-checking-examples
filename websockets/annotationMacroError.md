# Annotation macro error

Not working:

```scala
object CompilerErrorTest {
  @main def test() = {
    val default: Option[String => Int] = Some(s => s.toInt)
    val clazz: String = "1234"
    val testValue: Option[Any] = default match {
      case None => None
      case Some(getter) =>
        val value: Any = getter.asInstanceOf[String => Any](clazz)
        Some(value)
    }
    println(testValue)
  }
}
```

Working:

```scala
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
```

These codes are similar to cask/.../Macros.scala:

```scala
...
// lines 214-218
val defaultGetter: Expr[Option[Cls => Any]] = defaults.get(param) match {
    case None => '{None}
    case Some(expr) =>
    '{Some((_: Cls) => $expr)}
}
...
// lines 267-272
(sig.default match {
    case None => None
    case Some(getter) =>
        val value = getter.asInstanceOf[Cls => Any](clazz)
        Some(value)
}),
...
```