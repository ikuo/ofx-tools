package net.shiroka.tools.ofx.conversions

import java.io._
import scala.io.Source
import scala.util.control.Exception.allCatch
import com.github.tototoshi.csv._
import org.joda.time._
import org.joda.time.format._
import com.typesafe.config.Config
import net.shiroka.tools.ofx._
import Transaction._
import implicits.Tapper

case class ShinseiSpauth(config: Config) extends Conversion {
  import ShinseiSpauth._
  lazy val accountId = config.getString("account-id")

  def apply(
    source: InputStream,
    sink: PrintStream): Result = {
    val csv = CSVReader.open(Source.fromInputStream(source, "Shift_JIS"))
    lazy val transactions = read(csv.iterator.dropWhile(_ != header).drop(1))

    closing(csv)(_ =>
      Statement(accountId, Statement.Savings, "JPY", transactions)
        .wrap
        .writeOfx(sink))

    sink :: source :: Nil
  }

  private def read(rows: Iterator[Seq[String]]): Iterator[Transaction] = {
    var lastTxn: Option[Transaction] = None
    rows.map(_.toList match {
      case row @ date :: desc :: debitStr :: creditStr :: balanceStr :: Nil =>
        allCatch.either {
          val (_type, amount) = typeAndAmount(debitStr, creditStr, desc)

          Transaction(
            dateTime = DateTime.parse(s"$date +09:00", dateFormat),
            `type` = _type,
            description = desc.trim,
            amount = amount,
            balance = money(balanceStr)).uniquifyTime(lastTxn.map(_.dateTime), ascending = false)
            .tap(txn => lastTxn = Some(txn))

        }.fold(rethrow(_, s"Failed process row $row"), identity)
      case row => sys.error(s"Malformed row $row")
    })
  }

  private def typeAndAmount(debitStr: String, creditStr: String, desc: String): (Type, BigDecimal) =
    (moneyOpt(debitStr), moneyOpt(creditStr)) match {
      case (Some(debit), _) => (Debit, -debit)
      case (_, Some(credit)) =>
        val `type` = if (desc == "税引前利息") Interest else Deposit
        (`type`, credit)
      case _ => sys.error("Cannot find debit or credit.")
    }
}

object ShinseiSpauth {
  val header = List("取引日", "摘要", "出金金額", "入金金額", "残高")
  val dateFormat = DateTimeFormat.forPattern("yyyy/MM/dd Z")
}
