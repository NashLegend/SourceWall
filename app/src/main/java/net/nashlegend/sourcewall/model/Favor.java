package net.nashlegend.sourcewall.model;

import android.os.Parcel;

import net.nashlegend.sourcewall.request.api.APIBase;

import org.json.JSONObject;

/**
 * Created by NashLegend on 16/4/26.
 */
public class Favor extends AceModel {
    private String id = "";
    private String title = "";
    private String url = "";
    private String createTime = "";

    public Favor() {
        // TODO: 16/4/26
    }

    public static Favor fromJson(JSONObject jo) throws Exception {
        // TODO: 16/5/5
        Favor favor = new Favor();
        favor.setId(jo.optString("id"));
        favor.setTitle(jo.optString("title"));
        favor.setUrl(jo.optString("url"));
        favor.setCreateTime(APIBase.parseDate(jo.optString("date_created")));
        return favor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.url);
        dest.writeString(this.createTime);
    }

    protected Favor(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.url = in.readString();
        this.createTime = in.readString();
    }

    public static final Creator<Favor> CREATOR = new Creator<Favor>() {
        @Override
        public Favor createFromParcel(Parcel source) {
            return new Favor(source);
        }

        @Override
        public Favor[] newArray(int size) {
            return new Favor[size];
        }
    };
}
