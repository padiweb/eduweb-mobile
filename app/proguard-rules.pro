-keep class id.padiweb.eduweb.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
