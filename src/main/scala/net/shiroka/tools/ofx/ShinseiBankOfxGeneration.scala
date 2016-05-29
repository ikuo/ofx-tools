package net.shiroka.tools.ofx

import java.io._
import scala.io.Source
import com.github.tototoshi.csv._

case class ShinseiBankOfxGeneration(sources: List[InputStream]) extends BankOfxGeneration {
  import ShinseiBankOfxGeneration._
  require(sources.forall(_ != null), "sources contains null element")

  def apply(sink: PrintWriter): Unit =
    sources
      .map(src => CSVReader.open(Source.fromInputStream(src, "UTF-16"))(tsvFormat))
      .foreach { csv =>
        try {
          csv.iterator.dropWhile(_ != header).drop(1).foreach { row =>
            sink.println(row)
          }
        } finally (csv.close)
      }
}

object ShinseiBankOfxGeneration {
  val tsvFormat = new TSVFormat {}
  val header = "取引日, 照会番号, 摘要, お支払金額, お預り金額, 残高".split(", ").toList

  def apply(source: InputStream): ShinseiBankOfxGeneration = apply(List(source))
}
