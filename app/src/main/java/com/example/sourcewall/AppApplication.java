package com.example.sourcewall;

import android.app.Application;

/**
 * Created by NashLegend on 2014/9/24 0024
 */
public class AppApplication extends Application {

    static AppApplication application;
    static int screenWidthInDP=360;
    public static String cookieString="";
    public static String tokenString="";
    public static String ukeyString="";

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static Application getApplication() {
        return application;
    }
}
