package net.shiroka.tools.ofx.aws

import java.io._
import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }

class Lambda {
  def handler(uri: String, context: Context): String = {
    val lambdaLogger: LambdaLogger = context.getLogger()
    lambdaLogger.log("uri = " + uri)
    handleUri(uri)
  }

  def handleUri(uriStr: String): String = {
    //generation(src, sink)
    uriStr
  }
}
