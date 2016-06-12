package net.shiroka.tools.ofx

import java.io._
import com.typesafe.config.Config
import com.netaporter.uri.Uri
import net.shiroka.tools.ofx.aws.S3
import Implicits.Tapper

trait Conversion {
  type Result = List[Closeable]
  val config: Config

  def apply(source: InputStream, sink: PrintStream): Result

  def apply(source: InputStream, sink: OutputStream): Result =
    apply(source, new PrintStream(sink, true, "UTF-8"))

  def apply(source: InputStream, sink: String): Result =
    closing(new FileOutputStream(new File(sink)))(apply(source, _))

  def apply(source: String, sink: String): Result =
    closing(new FileInputStream(new File(source)))(apply(_, sink))

  def apply(source: InputStream): String =
    printToBaos(out => apply(source, out)).toString

  def moneyOpt(str: String): Option[BigDecimal] =
    try {
      Option(str).map(_.trim).filter(_.nonEmpty).map(BigDecimal.apply)
    } catch {
      case err: Throwable => rethrow(err, s"Failed to parse $str")
    }

  def money(str: String): BigDecimal = moneyOpt(str).getOrElse(sys.error(s"Failed to parse '$str'"))
}

object Conversion {
  case class Cli[T <: Conversion](
      conversionName: String,
      conversion: Conversion,
      s3: S3
  ) {
    lazy val sourceFileSuffix = conversion.config.getString("source-file-suffix")

    def conversionWithSrc[T](uri: Uri) =
      conversion(s3.source(uri), _: PrintStream)

    def apply(args: List[String]) = args match {
      case s3uri :: "-" :: Nil =>
        val uri = Uri.parse(s3uri)
        conversionWithSrc(uri)(System.out).tap(closeAll)

      case s3uri :: Nil =>
        val uri = Uri.parse(s3uri)
        printToBaos(out => conversionWithSrc(uri)(out).tap(closeAll))
          .tap(s3.uploadAndAwait(uri, sourceFileSuffix, _))

      case src :: sink :: Nil =>
        conversion(src, sink).tap(closeAll)

      case _ => throw new IllegalArgumentException(args.mkString)
    }
  }
}
