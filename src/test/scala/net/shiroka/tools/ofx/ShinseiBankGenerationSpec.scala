package net.shiroka.tools.ofx

import java.io._
import org.specs2.mutable._
import org.specs2.specification.Scope

class ShinseiBankGenerationSpec extends SpecificationLike {
  "ShinseiBankGeneration" >> {
    "#apply" >> {
      "it generates OFX statement" in {
        val generation = ShinseiBankGeneration(6300215825L)
        val src = getClass.getResourceAsStream("/shinsei-bank.csv")
        val result = printToBaos(out => generation(src, out)).toString
        result must contain("<ACCTID>6300215825</ACCTID>")
        result must contain("<STMTTRN>")
      }
    }
  }
}
