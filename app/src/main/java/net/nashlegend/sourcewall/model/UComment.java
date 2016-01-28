package net.nashlegend.sourcewall.model;

import net.nashlegend.sourcewall.request.api.APIBase;
import net.nashlegend.sourcewall.swrequest.JsonHandler;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2014/9/16 0016
 */
public class UComment extends AceModel {

    private String content = "";
    private String date = "";
    private String author = "";
    private boolean authorExists = true;
    private String authorAvatarUrl = "";
    private String authorTitle = "";
    private String floor = "";// 楼层
    private String authorID = "";
    private String ID = "";
    private String hostID = "";
    private String hostTitle = "";
    private int likeNum = 0;

    private boolean hasLiked = false;

    public static UComment fromArticleJson(String article_id, String article_title, JSONObject replyObject) throws Exception {
        UComment comment = new UComment();
        assert replyObject != null;
        String id = replyObject.optString("id");
        boolean hasLiked = replyObject.optBoolean("current_user_has_liked");
        String date = APIBase.parseDate(replyObject.optString("date_created"));
        int likeNum = replyObject.optInt("likings_count");
        String content = replyObject.optString("html");
        JSONObject authorObject = APIBase.getJsonObject(replyObject, "author");
        boolean is_exists = authorObject.optBoolean("is_exists");
        if (is_exists) {
            String author = authorObject.optString("nickname");
            String authorID = authorObject.optString("url").replaceAll("\\D+", "");
            String authorTitle = authorObject.optString("title");
            JSONObject avatarObject = APIBase.getJsonObject(authorObject, "avatar");
            String avatarUrl = avatarObject.optString("large").replaceAll("\\?.*$", "");
            comment.setAuthor(author);
            comment.setAuthorTitle(authorTitle);
            comment.setAuthorID(authorID);
            comment.setAuthorAvatarUrl(avatarUrl);
        } else {
            comment.setAuthor("此用户不存在");
        }
        comment.setHostID(article_id);
        comment.setHostTitle(article_title);
        comment.setDate(date);
        comment.setHasLiked(hasLiked);
        comment.setLikeNum(likeNum);
        comment.setContent(content);
        comment.setID(id);
        return comment;
    }

    public static UComment fromPostJson(JSONObject replyObject) throws Exception {
        UComment comment = new UComment();
        String rid = replyObject.getString("id");
        JSONObject postObject = APIBase.getJsonObject(replyObject, "post");
        String hostTitle = postObject.getString("title");
        String hostID = postObject.getString("id");
        boolean hasLiked = replyObject.optBoolean("current_user_has_liked");
        String floor = replyObject.getString("level");
        String date = APIBase.parseDate(replyObject.getString("date_created"));
        int likeNum = replyObject.optInt("likings_count");
        String content = replyObject.optString("html");

        JSONObject authorObject = APIBase.getJsonObject(replyObject, "author");
        boolean is_exists = authorObject.optBoolean("is_exists");
        if (is_exists) {
            String author = authorObject.optString("nickname");
            String authorID = authorObject.optString("url").replaceAll("\\D+", "");
            String authorTitle = authorObject.optString("title");
            JSONObject avatarObject = APIBase.getJsonObject(authorObject, "avatar");
            String avatarUrl = avatarObject.optString("large").replaceAll("\\?.*$", "");

            comment.setAuthor(author);
            comment.setAuthorTitle(authorTitle);
            comment.setAuthorID(authorID);
            comment.setAuthorAvatarUrl(avatarUrl);
        } else {
            comment.setAuthor("此用户不存在");
        }

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

    public static UComment fromAnswerJson(JSONObject jsonObject)throws Exception{
        UComment comment = new UComment();
        JSONObject authorObject = JsonHandler.getJsonObject(jsonObject, "author");
        boolean exists = authorObject.optBoolean("is_exists");
        comment.setAuthorExists(exists);
        if (exists) {
            comment.setAuthor(authorObject.optString("nickname"));
            comment.setAuthorID(authorObject.optString("url").replaceAll("\\D+", ""));
            comment.setAuthorAvatarUrl(JsonHandler.getJsonObject(authorObject, "avatar").getString("large").replaceAll("\\?.*$", ""));
        } else {
            comment.setAuthor("此用户不存在");
        }
        comment.setContent(jsonObject.optString("text"));
        comment.setDate(APIBase.parseDate(jsonObject.optString("date_created")));
        comment.setID(jsonObject.optString("id"));
        comment.setHostID(jsonObject.optString("answer_id"));
        return comment;
    }
    public static UComment fromQuestionJson(JSONObject jsonObject)throws Exception{
        UComment comment = new UComment();
        JSONObject authorObject = JsonHandler.getJsonObject(jsonObject, "author");
        boolean exists = authorObject.optBoolean("is_exists");
        comment.setAuthorExists(exists);
        if (exists) {
            comment.setAuthor(authorObject.optString("nickname"));
            comment.setAuthorID(authorObject.optString("url").replaceAll("\\D+", ""));
            comment.setAuthorAvatarUrl(JsonHandler.getJsonObject(authorObject, "avatar").getString("large").replaceAll("\\?.*$", ""));
        } else {
            comment.setAuthor("此用户不存在");
        }
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public String getAuthorTitle() {
        return authorTitle;
    }

    public void setAuthorTitle(String authorTitle) {
        this.authorTitle = authorTitle;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
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

    public boolean isAuthorExists() {
        return authorExists;
    }

    public void setAuthorExists(boolean authorExists) {
        this.authorExists = authorExists;
    }

    public String getHostTitle() {
        return hostTitle;
    }

    public void setHostTitle(String hostTitle) {
        this.hostTitle = hostTitle;
    }
}
