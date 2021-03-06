package net.shiroka.tools.ofx.aws

import scala.util.control.Exception.allCatch
import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }
import com.typesafe.config.ConfigFactory
import com.netaporter.uri.Uri
import net.shiroka.tools.ofx._

class Lambda {
  val config = ConfigFactory.load().getConfig("net.shiroka.tools.ofx.aws")
  val prefix = config.getString("s3.path.prefix")

  def handler(uri: String, context: Context): Unit = {
    val lambdaLogger: LambdaLogger = context.getLogger()
    lambdaLogger.log("Processing URI: " + uri)

    val name =
      allCatch.either(Uri.parse(uri).path.stripPrefix(prefix).takeWhile(_ != '/'))
        .fold(rethrow(_, s"Cannot get conversion name from uri $uri"), identity)

    Cli.main(Array("convert", name, uri))
  }
}
