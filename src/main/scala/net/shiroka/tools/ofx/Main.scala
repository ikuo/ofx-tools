package net.shiroka.tools.ofx

import com.typesafe.config._
import net.shiroka.tools.ofx.aws.S3

object Main {
  val config = ConfigFactory.load().getConfig("net.shiroka.tools.ofx")

  def main(args: Array[String]): Unit = args.toList match {
    case "convert" :: name :: tail =>
      val cfg = config.getConfig(s"conversions.$name")
      val conversion: Conversion =
        Class.forName(cfg.getString("class")).getConstructor(classOf[Config])
          .newInstance(cfg).asInstanceOf[Conversion]
      ConversionCli(name, conversion, S3())(tail)

    case _ => throw new IllegalArgumentException(args.mkString(" "))
  }
}
