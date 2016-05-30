package net.shiroka.tools.ofx

object Implicits {
  implicit class ReducePairs[A](self: Iterable[(A, Iterator[Transaction])]) {
    val empty = (Nil: List[A], Iterator.apply[Transaction]())

    def reducePairs: (List[A], Iterator[Transaction]) =
      self.foldLeft(empty) { case ((csvs, iter), (csv, txns)) => (csv :: csvs, iter ++ txns) }
  }
}
