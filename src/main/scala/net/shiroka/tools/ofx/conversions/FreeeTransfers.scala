package net.shiroka.tools.ofx.conversions

import java.io._
import scala.io.Source
import scala.util.control.Exception.allCatch
import scala.collection._
import scala.collection.mutable.ArrayBuffer
import com.github.tototoshi.csv._
import org.joda.time._
import org.joda.time.format._
import net.ceedubs.ficus.Ficus._
import com.typesafe.config.Config
import net.shiroka.tools.ofx._
import Statement._
import Transaction._
import Implicits.Tapper

// A conversion for transfers.csv of freee https://www.freee.co.jp/
case class FreeeTransfers(config: Config) extends Conversion {
  import FreeeTransfers._
  lazy val accounts = config.as[Config]("accounts")
  val currencyCode = config.as[String]("currency-code")

  def apply(
    source: InputStream,
    sink: PrintStream
  ): Result = {
    val csv = CSVReader.open(Source.fromInputStream(source, "Shift_JIS"))
    val statements = read(csv.iterator.drop(1))

    closing(csv)(_ => Message(statements).writeOfx(sink))

    sink :: source :: Nil
  }

  private def read(rows: Iterator[Seq[String]]): Iterable[Statement] = {
    var lastTxn: Option[Transaction] = None
    val transactionGroups = mutable.Map.empty[String, ArrayBuffer[Transaction]]
    val accTypes = mutable.Map.empty[String, AccountType]

    for (row <- rows) row.toList match {
      case row @ date :: from :: to :: desc :: amountStr :: Nil =>
        allCatch.either {
          accTypes.getOrElseUpdate(from, findOrGuess(from))
          transactionGroups
            .getOrElseUpdate(from, ArrayBuffer.empty[Transaction])
            .append {
              val (_type, amount) = typeAndAmount(accTypes(from), amountStr)
              Transaction(
                dateTime = DateTime.parse(s"$date +09:00", dateFormat),
                `type` = _type,
                description = noneIfEmpty(desc).getOrElse(s"$from → $to"),
                amount = amount,
                balance = 0
              ).uniquifyTime(lastTxn.map(_.dateTime))
                .tap(txn => lastTxn = Some(txn))
            }
        }.fold(rethrow(_, s"Failed process row $row"), identity)
      case row => sys.error(s"Malformed row $row")
    }

    transactionGroups.map {
      case (name, transactions) =>
        Statement(
          accounts.as[Option[String]](s"$name.account-id").getOrElse(name),
          accTypes(name),
          currencyCode,
          transactions.iterator
        )
    }
  }

  private def findOrGuess(name: String): AccountType =
    AccountType.find(accounts, name)
      .orElse(if (name.endsWith("カード")) Some(CreditLine) else None)
      .getOrElse(Savings)

  private def typeAndAmount(accType: AccountType, amountStr: String) =
    (accType, money(amountStr)) match {
      case (`CreditLine`, amount) => (Credit, -amount)
      case (_, amount) => (Debit, -amount)
    }
}

object FreeeTransfers {
  val header = "振替日, 振替元口座, 振替先口座, 備考, 金額".split(", ").toList
  val dateFormat = DateTimeFormat.forPattern("yyyy/MM/dd Z")
}
