import scalatags.Text.all.*
import scalatags.generic.Frag
import websocket.{Dsl, Header, HttpRoutes, IOApp, Close, Method, Ok, RawServerRequest, RawWebSocket, Request, ServerBuilder, StaticFile, Uri, WebSocketBuilder, WebSocketFrame, Text, Pipe}
import websocket.Method.*
import websocket.ContentType.*

import java.util.concurrent.ConcurrentLinkedQueue

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

  def routes(wsb: WebSocketBuilder): HttpRoutes =
    val queueCapability: {*} LazyList[String] = null
    val openConnectionQueues: ListBuffer[{queueCapability} ConcurrentLinkedQueue[WebSocketFrame]] = ListBuffer[{queueCapability} ConcurrentLinkedQueue[WebSocketFrame]]()
    val bootstrap = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"
    HttpRoutes().of {
      case Request(GET, Uri("/static/app.js"), None) => // TODO filename should be string
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
          openConnectionQueues.map((s: {*} ConcurrentLinkedQueue[WebSocketFrame]) => s.offer(Text(messageList().render)))
          Ok(Response(true, "").asJson, Header(ApplicationJson))
        }

      case Request(GET, Uri("/subscribe"), None) =>
        val newQueue: ConcurrentLinkedQueue[WebSocketFrame] = new ConcurrentLinkedQueue[WebSocketFrame]()
        val queueAsCapability: {queueCapability} ConcurrentLinkedQueue[WebSocketFrame] = newQueue
        openConnectionQueues += queueAsCapability
        val toClient: ConcurrentLinkedQueue[WebSocketFrame] = newQueue
        val fromClient: Pipe[WebSocketFrame, Unit] = {
          case Text(t) => println(t)
          case Close(_) => openConnectionQueues -= newQueue
          case f => println(s"Unknown type: $f")
        }
        wsb.build(toClient, fromClient)
    }

  def messageList(): Frag[scalatags.text.Builder, String] = frag((for (u <- messages) yield p(b(u.name), " ", u.msg)).toScalaList)

  def stream: LazyList[Int] =
    ServerBuilder()
      .bindHttp(8080)
      .withHttpWebSocketApp(routes(WebSocketBuilder()).orNotFound)
      .serve
}
