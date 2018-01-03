package net.shiroka.tools.ofx.conversions

import com.typesafe.config.ConfigFactory
import org.specs2.mutable._
import net.shiroka.tools.ofx._

class SmbcVisaSpec extends SpecificationLike {
  "SmbcVisaConversion" >> {
    "#apply" >> {
      "it generates OFX statement" in {
        val config = ConfigFactory.load().getConfig("net.shiroka.tools.ofx.conversions.smbc-visa")
        val conversion = SmbcVisa(config)
        val src = getClass.getResourceAsStream("/smbc-visa.csv")
        val result = printToBaos(out => conversion(src, out)).toString
        result must contain("<ACCTID>1001000301</ACCTID>")
        result must contain("<STMTTRN>")
      }
    }
  }
}
