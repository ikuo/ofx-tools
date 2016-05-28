package net.shiroka.tools.ofx

import java.io._

trait OfxGeneration {
  def apply(sink: PrintWriter): Unit

  def apply(writer: Writer): Unit = {
    val pw = new PrintWriter(writer)
    try {
      apply(pw)
    } finally { pw.close() }
  }

  def apply(): String = {
    val sw = new StringWriter()
    try {
      apply(sw)
      sw.toString
    } finally { sw.close() }
  }
}
