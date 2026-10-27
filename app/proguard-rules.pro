# R8 / Proguard rules for The Investowl

# Keep data models serialized by kotlinx.serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Keep OkHttp / Retrofit models
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Google Tink
-keepclassmembers class * extends com.google.crypto.tink.shaded.protobuf.GeneratedMessageLite {
  <fields>;
}
-keep class com.google.crypto.tink.** { *; }

# Apache ECharts Javascript Interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
