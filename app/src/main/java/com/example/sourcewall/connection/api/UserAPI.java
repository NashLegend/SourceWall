package com.example.sourcewall.connection.api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.example.sourcewall.LoginActivity;
import com.example.sourcewall.connection.HttpFetcher;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.model.Basket;
import com.example.sourcewall.model.Category;
import com.example.sourcewall.model.UserInfo;
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

    public static boolean isLoggedIn() {
        return Logged;
    }

    public static void setLoggedInOK() {
        Logged = true;
    }

    public static String base36Encode(long id) {
        String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        id = Math.abs(id);
        StringBuilder sb = new StringBuilder();
        for (; id > 0; id /= 36) {
            sb.insert(0, ALPHABET.charAt((int) (id % 36)));
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 通过用户的ukey获取用户的详细信息
     *
     * @param ukey
     * @return
     */
    public static ResultObject getUserInfoByUkey(String ukey) {
        ResultObject resultObject = new ResultObject();
        try {
            JSONObject object = new JSONObject(HttpFetcher.get("http://apis.guokr.com/community/user/" + ukey + ".json"));
            if (getJsonBoolean(object, "ok")) {
                JSONObject subObject = getJsonObject(object, "result");
                UserInfo info = new UserInfo();
                info.setDate_created(getJsonString(subObject, "date_created"));
                info.setIntroduction(getJsonString(subObject, "introduction"));
                info.setNickname(getJsonString(subObject, "nickname"));
                info.setTitle(getJsonString(subObject, "title"));
                info.setUkey(getJsonString(subObject, "ukey"));
                info.setUrl(getJsonString(subObject, "url"));
                info.setId(info.getUrl().replaceAll("^\\D+(\\d+)\\D*", "$1"));
                info.setAvatar(subObject.getJSONObject("avatar")
                        .getString("large").replaceAll("\\?\\S*$", ""));
                resultObject.result = info;
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
     * 通过用户id获取用户信息
     *
     * @param id
     * @return
     */
    public static ResultObject getUserInfoByID(String id) {
        return getUserInfoByUkey(base36Encode(Long.valueOf(id)));
    }

    /**
     * 通过获取消息提醒的方式测试是否登录或者登录是否有效
     * 但是过期token是啥样子的还没见过…………
     *
     * @return
     */
    public static ResultObject testLogin() {
        ResultObject resultObject = new ResultObject();
        String token = getToken();
        String ukey = getUkey();
        if (!TextUtils.isEmpty(ukey) && ukey.length() == 6 && !TextUtils.isEmpty(token) && token.length() == 64) {
            resultObject = getMessageNum();
        } else {
            clearMyInfo();
            resultObject.code = ResultObject.ResultCode.CODE_NO_TOKEN;
        }
        Logged = resultObject.ok;
        return resultObject;
    }

    public static ResultObject getMessageNum() {
        ResultObject resultObject = new ResultObject();
        String token = getToken();
        try {
            JSONObject object = new JSONObject(HttpFetcher.get("http://www.guokr.com/apis/community/rn_num.json?_=" + System.currentTimeMillis() + "&access_token=" + token));
            if (getJsonBoolean(object, "ok")) {
                resultObject.ok = true;
            } else {
                resultObject.code = ResultObject.ResultCode.CODE_LOGIN_FAILED;
            }
        } catch (IOException e) {
            resultObject.code = ResultObject.ResultCode.CODE_NETWORK_ERROR;
            e.printStackTrace();
        } catch (JSONException e) {
            resultObject.code = ResultObject.ResultCode.CODE_JSON_ERROR;
            e.printStackTrace();
        }
        return resultObject;
    }

    public static void startLoginActivity(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    /**
     * 收藏一个链接，理论是任意链接都行，吧……
     *
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
     * 推荐一个链接
     *
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
     * 获取用户的果篮信息
     *
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
     * 创建一个果篮
     *
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

    /**
     * 获取分类 ，创建果篮有关
     *
     * @return
     */
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

    public static void clearMyInfo() {

    }

    /**
     * 获取保存的用户token
     *
     * @return
     */
    public static String getToken() {
        return SharedUtil.readString(Consts.Key_Access_Token, "");
    }

    /**
     * 获取保存的用户ukey
     *
     * @return
     */
    public static String getUkey() {
        return SharedUtil.readString(Consts.Key_Ukey, "");
    }

    /**
     * 获取保存的用户cookie
     *
     * @return
     */
    public static String getCookie() {
        return SharedUtil.readString(Consts.Key_Cookie, "");
    }

    /**
     * 获取保存的用户cookie
     *
     * @return
     */
    public static String getSimpleCookie() {
        return "_32353_access_token=" + getToken() + "; _32353_ukey=" + getUkey() + ";";
    }
}
