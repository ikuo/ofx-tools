package net.shiroka.tools.ofx

import java.io._

trait OfxGeneration {
  def apply(sink: PrintWriter): Unit

  def apply(writer: Writer): Unit = {
    val pw = new PrintWriter(writer)
    try {
      apply(pw)
    } finally { pw.close() }
  }

  def apply(): String = {
    val sw = new StringWriter()
    try {
      apply(sw)
      sw.toString
    } finally { sw.close() }
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
