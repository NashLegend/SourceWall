package net.nashlegend.sourcewall.db;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.db.gen.AskTag;
import net.nashlegend.sourcewall.db.gen.AskTagDao;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Consts.Keys;
import net.nashlegend.sourcewall.util.PrefsUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by NashLegend on 2015/1/20 0020
 */
public class AskTagHelper {

    public static long getAskTagsNumber() {
        AskTagDao tagDao = App.getDaoSession().getAskTagDao();
        return tagDao.count();
    }

    public static List<AskTag> getAllMyTags() {
        AskTagDao tagDao = App.getDaoSession().getAskTagDao();
        QueryBuilder<AskTag> builder = tagDao.queryBuilder().
                orderAsc(AskTagDao.Properties.Order);
        List<AskTag> list =  builder.list();
        return list;
    }

    public static List<SubItem> getAllMyTagSubItems() {
        AskTagDao tagDao = App.getDaoSession().getAskTagDao();
        QueryBuilder<AskTag> builder = tagDao.queryBuilder().
                orderAsc(AskTagDao.Properties.Order);
        List<AskTag> askTags =  builder.list();
        ArrayList<SubItem> subItems = new ArrayList<>();
        for (int i = 0; i < askTags.size(); i++) {
            AskTag tag = askTags.get(i);
            SubItem subItem = new SubItem(tag.getSection(), tag.getType(), tag.getName(), tag.getValue());
            subItems.add(subItem);
        }
        return subItems;
    }

    public static List<AskTag> getSelectedTags() {
        AskTagDao tagDao = App.getDaoSession().getAskTagDao();
        QueryBuilder<AskTag> builder = tagDao.queryBuilder().where(AskTagDao.Properties.Selected.eq(true)).
                orderAsc(AskTagDao.Properties.Order);
        return builder.list();
    }

    public static List<SubItem> getSelectedQuestionSubItems() {
        AskTagDao tagDao = App.getDaoSession().getAskTagDao();
        QueryBuilder<AskTag> builder = tagDao.queryBuilder().where(AskTagDao.Properties.Selected.eq(true)).
                orderAsc(AskTagDao.Properties.Order);
        List<AskTag> askTags = builder.list();
        ArrayList<SubItem> subItems = new ArrayList<>();
        for (int i = 0; i < askTags.size(); i++) {
            AskTag tag = askTags.get(i);
            SubItem subItem = new SubItem(tag.getSection(), tag.getType(), tag.getName(), tag.getValue());
            subItems.add(subItem);
        }
        return subItems;
    }

    public static List<AskTag> getUnselectedTags() {
        AskTagDao tagDao = App.getDaoSession().getAskTagDao();
        QueryBuilder<AskTag> builder = tagDao.queryBuilder().
                where(AskTagDao.Properties.Selected.eq(false)).
                orderAsc(AskTagDao.Properties.Order);
        return builder.list();
    }

    public static void putAllMyTags(List<AskTag> myTags) {
        App.getDaoSession().getDatabase().beginTransaction();
        try {
            AskTagDao tagDao = App.getDaoSession().getAskTagDao();
            tagDao.deleteAll();
            tagDao.insertInTx(myTags);
            App.getDaoSession().getDatabase().setTransactionSuccessful();
            PrefsUtil.saveLong(Keys.Key_Last_Ask_Tags_Version, System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            App.getDaoSession().getDatabase().endTransaction();
        }
    }

    /**
     * 调整未选中项的顺序，Key_Last_Ask_Tags_Version
     *
     * @param myTags tag
     */
    public static void putUnselectedTags(List<AskTag> myTags) {
        App.getDaoSession().getDatabase().beginTransaction();
        try {
            List<AskTag> tags = getSelectedTags();
            tags.addAll(myTags);
            for (int i = 0; i < tags.size(); i++) {
                tags.get(i).setId(null);
            }
            AskTagDao tagDao = App.getDaoSession().getAskTagDao();
            tagDao.deleteAll();
            tagDao.insertInTx(tags);
            App.getDaoSession().getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            App.getDaoSession().getDatabase().endTransaction();
        }
    }

    public static void clearAllMyTags() {
        AskTagDao tagDao = App.getDaoSession().getAskTagDao();
        tagDao.deleteAll();
        PrefsUtil.saveLong(Keys.Key_Last_Ask_Tags_Version, System.currentTimeMillis());
    }

}
