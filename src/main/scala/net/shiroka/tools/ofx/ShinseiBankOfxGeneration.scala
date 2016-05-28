package net.shiroka.tools.ofx

import java.io.{ PrintWriter, InputStream }

case class ShinseiBankOfxGeneration(sources: List[InputStream])
    extends OfxGeneration with BankOfxGeneration {
  def apply(sink: PrintWriter): Unit = ???
}
