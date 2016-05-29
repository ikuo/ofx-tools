package net.shiroka.tools.ofx

import Statement._
import java.io._

case class Statement(
    accountNumber: Long,
    accountType: AccountType,
    currencyCode: String,
    transactions: Iterator[Transaction]
) {
  def writeOfx(sink: PrintStream) = transactions.foreach(sink.println)
}

object Statement {
  sealed abstract class AccountType(name: String)
  object Savings extends AccountType("SAVINGS")
}
