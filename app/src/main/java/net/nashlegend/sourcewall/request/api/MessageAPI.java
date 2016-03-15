package net.nashlegend.sourcewall.request.api;

import net.nashlegend.sourcewall.model.Message;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.model.Reminder;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.request.HttpFetcher;
import net.nashlegend.sourcewall.swrequest.JsonHandler;
import net.nashlegend.sourcewall.swrequest.RequestBuilder;
import net.nashlegend.sourcewall.swrequest.RequestObject;
import net.nashlegend.sourcewall.swrequest.RequestObject.CallBack;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.swrequest.parsers.Parser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by NashLegend on 16/3/15.
 */
public class MessageAPI extends APIBase {

    /**
     * 获取通知和站内信数量
     *
     * @return ResponseObject.result是ReminderNoticeNum
     */
    public static RequestObject<ReminderNoticeNum> getReminderAndNoticeNum(CallBack<ReminderNoticeNum> callBack) {
        Parser<ReminderNoticeNum> parser = new Parser<ReminderNoticeNum>() {
            @Override
            public ReminderNoticeNum parse(String str, ResponseObject<ReminderNoticeNum> responseObject) throws Exception {
                return ReminderNoticeNum.fromJson(JsonHandler.getUniversalJsonObject(str, responseObject));
            }
        };
        String url = "http://www.guokr.com/apis/community/rn_num.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("_", String.valueOf(System.currentTimeMillis()));
        return new RequestBuilder<ReminderNoticeNum>()
                .setUrl(url)
                .setParser(parser)
                .setRequestCallBack(callBack)
                .setParams(pairs)
                .get()
                .requestAsync();
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
                ReminderNoticeNum num = ReminderNoticeNum.fromJson(object);
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
            pairs.put("offset", String.valueOf(offset));
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
            pairs.put("offset", String.valueOf(offset));
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray notices = getUniversalJsonArray(result, resultObject);
            if (notices != null) {
                ArrayList<Message> noticeList = new ArrayList<>();
                for (int i = 0; i < notices.length(); i++) {
                    JSONObject noticesObject = notices.getJSONObject(i);
                    Message message = Message.fromJson(noticesObject);
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
                Message message = Message.fromJson(noticesObject);
                resultObject.ok = true;
                resultObject.result = message;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }
}
