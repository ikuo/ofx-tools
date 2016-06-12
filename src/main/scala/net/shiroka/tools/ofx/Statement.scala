package net.shiroka.tools.ofx

import java.io._
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import Statement._

case class Statement(
    accountNumber: Long,
    accountType: AccountType,
    currencyCode: String,
    transactions: Iterator[Transaction] // assume descending order by date time
) {
  lazy val ofxKeyPrefix = accountNumber.toString

  def wrap: Message = Message(List(this))

  def writeOfx(sink: PrintStream) = {
    var first: Option[Transaction] = None
    sink.println(s"""
    <STMTTRNRS>
      <STMTRS>
        <CURDEF>$currencyCode</CURDEF>
        <BANKACCTFROM>
          <BANKID/>
          <ACCTID>$accountNumber</ACCTID>
          <ACCTTYPE>${accountType.name}</ACCTTYPE>
        </BANKACCTFROM>
        <BANKTRANLIST>""")

    transactions.foreach { txn =>
      if (first.isEmpty) first = Some(txn)
      txn.writeOfx(sink, ofxKeyPrefix)
    }

    val balance = first.map(_.balance).getOrElse("")
    val dateTime = first.map(_.dateTimeOfx).getOrElse("")
    sink.println(s"""
        </BANKTRANLIST>
        <LEDGERBAL>
          <BALAMT>$balance</BALAMT>
          <DTASOF>$dateTime</DTASOF>
        </LEDGERBAL>
        <AVAILBAL>
          <BALAMT>$balance</BALAMT>
          <DTASOF>$dateTime</DTASOF>
        </AVAILBAL>
      </STMTRS>
    </STMTTRNRS>""")
  }
}

object Statement {
  sealed abstract class AccountType(val name: String)
  object Savings extends AccountType("SAVINGS")
  object Checking extends AccountType("CHECKING")
  object MoneyMarket extends AccountType("MONEYMRKT")
  object CreditLine extends AccountType("CREDITLINE")

  object AccountType {
    val byName = Map(List(Savings, Checking, MoneyMarket, CreditLine).map(t => t.name -> t): _*)
    def find(config: Config, name: String): Option[AccountType] =
      for {
        tp <- config.as[Option[String]](s"accounts.$name.type")
        accType <- byName.get(tp)
      } yield accType
  }
}
