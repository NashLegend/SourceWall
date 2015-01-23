package com.example.sourcewall.db;

import com.example.sourcewall.AppApplication;
import com.example.sourcewall.db.gen.MyGroup;
import com.example.sourcewall.db.gen.MyGroupDao;
import com.example.sourcewall.model.SubItem;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by NashLegend on 2015/1/20 0020
 */
public class GroupHelper {

    public static long getMyGroupsNumber() {
        MyGroupDao myGroupDao = AppApplication.getDaoSession().getMyGroupDao();
        return myGroupDao.count();
    }

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

    public static List<SubItem> getSelectedGroupSubItems() {
        MyGroupDao myGroupDao = AppApplication.getDaoSession().getMyGroupDao();
        QueryBuilder<MyGroup> builder = myGroupDao.queryBuilder().where(MyGroupDao.Properties.Selected.eq(true)).
                orderAsc(MyGroupDao.Properties.Order);
        List<MyGroup> groups = builder.list();
        ArrayList<SubItem> subItems = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            MyGroup myGroup = groups.get(i);
            SubItem subItem = new SubItem(myGroup.getSection(), myGroup.getType(), myGroup.getName(), myGroup.getValue());
            subItems.add(subItem);
        }
        return subItems;
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
