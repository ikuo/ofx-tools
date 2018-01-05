package net.shiroka.tools.ofx.conversions

import java.io._
import scala.io.Source
import scala.util.control.Exception.allCatch
import com.github.tototoshi.csv._
import org.joda.time._
import org.joda.time.format._
import com.typesafe.config.Config
import net.shiroka.tools.ofx._
import implicits.Tapper
import Transaction._

case class SbisNetbk(config: Config) extends Conversion {
  import SbisNetbk._
  lazy val accountId = config.getString("account-id")

  def apply(
    source: InputStream,
    sink: PrintStream
  ): Result = {
    val csv = CSVReader.open(Source.fromInputStream(source, "Shift_JIS"))
    val rows = csv.iterator.tap(it => assertHeader(it.next()))
    lazy val transactions = read(rows)

    closing(csv)(_ =>
      Statement(accountId, Statement.Savings, "JPY", transactions)
        .wrap
        .writeOfx(sink))

    sink :: source :: Nil
  }

  override def moneyOpt(str: String): Option[BigDecimal] =
    super.moneyOpt(str.replaceAll(",", ""))

  private def read(rows: Iterator[Seq[String]]): Iterator[Transaction] = {
    var lastTxn: Option[Transaction] = None
    rows.map(_.toList match {
      case row @ date :: desc :: debitStr :: creditStr :: balanceStr :: memo :: Nil =>
        allCatch.either {
          val (_type, amount) = typeAndAmount(debitStr, creditStr, desc)

          Transaction(
            dateTime = DateTime.parse(s"$date +09:00", dateFormat),
            `type` = _type,
            description = List(Some(desc.trim)).flatten.mkString(" #"),
            amount = amount,
            balance = money(balanceStr)
          ).uniquifyTime(lastTxn.map(_.dateTime), ascending = false)
            .tap(txn => lastTxn = Some(txn))

        }.fold(rethrow(_, s"Failed process row $row"), identity)
      case row => sys.error(s"Malformed row $row")
    })
  }

  private def typeAndAmount(debitStr: String, creditStr: String, desc: String): (Type, BigDecimal) =
    (moneyOpt(debitStr), moneyOpt(creditStr)) match {
      case (Some(debit), _) => (Debit, -debit)
      case (_, Some(credit)) => (Deposit, credit)
      case _ => sys.error("Cannot find debit or credit.")
    }

  private def assertHeader(row: Seq[String]) = row.toList match {
    case "日付" :: "内容" :: "出金金額(円)" :: "入金金額(円)" :: "残高(円)" :: "メモ" :: _ => // ok
    case _ => sys.error(s"Unexpected header: $row")
  }
}

object SbisNetbk {
  val dateFormat = DateTimeFormat.forPattern("yyyy/MM/dd Z")
}
