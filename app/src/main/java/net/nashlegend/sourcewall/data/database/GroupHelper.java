package net.nashlegend.sourcewall.data.database;

import net.nashlegend.sourcewall.data.Consts.Keys;
import net.nashlegend.sourcewall.data.database.gen.MyGroup;
import net.nashlegend.sourcewall.data.database.gen.MyGroupDao;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.util.ErrorUtils;
import net.nashlegend.sourcewall.util.PrefsUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by NashLegend on 2015/1/20 0020
 */
public class GroupHelper {

    public static long getMyGroupsNumber() {
        MyGroupDao myGroupDao = BaseDB.getDaoSession().getMyGroupDao();
        return myGroupDao.count();
    }

    public static List<MyGroup> getAllMyGroups() {
        MyGroupDao myGroupDao = BaseDB.getDaoSession().getMyGroupDao();
        QueryBuilder<MyGroup> builder = myGroupDao.queryBuilder().
                orderAsc(MyGroupDao.Properties.Order);
        List<MyGroup> list = builder.list();
        return list;
    }

    public static List<SubItem> getAllMyGroupSubItems() {
        List<MyGroup> groups = getAllMyGroups();
        ArrayList<SubItem> subItems = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            MyGroup myGroup = groups.get(i);
            SubItem subItem = new SubItem(myGroup.getSection(), myGroup.getType(),
                    myGroup.getName(), myGroup.getValue());
            subItems.add(subItem);
        }
        return subItems;
    }

    public static List<MyGroup> getSelectedGroups() {
        MyGroupDao myGroupDao = BaseDB.getDaoSession().getMyGroupDao();
        QueryBuilder<MyGroup> builder = myGroupDao.queryBuilder().where(
                MyGroupDao.Properties.Selected.eq(true)).
                orderAsc(MyGroupDao.Properties.Order);
        return builder.list();
    }

    public static List<SubItem> getSelectedGroupSubItems() {
        List<MyGroup> groups = getSelectedGroups();
        ArrayList<SubItem> subItems = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            MyGroup myGroup = groups.get(i);
            SubItem subItem = new SubItem(myGroup.getSection(), myGroup.getType(),
                    myGroup.getName(), myGroup.getValue());
            subItems.add(subItem);
        }
        return subItems;
    }

    public static List<MyGroup> getUnselectedGroups() {
        MyGroupDao myGroupDao = BaseDB.getDaoSession().getMyGroupDao();
        QueryBuilder<MyGroup> builder = myGroupDao.queryBuilder().
                where(MyGroupDao.Properties.Selected.eq(false)).
                orderAsc(MyGroupDao.Properties.Order);
        return builder.list();
    }

    public static void putAllMyGroups(List<MyGroup> myGroups) {
        BaseDB.getDaoSession().getDatabase().beginTransaction();
        try {
            MyGroupDao myGroupDao = BaseDB.getDaoSession().getMyGroupDao();
            myGroupDao.deleteAll();
            myGroupDao.insertInTx(myGroups);
            BaseDB.getDaoSession().getDatabase().setTransactionSuccessful();
            PrefsUtil.saveLong(Keys.Key_Last_Post_Groups_Version, System.currentTimeMillis());
        } catch (Exception e) {
            ErrorUtils.onException(e);
        } finally {
            BaseDB.getDaoSession().getDatabase().endTransaction();
        }
    }

    /**
     * 调整未选中项的顺序，不修改Key_Last_Post_Groups_Version
     */
    public static void putUnselectedGroups(List<MyGroup> myGroups) {
        BaseDB.getDaoSession().getDatabase().beginTransaction();
        try {
            List<MyGroup> groups = getSelectedGroups();
            groups.addAll(myGroups);
            for (int i = 0; i < groups.size(); i++) {
                groups.get(i).setId(null);
            }
            MyGroupDao myGroupDao = BaseDB.getDaoSession().getMyGroupDao();
            myGroupDao.deleteAll();
            myGroupDao.insertInTx(groups);
            BaseDB.getDaoSession().getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            ErrorUtils.onException(e);
        } finally {
            BaseDB.getDaoSession().getDatabase().endTransaction();
        }
    }

    public static void clearAllMyGroups() {
        MyGroupDao myGroupDao = BaseDB.getDaoSession().getMyGroupDao();
        myGroupDao.deleteAll();
        PrefsUtil.saveLong(Keys.Key_Last_Post_Groups_Version, System.currentTimeMillis());
    }

}
