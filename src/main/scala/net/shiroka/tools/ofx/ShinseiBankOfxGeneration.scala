package net.shiroka.tools.ofx

import java.io._
import scala.io.Source
import scala.util.control.Exception.allCatch
import com.github.tototoshi.csv._
import org.joda.time._
import org.joda.time.format._
import net.shiroka.tools.ofx.aws.S3
import Transaction._
import Implicits.{ ReducePairs, Tapper }

case class ShinseiBankOfxGeneration(accountNumber: Long) extends OfxGeneration {
  import ShinseiBankOfxGeneration._

  def apply(sources: List[InputStream], sinks: Option[String] => PrintStream): Unit = {
    val sink = sinks(Default)
    val (csvs, transactions) = sources
      .map(src => CSVReader.open(Source.fromInputStream(src, "UTF-16"))(tsvFormat))
      .map(csv => (csv, read(csv.iterator.dropWhile(_ != header).drop(1))))
      .reducePairs

    closing(csvs)(_ =>
      Statement("ShinseiBank", accountNumber, Statement.Savings, "JPY", transactions)
        .writeOfx(sink))
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

object ShinseiBankOfxGeneration {
  import com.amazonaws.services.s3.model._
  import com.netaporter.uri.Uri
  import Implicits.Tapper

  val tsvFormat = new TSVFormat {}
  val header = "取引日, 照会番号, 摘要, お支払金額, お預り金額, 残高".split(", ").toList
  val s3 = S3()

  def generate[T](uri: Uri)(f: (OfxGeneration, S3ObjectInputStream) => T): T = {
    uri.pathParts.takeRight(3).map(_.part).toList match {
      case "shinsei-bank" :: accountNum :: fileName :: Nil =>
        closing(s3.source(uri))(f(apply(accountNum.toLong), _))
      case parts => sys.error(s"Unexpected path parts $parts")
    }
  }

  def main(args: Array[String]): Unit = args.toList match {
    case s3uri :: "-" :: Nil =>
      generate(Uri.parse(s3uri))((gen, src) => gen.apply(src, System.out))

    case s3uri :: Nil =>
      val uri = Uri.parse(s3uri)
      val baos: ByteArrayOutputStream =
        generate(Uri.parse(s3uri))((gen, src) => new ByteArrayOutputStream().tap(os => gen.apply(src, os)))

      s3.uploadAndAwait(
        bucket = uri.host.get,
        key = uri.path.drop(1).stripSuffix(".txt") ++ ".ofx",
        is = new ByteArrayInputStream(baos.toByteArray),
        size = baos.size
      )

    case accountNum :: src :: sink :: Nil => apply(accountNum.toLong)(src, sink)
    case _ => throw new IllegalArgumentException(args.mkString)
  }
}
