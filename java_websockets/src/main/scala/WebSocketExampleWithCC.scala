
import server.{Dsl, ExitCode, Header, HttpRoutes, IOApp, Method, Ok, Pipe, Request, ServerBuilder, StaticFile, Uri}
import server.websocket.{Close, Text, WebSocketBuilder, WebSocketFrame}
import server.Method.*
import server.ContentType.*

import java.util.concurrent.{ConcurrentLinkedDeque, ConcurrentLinkedQueue}
import scala.collection.mutable

object WebSocketExampleWithCC extends IOApp {
  override def run(args: List[String]): ExitCode =
    new WebSocketExampleWithCCApp().stream.getLast
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

  def routes(wsb: WebSocketBuilder): {*} HttpRoutes =
    val queueCapability: {*} ConcurrentLinkedQueue[WebSocketFrame] = null
    val openConnectionQueues: ListBuffer[{queueCapability} ConcurrentLinkedQueue[WebSocketFrame]] = ListBuffer[{queueCapability} ConcurrentLinkedQueue[WebSocketFrame]]()
    val bootstrap = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"
    HttpRoutes.of(_ match {
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
          openConnectionQueues.map((s: {queueCapability} ConcurrentLinkedQueue[WebSocketFrame]) => s.offer(Text(messageList())))
          Ok(Response(true, "").asJson, Header(ApplicationJson))
        }

      case GET -> Root / "subscribe" =>
        val newQueue: {queueCapability} ConcurrentLinkedQueue[WebSocketFrame] = new ConcurrentLinkedQueue[WebSocketFrame]()
        openConnectionQueues += newQueue
        val toClient: {newQueue} ConcurrentLinkedQueue[WebSocketFrame] = newQueue
        val fromClient: {newQueue} Pipe[WebSocketFrame, Unit] = {
          case Text(t) => println(t)
          case Close(_) => openConnectionQueues -= newQueue
          case f => println(s"Unknown type: $f")
        }
        wsb.build(toClient, fromClient)
    })

  def messageList(): String = messages.reverse.map(m => s"<p><b>${m.name}</b> ${m.msg}</p>").mkString

  def stream: ConcurrentLinkedDeque[ExitCode] =
    ServerBuilder(8080, routes(WebSocketBuilder())) // TODO fix .orNotFound
      .serve
}
