package net.nashlegend.sourcewall.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by NashLegend on 16/2/23.
 */
public class Author extends AceModel {
    private String id = "";
    private String name = "";
    private String title = "";
    private String avatar = "";
    private boolean exists = true;

    public static Author fromJson(JSONObject authorObject) {
        Author author = new Author();
        if (authorObject != null) {
            boolean is_exists = authorObject.optBoolean("is_exists", true);
            author.exists = is_exists;
            if (is_exists) {
                author.name = authorObject.optString("nickname");
                author.id = authorObject.optString("url").replaceAll("\\D+", "");
                author.title = authorObject.optString("title");
                JSONObject avatarObject = authorObject.optJSONObject("avatar");
                if (avatarObject != null) {
                    author.avatar = avatarObject.optString("large").replaceAll("\\?.*$", "");
                }
            } else {
                author.name = "此用户不存在";
            }
        }
        return author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public Author() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.title);
        dest.writeString(this.avatar);
        dest.writeByte(this.exists ? (byte) 1 : (byte) 0);
    }

    protected Author(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.title = in.readString();
        this.avatar = in.readString();
        this.exists = in.readByte() != 0;
    }

    public static final Creator<Author> CREATOR = new Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel source) {
            return new Author(source);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };
}
