# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Pan\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-ignorewarnings

#picasso
-dontwarn okhttp3.**
-dontwarn com.google.**
-dontwarn java.**
-dontwarn android.**
-dontwarn com.android.**
-dontwarn com.umeng.**
-dontwarn org.junit.**
-dontwarn org.**

#greendao
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties

#eventbus
-keepclassmembers,includedescriptorclasses class ** { public void onEvent*(**); }

#umeng
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

#tencent
-keep class com.tencent.mm.sdk.** {
   *;
}
-keep class com.tencent.** { *; }

-keep class com.sina.** { *; }

-keep class org.jsoup.** { *; }

#umeng
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

#butterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#RxJava
-dontwarn rx.**
-keep class rx.** { *; }