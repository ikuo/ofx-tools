name := "ofx-tools"

organization := "net.shiroka"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "net.sf.ofx4j" % "ofx4j" % "1.6",
  "com.github.tototoshi" %% "scala-csv" % "1.3.1",
  "org.specs2" %% "specs2-core" % "3.8.3" % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")
