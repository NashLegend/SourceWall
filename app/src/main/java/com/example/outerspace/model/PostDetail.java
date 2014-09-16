package com.example.outerspace.model;
import java.util.ArrayList;

public class PostDetail {

    private String id = "";
    private String content = "";
    private String date = "";
    private String title = "";
    private String authorAvatarUrl = "";
    private String author = "";
    private String authorID = "";
    private String groupName = "";
    private String groupID = "";
    private int likeNum = 0;
    private int commentNum = 0;
    private ArrayList<PostComment> hotComments = new ArrayList<PostComment>();
    private ArrayList<PostComment> comments = new ArrayList<PostComment>();

    public PostDetail() {
        // TODO Auto-generated constructor stub
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
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

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public ArrayList<PostComment> getHotComments() {
        return hotComments;
    }

    public void setHotComments(ArrayList<PostComment> hotComments) {
        this.hotComments = hotComments;
    }

    public ArrayList<PostComment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<PostComment> comments) {
        this.comments = comments;
    }

}
