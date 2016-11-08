package net.nashlegend.sourcewall.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2015/2/2 0002
 * 站内信
 */
public class Message extends AceModel {

    private String id = "";
    private String content = "";
    private String direction = "send";
    private boolean is_read = false;
    private String dateCreated = "";
    private String ukey = "";
    private String another_ukey = "";
    private int unread_count = 0;//获取全部列表的时候才有这个属性
    private int total = 0;//获取全部列表的时候才有这个属性

    public static Message fromJson(JSONObject messageObject) {
        Message message = new Message();
        message.setContent(messageObject.optString("content"));
        message.setDirection(messageObject.optString("direction"));
        message.setUkey(messageObject.optString("5p6t9t"));
        message.setAnother_ukey(messageObject.optString("ukey_another"));
        message.setDateCreated(messageObject.optString("date_created"));
        message.setId(messageObject.optString("id"));
        message.setIs_read(messageObject.optBoolean("is_read"));
        //TODO
        message.setTotal(messageObject.optInt("total"));
        message.setUnread_count(messageObject.optInt("unread_count"));
        return message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * html格式
     */
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public int getUnread_count() {
        return unread_count;
    }

    public void setUnread_count(int unread_count) {
        this.unread_count = unread_count;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getAnother_ukey() {
        return another_ukey;
    }

    public void setAnother_ukey(String another_ukey) {
        this.another_ukey = another_ukey;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getUkey() {
        return ukey;
    }

    public void setUkey(String ukey) {
        this.ukey = ukey;
    }

    public Message() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.content);
        dest.writeString(this.direction);
        dest.writeByte(this.is_read ? (byte) 1 : (byte) 0);
        dest.writeString(this.dateCreated);
        dest.writeString(this.ukey);
        dest.writeString(this.another_ukey);
        dest.writeInt(this.unread_count);
        dest.writeInt(this.total);
    }

    protected Message(Parcel in) {
        this.id = in.readString();
        this.content = in.readString();
        this.direction = in.readString();
        this.is_read = in.readByte() != 0;
        this.dateCreated = in.readString();
        this.ukey = in.readString();
        this.another_ukey = in.readString();
        this.unread_count = in.readInt();
        this.total = in.readInt();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
