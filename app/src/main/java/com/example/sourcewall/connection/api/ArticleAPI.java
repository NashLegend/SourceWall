package com.example.sourcewall.connection.api;

import com.example.sourcewall.connection.HttpFetcher;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.model.UComment;

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
import java.util.ArrayList;

public class ArticleAPI extends APIBase {

    public ArticleAPI() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 获取《科学人》默认列表，取20个，我发现这样动态请求比果壳首页刷新的快……
     *
     * @param offset 从第offset个开始取
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static ArrayList<Article> getArticleListIndexPage(int offset) throws JSONException, IOException {
        String url = "http://www.guokr.com/apis/minisite/article.json?retrieve_type=by_subject" + "&limit=20&offset=" + offset;
        return getArticleListFromJsonUrl(url);
    }

    /**
     * 按频道取《科学人》的文章，比如热点、前沿什么的
     *
     * @param channelKey
     * @param offset
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static ArrayList<Article> getArticleListByChannel(String channelKey, int offset) throws JSONException, IOException {
        String url = "http://www.guokr.com/apis/minisite/article.json?retrieve_type=by_channel&channel_key="
                + channelKey + "&limit=20&offset=" + offset;
        return getArticleListFromJsonUrl(url);
    }

    /**
     * 按学科取《科学人》的文章
     *
     * @param subject_key
     * @param offset
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static ArrayList<Article> getArticleListBySubject(String subject_key, int offset) throws JSONException, IOException {
        String url = "http://www.guokr.com/apis/minisite/article.json?retrieve_type=by_subject&subject_key="
                + subject_key + "&limit=20&offset=" + offset;
        return getArticleListFromJsonUrl(url);
    }

    /**
     * 根据上面几个方法生成的url去取文章列表
     *
     * @param url
     * @return
     * @throws JSONException
     * @throws IOException
     */
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
                article.setDate(parseDate(getJsonString(jo, "date_published")));
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

    /**
     * 根据文章id，解析页面获得文章内容
     *
     * @param id
     * @return
     * @throws IOException
     */
    public static Article getArticleDetailByID(String id) throws IOException {
        return getArticleDetailByUrl("http://www.guokr.com/article/" + id + "/");
    }

    /**
     * 直接解析页面获得文章内容
     *
     * @param url
     */
    public static Article getArticleDetailByUrl(String url) throws IOException {
        Article article = new Article();
        String aid = url.replaceAll("\\?\\S*$", "").replaceAll("\\D+", "");
        String html = HttpFetcher.get(url);
        Document doc = Jsoup.parse(html);
        //replaceAll("line-height: normal;","");只是简单的处理，以防止Article样式不正确，字体过于紧凑
        //可能还有其他样式没有被我发现，所以加一个 TODO
        String articleContent = doc.getElementById("articleContent").outerHtml().replaceAll("line-height: normal;", "");
        String copyright = doc.getElementsByClass("copyright").outerHtml();
        article.setContent(articleContent + copyright);

        int likeNum = Integer.valueOf(doc.getElementsByClass("recom-num").get(0).text()
                .replaceAll("\\D+", ""));
        // 其他数据已经在列表取得，这里只要合过去就行了
        article.setLikeNum(likeNum);
        Elements elements = doc.getElementsByClass("cmts-list");
        if (elements != null && elements.size() > 0) {
            // hot
            article.setHotComments(getArticleHotComments(elements.get(0), aid));
        }
        return article;
    }

    /**
     * 解析html获得文章热门评论
     *
     * @param hotElement
     * @param aid
     * @return
     */
    public static ArrayList<UComment> getArticleHotComments(Element hotElement, String aid) {
        ArrayList<UComment> list = new ArrayList<>();
        Elements comments = hotElement.getElementsByTag("li");
        if (comments != null && comments.size() > 0) {
            for (int i = 0; i < comments.size(); i++) {
                Element element = comments.get(i);
                UComment comment = new UComment();
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
                Elements tmpElements = element.getElementsByClass("cmt-auth");
                if (tmpElements != null && tmpElements.size() > 0) {
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

    /**
     * 获取文章评论，json格式
     *
     * @param id
     * @param offset
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static ArrayList<UComment> getArticleComments(String id, int offset) throws IOException, JSONException {
        ArrayList<UComment> list = new ArrayList<>();
        String url = "http://apis.guokr.com/minisite/article_reply.json?article_id=" + id
                + "&limit=20&offset=" + offset;
        String jString = HttpFetcher.get(url);
        JSONObject jss = new JSONObject(jString);
        boolean ok = jss.getBoolean("ok");
        if (ok) {
            JSONArray articles = jss.getJSONArray("result");
            for (int i = 0; i < articles.length(); i++) {
                JSONObject jo = articles.getJSONObject(i);
                UComment comment = new UComment();
                comment.setID(getJsonString(jo, "id"));
                comment.setLikeNum(jo.getInt("likings_count"));
                comment.setAuthor(getJsonString(getJsonObject(jo, "author"), "nickname"));
                comment.setAuthorID(getJsonString(getJsonObject(jo, "author"), "url")
                        .replaceAll("\\D+", ""));
                comment.setAuthorAvatarUrl(jo.getJSONObject("author").getJSONObject("avatar")
                        .getString("large").replaceAll("\\?\\S*$", ""));
                comment.setAuthorTitle(getJsonString(getJsonObject(jo, "author"), "title"));
                //Date  TODO
                comment.setDate(parseDate(getJsonString(jo, "date_created")));
                comment.setFloor((offset + i + 1) + "楼");
                comment.setContent(getJsonString(jo, "html"));
                comment.setHostID(id);
                list.add(comment);
            }
        }
        return list;
    }

    /**
     * 推荐文章
     *
     * @param articleID
     * @param title
     * @param summary
     * @param comment
     * @return
     */
    public static ResultObject recommendArticle(String articleID, String title, String summary, String comment) {
        String articleUrl = "http://www.guokr.com/article/" + articleID + "/";
        return UserAPI.recommendLink(articleUrl, title, summary, comment);
    }

    /**
     * 赞一个文章评论
     *
     * @param id
     * @return
     */
    public static ResultObject likeComment(String id) {
        String url = "http://www.guokr.com/apis/minisite/article_reply_liking.json";
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("reply_id", id));
            pairs.add(new BasicNameValuePair("access_token", UserAPI.getToken()));
            String result = HttpFetcher.post(url, pairs);
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                resultObject.ok = true;
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 使用json请求回复文章
     *
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
            pairs.add(new BasicNameValuePair("access_token", UserAPI.getToken()));
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

    /**
     * 使用网页请求而不是json来获得结果，可以使用高级样式 TODO
     *
     * @param id
     * @param content
     * @return ResultObject.result is the reply_id if ok;
     */
    public static ResultObject replyArticleAdvanced(String id, String content) {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://apis.guokr.com/minisite/article_reply.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("article_id", id));
            pairs.add(new BasicNameValuePair("content", content));
            pairs.add(new BasicNameValuePair("access_token", UserAPI.getToken()));
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
