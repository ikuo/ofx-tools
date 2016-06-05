package net.shiroka.tools.ofx

import com.amazonaws.services.s3.model._
import com.netaporter.uri.Uri
import net.shiroka.tools.ofx.aws.S3

case class AccountNumberCli[T <: Generation](institutionKey: String, makeGeneration: Long => T)
    extends Cli {
  val s3 = S3()

  def generate[T](uri: Uri)(f: (Generation, S3ObjectInputStream) => T): T = {
    uri.pathParts.takeRight(3).map(_.part).toList match {
      case `institutionKey` :: accountNum :: fileName :: Nil =>
        closing(s3.source(uri))(f(makeGeneration(accountNum.toLong), _))
      case parts => sys.error(s"Unexpected path parts $parts")
    }
  }

  val handleArgs: PartialFunction[List[String], Unit] = {
    case s3uri :: "-" :: Nil =>
      generate(Uri.parse(s3uri))((gen, src) => gen.apply(src, System.out))

    case s3uri :: Nil =>
      val uri = Uri.parse(s3uri)
      generate(uri)((gen, src) => s3.uploadAndAwait(gen, src, uri))

    case accountNum :: src :: sink :: Nil => makeGeneration(accountNum.toLong)(src, sink)
  }
}
