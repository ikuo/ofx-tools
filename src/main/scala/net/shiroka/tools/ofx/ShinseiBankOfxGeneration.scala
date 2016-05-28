package net.shiroka.tools.ofx

import java.io._

case class ShinseiBankOfxGeneration(sources: List[InputStream])
    extends OfxGeneration with BankOfxGeneration {
  def apply(sink: PrintWriter): Unit = {
    sink.print("dummy")
  }
}
