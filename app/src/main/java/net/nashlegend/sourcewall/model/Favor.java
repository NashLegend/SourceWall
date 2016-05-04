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
    private String basket_id = "";
    private String basketName = "";
    private String createTime = "";

    public Favor() {
        // TODO: 16/4/26
    }

    public static Favor fromJson(JSONObject jo) throws Exception {
        // TODO: 16/5/5  
        Favor favor = new Favor();
        return favor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBasketName() {
        return basketName;
    }

    public void setBasketName(String basketName) {
        this.basketName = basketName;
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
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.basket_id);
        dest.writeString(this.basketName);
        dest.writeString(this.createTime);
    }

    protected Favor(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.basket_id = in.readString();
        this.basketName = in.readString();
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
