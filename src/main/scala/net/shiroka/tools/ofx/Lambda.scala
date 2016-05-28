package net.shiroka.tools.ofx

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger

class Lambda {
  def handler(count: Int, context: Context): String = {
    val lambdaLogger: LambdaLogger = context.getLogger()
    lambdaLogger.log("count = " + count)
    String.valueOf(count)
  }
}
