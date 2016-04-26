package net.nashlegend.sourcewall.model;

import android.os.Parcel;

/**
 * Created by NashLegend on 16/4/26.
 */
public class FavorItem extends AceModel {
    private String title = "";
    private String summary = "";
    private String url = "";
    private String basket_id = "";

    public FavorItem() {
        // TODO: 16/4/26  
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBasket_id() {
        return basket_id;
    }

    public void setBasket_id(String basket_id) {
        this.basket_id = basket_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.summary);
        dest.writeString(this.url);
        dest.writeString(this.basket_id);
    }

    protected FavorItem(Parcel in) {
        this.title = in.readString();
        this.summary = in.readString();
        this.url = in.readString();
        this.basket_id = in.readString();
    }

    public static final Creator<FavorItem> CREATOR = new Creator<FavorItem>() {
        @Override
        public FavorItem createFromParcel(Parcel source) {
            return new FavorItem(source);
        }

        @Override
        public FavorItem[] newArray(int size) {
            return new FavorItem[size];
        }
    };
}
