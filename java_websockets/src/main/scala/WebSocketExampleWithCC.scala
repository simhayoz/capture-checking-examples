import websocket.{Close, Dsl, Header, HttpRoutes, IOApp, Method, Ok, Pipe, Request, ServerBuilder, StaticFile, Text, Uri, WebSocketBuilder, WebSocketFrame}
import websocket.Method.*
import websocket.ContentType.*

import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.mutable

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

  extension [A](l: List[A])
    def mkString: String = {
      val strBuilder: mutable.StringBuilder = mutable.StringBuilder()
      val these: Iterator[A] = l.iterator
      while (these.hasNext) {
        val curr = these.next()
        strBuilder.append(curr)
      }
      strBuilder.mkString
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
      case GET -> Root / "static" / filename =>
        StaticFile.fromPath(f"static/$filename")

      case GET -> Root =>
        val htmlContent = s"""<!DOCTYPE html>
                            |<html>
                            |<head>
                            |    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"/>
                            |    <script src="/static/app.js"></script>
                            |</head>
                            |<body>
                            |<div class="container"><h1>Scala Chat!</h1>
                            |    <div id="messageList">${messageList()}</div>
                            |    <div id="errorDiv" style="color: red;"></div>
                            |    <form onsubmit="submitForm(); return false"><input type="text" id="nameInput" placeholder="User name"/><input
                            |            type="text" id="msgInput" placeholder="Write a message!"/><input type="submit"/></form>
                            |</div>
                            |</body>
                            |</html>""".stripMargin
        Ok(htmlContent, Header(TextHtml))

      case req@POST -> Root =>
        val m: Message = req.as[Message]
        if (m.name == "") Ok(Response(false, "Name cannot be empty").asJson, Header(ApplicationJson))
        else if (m.msg == "") Ok(Response(false, "Message cannot be empty").asJson, Header(ApplicationJson))
        else {
          messages = List(m) ++ messages
          openConnectionQueues.map((s: {*} ConcurrentLinkedQueue[WebSocketFrame]) => s.offer(Text(messageList())))
          Ok(Response(true, "").asJson, Header(ApplicationJson))
        }

      case GET -> Root / "subscribe" =>
        val newQueue: ConcurrentLinkedQueue[WebSocketFrame] = new ConcurrentLinkedQueue[WebSocketFrame]()
        val queueAsCapability: {queueCapability} ConcurrentLinkedQueue[WebSocketFrame] = newQueue // TODO use newQueue as capability directly
        openConnectionQueues += queueAsCapability
        val toClient: ConcurrentLinkedQueue[WebSocketFrame] = newQueue
        val fromClient: Pipe[WebSocketFrame, Unit] = {
          case Text(t) => println(t)
          case Close(_) => openConnectionQueues -= newQueue
          case f => println(s"Unknown type: $f")
        }
        wsb.build(toClient, fromClient)
    }

  def messageList(): String = messages.reverse.map(m => s"<p><b>${m.name}</b> ${m.msg}</p>").mkString

  def stream: LazyList[Int] =
    ServerBuilder()
      .bindHttp(8080)
      .withHttpWebSocketApp(routes(WebSocketBuilder()).orNotFound)
      .serve
}
