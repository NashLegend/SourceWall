package com.example.sourcewall.db;

import com.example.sourcewall.AppApplication;
import com.example.sourcewall.db.gen.AskTag;
import com.example.sourcewall.db.gen.AskTagDao;
import com.example.sourcewall.model.SubItem;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.SharedUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by NashLegend on 2015/1/20 0020
 */
public class AskTagHelper {

    public static long getAskTagsNumber() {
        AskTagDao tagDao = AppApplication.getDaoSession().getAskTagDao();
        return tagDao.count();
    }

    public static List<AskTag> getAllMyTags() {
        AskTagDao tagDao = AppApplication.getDaoSession().getAskTagDao();
        return tagDao.loadAll();
    }

    public static List<AskTag> getSelectedTags() {
        AskTagDao tagDao = AppApplication.getDaoSession().getAskTagDao();
        QueryBuilder<AskTag> builder = tagDao.queryBuilder().where(AskTagDao.Properties.Selected.eq(true)).
                orderAsc(AskTagDao.Properties.Order);
        return builder.list();
    }

    public static List<SubItem> getSelectedQuestionSubItems() {
        AskTagDao tagDao = AppApplication.getDaoSession().getAskTagDao();
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
        AskTagDao tagDao = AppApplication.getDaoSession().getAskTagDao();
        QueryBuilder<AskTag> builder = tagDao.queryBuilder().
                where(AskTagDao.Properties.Selected.eq(false)).
                orderAsc(AskTagDao.Properties.Order);
        return builder.list();
    }

    public static void putAllMyTags(List<AskTag> myTags) {
        AppApplication.getDaoSession().getDatabase().beginTransaction();
        try {
            AskTagDao tagDao = AppApplication.getDaoSession().getAskTagDao();
            tagDao.deleteAll();
            tagDao.insertInTx(myTags);
            AppApplication.getDaoSession().getDatabase().setTransactionSuccessful();
            SharedUtil.saveLong(Consts.Key_Last_Ask_Tags_Version, System.currentTimeMillis());
        } catch (Exception e) {
            System.out.println("putAllMyTags Failed");
        } finally {
            AppApplication.getDaoSession().getDatabase().endTransaction();
        }
    }

    /**
     * 调整未选中项的顺序，Key_Last_Ask_Tags_Version
     *
     * @param myTags
     */
    public static void putUnselectedTags(List<AskTag> myTags) {
        AppApplication.getDaoSession().getDatabase().beginTransaction();
        try {
            List<AskTag> tags = getSelectedTags();
            tags.addAll(myTags);
            for (int i = 0; i < tags.size(); i++) {
                tags.get(i).setId(null);
            }
            AskTagDao tagDao = AppApplication.getDaoSession().getAskTagDao();
            tagDao.deleteAll();
            tagDao.insertInTx(tags);
            AppApplication.getDaoSession().getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            System.out.println("putUnselectedTags Failed");
        } finally {
            AppApplication.getDaoSession().getDatabase().endTransaction();
        }
    }

    public static void clearAllMyTags() {
        AskTagDao tagDao = AppApplication.getDaoSession().getAskTagDao();
        tagDao.deleteAll();
        SharedUtil.saveLong(Consts.Key_Last_Ask_Tags_Version, System.currentTimeMillis());
    }

}
