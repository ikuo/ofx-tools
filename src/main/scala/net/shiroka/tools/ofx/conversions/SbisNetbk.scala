package net.shiroka.tools.ofx.conversions

import java.io._
import scala.io.Source

import com.github.tototoshi.csv._
import org.joda.time._
import org.joda.time.format._
import com.typesafe.config.Config
import net.shiroka.tools.ofx._
import Transaction._

case class SbisNetbk(config: Config) extends Conversion {
  import SbisNetbk._
  def apply(
    source: InputStream,
    sink: PrintStream
  ): Result = {
    val csv = CSVReader.open(Source.fromInputStream(source, "Shift_JIS"))
    ???
  }
}

object SbisNetbk {
}
