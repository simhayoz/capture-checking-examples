package websocket

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{ExecutionContext, Future}

class QueueSubscriber[A](val concurrentLinkedQueue: ConcurrentLinkedQueue[A]) {
  var subscribed = new AtomicBoolean(true)

  def onNewElement(f: A => Unit): Unit =
    var vlue: A = concurrentLinkedQueue.poll()
    while(subscribed.get()) {
      if(vlue != null) {
        f(vlue)
      }
      vlue = concurrentLinkedQueue.poll()
    }

    def unsubscribe: Unit = subscribed.set(false)
}

object QueueSubscriber {
  def apply[A](concurrentLinkedQueue: ConcurrentLinkedQueue[A], f: A => Unit)(implicit ec: ExecutionContext): QueueSubscriber[A] = {
    val q = new QueueSubscriber(concurrentLinkedQueue)
    Future {
      q.onNewElement(f)
    }
    q
  }
}
