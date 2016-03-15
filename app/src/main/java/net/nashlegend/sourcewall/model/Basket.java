package net.nashlegend.sourcewall.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class Basket extends AceModel {
    private String category_id = "";
    private String category_name = "";
    private String id = "";
    private String name = "";
    private String introduction = "";
    private int links_count = 0;
    private boolean hasFavored = false;
    private boolean isFavoring = false;

    public Basket() {
    }

    public static Basket fromJson(JSONObject subObject) throws Exception {
        Basket basket = new Basket();
        basket.setId(subObject.optString("id"));
        basket.setIntroduction(subObject.optString("introduction"));
        basket.setLinks_count(subObject.optInt("links_count"));
        basket.setName(subObject.optString("title"));
        JSONObject category = subObject.optJSONObject("category");
        if (category != null) {
            basket.setCategory_id(category.optString("id"));
            basket.setCategory_name(category.optString("name"));
        }
        return basket;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
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

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public int getLinks_count() {
        return links_count;
    }

    public void setLinks_count(int links_count) {
        this.links_count = links_count;
    }

    public boolean isHasFavored() {
        return hasFavored;
    }

    public void setHasFavored(boolean hasFavored) {
        this.hasFavored = hasFavored;
    }

    public boolean isFavoring() {
        return isFavoring;
    }

    public void setFavoring(boolean isFavoring) {
        this.isFavoring = isFavoring;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.category_id);
        dest.writeString(this.category_name);
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.introduction);
        dest.writeInt(this.links_count);
        dest.writeByte(hasFavored ? (byte) 1 : (byte) 0);
        dest.writeByte(isFavoring ? (byte) 1 : (byte) 0);
    }

    protected Basket(Parcel in) {
        this.category_id = in.readString();
        this.category_name = in.readString();
        this.id = in.readString();
        this.name = in.readString();
        this.introduction = in.readString();
        this.links_count = in.readInt();
        this.hasFavored = in.readByte() != 0;
        this.isFavoring = in.readByte() != 0;
    }

    public static final Creator<Basket> CREATOR = new Creator<Basket>() {
        public Basket createFromParcel(Parcel source) {
            return new Basket(source);
        }

        public Basket[] newArray(int size) {
            return new Basket[size];
        }
    };
}
