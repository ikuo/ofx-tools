package net.shiroka.tools.ofx.conversions

import java.io._
import scala.io.Source
import scala.util.control.Exception.allCatch
import org.joda.time._
import com.github.tototoshi.csv._
import com.typesafe.config.Config
import net.shiroka.tools.ofx._
import Transaction._

case class SmbcPrompt(config: Config) extends Conversion {
  import SmbcPrompt._
  lazy val accountId = config.getString("account-id")

  def apply(
    source: InputStream,
    sink: PrintStream): Result = {
    val csv = CSVReader.open(Source.fromInputStream(source, "Shift_JIS"))
    lazy val transactions = read(csv.iterator)

    closing(csv)(_ =>
      Statement(accountId, Statement.Savings, "JPY", transactions)
        .wrap.writeOfx(sink))

    sink :: source :: Nil
  }

  private def read(rows: Iterator[Seq[String]]): Iterator[Transaction] = {
    rows.map(_.toList match {
      case row @ date(year, month, day) :: desc :: name :: paymentKinds :: _ :: subDate :: amount :: _ =>
        allCatch.either {
          require(paymentKinds == "1回払い")
          Transaction(
            dateTime = new DateTime(year.toInt, month.toInt, day.toInt, 0, 0),
            `type` = Credit,
            description = makeDescription(name, desc),
            amount = -BigDecimal(amount),
            balance = BigDecimal(0) // dummy
          )
        }.fold(rethrow(_, s"Failed process row $row"), identity)
      case row => sys.error(s"Malformed row $row")
    })
  }

  private def makeDescription(name: String, desc: String): String =
    List(
      name.trim.replaceAll("^ご", ""),
      desc.trim).flatMap(noneIfEmpty).mkString("; ")
}

object SmbcPrompt {
  val date = """(\d\d\d\d)/(\d\d?)/(\d\d?)""".r
}
