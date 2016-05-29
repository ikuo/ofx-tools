package net.shiroka.tools.ofx

import java.io._
import org.specs2.mutable._
import org.specs2.specification.Scope

class ShinseiBankOfxGenerationSpec extends SpecificationLike {
  "ShinseiBankOfxGeneration" >> {
    "#apply" >> {
      "it generates OFX statement" in {
        val generation = ShinseiBankOfxGeneration(6300215825L)
        val src = getClass.getResourceAsStream("/shinsei.txt")
        val result = generation(src)
        result must contain("<ACCTID>6300215825</ACCTID>")
        result must contain("<STMTTRN>")
      }
    }
  }
}
