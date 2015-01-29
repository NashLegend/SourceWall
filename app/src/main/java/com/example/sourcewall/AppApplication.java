package com.example.sourcewall;

import android.app.Application;

import com.example.sourcewall.db.BaseDB;
import com.example.sourcewall.db.gen.DaoMaster;
import com.example.sourcewall.db.gen.DaoSession;
import com.example.sourcewall.util.CrashReporter;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Created by NashLegend on 2014/9/24 0024
 */
public class AppApplication extends Application {

    private static AppApplication application;
    private UncaughtExceptionHandler uncaughtExceptionHandler;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;
    public static String cookieString = "";
    public static String tokenString = "";
    public static String ukeyString = "";

    //TODO Network Monitor

    @Override
    public void onCreate() {
        super.onCreate();
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        CrashReporter crashReporter = new CrashReporter(getApplicationContext());
        crashReporter.setOnCrashListener(new CrashReporter.CrashListener() {

            @Override
            public void onCrash(String info, Thread thread, Throwable ex) {
                //用于调用系统关闭程序窗口
                uncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(crashReporter);
        application = this;
    }

    public static Application getApplication() {
        return application;
    }

    public static DaoMaster getDaoMaster() {
        if (daoMaster == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(application, BaseDB.DB_NAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    public static DaoSession getDaoSession() {
        if (daoSession == null) {
            daoSession = getDaoMaster().newSession();
        }
        return daoSession;
    }


}
