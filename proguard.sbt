import com.typesafe.sbt.SbtProguard.ProguardKeys._

proguardSettings

javaOptions in (Proguard, proguard) := Seq("-Xmx4G")

ProguardKeys.options in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings")

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

ProguardKeys.merge in Proguard := true

ProguardKeys.mergeStrategies in Proguard += ProguardMerge.discard("META-INF/.*".r)

ProguardKeys.mergeStrategies in Proguard += ProguardMerge.append("reference.conf")

ProguardKeys.options in Proguard += ProguardOptions.keepMain("net.shiroka.tools.ofx.*")

scalacOptions += "-target:jvm-1.7"
