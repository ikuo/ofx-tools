package net.shiroka.tools.ofx

import java.io._
import java.security.MessageDigest
import scala.xml.PrettyPrinter
import org.joda.time.DateTime
import org.joda.time.format._
import Transaction._

case class Transaction(
  dateTime: DateTime,
  `type`: Type,
  description: String,
  amount: BigDecimal,
  balance: BigDecimal) {
  lazy val date: DateTime = dateBase(dateTime)
  lazy val moveToLastMinuteOfTheDay = copy(dateTime = date.plusDays(1).minusMinutes(1))
  lazy val dateTimeOfx = dateTime.toString(ofxDateFormat)

  def uniquifyTime(previousOpt: Option[DateTime], ascending: Boolean): Transaction =
    if (ascending)
      previousOpt match {
        case None => copy(dateTime = date)
        case Some(previous) if (date != dateBase(previous)) => copy(dateTime = date)
        case Some(previous) => copy(dateTime = previous.plusMinutes(1))
      }
    else
      previousOpt match {
        case None => moveToLastMinuteOfTheDay
        case Some(previous) if (date != dateBase(previous)) => moveToLastMinuteOfTheDay
        case Some(previous) => copy(dateTime = previous.minusMinutes(1))
      }

  def writeOfx(sink: PrintStream, ofxKeyPrefix: String = ""): Unit = {
    sink.print(ppXml.format(
      <STMTTRN>
        <TRNTYPE>{ `type`.name }</TRNTYPE>
        <DTPOSTED>{ dateTimeOfx }</DTPOSTED>
        <TRNAMT>{ amount }</TRNAMT>
        <FITID>{ ofxId(ofxKeyPrefix) }</FITID>
        <MEMO>{ description }</MEMO>
      </STMTTRN>))
  }

  def ofxId(prefix: String) = {
    val text = List(prefix, dateTimeOfx, description, `type`.name, amount, balance).mkString(":")
    MessageDigest.getInstance("MD5").digest(text.getBytes).map("%02x".format(_)).mkString
  }
}

object Transaction {
  val ofxDateFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss")
  val ppXml = new PrettyPrinter(120, 2)

  sealed abstract class Type(val name: String)
  case object Debit extends Type("DEBIT")
  case object Deposit extends Type("DEP")
  case object Interest extends Type("INT")
  case object Credit extends Type("CREDIT")

  def dateBase(dt: DateTime) = dt.dayOfMonth.roundFloorCopy
}
