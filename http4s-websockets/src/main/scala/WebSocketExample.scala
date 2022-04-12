//import scalatags.Text.all.*
//import cats.effect.*
//import cats.implicits._
//import cats.effect.std.Queue
//import cats.syntax.all.*
//import org.http4s.*
//import org.http4s.blaze.server.BlazeServerBuilder
//import org.http4s.dsl.Http4sDsl
//import org.http4s.implicits.*
//import org.http4s.headers.*
//import org.http4s.server.websocket.*
//import org.http4s.websocket.WebSocketFrame
//import org.http4s.websocket.WebSocketFrame.*
//import scalatags.generic.Frag
//import scalatags.text.Builder
//import io.circe.generic.auto.*
//import io.circe.syntax.*
//import org.http4s.circe.*
//import org.http4s.dsl.io.*
//import org.http4s.implicits.*
//import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
//import io.circe.Encoder.AsArray.importedAsArrayEncoder
//import io.circe.Encoder.AsObject.importedAsObjectEncoder
//import io.circe.Encoder.AsRoot.importedAsRootEncoder
//import fs2.{Pipe, Stream}
//
//import scala.collection.mutable.ListBuffer
//import scala.concurrent.duration.*
//
//object WebSocketExample extends IOApp {
//  override def run(args: List[String]): IO[ExitCode] =
//    WebSocketExampleApp[IO].stream.compile.drain.as(ExitCode.Success)
//}
//
//class WebSocketExampleApp[F[_]](implicit F: Async[F]) extends Http4sDsl[F] {
//
//  var messages: Vector[Message] = Vector(Message("alice", "Hello World!"), Message("bob", "I am cow, hear me moo"))
//  val openConnectionQueues: ListBuffer[Queue[F, Option[String]]] = ListBuffer[Queue[F, Option[String]]]()
//
//  case class Message(name: String, msg: String)
//
//  case class Response(success: Boolean, err: String)
//
//  def routes(wsb: WebSocketBuilder2[F]): HttpRoutes[F] =
//    val bootstrap = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"
//    HttpRoutes.of[F] {
//      case request@GET -> Root / static / fileName =>
//        StaticFile.fromPath(fs2.io.file.Path(s"static/$fileName"), Some(request))
//          .getOrElseF(NotFound())
//
//      case GET -> Root =>
//        val htmlContent = doctype("html")(
//          html(
//            head(
//              link(rel := "stylesheet", href := bootstrap),
//              script(src := "/static/app.js")
//            ),
//            body(
//              div(cls := "container")(
//                h1("Scala Chat!"),
//                div(id := "messageList")(messageList()),
//                div(id := "errorDiv", color.red),
//                form(onsubmit := "submitForm(); return false")(
//                  input(`type` := "text", id := "nameInput", placeholder := "User name"),
//                  input(`type` := "text", id := "msgInput", placeholder := "Write a message!"),
//                  input(`type` := "submit")
//                )
//              )
//            )
//          )
//        )
//        Ok(htmlContent.render, `Content-Type`(MediaType.text.html, Charset.`UTF-8`))
//
//      case req@POST -> Root =>
//        for {
//          m: Message <- req.as[Message]
//          resp <- if (m.name == "") Ok(Response(false, "Name cannot be empty").asJson)
//          else if (m.msg == "") Ok(Response(false, "Message cannot be empty").asJson)
//          else {
//            messages = messages :+ m
//            for {
//              _ <- openConnectionQueues.map(s => s.offer(Some(messageList().render))).toList.sequence
//              res <- Ok(Response(true, "").asJson)
//            } yield res
//          }
//        } yield resp
//
//      case GET -> Root / "subscribe" =>
//        for {
//          newQueue: Queue[F, Option[String]] <- Queue.unbounded[F, Option[String]]
//          _ = openConnectionQueues += newQueue
//          toClient: Stream[F, WebSocketFrame] =
//            Stream.fromQueueNoneTerminated(newQueue).map(s => Text(s))
//          fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
//            case Text(t, _) => F.delay(println(t))
//            case Close(_) => F.delay(openConnectionQueues -= newQueue)
//            case f => F.delay(println(s"Unknown type: $f"))
//          }
//          res <- wsb.build(toClient, fromClient)
//        } yield res
//    }
//
//  def messageList(): Frag[Builder, String] = frag(for (u <- messages) yield p(b(u.name), " ", u.msg))
//
//  def stream: Stream[F, ExitCode] =
//    BlazeServerBuilder[F]
//      .bindHttp(8080)
//      .withHttpWebSocketApp(routes(_).orNotFound)
//      .serve
//}
//
//object WebSocketExampleApp {
//  def apply[F[_] : Async]: WebSocketExampleApp[F] =
//    new WebSocketExampleApp[F]
//}
