proguardSettings

ProguardKeys.options in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings")

ProguardKeys.options in Proguard += """
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-keep public class org.apache.commons.logging.impl.Jdk14Logger { *** <init>(...); }
-keep public class net.shiroka.tools.ofx.aws.Lambda { *; }
-keepnames public class com.amazonaws.services.lambda.runtime.Context
-keep public class org.apache.commons.logging.impl.LogFactoryImpl
"""

ProguardKeys.merge in Proguard := true

ProguardKeys.mergeStrategies in Proguard += ProguardMerge.discard("META-INF/.*".r)

ProguardKeys.mergeStrategies in Proguard += ProguardMerge.append("reference.conf")

scalacOptions += "-target:jvm-1.7"
