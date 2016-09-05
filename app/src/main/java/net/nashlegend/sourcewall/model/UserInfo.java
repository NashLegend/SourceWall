package net.nashlegend.sourcewall.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2014/12/23 0023
 */
public class UserInfo extends AceModel {

    private String id = "";
    private String ukey = "";
    private String title = "";
    private String nickname = "";
    private String introduction = "";
    private String avatar = "";
    private String date_created = "";
    private String url = "";

    public static UserInfo fromJson(JSONObject subObject) throws Exception {
        UserInfo info = new UserInfo();
        info.setDate_created(subObject.optString("date_created"));
        info.setIntroduction(subObject.optString("introduction"));
        info.setNickname(subObject.optString("nickname"));
        info.setTitle(subObject.optString("title"));
        info.setUkey(subObject.optString("ukey"));
        info.setUrl(subObject.optString("url"));
        info.setId(info.getUrl().replaceAll("^\\D+(\\d+)\\D*", "$1"));
        JSONObject avatarObject = subObject.optJSONObject("avatar");
        if (avatarObject != null) {
            info.setAvatar(avatarObject.optString("large").replaceAll("\\?.*$", ""));
        }
        return info;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UserInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.ukey);
        dest.writeString(this.title);
        dest.writeString(this.nickname);
        dest.writeString(this.introduction);
        dest.writeString(this.avatar);
        dest.writeString(this.date_created);
        dest.writeString(this.url);
    }

    protected UserInfo(Parcel in) {
        this.id = in.readString();
        this.ukey = in.readString();
        this.title = in.readString();
        this.nickname = in.readString();
        this.introduction = in.readString();
        this.avatar = in.readString();
        this.date_created = in.readString();
        this.url = in.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}
