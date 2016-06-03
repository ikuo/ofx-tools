package net.shiroka.tools.ofx.aws

import java.io._
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.netaporter.uri.Uri.parse
import com.typesafe.config.ConfigFactory
import net.shiroka.tools.ofx.ShinseiBankOfxGeneration

class Lambda {
  val awsConfig = ConfigFactory.load("net.shiroka.tools.ofx.aws")

  def handler(uri: String, context: Context): String = {
    val lambdaLogger: LambdaLogger = context.getLogger()
    lambdaLogger.log("uri = " + uri)
    handleUri(uri)
  }

  def handleUri(uriStr: String): String = {
    val uri = parse(uriStr)
    val generation =
      uri.pathParts.takeRight(3).map(_.part).toList match {
        case "shinsei-bank" :: accountNum :: fileName :: Nil =>
          ShinseiBankOfxGeneration(accountNum.toLong)
        case parts => sys.error(s"Unexpected path parts $parts")
      }

    val client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain())
      .withRegion(Regions.valueOf(awsConfig.getString("region")))

    val src: InputStream = ???
    val sink: PrintStream = ???
    //generation(src, sink)
    uriStr
  }
}
