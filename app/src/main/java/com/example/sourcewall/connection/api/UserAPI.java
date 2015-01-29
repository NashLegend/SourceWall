package com.example.sourcewall.connection.api;

import android.text.TextUtils;

import com.example.sourcewall.connection.HttpFetcher;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.db.AskTagHelper;
import com.example.sourcewall.db.GroupHelper;
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

    private static ArrayList<Basket> myBaskets = new ArrayList<>();

    public static boolean isLoggedIn() {
        String token = getToken();
        String ukey = getUkey();
        return !TextUtils.isEmpty(ukey) && ukey.length() == 6 && !TextUtils.isEmpty(token) && token.length() == 64;
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
     * @param ukey 用户ukey
     * @return ResultObject
     */
    public static ResultObject getUserInfoByUkey(String ukey) {
        ResultObject resultObject = new ResultObject();
        try {
            String result = HttpFetcher.get("http://apis.guokr.com/community/user/" + ukey + ".json").toString();
            JSONObject subObject = getUniversalJsonObject(result, resultObject);
            if (subObject != null) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultObject;
    }

    /**
     * 通过用户id获取用户信息
     *
     * @param id 用户id
     * @return ResultObject
     */
    public static ResultObject getUserInfoByID(String id) {
        return getUserInfoByUkey(base36Encode(Long.valueOf(id)));
    }

    /**
     * 通过获取消息提醒的方式测试是否登录或者登录是否有效
     *
     * @return ResultObject
     */
    public static ResultObject testLogin() {
        ResultObject resultObject = new ResultObject();
        String token = getToken();
        String ukey = getUkey();
        //先判断有没有token，没有就是未登录，有的话检测一下是否过期
        if (!TextUtils.isEmpty(ukey) && ukey.length() == 6 && !TextUtils.isEmpty(token) && token.length() == 64) {
            resultObject = getMessageNum();
        } else {
            clearMyInfo();
            resultObject.code = ResultObject.ResultCode.CODE_NO_TOKEN;
        }
        return resultObject;
    }

    public static ResultObject getMessageNum() {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/community/rn_num.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("_", System.currentTimeMillis() + ""));
            String result = HttpFetcher.get(url, pairs).toString();
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                resultObject.ok = true;
            } else {
                if (getJsonInt(object, "error_code") == 200004) {
                    resultObject.code = ResultObject.ResultCode.CODE_TOKEN_INVALID;
                } else {
                    resultObject.code = ResultObject.ResultCode.CODE_LOGIN_FAILED;
                }
                clearMyInfo();
            }
        } catch (IOException e) {
            resultObject.code = ResultObject.ResultCode.CODE_NETWORK_ERROR;
            e.printStackTrace();
        } catch (JSONException e) {
            resultObject.code = ResultObject.ResultCode.CODE_JSON_ERROR;
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 收藏一个链接，理论是任意链接都行，吧……
     *
     * @param link     链接地址
     * @param title    链接标题
     * @param basketID 收藏果篮的id
     * @return ResultObject
     */
    public static ResultObject favorLink(String link, String title, String basketID) {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/favorite/link.json";
            ArrayList<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("basket_id", basketID));
            params.add(new BasicNameValuePair("url", link));
            params.add(new BasicNameValuePair("title", title));
            String result = HttpFetcher.post(url, params).toString();
            if (getUniversalJsonSimpleBoolean(result, resultObject)) {
                resultObject.ok = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 推荐一个链接
     *
     * @param link    链接地址
     * @param title   链接标题
     * @param summary 内容概述
     * @param comment 评语
     * @return ResultObject
     */
    public static ResultObject recommendLink(String link, String title, String summary, String comment) {
        String url = "http://www.guokr.com/apis/community/user/recommend.json";
        ResultObject resultObject = new ResultObject();
        if (TextUtils.isEmpty(summary)) {
            summary = title;
        }
        try {
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("title", title));
            pairs.add(new BasicNameValuePair("url", link));
            pairs.add(new BasicNameValuePair("summary", summary));
            pairs.add(new BasicNameValuePair("comment", comment));
            pairs.add(new BasicNameValuePair("target", "activity"));
            String result = HttpFetcher.post(url, pairs).toString();
            if (getUniversalJsonSimpleBoolean(result, resultObject)) {
                resultObject.ok = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 获取用户的果篮信息
     *
     * @return ResultObject.result is ArrayList[Basket]
     */
    public static ResultObject getBaskets() {
        ResultObject resultObject = new ResultObject();
        String url = "http://www.guokr.com/apis/favorite/basket.json";
        try {
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("t", System.currentTimeMillis() + ""));
            pairs.add(new BasicNameValuePair("retrieve_type", "by_ukey"));
            pairs.add(new BasicNameValuePair("ukey", getUkey()));
            pairs.add(new BasicNameValuePair("limit", "100"));
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray jsonArray = getUniversalJsonArray(result, resultObject);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 创建一个果篮
     *
     * @param title        果篮名
     * @param introduction 果篮介绍
     * @param category_id  category
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
            String result = HttpFetcher.post(url, params).toString();
            JSONObject subObject = getUniversalJsonObject(result, resultObject);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 获取分类 ，创建果篮有关
     *
     * @return ResultObject
     */
    public static ResultObject getCategoryList() {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/favorite/category.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray jsonArray = getUniversalJsonArray(result, resultObject);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 退出登录、清除过期数据
     */
    public static void clearMyInfo() {
        SharedUtil.remove(Consts.Key_Access_Token);
        SharedUtil.remove(Consts.Key_Ukey);
        SharedUtil.remove(Consts.Key_User_Avatar);
        SharedUtil.remove(Consts.Key_User_ID);
        SharedUtil.remove(Consts.Key_User_Name);
        GroupHelper.clearAllMyGroups();
        AskTagHelper.clearAllMyTags();
        HttpFetcher.getDefaultHttpClient().getCookieStore().clear();
        HttpFetcher.getDefaultUploadHttpClient().getCookieStore().clear();
    }

    /**
     * 获取保存的用户token
     *
     * @return 用户token，正确的话，64位长度
     */
    public static String getToken() {
        return SharedUtil.readString(Consts.Key_Access_Token, "");
    }

    /**
     * 获取保存的用户ukey
     *
     * @return 用户ukey，6位长度
     */
    public static String getUkey() {
        return SharedUtil.readString(Consts.Key_Ukey, "");
    }

    /**
     * 获取保存的用户id
     *
     * @return 用户id，一串数字
     */
    public static String getUserID() {
        return SharedUtil.readString(Consts.Key_User_ID, "");
    }

    /**
     * 获取保存的用户头像地址
     *
     * @return 头像地址为http链接
     */
    public static String getUserAvatar() {
        return SharedUtil.readString(Consts.Key_User_Avatar, "");
    }

    /**
     * 获取保存的用户cookie
     *
     * @return 用户登录时保存下来的cookie，未使用
     */
    public static String getCookie() {
        return SharedUtil.readString(Consts.Key_Cookie, "");
    }

    /**
     * 获取保存的用户cookie
     *
     * @return 生成一个简单的cookie
     */
    public static String getSimpleCookie() {
        return "_32353_access_token=" + getToken() + "; _32353_ukey=" + getUkey() + ";";
    }
}
