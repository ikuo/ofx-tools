package net.shiroka.tools.ofx.conversions

import com.typesafe.config.ConfigFactory
import org.specs2.mutable._
import net.shiroka.tools.ofx._

class ShinseiSpauthSpec extends SpecificationLike {
  "ShinseiSpauthConversion" >> {
    "#apply" >> {
      "it generates OFX statement" in {
        val config = ConfigFactory.load().getConfig("net.shiroka.tools.ofx.conversions.shinsei-spauth")
        val conversion = ShinseiSpauth(config)
        val src = getClass.getResourceAsStream("/shinsei-spauth.csv")
        val result = printToBaos(out => conversion(src, out)).toString
        result must contain("<ACCTID>1001000100</ACCTID>")
        result must contain("<STMTTRN>")
        result must contain("<MEMO>税引前利息</MEMO>")
        result must contain("<MEMO>ＮＥＴ 振込・振替</MEMO>")
      }
    }
  }
}
