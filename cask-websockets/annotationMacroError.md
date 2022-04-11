# Annotation macro error

Original code in cask/.../Macros.scala:

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

Not working:

```scala
object CompilerErrorTest {
  @main def test() = {
    val defaultGetter: Option[String => Int] = Some(s => s.toInt)
    val clazz: String = "1234"
    val testValue: Option[Any] = defaultGetter match {
      case None => None
      case Some(getter) =>
        val value: Any = getter.asInstanceOf[String => Any](clazz)
        Some(value)
    }
    println(testValue)
  }
}
```

Fails with error:
```scala
[error] -- Error: /home/simon/capture-checking-examples/websockets/src/main/scala/CompilerErrorTest.scala:8:8 
[error] 8 |        val value: Any = getter.asInstanceOf[String => Any](clazz)
[error]   |        ^
[error]   |The expression's type {*} String -> Int is not allowed to capture the root capability `*`.
[error]   |This usually means that a capability persists longer than its allowed lifetime.
[error] -- Error: /home/simon/capture-checking-examples/websockets/src/main/scala/CompilerErrorTest.scala:8:44 
[error] 8 |        val value: Any = getter.asInstanceOf[String => Any](clazz)
[error]   |                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
[error]   |The expression's type {*} String -> Any is not allowed to capture the root capability `*`.
[error]   |This usually means that a capability persists longer than its allowed lifetime.
```


Working:

```scala
object CompilerNoErrorTest {
  @main def testNoError() = {
    val defaultGetter: Option[{*} String -> Int] = Some(s => s.toInt)
    val clazz: String = "1234"
    val testValue: Option[Any] = defaultGetter match {
      case None => None
      case Some(getter) =>
        val value: Any = getter.asInstanceOf[String -> Any](clazz)
        Some(value)
    }
    println(testValue)
  }
}
```
