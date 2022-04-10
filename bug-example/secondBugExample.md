## Compiler version

3.1.3-RC1-bin-SNAPSHOT on the `cc-experiment` branch (last commit: 952a829ae39700849bf4ad98d9b7b110d3f89a1a) with `-Ycc`

## Minimized code

<!--
This code should be self contained, compilable (with possible failures) and as small as possible.

Ideally, we should be able to just copy this code in a file and run `scalac` (and maybe `scala`) to reproduce the issue.
-->

This works:
```Scala
object BugExample1 {
  val optToInt: Option[{*} String -> Int] = Some(s => s.toInt)
  optToInt match {
    case None => None
    case Some(toInt) => toInt.asInstanceOf[String -> Any]("1234")
  }
}
```

This does not work
```scala
object BugExample2 {
  val optToInt: Option[{*} String -> Int] = Some(s => s.toInt)
  optToInt.get.asInstanceOf[String -> Any]("1234")
  // optToInt.map(_.asInstanceOf[String -> Any]("1234")) <-- Compiles without error
}
```

This does not work as well
```Scala
object BugExample3 {
  val optToInt: Option[String => Int] = Some(s => s.toInt)
  optToInt match {
    case None => None
    case Some(toInt) => toInt.asInstanceOf[String -> Any]("1234")
  }
}
```

## Output

```scala
[info] compiling 6 Scala sources to /home/simon/capture-checking-examples/bug-example/target/scala-3.1.3-RC1-bin-SNAPSHOT/classes ...
[error] -- Error: /home/simon/capture-checking-examples/bug-example/src/main/scala/BugExample2.scala:3:11 
[error] 3 |  optToInt.get.asInstanceOf[String -> Any]("1234")
[error]   |  ^^^^^^^^^^^^
[error]   |The expression's type {*} String -> Int is not allowed to capture the root capability `*`.
[error]   |This usually means that a capability persists longer than its allowed lifetime.
[error] -- Error: /home/simon/capture-checking-examples/bug-example/src/main/scala/BugExample3.scala:5:21 
[error] 5 |    case Some(toInt) => toInt.asInstanceOf[String -> Any]("1234")
[error]   |                     ^
[error]   |The expression's type {*} String -> Int is not allowed to capture the root capability `*`.
[error]   |This usually means that a capability persists longer than its allowed lifetime.
[error] two errors found
[error] (Compile / compileIncremental) Compilation failed
```

## Expectation