package net.shiroka.tools.ofx.aws

import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }

class Lambda {
  def handler(uri: String, context: Context): String = {
    val lambdaLogger: LambdaLogger = context.getLogger()
    lambdaLogger.log("uri = " + uri)
    uri
  }
}
