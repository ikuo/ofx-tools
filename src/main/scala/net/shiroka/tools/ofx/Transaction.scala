package net.shiroka.tools.ofx

import org.joda.time.DateTime
import Transaction._

case class Transaction(
    dateTime: DateTime,
    `type`: Type,
    description: String,
    amount: BigDecimal,
    balance: BigDecimal
) {
  lazy val date: DateTime = dateBase(dateTime)
  lazy val moveToLastMinuteOfTheDay = copy(dateTime = dateTime.plusDays(1).minusMinutes(1))

  def uniquifyTime(previousOpt: Option[DateTime]) = previousOpt match {
    case None => moveToLastMinuteOfTheDay
    case Some(previous) if (date != dateBase(previous)) => moveToLastMinuteOfTheDay
    case Some(previous) => copy(dateTime = previous.minusMinutes(1))
  }
}

object Transaction {
  sealed abstract class Type(val name: String)
  case object Debit extends Type("DEBIT")
  case object Deposit extends Type("DEP")
  case object Interest extends Type("INT")
  case object Credit extends Type("CREDIT")

  def dateBase(dt: DateTime) = dt.dayOfMonth.roundFloorCopy
}
