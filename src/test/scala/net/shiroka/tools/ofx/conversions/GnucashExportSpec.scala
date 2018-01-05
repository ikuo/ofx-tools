package net.shiroka.tools.ofx.conversions

import com.typesafe.config.ConfigFactory
import org.specs2.mutable._
import net.shiroka.tools.ofx._

class GnucashExportSpec extends SpecificationLike {
  "GnucashExportConversion" >> {
    "#apply" >> {
      "it generates OFX statement" in {
        val config = ConfigFactory.load().getConfig("net.shiroka.tools.ofx.conversions.gnucash-export")
        val conversion = GnucashExport(config)
        val src = getClass.getResourceAsStream("/gnucash-export.csv")
        val result = printToBaos(out => conversion(src, out)).toString
        result must contain("<ACCTID>1001000500</ACCTID>")
        result must contain("<STMTTRN>")
        result must contain("<MEMO>ニホヘ; トチリ</MEMO>")
      }
    }
  }
}
