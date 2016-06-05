package net.shiroka.tools

import java.io._
import scala.util.control.Exception.allCatch

package object ofx {
  def noneIfEmpty(str: String): Option[String] = Option(str).map(_.trim).filter(_.nonEmpty)

  def closing[T <: Closeable, U](src: T)(f: T => U): U = try (f(src)) finally (allCatch.either(src.close))

  def closing[T <: Closeable, U](srcs: Iterable[T])(f: Iterable[T] => U): U =
    try (f(srcs)) finally (srcs.map(src => allCatch.either(src.close)))

  def rethrow(cause: Throwable, msg: String) = throw new RuntimeException(msg, cause)
}