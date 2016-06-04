package net.shiroka.tools.ofx

import java.io._
import scala.io.Source
import scala.util.control.Exception.allCatch
import com.github.tototoshi.csv._
import org.joda.time._
import org.joda.time.format._
import Transaction._
import Implicits.{ ReducePairs, Tapper }

case class ShinseiBankOfxGeneration(accountNumber: Long) extends OfxGeneration {
  import ShinseiBankOfxGeneration._

  def apply(sources: List[InputStream], sinks: Option[String] => PrintStream): Unit =
    closing(sinks(Default)) { sink =>
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
  import com.amazonaws.regions._
  import com.amazonaws.services.s3.AmazonS3Client
  import com.amazonaws.services.s3.model._
  import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
  import com.netaporter.uri.Uri
  import com.typesafe.config.ConfigFactory
  import Implicits.Tapper

  val tsvFormat = new TSVFormat {}
  val header = "取引日, 照会番号, 摘要, お支払金額, お預り金額, 残高".split(", ").toList

  val awsConfig = ConfigFactory.load(getClass.getClassLoader)
  val region = awsConfig.getString("net.shiroka.tools.ofx.aws.region")

  def source(uri: Uri): S3ObjectInputStream = {
    val client: AmazonS3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain())
      .tap(_.setRegion(RegionUtils.getRegion(region)))
    val obj = client.getObject(uri.host.get, uri.path.drop(1))
    obj.getObjectContent
  }

  def generate(uri: Uri, sink: OutputStream): Unit = {
    val generation =
      uri.pathParts.takeRight(3).map(_.part).toList match {
        case "shinsei-bank" :: accountNum :: fileName :: Nil =>
          apply(accountNum.toLong)(source(uri), sink)
        case parts => sys.error(s"Unexpected path parts $parts")
      }
  }

  def main(args: Array[String]) = args.toList match {
    case s3uri :: "-" :: Nil => generate(Uri.parse(s3uri), System.out)
    case s3uri :: Nil =>
      val uri = Uri.parse(s3uri)
      generate(uri, System.out)

    case accountNum :: src :: sink :: Nil => apply(accountNum.toLong)(src, sink)
    case _ => throw new IllegalArgumentException(args.mkString)
  }
}
