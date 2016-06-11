package net.shiroka.tools.ofx

import java.io._
import org.specs2.mutable._
import org.specs2.specification.Scope

class ShinseiBankConversionSpec extends SpecificationLike {
  "ShinseiBankConversion" >> {
    "#apply" >> {
      "it generates OFX statement" in {
        val conversion = ShinseiBankConversion(6300215825L)
        val src = getClass.getResourceAsStream("/shinsei-bank.csv")
        val result = printToBaos(out => conversion(src, out)).toString
        result must contain("<ACCTID>6300215825</ACCTID>")
        result must contain("<STMTTRN>")
      }
    }
  }
}
