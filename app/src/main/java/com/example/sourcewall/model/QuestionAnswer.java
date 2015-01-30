package com.example.sourcewall.model;

/**
 * Created by NashLegend on 2014/9/16 0016
 */
public class QuestionAnswer extends AceModel {

    private String content = "";
    private String date_created = "";
    private String date_modified = "";
    private boolean authorExists = true;
    private String author = "";
    private String authorAvatarUrl = "";
    private String authorID = "";
    private String authorTitle = "";
    private String ID = "";
    private String questionID = "";
    private int commentNum = 0;
    private int upvoteNum = 0;
    private boolean hasUpVoted = false;
    private boolean hasDownVoted = false;
    private boolean hasBuried = false;
    private boolean hasThanked = false;
    private boolean isContentComplex = false;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getDate_modified() {
        return date_modified;
    }

    public void setDate_modified(String date_modified) {
        this.date_modified = date_modified;
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

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getAuthorTitle() {
        return authorTitle;
    }

    public void setAuthorTitle(String authorTitle) {
        this.authorTitle = authorTitle;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getQuestionID() {
        return questionID;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public int getUpvoteNum() {
        return upvoteNum;
    }

    public void setUpvoteNum(int upvoteNum) {
        this.upvoteNum = upvoteNum;
    }

    public boolean isHasUpVoted() {
        return hasUpVoted;
    }

    public void setHasUpVoted(boolean hasUpVoted) {
        this.hasUpVoted = hasUpVoted;
    }

    public boolean isHasDownVoted() {
        return hasDownVoted;
    }

    public void setHasDownVoted(boolean hasDownVoted) {
        this.hasDownVoted = hasDownVoted;
    }

    public boolean isHasBuried() {
        return hasBuried;
    }

    public void setHasBuried(boolean hasBuried) {
        this.hasBuried = hasBuried;
    }

    public boolean isHasThanked() {
        return hasThanked;
    }

    public void setHasThanked(boolean hasThanked) {
        this.hasThanked = hasThanked;
    }

    public boolean isContentComplex() {
        return isContentComplex;
    }

    public void setContentComplex(boolean isContentComplex) {
        this.isContentComplex = isContentComplex;
    }

    public boolean isAuthorExists() {
        return authorExists;
    }

    public void setAuthorExists(boolean authorExists) {
        this.authorExists = authorExists;
    }
}
