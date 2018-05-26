package net.shiroka.tools.ofx.conversions

import java.io._
import scala.io.Source
import org.joda.time._
import com.github.tototoshi.csv._
import com.typesafe.config.Config
import net.shiroka.tools.ofx._
import Transaction._

case class SmbcFixed(config: Config) extends Conversion {
  import SmbcFixed._
  lazy val accountId = config.getString("account-id")

  def apply(
    source: InputStream,
    sink: PrintStream): Result = {
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
        case date(year, month, day) :: desc :: expense :: payCategory :: count :: payment :: details :: _ =>
          val (_type, amount) = typeAndAmount(BigDecimal(payment))
          val txn = Transaction(
            dateTime = new DateTime(year.toInt, month.toInt, day.toInt, 0, 0),
            `type` = _type,
            description = makeDescription(state, desc, details),
            amount = amount,
            balance = dummyZero).uniquifyTime(state.lastTxn.map(_.dateTime), ascending = true)
          read(state.addTxn(txn))(rows)

        case hd :: _ :: _ :: _ :: _ :: digits(total) :: _ if (hd.isEmpty) =>
          read(state)(rows)

        case name :: cardNr :: cardKind :: _ if (name.nonEmpty && cardNr.nonEmpty) =>
          read(state.initCard(name))(rows)

        case row if row.nonEmpty => sys.error(s"${row.size} Malformed row $row in state: $state")
        case row => read(state)(rows)
      }
    } else state

  private def typeAndAmount(amount: BigDecimal): (Type, BigDecimal) = (Credit, -amount)

  private def makeDescription(state: State, desc: String, details: String): String = {
    List(
      state.cardName.map(_.replaceAll("(　|様$)", "")).getOrElse(""),
      desc,
      details.replaceAll("　", " / ")).filter(_.nonEmpty).mkString("; ")
  }
}

object SmbcFixed {
  val digits = "(\\d+)".r
  val date = """(\d\d\d\d)/(\d\d)/(\d\d)""".r
  val dummyZero = BigDecimal(0)

  case class State(
    memo: Iterator[Transaction],
    lastTxn: Option[Transaction],
    cardName: Option[String]) {
    def initCard(name: String) = copy(cardName = Some(name), lastTxn = None)
    def addTxn(txn: Transaction) = copy(
      memo = memo ++ Iterator(txn),
      lastTxn = Some(txn))
  }

  object State {
    def empty = apply(Iterator.empty, None, None)
  }
}
