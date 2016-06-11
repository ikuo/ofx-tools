package net.shiroka.tools.ofx

import java.io._
import scala.io.Source
import scala.util.control.Exception.allCatch
import com.github.tototoshi.csv._
import org.joda.time._
import org.joda.time.format._
import com.typesafe.config.Config
import net.shiroka.tools.ofx.aws.S3
import Transaction._
import Implicits.{ ReducePairs, Tapper }

case class ShinseiBankConversion(config: Config) extends Conversion {
  import ShinseiBankConversion._
  lazy val accountNumber = config.getLong("account-number")

  def apply(
    sources: List[InputStream],
    sinks: Option[String] => PrintStream
  ): Result = {
    val sink = sinks(Default)
    val (csvs, transactions) = sources
      .map(src => CSVReader.open(Source.fromInputStream(src, "UTF-16"))(tsvFormat))
      .map(csv => (csv, read(csv.iterator.dropWhile(_ != header).drop(1))))
      .reducePairs

    closing(csvs)(_ =>
      Statement("ShinseiBank", accountNumber, Statement.Savings, "JPY", transactions)
        .writeOfx(sink))

    sink :: sources
  }

  private def read(rows: Iterator[Seq[String]]): Iterator[Transaction] = {
    var lastTxn: Option[Transaction] = None
    val dateFormat = DateTimeFormat.forPattern("yyyy/MM/dd Z")
    rows.map(_.toList match {
      case row @ date :: inqNum :: desc :: debitStr :: creditStr :: balanceStr :: Nil =>
        allCatch.either {
          val (_type, amount) = typeAndAmount(debitStr, creditStr, desc)

          Transaction(
            dateTime = DateTime.parse(s"$date +09:00", dateFormat),
            `type` = _type,
            description = List(Some(desc.trim), noneIfEmpty(inqNum)).flatten.mkString(" #"),
            amount = amount,
            balance = money(balanceStr)
          ).uniquifyTime(lastTxn.map(_.dateTime))
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

object ShinseiBankConversion {
  val tsvFormat = new TSVFormat {}
  val header = "取引日, 照会番号, 摘要, お支払金額, お預り金額, 残高".split(", ").toList
}
