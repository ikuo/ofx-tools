package net.shiroka.tools.ofx.conversions

import java.io._
import scala.io.Source
import com.github.tototoshi.csv._
import com.typesafe.config.Config
import net.shiroka.tools.ofx._

case class SmbcVisa(config: Config) extends Conversion {
  import SmbcVisa._
  lazy val accountId = config.getString("account-id")

  def apply(
    source: InputStream,
    sink: PrintStream
  ): Result = {
    val csv = CSVReader.open(Source.fromInputStream(source, "Shift_JIS"))(tsvFormat)
    val rows = csv.iterator //.foreach(println)
    ???
  }
}

object SmbcVisa {
  val tsvFormat = new TSVFormat {}
}
