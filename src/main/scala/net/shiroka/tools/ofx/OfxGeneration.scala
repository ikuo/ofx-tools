package net.shiroka.tools.ofx

import java.io._

trait OfxGeneration {
  val Default: Option[String] = None

  def apply(sources: List[InputStream], sinks: (Option[String]) => PrintStream): Unit

  def apply(sources: List[InputStream], sink: PrintStream): Unit = apply(sources, (_ => sink))

  def apply(source: InputStream, sink: PrintStream): Unit = apply(List(source), sink)

  def apply(sources: List[InputStream], sink: OutputStream): Unit = {
    val out = new PrintStream(sink)
    try { apply(sources, out) }
    finally { out.close }
  }

  def apply(source: InputStream, sink: OutputStream): Unit = apply(List(source), sink)

  def apply(sources: List[InputStream]): String = {
    val out = new PrintStream(new ByteArrayOutputStream())
    try {
      apply(sources, out)
      out.toString
    } finally { out.close() }
  }

  def moneyOpt(str: String, row: List[String]): Option[BigDecimal] =
    try {
      Option(str).map(_.trim).filter(_.nonEmpty).map(BigDecimal.apply)
    } catch {
      case err: Throwable => throw new RuntimeException(s"Failed to parse $str in row $row")
    }

  def money(str: String, row: List[String]): BigDecimal =
    moneyOpt(str, row).getOrElse(sys.error(s"Failed to parse '$str' in row: $row"))

  def noneIfEmpty(str: String): Option[String] = Option(str).map(_.trim).filter(_.nonEmpty)

  def closing[T <: Closeable, U](src: T)(f: T => U) = try (f(src)) finally (src.close)
}
