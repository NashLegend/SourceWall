package net.nashlegend.sourcewall.model;

import android.text.TextUtils;

import net.nashlegend.sourcewall.request.api.APIBase;
import net.nashlegend.sourcewall.swrequest.JsonHandler;

import org.json.JSONObject;

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
    private String question = "";
    private int commentNum = 0;
    private int upvoteNum = 0;
    private int downvoteNum = 0;
    private boolean hasUpVoted = false;
    private boolean hasDownVoted = false;
    private boolean hasBuried = false;
    private boolean hasThanked = false;
    private boolean isContentComplex = false;

    private static int maxImageWidth = 240;
    private static String prefix = "<div class=\"ZoomBox\"><div class=\"content-zoom ZoomIn\">";
    private static String suffix = "</div></div>";

    public static QuestionAnswer fromJson(JSONObject answerObject)throws Exception{
        JSONObject questionObject = APIBase.getJsonObject(answerObject, "question");
        String hostTitle = questionObject.optString("question");
        String hostID = questionObject.optString("id");
        if (TextUtils.isEmpty(hostID)){
            hostID = answerObject.optString("question_id");
        }

        String id = answerObject.optString("id");

        boolean current_user_has_supported = answerObject.optBoolean("current_user_has_supported");
        boolean current_user_has_buried = answerObject.optBoolean("current_user_has_buried");
        boolean current_user_has_thanked = answerObject.optBoolean("current_user_has_thanked");
        boolean current_user_has_opposed = answerObject.optBoolean("current_user_has_opposed");

        JSONObject authorObject = JsonHandler.getJsonObject(answerObject, "author");
        String author = authorObject.optString("nickname");
        String authorTitle = authorObject.optString("title");
        String authorID = authorObject.optString("url").replaceAll("\\D+", "");
        boolean is_exists = authorObject.optBoolean("is_exists");
        JSONObject avatarObject = JsonHandler.getJsonObject(authorObject, "avatar");
        String avatarUrl = avatarObject.optString("large").replaceAll("\\?.*$", "");

        String date_created = APIBase.parseDate(answerObject.optString("date_created"));
        String date_modified = APIBase.parseDate(answerObject.optString("date_modified"));
        int replies_count = answerObject.optInt("replies_count");
        int supportings_count = answerObject.optInt("supportings_count");
        int opposings_count = answerObject.optInt("opposings_count");
        String content = answerObject.optString("html");

        QuestionAnswer answer = new QuestionAnswer();
        answer.setAuthorExists(is_exists);
        if (is_exists) {
            answer.setAuthor(author);
            answer.setAuthorTitle(authorTitle);
            answer.setAuthorID(authorID);
            answer.setAuthorAvatarUrl(avatarUrl);
        } else {
            answer.setAuthor("此用户不存在");
        }
        answer.setCommentNum(replies_count);
        answer.setContent(content.replaceAll("<img .*?/>", prefix + "$0" + suffix).replaceAll("style=\"max-width: \\d+px\"", "style=\"max-width: " + maxImageWidth + "px\""));
        answer.setDate_created(date_created);
        answer.setDate_modified(date_modified);
        answer.setHasDownVoted(current_user_has_opposed);
        answer.setHasBuried(current_user_has_buried);
        answer.setHasUpVoted(current_user_has_supported);
        answer.setHasThanked(current_user_has_thanked);
        answer.setID(id);
        answer.setQuestionID(hostID);
        answer.setQuestion(hostTitle);
        answer.setUpvoteNum(supportings_count);
        answer.setDownvoteNum(opposings_count);
        return answer;
    }

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

    public int getDownvoteNum() {
        return downvoteNum;
    }

    public void setDownvoteNum(int downvoteNum) {
        this.downvoteNum = downvoteNum;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
