package net.shiroka.tools.ofx

import java.io._
import com.typesafe.config.ConfigFactory
import org.specs2.mutable._
import org.specs2.specification.Scope

class ShinseiBankConversionSpec extends SpecificationLike {
  "ShinseiBankConversion" >> {
    "#apply" >> {
      "it generates OFX statement" in {
        val config = ConfigFactory.load().getConfig("net.shiroka.tools.ofx.conversions.shinsei-bank")
        val conversion = ShinseiBankConversion(config)
        val src = getClass.getResourceAsStream("/shinsei-bank.csv")
        val result = printToBaos(out => conversion(src, out)).toString
        result must contain("<ACCTID>1001000100</ACCTID>")
        result must contain("<STMTTRN>")
      }
    }
  }
}
