package net.nashlegend.sourcewall.model;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/16 0016
 */
public class Article extends AceModel {

    private String id = "";
    private String title = "";
    private String url = "";
    private String imageUrl = "";
    private String author = "";
    private String authorID = "";
    private String authorAvatarUrl = "";
    private String subjectName = "";
    private String subjectKey = "";
    private String date = "";
    private int commentNum = 0;
    private int likeNum = 0;
    private String summary = "";
    private String content = "";
    private ArrayList<UComment> hotComments = new ArrayList<UComment>();
    private ArrayList<UComment> comments = new ArrayList<UComment>();
    private boolean desc = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        if (TextUtils.isEmpty(title)) {
            url = "科学人--果壳网";
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        if (!TextUtils.isEmpty(id) && TextUtils.isEmpty(url)) {
            url = "http://www.guokr.com/article/" + id + "/";
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        this.subjectKey = subjectKey;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public String getSummary() {
        if (TextUtils.isEmpty(summary)) {
            summary = getTitle();
        }
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }
}
