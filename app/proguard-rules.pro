# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class fr.acinq.secp256k1.jni.** { *; }
-keep class com.dluvian.nozzle.model.nostr.Filter { <fields>; }
-keep class com.dluvian.nozzle.model.nostr.Event { <fields>; }
-keep class com.dluvian.nozzle.model.nostr.Metadata { <fields>; }
-keep class com.dluvian.nozzle.model.nostr.nip05.Nip05Response { <fields>; }
-keep class com.dluvian.nozzle.model.nostr.nip11.Nip11Document { <fields>; }

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.google.errorprone.annotations.**