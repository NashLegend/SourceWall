package net.nashlegend.sourcewall.model;

import android.os.Parcel;
import android.text.TextUtils;

import net.nashlegend.sourcewall.request.api.APIBase;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/16 0016
 */
public class Article extends AceModel {

    private String id = "";
    private String title = "";
    private String url = "";
    private String imageUrl = "";
    private Author author;
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

    public static Article fromJsonSimple(JSONObject jo) throws Exception {
        Article article = new Article();
        article.setId(jo.optString("id"));
        article.setCommentNum(jo.optInt("replies_count"));
        article.setAuthor(Author.fromJson(jo.optJSONObject("author")));
        article.setDate(APIBase.parseDate(jo.optString("date_published")));
        article.setSubjectName(jo.optJSONObject("subject").optString("name"));
        article.setSubjectKey(jo.optJSONObject("subject").optString("key"));
        article.setUrl(jo.optString("url"));
        article.setImageUrl(jo.optString("small_image"));
        article.setSummary(jo.optString("summary"));
        article.setTitle(jo.optString("title").trim());
        return article;
    }

    public static Article fromHtmlDetail(String id, String html) throws Exception {
        Article article = new Article();
        Document doc = Jsoup.parse(html);
        //replaceAll("line-height: normal;","");只是简单的处理，以防止Article样式不正确，字体过于紧凑
        //可能还有其他样式没有被我发现，所以加一个 TODO
        String articleContent = doc.getElementById("articleContent").outerHtml().replaceAll("line-height: normal;", "");
        String copyright = doc.getElementsByClass("copyright").outerHtml();
        article.setContent(articleContent + copyright);
        int likeNum = Integer.valueOf(doc.getElementsByClass("recom-num").get(0).text().replaceAll("\\D+", ""));
        // 其他数据已经在列表取得，按理说这里只要合过去就行了，
        // 但是因为有可能从其他地方进入这个页面，所以下面的数据还是要取
        // 但是可以尽量少取，因为很多数据基本已经用不到了
        article.setId(id);
        Elements infos = doc.getElementsByClass("content-th-info");
        if (infos != null && infos.size() == 1) {
            Element info = infos.get(0);
            Elements infoSubs = info.getElementsByTag("a");//记得见过不是a的
            if (infoSubs != null && infoSubs.size() > 0) {
                Author author = new Author();
                author.setName(info.getElementsByTag("a").text());
                //href有可能为空且id在article里面没有什么用，所以不解析了
                //String authorId = info.getElementsByTag("a").attr("href").replaceAll("\\D+", "");
                //article.setAuthorID(authorId);
                article.setAuthor(author);
            }
            Elements meta = info.getElementsByTag("meta");
            if (meta != null && meta.size() > 0) {
                String date = APIBase.parseDate(info.getElementsByTag("meta").attr("content"));
                article.setDate(date);
            }
        }
        // String num = doc.select(".cmts-title").select(".cmts-hide").get(0).getElementsByClass("gfl").get(0).text().replaceAll("\\D+", "");
        // article.setCommentNum(Integer.valueOf(num));
        article.setTitle(doc.getElementById("articleTitle").text().trim());
        //            article.setLikeNum(likeNum);
        return article;
    }

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

    public Author getAuthor() {
        if (author == null) {
            author = new Author();
        }
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
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

    public Article() {
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
        dest.writeString(this.imageUrl);
        dest.writeParcelable(this.author, 0);
        dest.writeString(this.subjectName);
        dest.writeString(this.subjectKey);
        dest.writeString(this.date);
        dest.writeInt(this.commentNum);
        dest.writeInt(this.likeNum);
        dest.writeString(this.summary);
        dest.writeString(this.content);
        dest.writeTypedList(hotComments);
        dest.writeTypedList(comments);
        dest.writeByte(desc ? (byte) 1 : (byte) 0);
    }

    protected Article(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.url = in.readString();
        this.imageUrl = in.readString();
        this.author = in.readParcelable(Author.class.getClassLoader());
        this.subjectName = in.readString();
        this.subjectKey = in.readString();
        this.date = in.readString();
        this.commentNum = in.readInt();
        this.likeNum = in.readInt();
        this.summary = in.readString();
        this.content = in.readString();
        this.hotComments = in.createTypedArrayList(UComment.CREATOR);
        this.comments = in.createTypedArrayList(UComment.CREATOR);
        this.desc = in.readByte() != 0;
    }

    public static final Creator<Article> CREATOR = new Creator<Article>() {
        public Article createFromParcel(Parcel source) {
            return new Article(source);
        }

        public Article[] newArray(int size) {
            return new Article[size];
        }
    };
}
