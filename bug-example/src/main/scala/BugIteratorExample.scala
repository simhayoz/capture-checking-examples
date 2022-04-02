object BugIteratorExample {

  // Extremely slow if using CollectionStrawman
  import colltest5.strawman.collections.*
  import CollectionStrawMan5.*

  def main(args: Array[String]) = {
    // Strange bug
    var queue: Iterator[String] = Iterator("start")
    println(queue.getClass())
    var i = 0
    while(queue.hasNext && i < 3000){
      val current = queue.next()
      println(current)
      queue ++= Iterator("test " + i)
      queue ++= Iterator("test2 " + i)
      i += 1
    }
  }
}
