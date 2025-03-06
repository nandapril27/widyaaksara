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

# Keep OpenCV classes
-keep class org.opencv.** { *; }

# Keep all OpenCV classes
-keep class org.opencv.** { *; }

# Keep all native methods
-keepclassmembers class org.opencv.** {
    native <methods>;
}

# Prevent OpenCV static initializer from being removed
-keepclassmembers class org.opencv.** {
    static <clinit>();
}

# Prevent obfuscation of OpenCV-related classes
-keep class org.opencv.** { *; }
-keep class com.example.widyaaksara.** { *; }

# Keep classes with JNI bindings
-keep class org.opencv.** { *; }
-keepclassmembers class * {
    native <methods>;
}

# Ensure OpenCV static initializers are not removed
-keepclassmembers class org.opencv.** {
    static <clinit>();
}
