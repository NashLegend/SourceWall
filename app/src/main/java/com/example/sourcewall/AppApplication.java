package com.example.sourcewall;

import android.app.Application;

import com.example.sourcewall.util.CrashReporter;

/**
 * Created by NashLegend on 2014/9/24 0024
 */
public class AppApplication extends Application {

    static AppApplication application;
    Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;
    public static String cookieString = "";
    public static String tokenString = "";
    public static String ukeyString = "";

    //TODO Network Monitor

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        CrashReporter crashReporter = new CrashReporter(getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(crashReporter);
        crashReporter.setOnCrashListener(new CrashReporter.CrashListener() {
            @Override
            public void onCrash(String info, Thread thread, Throwable ex) {
                mUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
    }

    public static Application getApplication() {
        return application;
    }


}
