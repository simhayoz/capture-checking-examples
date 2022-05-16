package server

trait IOApp {

  def main(args: Array[String]): Unit = run(args.toList)

  /**
   * Run the application
   * @param args arguments provided to the app
   * @return the exit code
   */
  def run(args: List[String]): Int

}
