package net.shiroka.tools.ofx

object Implicits {
  implicit class ReducePairs[A](self: Iterable[(A, Iterator[Transaction])]) {
    val empty = (Nil: List[A], Iterator.apply[Transaction]())

    def reducePairs: (List[A], Iterator[Transaction]) =
      self.foldLeft(empty) { case ((csvs, iter), (csv, txns)) => (csv :: csvs, iter ++ txns) }
  }

  implicit class Tapper[T](self: T) {
    def tap[U](f: T => U): T = { f(self); self }
  }
}
