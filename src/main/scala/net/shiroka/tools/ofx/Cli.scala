package net.shiroka.tools.ofx

trait Cli {
  def illegalArgs(args: List[String]) = throw new IllegalArgumentException(args.mkString)
}
