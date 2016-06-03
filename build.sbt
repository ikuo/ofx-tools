name := "ofx-tools"

organization := "net.shiroka"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" %  "1.0.1",
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.10.52",
  "com.github.tototoshi" %% "scala-csv" % "1.3.1",
  "joda-time" % "joda-time" % "2.9.4",
  "com.typesafe" % "config" % "1.2.1",
  "com.netaporter" %% "scala-uri" % "0.4.14",
  "org.specs2" %% "specs2-core" % "3.8.3" % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")
