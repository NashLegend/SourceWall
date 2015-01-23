package com.example.sourcewall.db;

import com.example.sourcewall.AppApplication;
import com.example.sourcewall.db.gen.MyGroup;
import com.example.sourcewall.db.gen.MyGroupDao;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by NashLegend on 2015/1/20 0020
 */
public class GroupHelper {

    public static List<MyGroup> getAllMyGroups() {
        MyGroupDao myGroupDao = AppApplication.getDaoSession().getMyGroupDao();
        return myGroupDao.loadAll();
    }

    public static List<MyGroup> getSelectedGroups() {
        MyGroupDao myGroupDao = AppApplication.getDaoSession().getMyGroupDao();
        QueryBuilder<MyGroup> builder = myGroupDao.queryBuilder().where(MyGroupDao.Properties.Selected.eq(true)).
                orderAsc(MyGroupDao.Properties.Order);
        return builder.list();
    }

    public static List<MyGroup> getUnselectedGroups() {
        MyGroupDao myGroupDao = AppApplication.getDaoSession().getMyGroupDao();
        QueryBuilder<MyGroup> builder = myGroupDao.queryBuilder().
                where(MyGroupDao.Properties.Selected.eq(false)).
                orderAsc(MyGroupDao.Properties.Order);
        return builder.list();
    }

    public static void putAllMyGroups(List<MyGroup> myGroups) {
        MyGroupDao myGroupDao = AppApplication.getDaoSession().getMyGroupDao();
        myGroupDao.deleteAll();
        myGroupDao.insertInTx(myGroups);
    }

    public static void putUnselectedGroups(List<MyGroup> myGroups) {
        List<MyGroup> groups = getSelectedGroups();
        groups.addAll(myGroups);
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).setId(null);
        }
        MyGroupDao myGroupDao = AppApplication.getDaoSession().getMyGroupDao();
        myGroupDao.deleteAll();
        myGroupDao.insertInTx(groups);
    }

}
