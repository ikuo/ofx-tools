package net.shiroka.tools.ofx

import java.io._

case class Message(statements: Iterable[Statement], encoding: String = "UTF-8") {
  def writeOfx(sink: PrintStream): Unit = {
    sink.println(s"ENCODING:$encoding")
    sink.println("""
<OFX>
  <BANKMSGSRSV1>""")

    for (stmt <- statements) stmt.writeOfx(sink)

    sink.println("""
  </BANKMSGSRSV1>
</OFX>""")
  }
}
