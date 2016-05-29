package net.shiroka.tools.ofx

import java.io._
import org.specs2.mutable._
import org.specs2.specification.Scope

class ShinseiBankOfxGenerationSpec extends SpecificationLike {
  "ShinseiBankOfxGeneration" >> {
    "#apply" >> {
      "it generates OFX statement" in {
        val gen = ShinseiBankOfxGeneration(6300215825L)
        val src = getClass.getResourceAsStream("/shinsei.txt")
        val sink = new FileOutputStream(new File("./out.ofx"))
        gen(src, sink) must beNull.not
      }
    }
  }
}
