package net.nashlegend.sourcewall.data.database;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.data.Consts.Keys;
import net.nashlegend.sourcewall.data.database.gen.MyBasket;
import net.nashlegend.sourcewall.data.database.gen.MyBasketDao;
import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.util.ErrorUtils;
import net.nashlegend.sourcewall.util.PrefsUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by NashLegend on 2015/1/20 0020
 */
public class BasketHelper {

    public static long getBasketsNumber() {
        MyBasketDao basketDao = BaseDB.getDaoSession().getMyBasketDao();
        return basketDao.count();
    }

    public static List<SubItem> getAllMyBasketsSubItems() {
        List<MyBasket> MyBaskets = getAllMyBaskets();
        ArrayList<SubItem> subItems = new ArrayList<>();
        for (int i = 0; i < MyBaskets.size(); i++) {
            MyBasket basket = MyBaskets.get(i);
            SubItem subItem = new SubItem(basket.getSection(), basket.getType(), basket.getName(), basket.getValue());
            subItems.add(subItem);
        }
        return subItems;
    }

    public static List<MyBasket> getAllMyBaskets() {
        MyBasketDao basketDao = BaseDB.getDaoSession().getMyBasketDao();
        return basketDao.loadAll();
    }

    public static List<MyBasket> getSelectedBaskets() {
        MyBasketDao basketDao = BaseDB.getDaoSession().getMyBasketDao();
        QueryBuilder<MyBasket> builder = basketDao.queryBuilder()
                .where(MyBasketDao.Properties.Selected.eq(true)).
                        orderAsc(MyBasketDao.Properties.Order);
        return builder.list();
    }

    public static List<SubItem> getSelectedBasketSubItems() {
        List<MyBasket> MyBaskets = getSelectedBaskets();
        ArrayList<SubItem> subItems = new ArrayList<>();
        for (int i = 0; i < MyBaskets.size(); i++) {
            MyBasket basket = MyBaskets.get(i);
            SubItem subItem = new SubItem(basket.getSection(), basket.getType(), basket.getName(), basket.getValue());
            subItems.add(subItem);
        }
        return subItems;
    }

    public static List<MyBasket> getUnselectedBaskets() {
        MyBasketDao basketDao = BaseDB.getDaoSession().getMyBasketDao();
        QueryBuilder<MyBasket> builder = basketDao.queryBuilder().
                where(MyBasketDao.Properties.Selected.eq(false)).
                orderAsc(MyBasketDao.Properties.Order);
        return builder.list();
    }

    public static void putAllBaskets(List<Basket> baskets) {
        ArrayList<MyBasket> myBaskets = new ArrayList<>();
        for (int i = 0; i < baskets.size(); i++) {
            MyBasket myBasket = new MyBasket();
            Basket basket = baskets.get(i);
            myBasket.setCategoryId(basket.getCategory_id());
            myBasket.setCategoryName(basket.getCategory_name());
            myBasket.setName(basket.getName());
            myBasket.setOrder(i);
            myBasket.setSection(SubItem.Section_Favor);
            myBasket.setType(SubItem.Type_Single_Channel);
            myBasket.setValue(basket.getId());
            myBaskets.add(myBasket);
        }
        putAllMyBaskets(myBaskets);
    }

    public static void putAllMyBaskets(List<MyBasket> myBaskets) {
        BaseDB.getDaoSession().getDatabase().beginTransaction();
        try {
            MyBasketDao basketDao = BaseDB.getDaoSession().getMyBasketDao();
            basketDao.deleteAll();
            basketDao.insertInTx(myBaskets);
            BaseDB.getDaoSession().getDatabase().setTransactionSuccessful();
            PrefsUtil.saveLong(Keys.Key_Last_Basket_Version, System.currentTimeMillis());
        } catch (Exception e) {
            ErrorUtils.onException(e);
        } finally {
            BaseDB.getDaoSession().getDatabase().endTransaction();
        }
    }

    /**
     * 调整未选中项的顺序，Key_Last_Basket_Version
     *
     * @param myBaskets myBaskets
     */
    public static void putUnselectedBaskets(List<MyBasket> myBaskets) {
        BaseDB.getDaoSession().getDatabase().beginTransaction();
        try {
            List<MyBasket> baskets = getSelectedBaskets();
            baskets.addAll(myBaskets);
            for (int i = 0; i < baskets.size(); i++) {
                baskets.get(i).setId(null);
            }
            MyBasketDao basketDao = BaseDB.getDaoSession().getMyBasketDao();
            basketDao.deleteAll();
            basketDao.insertInTx(baskets);
            BaseDB.getDaoSession().getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            ErrorUtils.onException(e);
        } finally {
            BaseDB.getDaoSession().getDatabase().endTransaction();
        }
    }

    public static void clearAllMyBaskets() {
        MyBasketDao basketDao = BaseDB.getDaoSession().getMyBasketDao();
        basketDao.deleteAll();
        PrefsUtil.saveLong(Keys.Key_Last_Basket_Version, System.currentTimeMillis());
    }

}
