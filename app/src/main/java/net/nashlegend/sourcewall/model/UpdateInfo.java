package net.nashlegend.sourcewall.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by NashLegend on 16/9/20.
 */

public class UpdateInfo implements Parcelable {
    private String versionName = "0.0";
    private int versionCode = 1;
    private String updateInfo = "";
    private String url = "";
    private long size = 1;

    public static UpdateInfo fromJson(JSONObject jsonObject) throws Exception {
        UpdateInfo info = new UpdateInfo();
        info.setUrl(jsonObject.getString("url"));
        info.setVersionCode(jsonObject.getInt("versionCode"));
        info.setVersionName(jsonObject.getString("versionName"));
        info.setUpdateInfo(jsonObject.getString("updateInfo"));
        info.setSize(jsonObject.getLong("size"));
        return info;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.versionName);
        dest.writeInt(this.versionCode);
        dest.writeString(this.updateInfo);
        dest.writeString(this.url);
        dest.writeLong(this.size);
    }

    public UpdateInfo() {
    }

    protected UpdateInfo(Parcel in) {
        this.versionName = in.readString();
        this.versionCode = in.readInt();
        this.updateInfo = in.readString();
        this.url = in.readString();
        this.size = in.readLong();
    }

    public static final Parcelable.Creator<UpdateInfo> CREATOR = new Parcelable.Creator<UpdateInfo>() {
        @Override
        public UpdateInfo createFromParcel(Parcel source) {
            return new UpdateInfo(source);
        }

        @Override
        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };
}
