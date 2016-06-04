package net.shiroka.tools.ofx

import java.io._

trait OfxGeneration {
  val Default: Option[String] = None

  def apply(sources: List[InputStream], sinks: (Option[String]) => PrintStream): Unit

  def apply(sources: List[InputStream], sink: PrintStream): Unit = apply(sources, (_ => sink))

  def apply(source: InputStream, sink: PrintStream): Unit = apply(List(source), sink)

  def apply(source: InputStream, sink: OutputStream): Unit = apply(List(source), new PrintStream(sink))

  def apply(sources: List[InputStream]): String =
    closing(new ByteArrayOutputStream()) { os =>
      closing(new PrintStream(os)) { out =>
        apply(sources, out)
        os.toString
      }
    }

  def apply(source: InputStream): String = apply(List(source))

  def apply(source: InputStream, sink: String): Unit =
    closing(new FileOutputStream(new File(sink)))(apply(source, _))

  def apply(source: String, sink: String): Unit =
    closing(new FileInputStream(new File(source)))(apply(_, sink))

  def moneyOpt(str: String): Option[BigDecimal] =
    try {
      Option(str).map(_.trim).filter(_.nonEmpty).map(BigDecimal.apply)
    } catch {
      case err: Throwable => rethrow(err, s"Failed to parse $str")
    }

  def money(str: String): BigDecimal = moneyOpt(str).getOrElse(sys.error(s"Failed to parse '$str'"))
}
