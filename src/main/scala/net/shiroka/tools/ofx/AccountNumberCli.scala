package net.shiroka.tools.ofx

import java.io._
import com.netaporter.uri.Uri
import net.shiroka.tools.ofx.aws.S3
import Implicits.Tapper

case class AccountNumberCli[T <: Generation](
    institutionKey: String,
    makeGeneration: Long => T,
    sourceFileSuffix: String,
    s3: S3
) {
  def ofxGenerationWithSrc[T](uri: Uri) = {
    val pathParts = uri.pathParts.takeRight(3).map(_.part).toList
    pathParts match {
      case `institutionKey` :: accountNum :: fileName :: Nil =>
        closing(s3.source(uri)) { src =>
          makeGeneration(accountNum.toLong)(List(src), _: Option[String] => PrintStream)
        }

      case parts => sys.error(s"Unexpected path parts $parts")
    }
  }

  def apply(args: Array[String]) = args.toList match {
    case s3uri :: "-" :: Nil =>
      val uri = Uri.parse(s3uri)
      ofxGenerationWithSrc(uri)(_ => System.out)

    case s3uri :: Nil =>
      val uri = Uri.parse(s3uri)
      printToBaos(out => ofxGenerationWithSrc(uri)(_ => out))
        .tap(s3.uploadAndAwait(uri, sourceFileSuffix, _))

    case accountNum :: src :: sink :: Nil =>
      makeGeneration(accountNum.toLong)(src, sink)

    case _ => throw new IllegalArgumentException(args.mkString)
  }
}
