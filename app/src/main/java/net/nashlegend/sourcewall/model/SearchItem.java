package net.nashlegend.sourcewall.model;

import android.os.Parcel;
import android.text.TextUtils;
import android.webkit.URLUtil;

import org.jsoup.nodes.Element;

/**
 * Created by NashLegend on 16/4/26.
 */
public class SearchItem extends AceModel {
    private String title = "";
    private String summary = "";
    private String from = "";
    private String url = "";
    private String datetime = "";

    public SearchItem() {

    }

    public static SearchItem fromHtml(Element element) throws Exception {
        Element item = element.child(0);
        SearchItem searchItem = new SearchItem();
        searchItem.url = item.attr("href");
        if (!TextUtils.isEmpty(searchItem.url) && !URLUtil.isNetworkUrl(searchItem.url)) {
            if (!searchItem.url.startsWith("/")) {
                searchItem.url = "/" + searchItem.url;
            }
            searchItem.url = "http://m.guokr.com" + searchItem.url;
        }
        searchItem.title = item.child(0).text();
        searchItem.summary = item.child(1).text();
        searchItem.from = element.child(1).child(0).text();
        searchItem.datetime = element.child(1).child(1).attr("datetime");
        return searchItem;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
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
        dest.writeString(this.datetime);
    }

    protected SearchItem(Parcel in) {
        this.title = in.readString();
        this.summary = in.readString();
        this.from = in.readString();
        this.url = in.readString();
        this.datetime = in.readString();
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
