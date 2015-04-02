package net.nashlegend.sourcewall.request.api;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import net.nashlegend.sourcewall.AppApplication;
import net.nashlegend.sourcewall.db.AskTagHelper;
import net.nashlegend.sourcewall.db.GroupHelper;
import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.model.Category;
import net.nashlegend.sourcewall.model.Message;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.model.Reminder;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.model.UserInfo;
import net.nashlegend.sourcewall.request.HttpFetcher;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

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

    public static String getUserInfoString() {
        return "用户名：" + UserAPI.getName() + "\n用户key：" + getUkey() + "\n用户ID：" + UserAPI.getUserID() + "\n";
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
                        .getString("large").replaceAll("\\?.*$", ""));
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
            resultObject = getReminderAndNoticeNum();
        } else {
            clearMyInfo();
            resultObject.code = ResultObject.ResultCode.CODE_NO_TOKEN;
        }
        return resultObject;
    }

    /**
     * 获取通知和站内信数量
     *
     * @return ResultObject.result是ReminderNoticeNum
     */
    public static ResultObject getReminderAndNoticeNum() {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/community/rn_num.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("_", System.currentTimeMillis() + ""));
            String result = HttpFetcher.get(url, pairs).toString();
            JSONObject object = getUniversalJsonObject(result, resultObject);
            if (object != null) {
                ReminderNoticeNum num = new ReminderNoticeNum();
                num.setNotice_num(getJsonInt(object, "n"));//通知数量
                num.setReminder_num(getJsonInt(object, "r"));//站内信数量
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
     * @return ResultObject
     */
    public static ResultObject getReminderList(int offset) {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/community/reminder.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("_", System.currentTimeMillis() + ""));
            pairs.add(new BasicNameValuePair("limit", "20"));
            pairs.add(new BasicNameValuePair("offset", offset + ""));
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray reminders = getUniversalJsonArray(result, resultObject);
            if (reminders != null) {
                ArrayList<Reminder> noticeList = new ArrayList<>();
                for (int i = 0; i < reminders.length(); i++) {
                    JSONObject reminderObject = reminders.getJSONObject(i);
                    Reminder notice = new Reminder();
                    notice.setContent(getJsonString(reminderObject, "content"));
                    notice.setUrl(getJsonString(reminderObject, "url"));
                    notice.setUkey(getJsonString(reminderObject, "ukey"));
                    notice.setDateCreated(getJsonLong(reminderObject, "date_created"));
                    notice.setId(getJsonString(reminderObject, "id"));
                    notice.setGroup(getJsonString(reminderObject, "group"));
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
     * @return ResultObject
     */
    public static ResultObject getNoticeList() {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/community/notice.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("_", System.currentTimeMillis() + ""));
//            pairs.add(new BasicNameValuePair("limit", "20"));
//            pairs.add(new BasicNameValuePair("offset", offset + ""));
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray notices = getUniversalJsonArray(result, resultObject);
            if (notices != null) {
                ArrayList<Notice> noticeList = new ArrayList<>();
                for (int i = 0; i < notices.length(); i++) {
                    JSONObject noticesObject = notices.getJSONObject(i);
                    Notice notice = new Notice();
                    notice.setContent(getJsonString(noticesObject, "content"));
                    notice.setUrl(getJsonString(noticesObject, "url"));
                    notice.setUkey(getJsonString(noticesObject, "ukey"));
                    notice.setDate_last_updated(getJsonLong(noticesObject, "date_last_updated"));
                    notice.setId(getJsonString(noticesObject, "id"));
                    notice.setIs_read(getJsonBoolean(noticesObject, "is_read"));
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
     * @return ResultObject，仅仅有ok，result是空
     */
    public static ResultObject ignoreAllNotice() {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/community/notice_ignore.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
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
     * @return ResultObject resultObject.result是剩余的NoticeList
     */
    public static ResultObject ignoreOneNotice(String noticeID) {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/community/notice_ignore.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("nid", noticeID));
            pairs.add(new BasicNameValuePair("_", System.currentTimeMillis() + ""));
            String result = HttpFetcher.put(url, pairs).toString();
            JSONObject nObject = getUniversalJsonObject(result, resultObject);
            if (nObject != null) {
                JSONArray notices = getJsonArray(nObject, "list");
                ArrayList<Notice> noticeList = new ArrayList<>();
                for (int i = 0; i < notices.length(); i++) {
                    JSONObject noticeObject = notices.getJSONObject(i);
                    Notice notice = new Notice();
                    notice.setContent(getJsonString(noticeObject, "content"));
                    notice.setUrl(getJsonString(noticeObject, "url"));
                    notice.setUkey(getJsonString(noticeObject, "ukey"));
                    notice.setDate_last_updated(getJsonLong(noticeObject, "date_last_updated"));
                    notice.setId(getJsonString(noticeObject, "id"));
                    notice.setIs_read(getJsonBoolean(noticeObject, "is_read"));
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
     * @return ResultObject
     */
    public static ResultObject getMessageList(int offset) {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/community/user/message.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("limit", "20"));
            pairs.add(new BasicNameValuePair("offset", offset + ""));
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray notices = getUniversalJsonArray(result, resultObject);
            if (notices != null) {
                ArrayList<Message> noticeList = new ArrayList<>();
                for (int i = 0; i < notices.length(); i++) {
                    JSONObject noticesObject = notices.getJSONObject(i);
                    Message message = new Message();
                    message.setContent(getJsonString(noticesObject, "content"));
                    message.setDirection(getJsonString(noticesObject, "direction"));
                    message.setUkey(getJsonString(noticesObject, "5p6t9t"));
                    message.setAnother_ukey(getJsonString(noticesObject, "ukey_another"));
                    message.setDateCreated(getJsonString(noticesObject, "date_created"));
                    message.setId(getJsonString(noticesObject, "id"));
                    message.setIs_read(getJsonBoolean(noticesObject, "is_read"));
                    message.setTotal(getJsonInt(noticesObject, "total"));
                    message.setUnread_count(getJsonInt(noticesObject, "unread_count"));
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
     * @return ResultObject
     */
    public static ResultObject getOneMessage(String id) {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/apis/community/user/message/" + id + ".json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            String result = HttpFetcher.get(url, pairs).toString();
            JSONObject noticesObject = getUniversalJsonObject(result, resultObject);
            if (noticesObject != null) {
                Message message = new Message();
                message.setContent(getJsonString(noticesObject, "content"));
                message.setDirection(getJsonString(noticesObject, "direction"));
                message.setUkey(getJsonString(noticesObject, "5p6t9t"));
                message.setAnother_ukey(getJsonString(noticesObject, "ukey_another"));
                message.setDateCreated(getJsonString(noticesObject, "date_created"));
                message.setId(getJsonString(noticesObject, "id"));
                message.setIs_read(getJsonBoolean(noticesObject, "is_read"));
                resultObject.ok = true;
                resultObject.result = noticesObject;
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
            handleRequestException(e, resultObject);
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
            handleRequestException(e, resultObject);
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
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 退出登录、清除过期数据
     */
    public static void clearMyInfo() {
        SharedPreferencesUtil.remove(Consts.Key_Access_Token);
        SharedPreferencesUtil.remove(Consts.Key_Ukey);
        SharedPreferencesUtil.remove(Consts.Key_User_Avatar);
        SharedPreferencesUtil.remove(Consts.Key_User_ID);
        SharedPreferencesUtil.remove(Consts.Key_User_Name);
        GroupHelper.clearAllMyGroups();
        AskTagHelper.clearAllMyTags();
        CookieSyncManager.createInstance(AppApplication.getApplication());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.hasCookies();
        cookieManager.removeSessionCookie();
        CookieSyncManager.getInstance().sync();
        HttpFetcher.getDefaultHttpClient().getCookieStore().clear();
        HttpFetcher.getDefaultUploadHttpClient().getCookieStore().clear();
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
