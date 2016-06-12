package net.shiroka.tools.ofx

object Implicits {
  implicit class Tapper[T](self: T) {
    def tap[U](f: T => U): T = { f(self); self }
  }
}
