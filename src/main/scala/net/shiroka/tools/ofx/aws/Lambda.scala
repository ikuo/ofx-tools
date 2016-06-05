package net.shiroka.tools.ofx.aws

import java.io._
import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }
import net.shiroka.tools.ofx.ShinseiBankOfxGeneration

class Lambda {
  def handler(uri: String, context: Context): String = {
    val lambdaLogger: LambdaLogger = context.getLogger()
    lambdaLogger.log("Processing URI: " + uri)
    ShinseiBankOfxGeneration.main(Array(uri))
    uri
  }
}
