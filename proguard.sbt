proguardSettings

ProguardKeys.options in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings")

ProguardKeys.options in Proguard += """
-keep public class net.shiroka.tools.ofx.aws.Lambda { *; }
-keepnames public class com.amazonaws.services.lambda.runtime.Context
"""
