package net.nashlegend.sourcewall.request.api;

import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.model.Category;
import net.nashlegend.sourcewall.model.Favor;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.ParamsMap;
import net.nashlegend.sourcewall.request.RequestBuilder;
import net.nashlegend.sourcewall.request.RequestObject.CallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.parsers.BasketListParser;
import net.nashlegend.sourcewall.request.parsers.BasketParser;
import net.nashlegend.sourcewall.request.parsers.BooleanParser;
import net.nashlegend.sourcewall.request.parsers.CategoryListParser;
import net.nashlegend.sourcewall.request.parsers.FavorListParser;

import java.util.ArrayList;

import rx.Observable;

/**
 * Created by NashLegend on 16/3/15.
 */
public class FavorAPI extends APIBase {

    /**
     * 返回文章列表
     *
     * @param basketId
     * @param offset
     * @param useCache 是否使用cache
     * @return
     */
    public static Observable<ResponseObject<ArrayList<Favor>>> getFavorList(String basketId, int offset, boolean useCache) {
        String url = "http://apis.guokr.com/favorite/link.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("retrieve_type", "by_basket");
        pairs.put("basket_id", basketId);
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<Favor>>()
                .get()
                .url(url)
                .params(pairs)
                .withToken(false)
                .useCacheFirst(useCache)
                .cacheTimeOut(300000)
                .parser(new FavorListParser())
                .flatMap();
    }

    /**
     * 收藏一个链接，理论是任意链接都行，吧……
     *
     * @param link     链接地址
     * @param title    链接标题
     * @param basket
     * @param callBack
     * @return ResponseObject
     */
    public static NetworkTask<Boolean> favorLink(String link, String title, Basket basket, CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/favorite/link.json";
        ParamsMap params = new ParamsMap();
        params.put("basket_id", basket.getId());
        params.put("url", link);
        params.put("title", title);
        return new RequestBuilder<Boolean>()
                .post()
                .url(url)
                .params(params)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 获取用户的果篮信息
     *
     * @return ResponseObject.result is ArrayList[Basket]
     */
    public static NetworkTask<ArrayList<Basket>> getBaskets(CallBack<ArrayList<Basket>> callBack) {
        String url = "http://www.guokr.com/apis/favorite/basket.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("t", System.currentTimeMillis() + "");
        pairs.put("retrieve_type", "by_ukey");
        pairs.put("ukey", UserAPI.getUkey());
        pairs.put("limit", "100");
        return new RequestBuilder<ArrayList<Basket>>()
                .get()
                .url(url)
                .params(pairs)
                .parser(new BasketListParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 创建一个果篮
     *
     * @param title        果篮名
     * @param introduction 果篮介绍
     * @param category_id  category
     * @return ResponseObject.result is Basket
     */
    public static NetworkTask<Basket> createBasket(String title, String introduction, String category_id, CallBack<Basket> callBack) {
        String url = "http://www.guokr.com/apis/favorite/basket.json";
        ParamsMap params = new ParamsMap();
        params.put("title", title);
        params.put("introduction", introduction);
        params.put("category_id", category_id);
        return new RequestBuilder<Basket>()
                .post()
                .url(url)
                .params(params)
                .parser(new BasketParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 获取分类 ，创建果篮有关
     *
     * @return ResponseObject
     */
    public static NetworkTask<ArrayList<Category>> getCategoryList(CallBack<ArrayList<Category>> callBack) {
        String url = "http://www.guokr.com/apis/favorite/category.json";
        return new RequestBuilder<ArrayList<Category>>()
                .get()
                .url(url)
                .parser(new CategoryListParser())
                .callback(callBack)
                .requestAsync();
    }
}
