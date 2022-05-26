object ForComprehensionBugWorking {
  type OptFunc[-I, +O] = Option[I] => Option[O]

  def toOptional(i: Int, optFunc: OptFunc[Int, String]): Option[String] = optFunc(Some(i))

  Some(1).flatMap(a => {
    val optFunction: OptFunc[Int, String] = _.map(_.toString)
    toOptional(a, optFunction)
  })
}