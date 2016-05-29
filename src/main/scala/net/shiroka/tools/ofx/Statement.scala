package net.shiroka.tools.ofx

import Statement._
import java.io._

case class Statement(
    instituteName: String,
    accountNumber: Long,
    accountType: AccountType,
    currencyCode: String,
    transactions: Iterator[Transaction] // assume descending order by date time
) {
  lazy val ofxKeyPrefix = List(instituteName, accountNumber).mkString(":")

  def writeOfx(sink: PrintStream) = {
    var first: Option[Transaction] = None
    sink.println("ENCODING:UTF-8")
    sink.println(s"""
<OFX>
  <BANKMSGSRSV1>
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
    </STMTTRNRS>
  </BANKMSGSRSV1>
</OFX>""")
  }
}

object Statement {
  sealed abstract class AccountType(val name: String)
  object Savings extends AccountType("SAVINGS")
}
