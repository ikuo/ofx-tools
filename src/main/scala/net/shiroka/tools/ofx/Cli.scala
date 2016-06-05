package net.shiroka.tools.ofx

object Cli {
  def illegalArgs(args: List[String]) = throw new IllegalArgumentException(args.mkString)
}
