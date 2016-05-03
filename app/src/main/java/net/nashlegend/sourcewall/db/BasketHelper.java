package net.nashlegend.sourcewall.db;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.db.gen.MyBasket;
import net.nashlegend.sourcewall.db.gen.MyBasketDao;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by NashLegend on 2015/1/20 0020
 */
public class BasketHelper {

    public static long getBasketsNumber() {
        MyBasketDao basketDao = App.getDaoSession().getMyBasketDao();
        return basketDao.count();
    }

    public static List<MyBasket> getAllMyBaskets() {
        MyBasketDao basketDao = App.getDaoSession().getMyBasketDao();
        return basketDao.loadAll();
    }

    public static List<MyBasket> getSelectedBaskets() {
        MyBasketDao basketDao = App.getDaoSession().getMyBasketDao();
        QueryBuilder<MyBasket> builder = basketDao.queryBuilder()
                .where(MyBasketDao.Properties.Selected.eq(true)).
                        orderAsc(MyBasketDao.Properties.Order);
        return builder.list();
    }

    public static List<SubItem> getSelectedBasketSubItems() {
        MyBasketDao basketDao = App.getDaoSession().getMyBasketDao();
        QueryBuilder<MyBasket> builder = basketDao.queryBuilder().where(MyBasketDao.Properties.Selected.eq(true)).
                orderAsc(MyBasketDao.Properties.Order);
        List<MyBasket> MyBaskets = builder.list();
        ArrayList<SubItem> subItems = new ArrayList<>();
        for (int i = 0; i < MyBaskets.size(); i++) {
            MyBasket basket = MyBaskets.get(i);
            SubItem subItem = new SubItem(basket.getSection(), basket.getType(), basket.getName(), basket.getValue());
            subItems.add(subItem);
        }
        return subItems;
    }

    public static List<MyBasket> getUnselectedBaskets() {
        MyBasketDao basketDao = App.getDaoSession().getMyBasketDao();
        QueryBuilder<MyBasket> builder = basketDao.queryBuilder().
                where(MyBasketDao.Properties.Selected.eq(false)).
                orderAsc(MyBasketDao.Properties.Order);
        return builder.list();
    }

    public static void putAllMyBaskets(List<MyBasket> myBaskets) {
        App.getDaoSession().getDatabase().beginTransaction();
        try {
            MyBasketDao basketDao = App.getDaoSession().getMyBasketDao();
            basketDao.deleteAll();
            basketDao.insertInTx(myBaskets);
            App.getDaoSession().getDatabase().setTransactionSuccessful();
            SharedPreferencesUtil.saveLong(Consts.Key_Last_Basket_Version, System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            App.getDaoSession().getDatabase().endTransaction();
        }
    }

    /**
     * 调整未选中项的顺序，Key_Last_Basket_Version
     *
     * @param myBaskets myBaskets
     */
    public static void putUnselectedBaskets(List<MyBasket> myBaskets) {
        App.getDaoSession().getDatabase().beginTransaction();
        try {
            List<MyBasket> baskets = getSelectedBaskets();
            baskets.addAll(myBaskets);
            for (int i = 0; i < baskets.size(); i++) {
                baskets.get(i).setId(null);
            }
            MyBasketDao basketDao = App.getDaoSession().getMyBasketDao();
            basketDao.deleteAll();
            basketDao.insertInTx(baskets);
            App.getDaoSession().getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            App.getDaoSession().getDatabase().endTransaction();
        }
    }

    public static void clearAllMyBaskets() {
        MyBasketDao basketDao = App.getDaoSession().getMyBasketDao();
        basketDao.deleteAll();
        SharedPreferencesUtil.saveLong(Consts.Key_Last_Basket_Version, System.currentTimeMillis());
    }

}
