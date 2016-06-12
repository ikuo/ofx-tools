package net.shiroka.tools.ofx.conversions

import com.typesafe.config.ConfigFactory
import org.specs2.mutable._
import net.shiroka.tools.ofx._

class ShinseiBankSpec extends SpecificationLike {
  "ShinseiBankConversion" >> {
    "#apply" >> {
      "it generates OFX statement" in {
        val config = ConfigFactory.load().getConfig("net.shiroka.tools.ofx.conversions.shinsei-bank")
        val conversion = ShinseiBank(config)
        val src = getClass.getResourceAsStream("/shinsei-bank.csv")
        val result = printToBaos(out => conversion(src, out)).toString
        result must contain("<ACCTID>1001000200</ACCTID>")
        result must contain("<STMTTRN>")
      }
    }
  }
}
