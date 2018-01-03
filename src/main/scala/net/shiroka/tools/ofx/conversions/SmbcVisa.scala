package net.shiroka.tools.ofx.conversions

import java.io._
import scala.io.Source
import org.joda.time._
import com.github.tototoshi.csv._
import com.typesafe.config.Config
import net.shiroka.tools.ofx._
import Transaction._

case class SmbcVisa(config: Config) extends Conversion {
  import SmbcVisa._
  lazy val accountId = config.getString("account-id")

  def apply(
    source: InputStream,
    sink: PrintStream
  ): Result = {
    val csv = CSVReader.open(Source.fromInputStream(source, "Shift_JIS"))
    lazy val transactions = read(State.empty)(csv.iterator).memo
    closing(csv)(_ =>
      Statement(accountId, Statement.CreditLine, "JPY", transactions)
        .wrap
        .writeOfx(sink))
    sink :: source :: Nil
  }

  private def read(state: State)(rows: Iterator[Seq[String]]): State =
    if (rows.hasNext) {
      var cardName: Option[String] = None
      rows.next.toList match {
        case Date(year, month, day) :: desc :: expense :: payCategory :: count :: payment :: details :: _ =>
          val (_type, amount) = typeAndAmount(BigDecimal(expense))
          val txn = Transaction(
            dateTime = new DateTime(year.toInt, month.toInt, day.toInt, 0, 0),
            `type` = _type,
            description = List(desc, details.replaceAll("　", " / ")).filter(_.nonEmpty).mkString("; "),
            amount = amount,
            balance = dummyZero
          ).uniquifyTime(state.lastTxn.map(_.dateTime))
          read(state.addTxn(txn))(rows)

        case hd :: _ :: _ :: _ :: _ :: Digits(total) :: _ if (hd.isEmpty) => state

        case name :: cardNr :: cardKind :: _ if (name.nonEmpty && cardNr.nonEmpty) =>
          read(state.setCardName(name))(rows)

        case row if row.nonEmpty => sys.error(s"${row.size} Malformed row $row in state: $state")
        case row => state
      }
    } else state

  private def typeAndAmount(amount: BigDecimal): (Type, BigDecimal) = (Credit, -amount)
}

object SmbcVisa {
  val Digits = "(\\d+)".r
  val Date = """(\d\d\d\d)/(\d\d)/(\d\d)""".r
  val dummyZero = BigDecimal(0)

  case class State(
      memo: Iterator[Transaction],
      lastTxn: Option[Transaction],
      cardName: Option[String]
  ) {
    def setCardName(n: String) = copy(cardName = Some(n))
    def addTxn(txn: Transaction) = copy(
      memo = memo ++ Iterator(txn),
      lastTxn = Some(txn)
    )
  }

  object State {
    def empty = apply(Iterator.empty, None, None)
  }
}
