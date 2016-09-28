package net.nashlegend.sourcewall.data.database;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.data.database.gen.DaoMaster;
import net.nashlegend.sourcewall.data.database.gen.DaoSession;

/**
 * Created by NashLegend on 2015/1/20 0020
 */
public class BaseDB {
    public final static String DB_NAME = "SourceWallDB";


    private static DaoMaster daoMaster;
    private static DaoSession daoSession;


    public static DaoMaster getDaoMaster() {
        if (daoMaster == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(App.getApp(), BaseDB.DB_NAME, null);
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
