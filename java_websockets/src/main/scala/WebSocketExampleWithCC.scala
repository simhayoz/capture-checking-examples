import scalatags.Text.all.*
import scalatags.generic.Frag
import websocket.{Dsl, Header, HttpRoutes, IOApp, Method, Ok, RawServerRequest, RawWebSocket, Request, ServerBuilder, Uri, WebSocketBuilder2, StaticFile}
import websocket.Method.*
import websocket.ContentType.*

object WebSocketExampleWithCC extends IOApp {
  override def run(args: List[String]): Int =
    new WebSocketExampleWithCCApp().stream.last
}

class WebSocketExampleWithCCApp extends Dsl {
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
  implicit val messageTranslation: String => Message = s => {
    s.split(',') match {
      case Array(namePart, msgPart) => Message(namePart.substring(9, namePart.length-1), msgPart.substring(7, msgPart.length-2))
      case _ => throw new RuntimeException("Cannot parse message")
    }
  }
  case class Response(success: Boolean, err: String) {
    def asJson: String =
      f"{\"success\": $success, \"err\": \"$err\"}"
  }

  def routes(wsb: WebSocketBuilder2): HttpRoutes =
    val queueCapability: {*} LazyList[Option[String]] = null
    val openConnectionQueues: ListBuffer[{queueCapability} LazyList[Option[String]]] = ListBuffer[{queueCapability} LazyList[Option[String]]]()
    val bootstrap = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"
    HttpRoutes().of {
      case request@Request(GET, Uri("/static/app.js"), None) => // TODO filename should be string
        StaticFile.fromPath("static/app.js")

      case Request(GET, Uri("/"), None) =>
        val htmlContent = doctype("html")(
          html(
            head(
              link(rel := "stylesheet", href := bootstrap),
              script(src := "/static/app.js")
            ),
            body(
              div(cls := "container")(
                h1("Scala Chat!"),
                div(id := "messageList")(messageList()),
                div(id := "errorDiv", color.red),
                form(onsubmit := "submitForm(); return false")(
                  input(`type` := "text", id := "nameInput", placeholder := "User name"),
                  input(`type` := "text", id := "msgInput", placeholder := "Write a message!"),
                  input(`type` := "submit")
                )
              )
            )
          )
        )
        Ok(htmlContent.render, Header(TextHtml))

      case req@Request(POST, Uri("/"), Some(_)) =>
        val m: Message = req.as[Message]
        if (m.name == "") Ok(Response(false, "Name cannot be empty").asJson, Header(ApplicationJson))
        else if (m.msg == "") Ok(Response(false, "Message cannot be empty").asJson, Header(ApplicationJson))
        else {
          messages = List(m) ++ messages
//          openConnectionQueues.map((s: {*} LazyList[Option[String]]) => s.offer(Some(messageList().render))).toScalaList.sequence
          Ok(Response(true, "").asJson, Header(ApplicationJson))
        }

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
    }

  def messageList(): Frag[scalatags.text.Builder, String] = frag((for (u <- messages) yield p(b(u.name), " ", u.msg)).toScalaList)

  def stream: LazyList[Int] =
    ServerBuilder()
      .bindHttp(8080)
      .withHttpWebSocketApp(routes(WebSocketBuilder2()).orNotFound)
      .serve
}
