package net.shiroka.tools.ofx.conversions

import java.io._
import scala.io.Source
import scala.util.control.Exception.allCatch
import org.joda.time._
import org.joda.time.format._
import com.github.tototoshi.csv._
import com.typesafe.config.Config
import net.shiroka.tools.ofx._
import implicits.Tapper
import Transaction._

case class GnucashExport(config: Config) extends Conversion {
  import GnucashExport._
  lazy val accountId = config.getString("account-id")
  lazy val timezoneOffset = config.getString("timezone-offset")
  lazy val currency = config.getString("currency")

  def apply(
    source: InputStream,
    sink: PrintStream
  ): Result = {
    val csv = CSVReader.open(Source.fromInputStream(source, "UTF-8"))
    val rows = csv.iterator.tap(it => assertHeader(it.next()))
    lazy val transactions = read(rows)

    closing(csv)(_ =>
      Statement(accountId, Statement.Savings, currency, transactions)
        .wrap.writeOfx(sink))

    sink :: source :: Nil
  }

  private def read(rows: Iterator[Seq[String]]): Iterator[Transaction] = {
    rows.map(_.toList match {
      case row @ postDate :: enterDate :: desc :: memo :: _ :: valueNum :: valueDenom ::
        qtyNum :: qtyDenom :: _currency :: num :: _ =>
        allCatch.either {
          require(_currency == currency)
          require(valueDenom == "1")
          require(qtyNum == valueNum)
          require(qtyDenom == valueDenom)
          require(num.isEmpty)
          Transaction(
            dateTime = DateTime.parse(s"$postDate $timezoneOffset", dateFormat),
            `type` = Debit,
            description = List(desc.trim, memo.trim).flatMap(noneIfEmpty).mkString("; "),
            amount = BigDecimal(valueNum),
            balance = BigDecimal(0) // dummy
          )
        }.fold(rethrow(_, s"Failed process row $row"), identity)
      case row => sys.error(s"Malformed row $row")
    })
  }

  private def assertHeader(row: Seq[String]) =
    if (row.mkString(",") !=
      "post_date,enter_date,tx_description,memo,reconcile_state,value_num,value_denom,quantity_num,quantity_denom,currency,num") sys.error(s"Unexpected header: $row")
}

object GnucashExport {
  val dateFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss Z")
}
