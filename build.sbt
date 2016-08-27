lazy val root = (project in file(".")).
  settings(
    name := "ofx-tools",
    organization := "net.shiroka",
    version := "1.1.0",
    scalaVersion := "2.11.8",
    libraryDependencies ++=
      Seq(
        "org.scala-lang.modules" %% "scala-xml" %  "1.0.1",
        "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
        "com.amazonaws" % "aws-java-sdk" % "1.6.6",
        "com.github.tototoshi" %% "scala-csv" % "1.3.1",
        "joda-time" % "joda-time" % "2.9.4",
        "org.joda" % "joda-convert" % "1.8.1",
        "com.typesafe" % "config" % "1.2.1",
        "com.netaporter" %% "scala-uri" % "0.4.14",
        "org.specs2" %% "specs2-core" % "3.8.3" % "test"
      ),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    scalacOptions ++= Seq("-Xlint", "-Ywarn-unused-import", "-unchecked", "-deprecation", "-feature"),
    fork in run := true
  )
    .settings(ficusSettings: _*)
    .settings(_proguardSettings: _*)

lazy val ficusSettings = Seq(
    resolvers += Resolver.jcenterRepo,
    // for Scala 2.11.x and Java 7
    libraryDependencies += "com.iheart" %% "ficus" % "1.1.3"
  )

lazy val _proguardSettings = {
  import com.typesafe.sbt.SbtProguard.ProguardKeys._
  proguardSettings ++ Seq(
    javaOptions in (Proguard, proguard) := Seq("-Xmx4G"),
    scalacOptions += "-target:jvm-1.7",
    ProguardKeys.merge in Proguard := true,
    ProguardKeys.mergeStrategies in Proguard += ProguardMerge.discard("META-INF/.*".r),
    ProguardKeys.mergeStrategies in Proguard += ProguardMerge.append("reference.conf"),
    ProguardKeys.options in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings"),
    ProguardKeys.options in Proguard += ProguardOptions.keepMain("net.shiroka.tools.ofx.*"),
    ProguardKeys.options in Proguard += """
    -dontoptimize
    -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
    -keepnames class ** { *; }
    -keepnames enum ** { *; }
    -keepattributes SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

    -keep public enum com.amazonaws.RequestClientOptions$Marker** {
        **[] $VALUES;
        public *;
    }
    -keep class com.amazonaws.metrics.AwsSdkMetrics$* {
      *** <init>(...);
      *** *;
    }
    -keep public class org.apache.commons.logging.impl.LogFactoryImpl
    -keep public class org.apache.commons.logging.impl.Jdk14Logger { *** <init>(...); }

    -keep public class net.shiroka.tools.ofx.aws.Lambda { *; }
    -keep public class ** extends net.shiroka.tools.ofx.Conversion { *; }
    """
  )
}
