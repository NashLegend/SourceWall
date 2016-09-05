package net.nashlegend.sourcewall.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2015/2/2 0002
 * 私信，貌似不仅仅是私信还有草稿，不知道还有多少
 * 私信是message，草稿是draft，草稿在这里就算了吧，我这里也不支持html编辑
 */
public class Reminder extends AceModel {

    private String content = "";
    private long date_created = 0l;
    private String id = "";
    private String group = "message";
    private String ukey = "";//永远是用户自己
    private String url = "";

    public static Reminder fromJson(JSONObject reminderObject) throws Exception {
        Reminder reminder = new Reminder();
        reminder.setContent(reminderObject.optString("content"));
        reminder.setUrl(reminderObject.optString("url"));
        reminder.setUkey(reminderObject.optString("ukey"));
        reminder.setDateCreated(reminderObject.optLong("date_created"));
        reminder.setId(reminderObject.optString("id"));
        reminder.setGroup(reminderObject.optString("group"));
        return reminder;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDateCreated() {
        return date_created;
    }

    public void setDateCreated(long date_last_updated) {
        this.date_created = date_last_updated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUkey() {
        return ukey;
    }

    public void setUkey(String ukey) {
        this.ukey = ukey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Reminder() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeLong(this.date_created);
        dest.writeString(this.id);
        dest.writeString(this.group);
        dest.writeString(this.ukey);
        dest.writeString(this.url);
    }

    protected Reminder(Parcel in) {
        this.content = in.readString();
        this.date_created = in.readLong();
        this.id = in.readString();
        this.group = in.readString();
        this.ukey = in.readString();
        this.url = in.readString();
    }

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel source) {
            return new Reminder(source);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };
}
