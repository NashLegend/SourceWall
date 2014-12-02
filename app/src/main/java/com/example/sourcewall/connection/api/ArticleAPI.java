package com.example.sourcewall.connection.api;

import com.example.sourcewall.AppApplication;
import com.example.sourcewall.connection.HttpFetcher;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.model.SimpleComment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ArticleAPI extends APIBase {

    public ArticleAPI() {
        // TODO Auto-generated constructor stub
    }

    public static ArrayList<Article> getArticleListIndexPage(int offset) throws JSONException, IOException {
        String url = "http://www.guokr.com/apis/minisite/article.json?retrieve_type=by_subject" + "&limit=20&offset=" + offset;
        return getArticleListFromJsonUrl(url);
    }

    public static ArrayList<Article> getArticleListByChannel(String channelKey, int offset) throws JSONException, IOException {
        String url = "http://www.guokr.com/apis/minisite/article.json?retrieve_type=by_channel&channel_key="
                + channelKey + "&limit=20&offset=" + offset;
        return getArticleListFromJsonUrl(url);
    }

    public static ArrayList<Article> getArticleListBySubject(String subject_key, int offset) throws JSONException, IOException {
        String url = "http://www.guokr.com/apis/minisite/article.json?retrieve_type=by_subject&subject_key="
                + subject_key + "&limit=20&offset=" + offset;
        return getArticleListFromJsonUrl(url);
    }

    private static ArrayList<Article> getArticleListFromJsonUrl(String url) throws JSONException, IOException {
        ArrayList<Article> articleList = new ArrayList<Article>();
        String jString = HttpFetcher.get(url);
        JSONObject jss = new JSONObject(jString);
        boolean ok = jss.getBoolean("ok");
        if (ok) {
            JSONArray articles = jss.getJSONArray("result");
            for (int i = 0; i < articles.length(); i++) {
                JSONObject jo = articles.getJSONObject(i);
                Article article = new Article();
                article.setId(getJsonString(jo, "id"));
                article.setCommentNum(jo.getInt("replies_count"));
                article.setAuthor(getJsonString(getJsonObject(jo, "author"), "nickname"));
                article.setAuthorID(getJsonString(getJsonObject(jo, "author"), "url")
                        .replaceAll("\\D+", ""));
                article.setAuthorAvatarUrl(jo.getJSONObject("author").getJSONObject("avatar")
                        .getString("large").replaceAll("\\?\\S*$", ""));
                String dateString = getJsonString(jo, "date_published");
                dateString = dateString.replace("T", " ");
                dateString = dateString.replaceAll("\\+\\S+$", "");
                article.setDate(dateString);

                article.setSubjectName(getJsonString(getJsonObject(jo, "subject"), "name"));
                article.setSubjectKey(getJsonString(getJsonObject(jo, "subject"), "key"));
                article.setUrl(getJsonString(jo, "url"));
                article.setImageUrl(getJsonString(jo, "small_image"));
                article.setSummary(getJsonString(jo, "summary"));
                article.setTitle(getJsonString(jo, "title"));
                articleList.add(article);
            }
        }
        return articleList;
    }

    public static Article getArticleDetailByID(String id) throws IOException {
        return getArticleDetailByUrl("http://www.guokr.com/article/" + id + "/");
    }

    /**
     * 仅限第一页
     *
     * @param url
     */
    public static Article getArticleDetailByUrl(String url) throws IOException {
        Article article = new Article();
        String aid = url.replaceAll("\\?\\S*$", "").replaceAll("\\D+", "");
        Document doc = Jsoup.parse(HttpFetcher.get(url));
        String content = doc.getElementsByClass("document").outerHtml();
        int likeNum = Integer.valueOf(doc.getElementsByClass("recom-num").get(0).text()
                .replaceAll("\\D+", ""));
        // 其他数据已经在列表取得，这里只要合过去就行了
        article.setContent(content);
        article.setLikeNum(likeNum);
        Elements elements = doc.getElementsByClass("cmts-list");
        if (elements != null && elements.size() > 0) {
            // hot
            article.setHotComments(getArticleHotComments(elements.get(0), aid));
        }
        return article;
    }

    public static ArrayList<SimpleComment> getArticleHotComments(Element hotElement, String aid) {
        ArrayList<SimpleComment> list = new ArrayList<SimpleComment>();
        Elements comments = hotElement.getElementsByTag("li");
        if (comments != null && comments.size() > 0) {
            for (int i = 0; i < comments.size(); i++) {
                Element element = comments.get(i);
                SimpleComment comment = new SimpleComment();
                String id = element.id().replace("reply", "");
                Element tmp = element.select(".cmt-img").select(".cmtImg").select(".pt-pic").get(0);

                String authorID = tmp.getElementsByTag("a").get(0).attr("href")
                        .replaceAll("\\D+", "");
                String authorAvatarUrl = tmp.getElementsByTag("img").get(0).attr("src")
                        .replaceAll("\\?\\S*$", "");
                String author = tmp.getElementsByTag("a").get(0).attr("title");
                String likeNum = element.getElementsByClass("cmt-do-num").get(0).text();
                String date = element.getElementsByClass("cmt-info").get(0).text();
                String content = element.select(".cmt-content").select(".gbbcode-content")
                        .select(".cmtContent").get(0).outerHtml();
                Elements tmpelements = element.getElementsByClass("cmt-auth");
                if (tmpelements != null && tmpelements.size() > 0) {
                    String authorTitle = element.getElementsByClass("cmt-auth").get(0)
                            .attr("title");
                    comment.setAuthorTitle(authorTitle);
                }
                comment.setID(id);
                comment.setLikeNum(Integer.valueOf(likeNum));
                comment.setAuthor(author);
                comment.setAuthorID(authorID);
                comment.setAuthorAvatarUrl(authorAvatarUrl);
                comment.setDate(date);
                comment.setContent(content);
                comment.setHostID(aid);
                list.add(comment);
            }
        }
        return list;
    }

    public static ArrayList<SimpleComment> getArticleComments(String id, int offset) throws IOException, JSONException {
        ArrayList<SimpleComment> list = new ArrayList<SimpleComment>();
        String url = "http://apis.guokr.com/minisite/article_reply.json?article_id=" + id
                + "&limit=20&offset=" + offset;
        String jString = HttpFetcher.get(url);
        JSONObject jss = new JSONObject(jString);
        boolean ok = jss.getBoolean("ok");
        if (ok) {
            JSONArray articles = jss.getJSONArray("result");
            for (int i = 0; i < articles.length(); i++) {
                JSONObject jo = articles.getJSONObject(i);
                SimpleComment comment = new SimpleComment();
                comment.setID(getJsonString(jo, "id"));
                comment.setLikeNum(jo.getInt("likings_count"));
                comment.setAuthor(getJsonString(getJsonObject(jo, "author"), "nickname"));
                comment.setAuthorID(getJsonString(getJsonObject(jo, "author"), "url")
                        .replaceAll("\\D+", ""));
                comment.setAuthorAvatarUrl(jo.getJSONObject("author").getJSONObject("avatar")
                        .getString("large").replaceAll("\\?\\S*$", ""));
                comment.setAuthorTitle(getJsonString(getJsonObject(jo, "author"), "title"));
                String dateString = getJsonString(jo, "date_created");
                dateString = dateString.replace("T", " ");
                dateString = dateString.replaceAll("\\.\\S+$", "");
                comment.setDate(dateString);
                comment.setFloor((offset + i + 1) + "楼");
                comment.setContent(getJsonString(jo, "html"));
                comment.setHostID(id);
                list.add(comment);
            }
        }
        return list;
    }

    public static ResultObject recommendArticle(String articleID, String title, String summary, String comment) {
        String url = "http://www.guokr.com/apis/community/user/recommend.json";
        ResultObject resultObject = new ResultObject();
        try {
            String articleUrl = "http://www.guokr.com/article/" + articleID + "/";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("title", title));
            pairs.add(new BasicNameValuePair("url", articleUrl));
            pairs.add(new BasicNameValuePair("summary", summary));
            pairs.add(new BasicNameValuePair("comment", comment));
            pairs.add(new BasicNameValuePair("target", "activity"));
            pairs.add(new BasicNameValuePair("access_token", UserAPI.getToken()));
            String result = HttpFetcher.post(url, pairs);
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                resultObject.ok = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    public static ResultObject collectArticle(String articleID, String title, String basketID) {
        String url = "http://www.guokr.com/apis/favorite/link.json";
        String param = "basket_id=#&url=#&title=#&access_token=#";
        return null;
    }

    public static ResultObject likeComment(String id) {
        String url = "http://www.guokr.com/apis/minisite/article_reply_liking.json";
        String param = "reply_id=#&access_token=#";
        return null;
    }

    /**
     * @param id
     * @param content
     * @return ResultObject.result is the reply_id if ok;
     */
    public static ResultObject replyArticle(String id, String content) {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://apis.guokr.com/minisite/article_reply.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("article_id", id));
            pairs.add(new BasicNameValuePair("content", content));
            pairs.add(new BasicNameValuePair("access_token", AppApplication.tokenString));
            String result = HttpFetcher.post(url, pairs);
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                JSONObject resultJson = getJsonObject(object, "result");
                String replyID = getJsonString(resultJson, "id");
                resultObject.ok = true;
                resultObject.result = replyID;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }


}
