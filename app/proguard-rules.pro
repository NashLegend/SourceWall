
-ignorewarnings

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
-keep class com.tencent.** { *; }

-keep class com.sina.** { *; }

-keep class org.jsoup.** { *; }

-keep class org.apache.** { *; }

-keep class org.markdown4j.** { *; }

-keep class net.nashlegend.** { *; }

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

-dontwarn okio.**