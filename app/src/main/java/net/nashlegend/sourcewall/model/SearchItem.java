package net.nashlegend.sourcewall.model;

import android.os.Parcel;

/**
 * Created by NashLegend on 16/4/26.
 */
public class SearchItem extends AceModel {
    private String title = "";
    private String summary = "";
    private String from = "";
    private String url = "";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
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
        dest.writeString(this.title);
        dest.writeString(this.summary);
        dest.writeString(this.from);
        dest.writeString(this.url);
    }

    public SearchItem() {
        // TODO: 16/4/26
    }

    protected SearchItem(Parcel in) {
        this.title = in.readString();
        this.summary = in.readString();
        this.from = in.readString();
        this.url = in.readString();
    }

    public static final Creator<SearchItem> CREATOR = new Creator<SearchItem>() {
        @Override
        public SearchItem createFromParcel(Parcel source) {
            return new SearchItem(source);
        }

        @Override
        public SearchItem[] newArray(int size) {
            return new SearchItem[size];
        }
    };
}
