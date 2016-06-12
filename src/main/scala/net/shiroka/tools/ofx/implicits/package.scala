package net.shiroka.tools.ofx

package object implicits {
  implicit class Tapper[T](self: T) {
    def tap[U](f: T => U): T = { f(self); self }
  }
}
