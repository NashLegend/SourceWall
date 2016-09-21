package net.nashlegend.sourcewall.model;

import android.os.Parcel;

import net.nashlegend.sourcewall.request.api.APIBase;
import net.nashlegend.sourcewall.util.RegUtil;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2014/9/16 0016
 */
public class Answer extends AceModel {

    private static int maxImageWidth = 240;
    private static String prefix = "<div class=\"ZoomBox\"><div class=\"content-zoom ZoomIn\">";
    private static String suffix = "</div></div>";
    private String content = "";
    private String simplifiedContent = "";
    private String date_created = "";
    private String date_modified = "";
    private Author author;
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

    public Answer() {
    }

    public static Answer fromJson(JSONObject answerObject) throws Exception {
        String hostTitle = "";
        String hostID = "";
        if (answerObject.has("question_id")) {
            hostID = answerObject.optString("question_id");
        } else {
            JSONObject questionObject = answerObject.optJSONObject("question");
            if (questionObject != null) {
                hostID = questionObject.optString("id");
                hostTitle = questionObject.optString("question");
            }
        }
        String id = answerObject.optString("id");
        boolean current_user_has_supported = answerObject.optBoolean("current_user_has_supported");
        boolean current_user_has_buried = answerObject.optBoolean("current_user_has_buried");
        boolean current_user_has_thanked = answerObject.optBoolean("current_user_has_thanked");
        boolean current_user_has_opposed = answerObject.optBoolean("current_user_has_opposed");
        JSONObject authorObject = answerObject.optJSONObject("author");
        String date_created = APIBase.parseDate(answerObject.optString("date_created"));
        String date_modified = APIBase.parseDate(answerObject.optString("date_modified"));
        int replies_count = answerObject.optInt("replies_count");
        int supportings_count = answerObject.optInt("supportings_count");
        int opposings_count = answerObject.optInt("opposings_count");
        String content = answerObject.optString("html");

        Answer answer = new Answer();
        answer.setAuthor(Author.fromJson(authorObject));
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

    public static Answer fromListJson(JSONObject answerObject) throws Exception {
        //取不到title
        String hostID = "";
        if (answerObject.has("question_id")) {
            hostID = answerObject.optString("question_id");
        } else {
            JSONObject questionObject = answerObject.optJSONObject("question");
            if (questionObject != null) {
                hostID = questionObject.optString("id");
            }
        }
        String id = answerObject.optString("id");
        boolean current_user_has_supported = answerObject.optBoolean("current_user_has_supported");
        boolean current_user_has_buried = answerObject.optBoolean("current_user_has_buried");
        boolean current_user_has_thanked = answerObject.optBoolean("current_user_has_thanked");
        boolean current_user_has_opposed = answerObject.optBoolean("current_user_has_opposed");
        JSONObject authorObject = answerObject.optJSONObject("author");
        String date_created = APIBase.parseDate(answerObject.optString("date_created"));
        String date_modified = APIBase.parseDate(answerObject.optString("date_modified"));
        int replies_count = answerObject.optInt("replies_count");
        int supportings_count = answerObject.optInt("supportings_count");
        int opposings_count = answerObject.optInt("opposings_count");
        String content = answerObject.optString("html");

        Answer answer = new Answer();
        answer.setAuthor(Author.fromJson(authorObject));
        answer.setCommentNum(replies_count);
        answer.setContent(content.replaceAll("<img .*?/>", prefix + "$0" + suffix).replaceAll("style=\"max-width: \\d+px\"", "style=\"max-width: " + maxImageWidth + "px\""));
        answer.setSimplifiedContent(RegUtil.tryGetStringByLength(RegUtil.html2PlainTextWithImageTag(answer.getContent()), 100));
        answer.setDate_created(date_created);
        answer.setDate_modified(date_modified);
        answer.setHasDownVoted(current_user_has_opposed);
        answer.setHasBuried(current_user_has_buried);
        answer.setHasUpVoted(current_user_has_supported);
        answer.setHasThanked(current_user_has_thanked);
        answer.setID(id);
        answer.setQuestionID(hostID);
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

    public Author getAuthor() {
        if (author == null) {
            author = new Author();
        }
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
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

    public String getSimplifiedContent() {
        return simplifiedContent;
    }

    public void setSimplifiedContent(String simplifiedContent) {
        this.simplifiedContent = simplifiedContent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeString(this.simplifiedContent);
        dest.writeString(this.date_created);
        dest.writeString(this.date_modified);
        dest.writeParcelable(this.author, flags);
        dest.writeString(this.ID);
        dest.writeString(this.questionID);
        dest.writeString(this.question);
        dest.writeInt(this.commentNum);
        dest.writeInt(this.upvoteNum);
        dest.writeInt(this.downvoteNum);
        dest.writeByte(this.hasUpVoted ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasDownVoted ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasBuried ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasThanked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isContentComplex ? (byte) 1 : (byte) 0);
    }

    protected Answer(Parcel in) {
        this.content = in.readString();
        this.simplifiedContent = in.readString();
        this.date_created = in.readString();
        this.date_modified = in.readString();
        this.author = in.readParcelable(Author.class.getClassLoader());
        this.ID = in.readString();
        this.questionID = in.readString();
        this.question = in.readString();
        this.commentNum = in.readInt();
        this.upvoteNum = in.readInt();
        this.downvoteNum = in.readInt();
        this.hasUpVoted = in.readByte() != 0;
        this.hasDownVoted = in.readByte() != 0;
        this.hasBuried = in.readByte() != 0;
        this.hasThanked = in.readByte() != 0;
        this.isContentComplex = in.readByte() != 0;
    }

    public static final Creator<Answer> CREATOR = new Creator<Answer>() {
        @Override
        public Answer createFromParcel(Parcel source) {
            return new Answer(source);
        }

        @Override
        public Answer[] newArray(int size) {
            return new Answer[size];
        }
    };
}
