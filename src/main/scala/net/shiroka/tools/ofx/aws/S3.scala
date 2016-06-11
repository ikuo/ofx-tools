package net.shiroka.tools.ofx.aws

import java.io._
import scala.util.control.Exception.catching
import com.amazonaws.regions._
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import com.amazonaws.services.s3.transfer._
import com.netaporter.uri.Uri
import com.typesafe.config.ConfigFactory
import net.shiroka.tools.ofx.Implicits.Tapper
import net.shiroka.tools.ofx._

case class S3() {
  val awsConfig = ConfigFactory.load(getClass.getClassLoader)
  val region = awsConfig.getString("net.shiroka.tools.ofx.aws.region")

  lazy val client: AmazonS3Client =
    new AmazonS3Client(getCredentials)
      .tap(_.setRegion(RegionUtils.getRegion(region)))

  def source(uri: Uri): S3ObjectInputStream = {
    val obj = client.getObject(uri.host.get, uri.path.drop(1))
    obj.getObjectContent
  }

  def uploadAndAwait(conversion: Conversion, src: InputStream, originalUri: Uri, suffix: String): Unit =
    closing(new ByteArrayOutputStream().tap(out => conversion.apply(src, out))) { baos =>
      uploadAndAwait(
        bucket = originalUri.host.get,
        key = originalUri.path.drop(1).stripSuffix(suffix) ++ "ofx",
        is = new ByteArrayInputStream(baos.toByteArray),
        size = baos.size
      )
    }

  def uploadAndAwait(originalUri: Uri, suffix: String, baos: ByteArrayOutputStream): Unit =
    uploadAndAwait(
      bucket = originalUri.host.get,
      key = originalUri.path.drop(1).stripSuffix(suffix) ++ "ofx",
      is = new ByteArrayInputStream(baos.toByteArray),
      size = baos.size
    )

  def uploadAndAwait(bucket: String, key: String, is: InputStream, size: Int): Unit =
    new TransferManager(client).tap { transfer =>
      catching(classOf[InterruptedException]).either {
        transfer.upload(
          bucket,
          key,
          is,
          new ObjectMetadata().tap(_.setContentLength(size))
        ).waitForCompletion
      }.fold(err => throw new IOException(err), identity)
    }.shutdownNow()
}
