package net.shiroka.tools.ofx

import java.io._

trait OfxGeneration {
  def apply(sink: PrintStream): Unit

  def apply(): String = {
    val out = new PrintStream(new ByteArrayOutputStream())
    try {
      apply(out)
      out.toString
    } finally { out.close() }
  }

  def apply(sink: OutputStream): Unit = {
    val out = new PrintStream(sink)
    try { apply(out) }
    finally { out.close }
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
}
