package utils

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Concurrent queue wrapper to trigger events on new element
 *
 * @param concurrentLinkedQueue the concurrent queue
 * @tparam A the type of the queue
 */
class QueueSubscriber[A](val concurrentLinkedQueue: ConcurrentLinkedQueue[A]) {
  var subscribed = new AtomicBoolean(true)

  /**
   * Block until new elements appear and apply function f to this new element
   *
   * @param f the function to be applied
   */
  def onNewElement(f: A => Unit): Unit =
    var vlue: A = concurrentLinkedQueue.poll()
    while (subscribed.get()) {
      if (vlue != null) {
        f(vlue)
      }
      vlue = concurrentLinkedQueue.poll()
    }

  /**
   * Unsubscribe to this, will stop the underlying [[onNewElement]] function
   */
  def unsubscribe(): Unit = subscribed.set(false)
}
