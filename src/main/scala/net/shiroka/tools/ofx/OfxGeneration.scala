package net.shiroka.tools.ofx

import java.io._

trait OfxGeneration {
  def apply(sink: PrintWriter): Unit

  def apply(): String = {
    val sw = new StringWriter()
    try {
      val pw = new PrintWriter(sw)
      try {
        apply(pw)
        pw.toString
      } finally { pw.close() }
    } finally { sw.close() }
  }
}
