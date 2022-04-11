// import scalatags.Text.all._

// import cats.effect._
// import org.http4s._
// import org.http4s.headers._
// import org.http4s.dsl.io._
// import org.http4s.implicits._
// import org.http4s.server.blaze._

// import java.io.File

// object WebSocketExample2 extends IOApp {
//   import scala.concurrent.ExecutionContext.global

//   var messages = Vector(("alice", "Hello World!"), ("bob", "I am cow, hear me moo"))
//   // var openConnections = Set.empty[cask.WsChannelActor]
//   val bootstrap = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"

//   val app = HttpRoutes.of[IO] {

//     case request @ GET -> Root / static / fileName =>
//       {println(s"static/$fileName")
//       StaticFile.fromFile(new File(s"static/$fileName"), Some(request))
//         .getOrElseF(NotFound())}

//     case GET -> Root => {
//       val htmlContent = doctype("html")(
//         html(
//           head(
//             link(rel := "stylesheet", href := bootstrap),
//             script(src := "/static/app.js")
//           ),
//           body(
//             div(cls := "container")(
//               h1("Scala Chat!"),
//               div(id := "messageList")(messageList()),
//               div(id := "errorDiv", color.red),
//               form(onsubmit := "submitForm(); return false")(
//                 input(`type` := "text", id := "nameInput", placeholder := "User name"),
//                 input(`type` := "text", id := "msgInput", placeholder := "Write a message!"),
//                 input(`type` := "submit")
//               )
//             )
//           )
//         )
//       )
//       Ok(htmlContent.render, `Content-Type`(MediaType.text.html, Charset.`UTF-8`))
//     }

//     // case GET -> Root / "ws" =>
//     //   val toClient: Stream[F, WebSocketFrame] =
//     //     Stream.awakeEvery[F](1.seconds).map(d => Text(s"Ping! $d"))
//     //   val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
//     //     case Text(t, _) => F.delay(println(t))
//     //     case f => F.delay(println(s"Unknown type: $f"))
//     //   }
//     //   wsb.build(toClient, fromClient)

//     // case GET -> Root / "hello" / name =>
//     //     Ok(s"Hello, $name.")
//     }.orNotFound
  
//   def messageList() = frag(for ((name, msg) <- messages) yield p(b(name), " ", msg))

//   def run(args: List[String]): IO[ExitCode] =
//     BlazeServerBuilder[IO](global)
//       .bindHttp(8080, "localhost")
//       .withHttpApp(app)
//       .serve
//       .compile
//       .drain
//       .as(ExitCode.Success)


//   // @cask.postJson("/")
//   // def postChatMsg(name: String, msg: String) = {
//   //   if (name == "") ujson.Obj("success" -> false, "err" -> "Name cannot be empty")
//   //   else if (msg == "") ujson.Obj("success" -> false, "err" -> "Message cannot be empty")
//   //   else {
//   //     messages = messages :+ (name -> msg)
//   //     for (conn <- openConnections) conn.send(cask.Ws.Text(messageList().render))
//   //     ujson.Obj("success" -> true, "err" -> "")
//   //   }
//   // }

//   // @cask.websocket("/subscribe")
//   // def subscribe() = cask.WsHandler { connection =>
//   //   connection.send(cask.Ws.Text(messageList().render))
//   //   openConnections += connection
//   //   cask.WsActor { case cask.Ws.Close(_, _) => openConnections -= connection }
//   // }

//   // initialize()
// }