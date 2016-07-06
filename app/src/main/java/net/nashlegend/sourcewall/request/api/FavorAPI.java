package net.nashlegend.sourcewall.request.api;

import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.model.Category;
import net.nashlegend.sourcewall.model.Favor;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.RequestBuilder;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.RequestObject.CallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.parsers.BooleanParser;
import net.nashlegend.sourcewall.request.parsers.FavorListParser;
import net.nashlegend.sourcewall.request.parsers.Parser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("retrieve_type", "by_basket");
        pairs.put("basket_id", basketId);
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<Favor>>()
                .url(url)
                .get()
                .withToken(false)
                .params(pairs)
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
        HashMap<String, String> params = new HashMap<>();
        params.put("basket_id", basket.getId());
        params.put("url", link);
        params.put("title", title);
        return new RequestBuilder<Boolean>()
                .url(url)
                .parser(new BooleanParser())
                .callback(callBack)
                .params(params)
                .post()
                .requestAsync();
    }

    /**
     * 获取用户的果篮信息
     *
     * @return ResponseObject.result is ArrayList[Basket]
     */
    public static NetworkTask<ArrayList<Basket>> getBaskets(CallBack<ArrayList<Basket>> callBack) {
        Parser<ArrayList<Basket>> parser = new Parser<ArrayList<Basket>>() {
            @Override
            public ArrayList<Basket> parse(String str, ResponseObject<ArrayList<Basket>> responseObject) throws Exception {
                JSONArray jsonArray = JsonHandler.getUniversalJsonArray(str, responseObject);
                ArrayList<Basket> baskets = new ArrayList<>();
                assert jsonArray != null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    Basket basket = Basket.fromJson(jsonArray.getJSONObject(i));
                    baskets.add(basket);
                }
                return baskets;
            }
        };
        String url = "http://www.guokr.com/apis/favorite/basket.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("t", System.currentTimeMillis() + "");
        pairs.put("retrieve_type", "by_ukey");
        pairs.put("ukey", UserAPI.getUkey());
        pairs.put("limit", "100");
        return new RequestBuilder<ArrayList<Basket>>()
                .url(url)
                .parser(parser)
                .callback(callBack)
                .params(pairs)
                .get()
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
        HashMap<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("introduction", introduction);
        params.put("category_id", category_id);
        return new RequestBuilder<Basket>()
                .url(url)
                .parser(new Parser<Basket>() {
                    @Override
                    public Basket parse(String str, ResponseObject<Basket> responseObject) throws Exception {
                        return Basket.fromJson(JsonHandler.getUniversalJsonObject(str, responseObject));
                    }
                })
                .callback(callBack)
                .params(params)
                .post()
                .requestAsync();
    }

    /**
     * 获取分类 ，创建果篮有关
     *
     * @return ResponseObject
     */
    public static NetworkTask<ArrayList<Category>> getCategoryList(CallBack<ArrayList<Category>> callBack) {
        Parser<ArrayList<Category>> parser = new Parser<ArrayList<Category>>() {
            @Override
            public ArrayList<Category> parse(String str, ResponseObject<ArrayList<Category>> responseObject) throws Exception {
                JSONArray jsonArray = JsonHandler.getUniversalJsonArray(str, responseObject);
                ArrayList<Category> categories = new ArrayList<>();
                assert jsonArray != null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subObject = jsonArray.getJSONObject(i);
                    categories.add(Category.fromJson(subObject));
                }
                return categories;
            }
        };
        String url = "http://www.guokr.com/apis/favorite/category.json";
        return new RequestBuilder<ArrayList<Category>>()
                .url(url)
                .parser(parser)
                .callback(callBack)
                .get()
                .requestAsync();
    }
}
