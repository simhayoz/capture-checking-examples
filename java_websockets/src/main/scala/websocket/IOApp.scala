package websocket

trait IOApp {

  def main(args: Array[String]): Unit = run(args.toList)

  def run(args: List[String]): Int

}
