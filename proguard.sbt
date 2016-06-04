proguardSettings

ProguardKeys.options in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings")

ProguardKeys.options in Proguard += """
-dontoptimize
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-keepnames public class ** { *; }
-keepattributes SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-keep public class org.apache.commons.logging.impl.LogFactoryImpl
-keep public class org.apache.commons.logging.impl.Jdk14Logger { *** <init>(...); }
-keep public class com.amazonaws.internal.config.*Json* {
  public *** <init>(...);
  public void set*(***);
  public *** get*();
}
-keep public class net.shiroka.tools.ofx.aws.Lambda { *; }
-keepclassmembers enum * {
  public static **[] values();
  public static ** valueOf(java.lang.String);
}
"""

ProguardKeys.merge in Proguard := true

ProguardKeys.mergeStrategies in Proguard += ProguardMerge.discard("META-INF/.*".r)

ProguardKeys.mergeStrategies in Proguard += ProguardMerge.append("reference.conf")

scalacOptions += "-target:jvm-1.7"
