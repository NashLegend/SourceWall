package com.example.outerspace;

import android.app.Application;

/**
 * Created by NashLegend on 2014/9/24 0024.
 */
public class AppApplication extends Application {

    static AppApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static Application getApplication() {
        return application;
    }
}
