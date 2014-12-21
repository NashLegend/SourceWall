package com.example.sourcewall.connection.api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.example.sourcewall.LoginActivity;
import com.example.sourcewall.connection.HttpFetcher;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.model.Basket;
import com.example.sourcewall.model.Category;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.SharedUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/11/25 0025
 */
public class UserAPI extends APIBase {

    public static ArrayList<Basket> myBaskets = new ArrayList<>();
    public static boolean Logged = false;

    public static ResultObject testLogin() {
        ResultObject resultObject = new ResultObject();
        String token = getToken();
        if (!TextUtils.isEmpty(token) && token.length() == 64) {
            try {
                JSONObject object = new JSONObject(HttpFetcher.get("http://www.guokr.com/apis/community/rn_num.json?access_token=" + token));
                if (getJsonBoolean(object, "ok")) {
                    resultObject.ok = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Logged = resultObject.ok;
        return resultObject;
    }

    public static void startLoginActivity(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    /**
     * @param link
     * @param title
     * @param basketID
     * @return
     */
    public static ResultObject favorLink(String link, String title, String basketID) {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/favorite/link.json";
            ArrayList<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("basket_id", basketID));
            params.add(new BasicNameValuePair("url", link));
            params.add(new BasicNameValuePair("title", title));
            params.add(new BasicNameValuePair("access_token", getToken()));
            String result = HttpFetcher.post(url, params);
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                resultObject.ok = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * @param link
     * @param title
     * @param summary
     * @param comment
     * @return
     */
    public static ResultObject recommendLink(String link, String title, String summary, String comment) {
        String url = "http://www.guokr.com/apis/community/user/recommend.json";
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("title", title));
            pairs.add(new BasicNameValuePair("url", link));
            pairs.add(new BasicNameValuePair("summary", summary));
            pairs.add(new BasicNameValuePair("comment", comment));
            pairs.add(new BasicNameValuePair("target", "activity"));
            pairs.add(new BasicNameValuePair("access_token", UserAPI.getToken()));
            String result = HttpFetcher.post(url, pairs);
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                resultObject.ok = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * @return ResultObject.result is ArrayList<Basket>
     */
    public static ResultObject getBaskets() {
        ResultObject resultObject = new ResultObject();
        String url = "http://www.guokr.com/apis/favorite/basket.json?t=" + System.currentTimeMillis() + "&retrieve_type=by_ukey&ukey=" + getUkey() + "&limit=100&access_token=" + getToken();
        try {
            String result = HttpFetcher.get(url);
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                JSONArray jsonArray = getJsonArray(object, "result");
                if (jsonArray != null) {
                    ArrayList<Basket> baskets = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject subObject = jsonArray.getJSONObject(i);
                        Basket basket = new Basket();
                        basket.setId(getJsonString(subObject, "id"));
                        basket.setIntroduction(getJsonString(subObject, "introduction"));
                        basket.setLinks_count(getJsonInt(subObject, "links_count"));
                        basket.setName(getJsonString(subObject, "title"));
                        JSONObject category = getJsonObject(subObject, "category");
                        if (category != null) {
                            basket.setCategory_id(getJsonString(category, "id"));
                            basket.setCategory_name(getJsonString(category, "name"));
                        }
                        baskets.add(basket);
                    }
                    resultObject.ok = true;
                    resultObject.result = baskets;
                    myBaskets = baskets;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * @param title
     * @param introduction
     * @param category_id
     * @return ResultObject.result is Basket
     */
    public static ResultObject createBasket(String title, String introduction, String category_id) {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/favorite/basket.json";
            ArrayList<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("title", title));
            params.add(new BasicNameValuePair("introduction", introduction));
            params.add(new BasicNameValuePair("category_id", category_id));
            params.add(new BasicNameValuePair("access_token", getToken()));
            String result = HttpFetcher.post(url, params);
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                JSONObject subObject = getJsonObject(object, "result");
                if (subObject != null) {
                    Basket basket = new Basket();
                    basket.setId(getJsonString(subObject, "id"));
                    basket.setIntroduction(getJsonString(subObject, "introduction"));
                    basket.setLinks_count(0);
                    basket.setName(getJsonString(subObject, "title"));
                    JSONObject category = getJsonObject(subObject, "category");
                    if (category != null) {
                        basket.setCategory_id(getJsonString(category, "id"));
                        basket.setCategory_name(getJsonString(category, "name"));
                    }
                    resultObject.ok = true;
                    resultObject.result = basket;
                    myBaskets.add(basket);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    public static ResultObject getCategoryList() {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/favorite/category.json?access_token=" + getToken();
            String result = HttpFetcher.get(url);
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                JSONArray jsonArray = getJsonArray(object, "result");
                if (jsonArray != null) {
                    ArrayList<Category> categories = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject subObject = jsonArray.getJSONObject(i);
                        Category category = new Category();
                        category.setId(getJsonString(subObject, "id"));
                        category.setName(getJsonString(subObject, "name"));
                        categories.add(category);
                    }
                    resultObject.ok = true;
                    resultObject.result = categories;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    public static String getToken() {
        return SharedUtil.readString(Consts.Key_Access_Token, "");
    }

    public static String getUkey() {
        return SharedUtil.readString(Consts.Key_Ukey, "");
    }

    public static String getCookie() {
        return SharedUtil.readString(Consts.Key_Cookie, "");
    }
}
