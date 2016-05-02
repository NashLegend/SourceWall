package net.nashlegend.sourcewall.model;

import android.os.Parcel;

import net.nashlegend.sourcewall.request.api.APIBase;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2014/9/16 0016
 */
public class UComment extends AceModel {

    private String content = "";
    private String date = "";
    private Author author;
    private String floor = "";// 楼层
    private String ID = "";
    private String hostID = "";
    private String hostTitle = "";
    private int likeNum = 0;

    private boolean hasLiked = false;

    /**
     * 缺少articleId与articleTitle
     *
     * @param replyObject
     * @return
     * @throws Exception
     */
    public static UComment fromArticleJson(JSONObject replyObject) throws Exception {
        UComment comment = new UComment();
        assert replyObject != null;
        String id = replyObject.optString("id");
        boolean hasLiked = replyObject.optBoolean("current_user_has_liked");
        String date = APIBase.parseDate(replyObject.optString("date_created"));
        int likeNum = replyObject.optInt("likings_count");
        String content = replyObject.optString("html");
        comment.setAuthor(Author.fromJson(replyObject.optJSONObject("author")));
        comment.setDate(date);
        comment.setHasLiked(hasLiked);
        comment.setLikeNum(likeNum);
        comment.setContent(content);
        comment.setID(id);
        return comment;
    }

    public static UComment fromArticleJson(String article_id, String article_title, JSONObject replyObject) throws Exception {
        UComment comment = fromArticleJson(replyObject);
        comment.setHostID(article_id);
        comment.setHostTitle(article_title);
        return comment;
    }

    public static UComment fromPostJson(JSONObject replyObject) throws Exception {
        UComment comment = new UComment();
        String rid = replyObject.getString("id");
        JSONObject postObject = replyObject.optJSONObject("post");
        String hostTitle = postObject.getString("title");
        String hostID = postObject.getString("id");
        boolean hasLiked = replyObject.optBoolean("current_user_has_liked");
        String floor = replyObject.getString("level");
        String date = APIBase.parseDate(replyObject.getString("date_created"));
        int likeNum = replyObject.optInt("likings_count");
        String content = replyObject.optString("html");
        comment.setAuthor(Author.fromJson(replyObject.optJSONObject("author")));
        comment.setHostTitle(hostTitle);
        comment.setHostID(hostID);
        comment.setHasLiked(hasLiked);
        comment.setFloor(floor + "楼");
        comment.setDate(date);
        comment.setLikeNum(likeNum);
        comment.setContent(content);
        comment.setID(rid);
        return comment;
    }

    public static UComment fromAnswerJson(JSONObject jsonObject) throws Exception {
        UComment comment = new UComment();
        comment.setAuthor(Author.fromJson(jsonObject.optJSONObject("author")));
        comment.setContent(jsonObject.optString("text"));
        comment.setDate(APIBase.parseDate(jsonObject.optString("date_created")));
        comment.setID(jsonObject.optString("id"));
        comment.setHostID(jsonObject.optString("answer_id"));
        return comment;
    }

    public static UComment fromQuestionJson(JSONObject jsonObject) throws Exception {
        UComment comment = new UComment();
        comment.setAuthor(Author.fromJson(jsonObject.optJSONObject("author")));
        comment.setContent(jsonObject.optString("text"));
        comment.setDate(APIBase.parseDate(jsonObject.optString("date_created")));
        comment.setID(jsonObject.optString("id"));
        comment.setHostID(jsonObject.optString("question_id"));
        return comment;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content.replaceAll("</noscript>", "</noscript>\n");
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getHostID() {
        return hostID;
    }

    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }

    public boolean isHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(boolean hasLiked) {
        this.hasLiked = hasLiked;
    }

    public String getHostTitle() {
        return hostTitle;
    }

    public void setHostTitle(String hostTitle) {
        this.hostTitle = hostTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeString(this.date);
        dest.writeParcelable(this.author, 0);
        dest.writeString(this.floor);
        dest.writeString(this.ID);
        dest.writeString(this.hostID);
        dest.writeString(this.hostTitle);
        dest.writeInt(this.likeNum);
        dest.writeByte(hasLiked ? (byte) 1 : (byte) 0);
    }

    public UComment() {
    }

    protected UComment(Parcel in) {
        this.content = in.readString();
        this.date = in.readString();
        this.author = in.readParcelable(Author.class.getClassLoader());
        this.floor = in.readString();
        this.ID = in.readString();
        this.hostID = in.readString();
        this.hostTitle = in.readString();
        this.likeNum = in.readInt();
        this.hasLiked = in.readByte() != 0;
    }

    public static final Creator<UComment> CREATOR = new Creator<UComment>() {
        public UComment createFromParcel(Parcel source) {
            return new UComment(source);
        }

        public UComment[] newArray(int size) {
            return new UComment[size];
        }
    };
}
