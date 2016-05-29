package net.shiroka.tools.ofx

import java.io._
import scala.io.Source
import com.github.tototoshi.csv._
import org.joda.time._
import org.joda.time.format._
import Transaction._

case class ShinseiBankOfxGeneration(accountNumber: Long, sources: List[InputStream])
    extends BankOfxGeneration {
  import ShinseiBankOfxGeneration._
  require(sources.forall(_ != null), "sources contain null elements")

  def apply(sink: PrintStream): Unit =
    sources
      .map(src => CSVReader.open(Source.fromInputStream(src, "UTF-16"))(tsvFormat))
      .foreach { csv =>
        try {
          val transactions = read(csv.iterator.dropWhile(_ != header).drop(1))
          Statement(accountNumber, Statement.Savings, "JPY", transactions)
            .writeOfx(sink)
        } finally (csv.close)
      }

  private def read(rows: Iterator[Seq[String]]): Iterator[Transaction] = {
    var lastTxn: Option[Transaction] = None
    rows.map(_.toList match {
      case row @ date :: inqNum :: desc :: debitStr :: creditStr :: balanceStr :: Nil =>
        val (_type, amount) =
          (moneyOpt(debitStr, row), moneyOpt(creditStr, row)) match {
            case (Some(debit), _) => (Debit, -debit)
            case (_, Some(credit)) =>
              val `type` = if (desc == "税引前利息") Interest else Deposit
              (`type`, credit)
            case _ => sys.error(s"Cannot find debit or credit in row: $row")
          }

        val txn = Transaction(
          dateTime = DateTime.parse(s"$date +09:00", dateFormat),
          `type` = _type,
          description = List(Some(desc.trim), noneIfEmpty(inqNum)).flatten.mkString(" #"),
          amount = amount,
          balance = money(balanceStr, row)
        ).uniquifyTime(lastTxn.map(_.dateTime))

        lastTxn = Some(txn)
        txn
      case row => sys.error(s"Malformed row: $row")
    })
  }
}

object ShinseiBankOfxGeneration {
  val dateFormat = DateTimeFormat.forPattern("yyyy/MM/dd Z")
  val tsvFormat = new TSVFormat {}
  val header = "取引日, 照会番号, 摘要, お支払金額, お預り金額, 残高".split(", ").toList

  def apply(accountNumber: Long, source: InputStream): ShinseiBankOfxGeneration =
    apply(accountNumber, List(source))
}
