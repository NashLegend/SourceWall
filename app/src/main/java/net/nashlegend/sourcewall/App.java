package net.nashlegend.sourcewall;

import android.app.Application;

import net.nashlegend.sourcewall.db.BaseDB;
import net.nashlegend.sourcewall.db.gen.DaoMaster;
import net.nashlegend.sourcewall.db.gen.DaoSession;

/**
 * Created by NashLegend on 2014/9/24 0024
 */
public class App extends Application {

    private static App application;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static Application getApp() {
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
