package net.nashlegend.sourcewall.model;

import android.os.Parcel;
import android.text.TextUtils;

import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.api.APIBase;

import org.json.JSONObject;

import java.util.ArrayList;

public class Post extends AceModel {

    private String id = "";
    private String title = "";
    private String url = "";
    private String titleImageUrl = "";
    private Author author;
    private String groupName = "";
    private String groupID = "";
    private String tag = "";
    private int likeNum = 0;
    private int replyNum = 0;
    private String content = "";
    private String date = "";
    private boolean featured = false;//是否展现在集合列表还是单个小组。true表示展示在单个小组列表中
    private boolean desc = false;
    private boolean isStick = false;//是否是置顶贴
    private ArrayList<UComment> hotComments = new ArrayList<>();
    private ArrayList<UComment> comments = new ArrayList<>();

    public Post() {
    }

    public static Post fromJson(JSONObject postResult) throws Exception {
        Post detail = new Post();
        String postID = postResult.getString("id");
        String title = postResult.getString("title");
        String date = postResult.optString("date_created");
        String content = "<div id=\"postContent\" class=\"html-text-mixin gbbcode-content\">" + postResult.optString("html") + "</div>";
        //int likeNum = getJsonInt(postResult, "");//取不到like数量
        int recommendNum = postResult.optInt("recommends_count");
        int reply_num = postResult.optInt("replies_count");
        Author author = Author.fromJson(postResult.optJSONObject("author"));
        JSONObject groupObject = JsonHandler.getJsonObject(postResult, "group");
        String groupName = groupObject.optString("name");
        String groupID = postResult.optString("group_id");
        detail.setGroupID(groupID);
        detail.setGroupName(groupName);
        detail.setAuthor(author);
        detail.setId(postID);
        detail.setTitle(title.trim());
        detail.setDate(APIBase.parseDate(date));
        detail.setContent(content);
        detail.setReplyNum(reply_num);
        detail.setStick(postResult.optBoolean("is_stick"));
        return detail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        if (TextUtils.isEmpty(title)) {
            url = "果壳小组";
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        if (!TextUtils.isEmpty(id)) {
            url = "http://www.guokr.com/post/" + id + "/";
        }
        return url;
    }

    public void setUrl(String url) {
        if (url != null && url.startsWith("http://m.guokr.com")) {
            url = url.replace("http://m.guokr.com", "http://www.guokr.com");
        }
        this.url = url;
    }

    public Author getAuthor() {
        if (author == null) {
            author = new Author();
        }
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getTitleImageUrl() {
        return titleImageUrl;
    }

    public void setTitleImageUrl(String titleImageUrl) {
        this.titleImageUrl = titleImageUrl;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }

    public int getReplyNum() {
        return replyNum;
    }

    public void setReplyNum(int replyNum) {
        this.replyNum = replyNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<UComment> getHotComments() {
        return hotComments;
    }

    public void setHotComments(ArrayList<UComment> hotComments) {
        this.hotComments = hotComments;
    }

    public ArrayList<UComment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<UComment> comments) {
        this.comments = comments;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    public boolean isStick() {
        return isStick;
    }

    public void setStick(boolean stick) {
        isStick = stick;
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
        dest.writeString(this.titleImageUrl);
        dest.writeParcelable(this.author, flags);
        dest.writeString(this.groupName);
        dest.writeString(this.groupID);
        dest.writeString(this.tag);
        dest.writeInt(this.likeNum);
        dest.writeInt(this.replyNum);
        dest.writeString(this.content);
        dest.writeString(this.date);
        dest.writeByte(this.featured ? (byte) 1 : (byte) 0);
        dest.writeByte(this.desc ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isStick ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.hotComments);
        dest.writeTypedList(this.comments);
    }

    protected Post(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.url = in.readString();
        this.titleImageUrl = in.readString();
        this.author = in.readParcelable(Author.class.getClassLoader());
        this.groupName = in.readString();
        this.groupID = in.readString();
        this.tag = in.readString();
        this.likeNum = in.readInt();
        this.replyNum = in.readInt();
        this.content = in.readString();
        this.date = in.readString();
        this.featured = in.readByte() != 0;
        this.desc = in.readByte() != 0;
        this.isStick = in.readByte() != 0;
        this.hotComments = in.createTypedArrayList(UComment.CREATOR);
        this.comments = in.createTypedArrayList(UComment.CREATOR);
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
}
