package websocket

trait IOApp {

  type ExitCode = Int

  def main(args: Array[String]): Unit = run(args.toList)

  def run(args: List[String]): ExitCode

}
