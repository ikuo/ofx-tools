package net.shiroka.tools.ofx.conversions

import com.typesafe.config.ConfigFactory
import org.specs2.mutable._
import net.shiroka.tools.ofx._

class SbisNetbkSpec extends SpecificationLike {
  "SbisNetbkConversion" >> {
    "#apply" >> {
      "it generates OFX statement" in {
        val config = ConfigFactory.load().getConfig("net.shiroka.tools.ofx.conversions.sbis-netbk")
        val conversion = SbisNetbk(config)
        val src = getClass.getResourceAsStream("/sbis-netbk.csv")
        val result = printToBaos(out => conversion(src, out)).toString
        result must contain("<ACCTID>1001000400</ACCTID>")
        result must contain("<STMTTRN>")
      }
    }
  }
}
