package com.example.sourcewall.model;

/**
 * Created by NashLegend on 2014/9/16 0016
 */
public class UComment extends AceModel {

    private String content = "";
    //    private CharSequence simpleHtml = "";
    private String date = "";
    private String author = "";
    private String authorAvatarUrl = "";
    private String authorTitle = "";
    private String floor = "";// 楼层
    private String authorID = "";
    private String ID = "";
    private String hostID = "";
    private int likeNum = 0;
    private boolean isContentComplex = false;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

//    public CharSequence getSimpleHtml() {
//        return simpleHtml;
//    }
//
//    public void setSimpleHtml(CharSequence simpleHtml) {
//        this.simpleHtml = simpleHtml;
//    }

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

    public boolean isContentComplex() {
        return isContentComplex;
    }

    public void setContentComplex(boolean isContentComplex) {
        this.isContentComplex = isContentComplex;
    }

}
