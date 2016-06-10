package net.shiroka.tools.ofx

import java.io._

trait Generation {
  type Result = List[Closeable]
  val Default: Option[String] = None

  def apply(sources: List[InputStream], sinks: (Option[String]) => PrintStream): Result

  def apply(sources: List[InputStream], sink: PrintStream): Result =
    apply(sources, (_ => sink))

  def apply(source: InputStream, sink: PrintStream): Result =
    apply(List(source), sink)

  def apply(source: InputStream, sink: OutputStream): Result =
    apply(List(source), new PrintStream(sink, true, "UTF-8"))

  def apply(source: InputStream, sink: String): Result =
    closing(new FileOutputStream(new File(sink)))(apply(source, _))

  def apply(source: String, sink: String): Result =
    closing(new FileInputStream(new File(source)))(apply(_, sink))

  def apply(sources: List[InputStream]): String =
    printToBaos(out => apply(sources, out)).toString

  def apply(source: InputStream): String = apply(List(source))

  def moneyOpt(str: String): Option[BigDecimal] =
    try {
      Option(str).map(_.trim).filter(_.nonEmpty).map(BigDecimal.apply)
    } catch {
      case err: Throwable => rethrow(err, s"Failed to parse $str")
    }

  def money(str: String): BigDecimal = moneyOpt(str).getOrElse(sys.error(s"Failed to parse '$str'"))
}
