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

# Keep Room and Moshi generated adapters discoverable while allowing R8 to shrink app code.
-keep class **_Impl { *; }
-keep class **JsonAdapter { *; }
-keep class com.squareup.moshi.adapters.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }

# Firebase, Hilt, and Google Play libraries ship consumer rules; keep model fields used by Firestore reflection.
-keepclassmembers class com.ashwathai.tradelab.data.LeaderboardEntry { *; }

# Protect all Data Models used for Room, Moshi, and Firestore
-keep class com.ashwathai.tradelab.data.** { *; }
-keep class com.ashwathai.tradelab.ui.** { *; }
-keep class com.ashwathai.tradelab.shared.** { *; }

# Moshi Kotlin Reflection Rules
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class com.ashwathai.tradelab.** {
    @com.squareup.moshi.Json <fields>;
}

# Hilt & Jetpack Compose (standard precautions for minification)
-keep class androidx.hilt.navigation.compose.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# Unity LevelPlay (ironSource) - Epic 26
-keep class com.ironsource.** { *; }
-keep class com.unity3d.** { *; }
-dontwarn com.ironsource.**
-dontwarn com.unity3d.**

# Strip verbose/debug logging from release builds (keep warn/error for diagnostics)
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
