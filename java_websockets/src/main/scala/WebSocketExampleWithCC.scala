import scalatags.Text.all.*
import scalatags.generic.Frag
import websocket.IOApp

object WebSocketExampleWithCC extends IOApp {
  override def run(args: List[String]): ExitCode = 0
    new WebSocketExampleWithCCApp().stream
}

class WebSocketExampleWithCCApp {
  import colltest5.strawman.collections.*
  import CollectionStrawMan5.*

  extension [A](l: ListBuffer[A])
    // Really not optimized method to remove one element from a ListBuffer
    def -=(elem: A): ListBuffer[A] = {
      val newList = ListBuffer[A]()
      val these: Iterator[A] = l.iterator
      while (these.hasNext) {
        val curr = these.next()
        if (curr != elem) {
          newList += curr
        }
      }
      newList
    }
    def toScalaList: scala.List[A] = {
      var newList = scala.List[A]()
      val these: Iterator[A] = l.iterator
      while (these.hasNext) {
        newList = these.next() :: newList
      }
      newList
    }

  extension [A](l: List[A])
    def toScalaList: scala.List[A] = {
      var newList = scala.List[A]()
      val these: Iterator[A] = l.iterator
      while (these.hasNext) {
        newList = these.next() :: newList
      }
      newList
    }

  var messages: List[Message] = List(Message("bob", "I am cow, hear me moo"), Message("alice", "Hello World!"))

  case class Message(name: String, msg: String)

  case class Response(success: Boolean, err: String)
//
//  // Not working
//  def removeCapability[A](el: {*} A): A = el.asInstanceOf[A]
//
//  def routes(wsb: WebSocketBuilder2[F]): HttpRoutes[F] =
//    val queueCapability: {*} Queue[F, Option[String]] = null
//    val openConnectionQueues: ListBuffer[{queueCapability} Queue[F, Option[String]]] = ListBuffer[{queueCapability} Queue[F, Option[String]]]()
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
//            messages = List(m) ++ messages
//            for {
//              _ <- openConnectionQueues.map((s: {*} Queue[F, Option[String]]) => s.offer(Some(messageList().render))).toScalaList.sequence
//              res <- Ok(Response(true, "").asJson)
//            } yield res
//          }
//        } yield resp
//
//      case GET -> Root / "subscribe" =>
//        Queue.unbounded[F, Option[String]].flatMap((newQueue: Queue[F, Option[String]]) => {
//          val queueAsCapability: {queueCapability} Queue[F, Option[String]] = newQueue
//          openConnectionQueues += queueAsCapability
//          val toClient: Stream[F, WebSocketFrame] =
//            Stream.fromQueueNoneTerminated(newQueue).map(s => Text(s))
//          val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
//            case Text(t, _) => F.delay(println(t))
//            case Close(_) => F.delay(openConnectionQueues -= newQueue)
//            case f => F.delay(println(s"Unknown type: $f"))
//          }
//          wsb.build(toClient, fromClient)
//        })
//    }
//
  def messageList(): Frag[scalatags.text.Builder, String] = frag((for (u <- messages) yield p(b(u.name), " ", u.msg)).toScalaList)

  def stream: ExitType = 0
//    BlazeServerBuilder[F]
//      .bindHttp(8080)
//      .withHttpWebSocketApp(routes(_).orNotFound)
//      .serve
}
