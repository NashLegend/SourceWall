package net.nashlegend.sourcewall.request.api;

import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.model.Category;
import net.nashlegend.sourcewall.model.Message;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.model.Reminder;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.model.UserInfo;
import net.nashlegend.sourcewall.request.HttpFetcher;
import net.nashlegend.sourcewall.swrequest.RequestBuilder;
import net.nashlegend.sourcewall.swrequest.RequestObject;
import net.nashlegend.sourcewall.swrequest.ResponseCode;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.swrequest.parsers.BooleanParser;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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

    public static String getUserInfoString() {
        return "用户名：" + UserAPI.getName() + "\n用户key：" + getUkey() + "\n用户ID：" + UserAPI.getUserID() + "\n";
    }

    /**
     * 通过用户的ukey获取用户的详细信息
     *
     * @param ukey 用户ukey
     * @return ResponseObject
     */
    public static ResponseObject<UserInfo> getUserInfoByUkey(String ukey) {
        ResponseObject<UserInfo> resultObject = new ResponseObject<>();
        try {
            String result = HttpFetcher.get("http://apis.guokr.com/community/user/" + ukey + ".json").toString();
            JSONObject subObject = getUniversalJsonObject(result, resultObject);
            if (subObject != null) {
                UserInfo info = new UserInfo();
                info.setDate_created(subObject.optString("date_created"));
                info.setIntroduction(subObject.optString("introduction"));
                info.setNickname(subObject.optString("nickname"));
                info.setTitle(subObject.optString("title"));
                info.setUkey(subObject.optString("ukey"));
                info.setUrl(subObject.optString("url"));
                info.setId(info.getUrl().replaceAll("^\\D+(\\d+)\\D*", "$1"));
                info.setAvatar(subObject.getJSONObject("avatar").getString("large").replaceAll("\\?.*$", ""));
                resultObject.result = info;
                resultObject.ok = true;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 通过用户id获取用户信息
     *
     * @param id 用户id
     * @return ResponseObject
     */
    public static ResponseObject<UserInfo> getUserInfoByID(String id) {
        return getUserInfoByUkey(base36Encode(Long.valueOf(id)));
    }

    /**
     * 通过获取消息提醒的方式测试是否登录或者登录是否有效
     *
     * @return ResponseObject
     */
    public static ResponseObject<ReminderNoticeNum> testLogin() {
        ResponseObject<ReminderNoticeNum> resultObject = new ResponseObject<>();
        String token = getToken();
        String ukey = getUkey();
        //先判断有没有token，没有就是未登录，有的话检测一下是否过期
        if (!TextUtils.isEmpty(ukey) && ukey.length() == 6 && !TextUtils.isEmpty(token) && token.length() == 64) {
            resultObject = getReminderAndNoticeNum();
        } else {
            clearMyInfo();
            resultObject.code = ResponseCode.CODE_TOKEN_INVALID;
        }
        return resultObject;
    }

    /**
     * 获取通知和站内信数量
     *
     * @return ResponseObject.result是ReminderNoticeNum
     */
    public static ResponseObject<ReminderNoticeNum> getReminderAndNoticeNum() {
        ResponseObject<ReminderNoticeNum> resultObject = new ResponseObject<>();
        try {
            String url = "http://www.guokr.com/apis/community/rn_num.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("_", System.currentTimeMillis() + "");
            String result = HttpFetcher.get(url, pairs).toString();
            JSONObject object = getUniversalJsonObject(result, resultObject);
            if (object != null) {
                ReminderNoticeNum num = new ReminderNoticeNum();
                num.setNotice_num(object.optInt("n"));//通知数量
                num.setReminder_num(object.optInt("r"));//站内信数量
                resultObject.ok = true;
                resultObject.result = num;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 获取提醒列表
     *
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<Reminder>> getReminderList(int offset) {
        ResponseObject<ArrayList<Reminder>> resultObject = new ResponseObject<>();
        try {
            String url = "http://www.guokr.com/apis/community/reminder.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("_", System.currentTimeMillis() + "");
            pairs.put("limit", "20");
            pairs.put("offset", offset + "");
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray reminders = getUniversalJsonArray(result, resultObject);
            if (reminders != null) {
                ArrayList<Reminder> noticeList = new ArrayList<>();
                for (int i = 0; i < reminders.length(); i++) {
                    JSONObject reminderObject = reminders.getJSONObject(i);
                    Reminder notice = new Reminder();
                    notice.setContent(reminderObject.optString("content"));
                    notice.setUrl(reminderObject.optString("url"));
                    notice.setUkey(reminderObject.optString("ukey"));
                    notice.setDateCreated(reminderObject.optLong("date_created"));
                    notice.setId(reminderObject.optString("id"));
                    notice.setGroup(reminderObject.optString("group"));
                    noticeList.add(notice);
                }
                resultObject.ok = true;
                resultObject.result = noticeList;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 获取通知详情列表，一次性取得全部
     *
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<Notice>> getNoticeList() {
        ResponseObject<ArrayList<Notice>> resultObject = new ResponseObject<>();
        try {
            String url = "http://www.guokr.com/apis/community/notice.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("_", System.currentTimeMillis() + "");
            pairs.put("limit", "1024");
            pairs.put("offset", "0");
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray notices = getUniversalJsonArray(result, resultObject);
            if (notices != null) {
                ArrayList<Notice> noticeList = new ArrayList<>();
                for (int i = 0; i < notices.length(); i++) {
                    JSONObject noticesObject = notices.getJSONObject(i);
                    Notice notice = new Notice();
                    notice.setContent(noticesObject.optString("content"));
                    notice.setUrl(noticesObject.optString("url"));
                    notice.setUkey(noticesObject.optString("ukey"));
                    notice.setDate_last_updated(noticesObject.optLong("date_last_updated"));
                    notice.setId(noticesObject.optString("id"));
                    notice.setIs_read(noticesObject.optBoolean("is_read"));
                    noticeList.add(notice);
                }
                resultObject.ok = true;
                resultObject.result = noticeList;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 忽略所有消息，相当于ignoreOneNotice("")
     *
     * @return ResponseObject，仅仅有ok，result是空
     */
    public static ResponseObject ignoreAllNotice() {
        ResponseObject resultObject = new ResponseObject();
        try {
            String url = "http://www.guokr.com/apis/community/notice_ignore.json";
            HashMap<String, String> pairs = new HashMap<>();
            String result = HttpFetcher.put(url, pairs).toString();
            resultObject.ok = getUniversalJsonSimpleBoolean(result, resultObject);
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 忽略一条通知消息，返回的是剩余的通知详情列表
     *
     * @return ResponseObject resultObject.result是剩余的NoticeList
     */
    public static ResponseObject<ArrayList<Notice>> ignoreOneNotice(String noticeID) {
        ResponseObject<ArrayList<Notice>> resultObject = new ResponseObject<>();
        try {
            String url = "http://www.guokr.com/apis/community/notice_ignore.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("nid", noticeID);
            pairs.put("_", System.currentTimeMillis() + "");
            String result = HttpFetcher.put(url, pairs).toString();
            JSONObject nObject = getUniversalJsonObject(result, resultObject);
            if (nObject != null) {
                JSONArray notices = getJsonArray(nObject, "list");
                ArrayList<Notice> noticeList = new ArrayList<>();
                for (int i = 0; i < notices.length(); i++) {
                    JSONObject noticeObject = notices.getJSONObject(i);
                    Notice notice = new Notice();
                    notice.setContent(noticeObject.optString("content"));
                    notice.setUrl(noticeObject.optString("url"));
                    notice.setUkey(noticeObject.optString("ukey"));
                    notice.setDate_last_updated(noticeObject.optLong("date_last_updated"));
                    notice.setId(noticeObject.optString("id"));
                    notice.setIs_read(noticeObject.optBoolean("is_read"));
                    noticeList.add(notice);
                }
                resultObject.ok = true;
                resultObject.result = noticeList;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 获取站内信详情列表，与某人的对话只显示最近一条。目前还不知道获取对话接口
     *
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<Message>> getMessageList(int offset) {
        ResponseObject<ArrayList<Message>> resultObject = new ResponseObject<>();
        try {
            String url = "http://www.guokr.com/apis/community/user/message.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("limit", "20");
            pairs.put("offset", offset + "");
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray notices = getUniversalJsonArray(result, resultObject);
            if (notices != null) {
                ArrayList<Message> noticeList = new ArrayList<>();
                for (int i = 0; i < notices.length(); i++) {
                    JSONObject noticesObject = notices.getJSONObject(i);
                    Message message = new Message();
                    message.setContent(noticesObject.optString("content"));
                    message.setDirection(noticesObject.optString("direction"));
                    message.setUkey(noticesObject.optString("5p6t9t"));
                    message.setAnother_ukey(noticesObject.optString("ukey_another"));
                    message.setDateCreated(noticesObject.optString("date_created"));
                    message.setId(noticesObject.optString("id"));
                    message.setIs_read(noticesObject.optBoolean("is_read"));
                    message.setTotal(noticesObject.optInt("total"));
                    message.setUnread_count(noticesObject.optInt("unread_count"));
                    noticeList.add(message);
                }
                resultObject.ok = true;
                resultObject.result = noticeList;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 根据id获取一条站内信
     *
     * @return ResponseObject
     */
    public static ResponseObject<Message> getOneMessage(String id) {
        ResponseObject<Message> resultObject = new ResponseObject<>();
        try {
            String url = "http://www.guokr.com/apis/community/user/message/" + id + ".json";
            HashMap<String, String> pairs = new HashMap<>();
            String result = HttpFetcher.get(url, pairs).toString();
            JSONObject noticesObject = getUniversalJsonObject(result, resultObject);
            if (noticesObject != null) {
                Message message = new Message();
                message.setContent(noticesObject.optString("content"));
                message.setDirection(noticesObject.optString("direction"));
                message.setUkey(noticesObject.optString("5p6t9t"));
                message.setAnother_ukey(noticesObject.optString("ukey_another"));
                message.setDateCreated(noticesObject.optString("date_created"));
                message.setId(noticesObject.optString("id"));
                message.setIs_read(noticesObject.optBoolean("is_read"));
                resultObject.ok = true;
                resultObject.result = message;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 收藏一个链接，理论是任意链接都行，吧……
     *
     * @param link     链接地址
     * @param title    链接标题
     * @param basketID 收藏果篮的id
     * @return ResponseObject
     */
    public static ResponseObject favorLink(String link, String title, String basketID) {
        ResponseObject resultObject = new ResponseObject();
        try {
            String url = "http://www.guokr.com/apis/favorite/link.json";
            HashMap<String, String> params = new HashMap<>();
            params.put("basket_id", basketID);
            params.put("url", link);
            params.put("title", title);
            String result = HttpFetcher.post(url, params).toString();
            if (getUniversalJsonSimpleBoolean(result, resultObject)) {
                resultObject.ok = true;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
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
     * @return ResponseObject
     */
    public static ResponseObject recommendLink(String link, String title, String summary, String comment) {
        String url = "http://www.guokr.com/apis/community/user/recommend.json";
        ResponseObject resultObject = new ResponseObject();
        if (TextUtils.isEmpty(summary)) {
            summary = title;
        }
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("title", title);
            pairs.put("url", link);
            pairs.put("summary", summary);
            pairs.put("comment", comment);
            pairs.put("target", "activity");
            String result = HttpFetcher.post(url, pairs).toString();
            if (getUniversalJsonSimpleBoolean(result, resultObject)) {
                resultObject.ok = true;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
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
     * @return ResponseObject
     */
    public static void recommendLink(String link, String title, String summary, String comment, RequestObject.CallBack<Boolean> callBack) {
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
        new RequestBuilder<Boolean>()
                .setUrl(url)
                .setParser(new BooleanParser())
                .setRequestCallBack(callBack)
                .setParams(pairs)
                .post()
                .requestAsync();
    }

    /**
     * 获取用户的果篮信息
     *
     * @return ResponseObject.result is ArrayList[Basket]
     */
    public static ResponseObject<ArrayList<Basket>> getBaskets() {
        ResponseObject<ArrayList<Basket>> resultObject = new ResponseObject<>();
        String url = "http://www.guokr.com/apis/favorite/basket.json";
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("t", System.currentTimeMillis() + "");
            pairs.put("retrieve_type", "by_ukey");
            pairs.put("ukey", getUkey());
            pairs.put("limit", "100");
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray jsonArray = getUniversalJsonArray(result, resultObject);
            if (jsonArray != null) {
                ArrayList<Basket> baskets = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subObject = jsonArray.getJSONObject(i);
                    Basket basket = new Basket();
                    basket.setId(subObject.optString("id"));
                    basket.setIntroduction(subObject.optString("introduction"));
                    basket.setLinks_count(subObject.optInt("links_count"));
                    basket.setName(subObject.optString("title"));
                    JSONObject category = getJsonObject(subObject, "category");
                    if (category != null) {
                        basket.setCategory_id(category.optString("id"));
                        basket.setCategory_name(category.optString("name"));
                    }
                    baskets.add(basket);
                }
                resultObject.ok = true;
                resultObject.result = baskets;
                myBaskets = baskets;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 创建一个果篮
     *
     * @param title        果篮名
     * @param introduction 果篮介绍
     * @param category_id  category
     * @return ResponseObject.result is Basket
     */
    public static ResponseObject<Basket> createBasket(String title, String introduction, String category_id) {
        ResponseObject<Basket> resultObject = new ResponseObject<>();
        try {
            String url = "http://www.guokr.com/apis/favorite/basket.json";
            HashMap<String, String> params = new HashMap<>();
            params.put("title", title);
            params.put("introduction", introduction);
            params.put("category_id", category_id);
            String result = HttpFetcher.post(url, params).toString();
            JSONObject subObject = getUniversalJsonObject(result, resultObject);
            if (subObject != null) {
                Basket basket = new Basket();
                basket.setId(subObject.optString("id"));
                basket.setIntroduction(subObject.optString("introduction"));
                basket.setLinks_count(0);
                basket.setName(subObject.optString("title"));
                JSONObject category = getJsonObject(subObject, "category");
                if (category != null) {
                    basket.setCategory_id(category.optString("id"));
                    basket.setCategory_name(category.optString("name"));
                }
                resultObject.ok = true;
                resultObject.result = basket;
                myBaskets.add(basket);

            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 获取分类 ，创建果篮有关
     *
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<Category>> getCategoryList() {
        ResponseObject<ArrayList<Category>> resultObject = new ResponseObject<>();
        try {
            String url = "http://www.guokr.com/apis/favorite/category.json";
            HashMap<String, String> pairs = new HashMap<>();
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray jsonArray = getUniversalJsonArray(result, resultObject);
            if (jsonArray != null) {
                ArrayList<Category> categories = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subObject = jsonArray.getJSONObject(i);
                    Category category = new Category();
                    category.setId(subObject.optString("id"));
                    category.setName(subObject.optString("name"));
                    categories.add(category);
                }
                resultObject.ok = true;
                resultObject.result = categories;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 退出登录、清除过期数据
     */
    @SuppressWarnings("deprecation")
    public static void clearMyInfo() {
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
        HttpFetcher.clearCookiesForOkHttp(HttpFetcher.getDefaultUploadHttpClient());
        HttpFetcher.clearCookiesForOkHttp(HttpFetcher.getDefaultUploadHttpClient());
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
     * 获取保存的用户ukey
     *
     * @return 用户ukey，6位长度
     */
    public static String getUkey() {
        return SharedPreferencesUtil.readString(Consts.Key_Ukey, "");
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
