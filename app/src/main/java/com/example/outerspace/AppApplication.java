package com.example.outerspace;

import android.app.Application;
import android.content.res.AssetManager;

/**
 * Created by NashLegend on 2014/9/24 0024.
 */
public class AppApplication extends Application {

    static AppApplication application;
    static int screenWidthInDP=360;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static Application getApplication() {
        return application;
    }
}
