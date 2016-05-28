package net.shiroka.tools.ofx

import java.io._
import org.specs2.mutable._
import org.specs2.specification.Scope

class ShinseiBankOfxGenerationSpec extends SpecificationLike {
  "ShinseiBankOfxGeneration" >> {
    "#apply" >> {
      "it saves content" in {
        val gen = ShinseiBankOfxGeneration(List(getClass.getResourceAsStream("shinsei.csv")))
        gen() must be_==("dummy")
      }
    }
  }
}
