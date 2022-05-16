package utils

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

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

  def unsubscribe(): Unit = subscribed.set(false)
}
