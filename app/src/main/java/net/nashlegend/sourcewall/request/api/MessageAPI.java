package net.nashlegend.sourcewall.request.api;

import net.nashlegend.sourcewall.model.Message;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.model.Reminder;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.ParamsMap;
import net.nashlegend.sourcewall.request.RequestBuilder;
import net.nashlegend.sourcewall.request.RequestObject.CallBack;
import net.nashlegend.sourcewall.request.parsers.BooleanParser;
import net.nashlegend.sourcewall.request.parsers.IgnoreNoticeParser;
import net.nashlegend.sourcewall.request.parsers.MessageListParser;
import net.nashlegend.sourcewall.request.parsers.MessageParser;
import net.nashlegend.sourcewall.request.parsers.NoticeListParser;
import net.nashlegend.sourcewall.request.parsers.ReminderListParser;
import net.nashlegend.sourcewall.request.parsers.ReminderNoticeNumParser;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/3/15.
 */
public class MessageAPI extends APIBase {

    /**
     * 获取通知和站内信数量
     *
     * @param callBack
     * @return
     */
    public static NetworkTask<ReminderNoticeNum> getReminderAndNoticeNum(CallBack<ReminderNoticeNum> callBack) {
        String url = "http://www.guokr.com/apis/community/rn_num.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("_", String.valueOf(System.currentTimeMillis()));
        return new RequestBuilder<ReminderNoticeNum>()
                .get()
                .url(url)
                .params(pairs)
                .parser(new ReminderNoticeNumParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 获取提醒列表
     *
     * @param offset
     * @param callBack
     * @return
     */
    public static NetworkTask<ArrayList<Reminder>> getReminderList(int offset, CallBack<ArrayList<Reminder>> callBack) {
        String url = "http://www.guokr.com/apis/community/reminder.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("_", System.currentTimeMillis() + "");
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<Reminder>>()
                .get()
                .url(url)
                .params(pairs)
                .parser(new ReminderListParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 获取通知详情列表，一次性取得全部
     *
     * @param callBack
     * @return
     */
    public static NetworkTask<ArrayList<Notice>> getNoticeList(CallBack<ArrayList<Notice>> callBack) {
        String url = "http://www.guokr.com/apis/community/notice.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("_", System.currentTimeMillis() + "");
        pairs.put("limit", "1024");
        pairs.put("offset", "0");
        return new RequestBuilder<ArrayList<Notice>>()
                .get()
                .url(url)
                .params(pairs)
                .parser(new NoticeListParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 忽略所有消息，相当于ignoreOneNotice("")
     *
     * @param callBack
     * @return
     */
    public static NetworkTask<Boolean> ignoreAllNotice(CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/community/notice_ignore.json";
        return new RequestBuilder<Boolean>()
                .put()
                .url(url)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 忽略一条通知消息，返回的是剩余的通知详情列表
     *
     * @param noticeID
     * @param callBack
     * @return
     */
    public static NetworkTask<ArrayList<Notice>> ignoreOneNotice(String noticeID, CallBack<ArrayList<Notice>> callBack) {
        String url = "http://www.guokr.com/apis/community/notice_ignore.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("nid", noticeID);
        pairs.put("_", System.currentTimeMillis() + "");
        return new RequestBuilder<ArrayList<Notice>>()
                .put()
                .url(url)
                .params(pairs)
                .parser(new IgnoreNoticeParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 忽略一条通知消息，返回的是剩余的通知详情列表
     *
     * @return RequestObject
     */
    public static NetworkTask<ArrayList<Notice>> ignoreOneNotice(String noticeID) {
        return ignoreOneNotice(noticeID, null);
    }

    /**
     * 获取站内信详情列表，与某人的对话只显示最近一条。目前还不知道获取对话接口
     *
     * @param offset
     * @param callBack
     * @return RequestObject
     */
    public static NetworkTask<ArrayList<Message>> getMessageList(int offset, CallBack<ArrayList<Message>> callBack) {
        String url = "http://www.guokr.com/apis/community/user/message.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<Message>>()
                .get()
                .url(url)
                .params(pairs)
                .parser(new MessageListParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 根据id获取一条站内信
     *
     * @param id
     * @param callBack
     * @return RequestObject
     */
    public static NetworkTask<Message> getOneMessage(String id, CallBack<Message> callBack) {
        String url = "http://www.guokr.com/apis/community/user/message/" + id + ".json";
        return new RequestBuilder<Message>()
                .get()
                .url(url)
                .parser(new MessageParser())
                .callback(callBack)
                .requestAsync();
    }

}
