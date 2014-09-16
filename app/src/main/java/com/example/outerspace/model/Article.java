package com.example.outerspace.model;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/16 0016.
 */
public class Article {

    private String id = "";
    private String title = "";
    private String url = "";
    private String imageUrl = "";
    private String author = "";
    private String authorID = "";
    private String subjectName = "";
    private String subjectKey="";
    private String date="";
    private int commentNum = 0;
    private String summary="";
    private String content = "";
    private ArrayList<ArticleComment> hotComments = new ArrayList<ArticleComment>();
    private ArrayList<ArticleComment> comments = new ArrayList<ArticleComment>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
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

    public ArrayList<ArticleComment> getHotComments() {
        return hotComments;
    }

    public void setHotComments(ArrayList<ArticleComment> hotComments) {
        this.hotComments = hotComments;
    }

    public ArrayList<ArticleComment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<ArticleComment> comments) {
        this.comments = comments;
    }
}
