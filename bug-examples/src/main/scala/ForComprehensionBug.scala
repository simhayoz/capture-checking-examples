object ForComprehensionBug {
  type OptFunc[-I, +O] = Option[I] => Option[O]

  def toOptional(i: Int, optFunc: OptFunc[Int, String]): Option[String] = optFunc(Some(i))

  for {
    a <- Some(1)
    optFunction: OptFunc[Int, String] = _.map(_.toString)
  } yield toOptional(a, optFunction)
}