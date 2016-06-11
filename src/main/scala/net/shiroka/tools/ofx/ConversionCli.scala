package net.shiroka.tools.ofx

import java.io._
import com.netaporter.uri.Uri
import net.shiroka.tools.ofx.aws.S3
import Implicits.Tapper

case class ConversionCli[T <: Conversion](
    conversionName: String,
    conversion: Conversion,
    s3: S3
) {
  lazy val sourceFileSuffix = conversion.config.getString("source-file-suffix")

  def conversionWithSrc[T](uri: Uri) = {
    val pathParts = uri.pathParts.takeRight(3).map(_.part).toList
    pathParts match {
      case `conversionName` :: fileName :: Nil =>
        conversion(List(s3.source(uri)), _: Option[String] => PrintStream)

      case parts => sys.error(s"Unexpected path parts $parts")
    }
  }

  def apply(args: List[String]) = args match {
    case s3uri :: "-" :: Nil =>
      val uri = Uri.parse(s3uri)
      conversionWithSrc(uri)(_ => System.out).tap(closeAll)

    case s3uri :: Nil =>
      val uri = Uri.parse(s3uri)
      printToBaos(out => conversionWithSrc(uri)(_ => out).tap(closeAll))
        .tap(s3.uploadAndAwait(uri, sourceFileSuffix, _))

    case src :: sink :: Nil =>
      conversion(src, sink).tap(closeAll)

    case _ => throw new IllegalArgumentException(args.mkString)
  }
}
