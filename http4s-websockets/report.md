# The http4s websocket example
See full example at [src/main/scala/WebSocketExampleWithCC.scala](src/main/scala/WebSocketExampleWithCC.scala)

## The for comprehension bug

**This bug has been reduced and reported to dotty** ([https://github.com/lampepfl/dotty/issues/15005](https://github.com/lampepfl/dotty/issues/15005))

This will fail:
```scala
for {
    newQueue: Queue[F, Option[String]] <- Queue.unbounded[F, Option[String]]
    _ = openConnectionQueues += newQueue
    toClient: Stream[F, WebSocketFrame] =
     Stream.fromQueueNoneTerminated(newQueue).map(s => Text(s))
    fromClient: ({*} Pipe[F, WebSocketFrame, Unit]) = _.evalMap {
     case Text(t, _) => F.delay(println(t))
     case Close(_) => F.delay(openConnectionQueues -= newQueue)
     case f => F.delay(println(s"Unknown type: $f"))
    }
    res <- wsb.build(toClient, fromClient)
} yield res
```
```scala
[error] -- [E007] Type Mismatch Error: /home/simon/capture-checking-examples/http4s-websockets/src/main/scala/WebSocketExampleWithCC.scala:105:15 
[error] 105 |               fromClient: ({*} Pipe[F, WebSocketFrame, Unit]) = _.evalMap {
[error]     |               ^^^^^^^^^^
[error]     |Found:    (fromClient : {*} fs2.Pipe[F, org.http4s.websocket.WebSocketFrame, Unit])
[error]     |Required: {fromClient, *} (x$0: F, x$1: ? org.http4s.websocket.WebSocketFrame) -> Unit
[error]     |
[error]     | longer explanation available when compiling with `-explain`

```
Strange thing is, variable becomes its own capability.
Whereas this will work (even though this is just a desugared version of it):

```scala
Queue.unbounded[F, Option[String]].flatMap(newQueue => {
    openConnectionQueues += newQueue
    val toClient: Stream[F, WebSocketFrame] =
      Stream.fromQueueNoneTerminated(newQueue).map(s => Text(s))
    val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
      case Text(t, _) => F.delay(println(newQueue))
      case Close(_) => F.delay(openConnectionQueues -= newQueue)
      case f => F.delay(println(s"Unknown type: $f"))
    }
    wsb.build(toClient, fromClient)
})
```

## Using the star
It seems like in some cases the star is too restrictive when trying to track a variable, for example in the `openConnectionQueues` below and while creating a new queue `Queue.unbounded[F, Option[String]].flatMap((newQueue: {*} Queue[F, Option[String]]) =>`, would that be the right way to track elements as capabilities? 

## Errors in Cats Effect

```scala
[error] -- [E007] Type Mismatch Error: /home/simon/capture-checking-examples/http4s-websockets/src/main/scala/WebSocketExampleWithCC.scala:92:41 
[error] 92 |          Stream.fromQueueNoneTerminated(newQueue).map(s => Text(s))
[error]    |                                         ^^^^^^^^
[error]    |     Found:    (newQueue : {*} cats.effect.std.Queue[F, Option[String]])
[error]    |     Required: cats.effect.std.QueueSource[F, Option[? String]]
```
`newQueue` that has a capability cannot be passed to a function that does not have a capability.

```scala
[error] -- [E007] Type Mismatch Error: /home/simon/capture-checking-examples/http4s-websockets/src/main/scala/WebSocketExampleWithCC.scala:94:44 
[error] 94 |          case Text(t, _) => F.delay(println(newQueue))
[error]    |                                     ^^^^^^^^^^^^^^^^^
[error]    |                                     Found:    {newQueue} () ?-> Unit
[error]    |                                     Required: () ?-> Unit
```
Functions with capabilities cannot be passed to functions that do not require a capability. This seems to be normal regarding the type system:
```
A => B <=> {*}A -> B
{newQueue} <: {*} => {newQueue}A <: {*}A
=> {*}A -> B <: {newQueue}A -> B
```
Thus, `{*}() -> Unit <: {newQueue}() -> Unit`, this explains why it cannot be passed to the function as it is a super type of the required type.

## Explicit type needed
The type of `s` in the following code cannot be infered:

```scala
val openConnectionQueues: ListBuffer[{*} Queue[F, Option[String]]] = ListBuffer[{*} Queue[F, Option[String]]]()
// ...
openConnectionQueues.map(s => s.offer(Some(messageList().render))).toList.sequence
```
```scala
[error] -- [E007] Type Mismatch Error: /home/simon/capture-checking-examples/http4s-websockets/src/main/scala/WebSocketExampleWithCC.scala:82:44 
[error] 82 |              _ <- openConnectionQueues.map(s => s.offer(Some(messageList().render))).toList.sequence
[error]    |                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
[error]    |   Found:    ? ({*} Nothing) -> ? F[Unit]
[error]    |   Required: ({*} cats.effect.std.Queue[F, Option[String]]) => ? F[Unit]
```

It had to be manually typed `.map((s: {*} Queue[F, Option[String]]) => ...`.