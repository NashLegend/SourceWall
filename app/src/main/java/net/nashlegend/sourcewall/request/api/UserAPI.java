package net.nashlegend.sourcewall.request.api;

import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.db.BasketHelper;
import net.nashlegend.sourcewall.events.LoginStateChangedEvent;
import net.nashlegend.sourcewall.model.UserInfo;
import net.nashlegend.sourcewall.request.HttpUtil;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.RequestBuilder;
import net.nashlegend.sourcewall.request.RequestObject.CallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.parsers.BooleanParser;
import net.nashlegend.sourcewall.request.parsers.Parser;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;

import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by NashLegend on 2014/11/25 0025
 */
public class UserAPI extends APIBase {

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

    public static String getUserInfoString() {
        return "用户名：" + UserAPI.getName() + "\n用户key：" + getUkey() + "\n用户ID：" + UserAPI.getUserID() + "\n";
    }

    /**
     * 通过用户的ukey获取用户的详细信息
     *
     * @param ukey 用户ukey
     * @return ResponseObject
     */
    public static NetworkTask<UserInfo> getUserInfoByUkey(String ukey, CallBack<UserInfo> callBack) {
        String url = "http://apis.guokr.com/community/user/" + ukey + ".json";
        return new RequestBuilder<UserInfo>()
                .url(url)
                .parser(new Parser<UserInfo>() {
                    @Override
                    public UserInfo parse(String str, ResponseObject<UserInfo> responseObject) throws Exception {
                        return UserInfo.fromJson(JsonHandler.getUniversalJsonObject(str, responseObject));
                    }
                })
                .callback(callBack)
                .get()
                .requestAsync();
    }

    /**
     * 通过用户id获取用户信息
     *
     * @param id 用户id
     * @return ResponseObject
     */
    public static NetworkTask<UserInfo> getUserInfoByID(String id, CallBack<UserInfo> callBack) {
        return getUserInfoByUkey(base36Encode(Long.valueOf(id)), callBack);
    }

    /**
     * 推荐一个链接
     *
     * @param link    链接地址
     * @param title   链接标题
     * @param summary 内容概述
     * @param comment 评语
     * @return ResponseObject
     */
    public static NetworkTask<Boolean> recommendLink(String link, String title, String summary, String comment, CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/community/user/recommend.json";
        if (TextUtils.isEmpty(summary)) {
            summary = title;
        }
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("title", title);
        pairs.put("url", link);
        pairs.put("summary", summary);
        pairs.put("comment", comment);
        pairs.put("target", "activity");
        return new RequestBuilder<Boolean>()
                .url(url)
                .parser(new BooleanParser())
                .callback(callBack)
                .params(pairs)
                .post()
                .requestAsync();
    }

    /**
     * 退出登录、清除过期数据
     */
    @SuppressWarnings("deprecation")
    public static void logout() {
        SharedPreferencesUtil.remove(Consts.Key_Access_Token);
        SharedPreferencesUtil.remove(Consts.Key_Ukey);
        SharedPreferencesUtil.remove(Consts.Key_User_Avatar);
        SharedPreferencesUtil.remove(Consts.Key_User_ID);
        SharedPreferencesUtil.remove(Consts.Key_User_Name);
        CookieSyncManager.createInstance(App.getApp());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.hasCookies();
        cookieManager.removeSessionCookie();
        CookieSyncManager.getInstance().sync();
        //直接置为null不就得了……
        HttpUtil.clearCookiesForOkHttp(HttpUtil.getDefaultUploadHttpClient());
        HttpUtil.clearCookiesForOkHttp(HttpUtil.getDefaultUploadHttpClient());
        BasketHelper.clearAllMyBaskets();
        EventBus.getDefault().post(new LoginStateChangedEvent());
    }

    /**
     * 获取保存的用户token
     *
     * @return 用户token，正确的话，64位长度
     */
    public static String getToken() {
        return SharedPreferencesUtil.readString(Consts.Key_Access_Token, "");
    }

    /**
     * 保存用户token
     *
     * @param token
     */
    public static void setToken(String token) {
        SharedPreferencesUtil.saveString(Consts.Key_Access_Token, token);
    }

    /**
     * 获取保存的用户ukey
     *
     * @return 用户ukey，6位长度
     */
    public static String getUkey() {
        return SharedPreferencesUtil.readString(Consts.Key_Ukey, "");
    }

    /**
     * 获取保存的用户ukey
     *
     * @return 用户ukey，6位长度
     */
    public static void setUkey(String ukey) {
        SharedPreferencesUtil.saveString(Consts.Key_Ukey, ukey);
    }

    /**
     * 获取保存的用户id
     *
     * @return 用户id，一串数字
     */
    public static String getUserID() {
        return SharedPreferencesUtil.readString(Consts.Key_User_ID, "");
    }

    /**
     * 获取保存的用户名
     *
     * @return 用户id，一串数字
     */
    public static String getName() {
        return SharedPreferencesUtil.readString(Consts.Key_User_Name, "");
    }

    /**
     * 获取保存的用户头像地址
     *
     * @return 头像地址为http链接
     */
    public static String getUserAvatar() {
        return SharedPreferencesUtil.readString(Consts.Key_User_Avatar, "");
    }

    /**
     * 获取保存的用户cookie
     *
     * @return 用户登录时保存下来的cookie，未使用
     */
    public static String getCookie() {
        return SharedPreferencesUtil.readString(Consts.Key_Cookie, "");
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
