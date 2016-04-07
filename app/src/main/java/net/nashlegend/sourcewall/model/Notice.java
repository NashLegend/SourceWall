package net.nashlegend.sourcewall.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2015/2/2 0002
 * 通知
 */
public class Notice extends AceModel {

    private String content = "";
    private long date_last_updated = 0L;
    private String id = "";
    private boolean is_read = false;
    private String ukey = "";//永远是用户自己
    private String url = "";

    public static Notice fromJson(JSONObject noticesObject) throws Exception {
        Notice notice = new Notice();
        notice.setContent(noticesObject.optString("content"));
        notice.setUrl(noticesObject.optString("url"));
        notice.setUkey(noticesObject.optString("ukey"));
        notice.setDate_last_updated(noticesObject.optLong("date_last_updated"));
        notice.setId(noticesObject.optString("id"));
        notice.setIs_read(noticesObject.optBoolean("is_read"));
        return notice;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDate_last_updated() {
        return date_last_updated;
    }

    public void setDate_last_updated(long date_last_updated) {
        this.date_last_updated = date_last_updated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeLong(this.date_last_updated);
        dest.writeString(this.id);
        dest.writeByte(is_read ? (byte) 1 : (byte) 0);
        dest.writeString(this.ukey);
        dest.writeString(this.url);
    }

    public Notice() {
    }

    protected Notice(Parcel in) {
        this.content = in.readString();
        this.date_last_updated = in.readLong();
        this.id = in.readString();
        this.is_read = in.readByte() != 0;
        this.ukey = in.readString();
        this.url = in.readString();
    }

    public static final Creator<Notice> CREATOR = new Creator<Notice>() {
        public Notice createFromParcel(Parcel source) {
            return new Notice(source);
        }

        public Notice[] newArray(int size) {
            return new Notice[size];
        }
    };
}
