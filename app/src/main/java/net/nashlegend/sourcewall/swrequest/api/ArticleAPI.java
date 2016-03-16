package net.nashlegend.sourcewall.swrequest.api;

import android.text.TextUtils;

import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.Author;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.HttpFetcher;
import net.nashlegend.sourcewall.swrequest.cache.RequestCache;
import net.nashlegend.sourcewall.swrequest.JsonHandler;
import net.nashlegend.sourcewall.swrequest.RequestBuilder;
import net.nashlegend.sourcewall.swrequest.RequestObject;
import net.nashlegend.sourcewall.swrequest.RequestObject.CallBack;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.swrequest.parsers.BooleanParser;
import net.nashlegend.sourcewall.swrequest.parsers.ContentValueForKeyParser;
import net.nashlegend.sourcewall.swrequest.parsers.Parser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 暂无单个回复地址
 * 缓存key规则：
 * 科学人的key是 article
 * 其余的是 article.{id}
 */
public class ArticleAPI extends APIBase {

    public ArticleAPI() {

    }

    /**
     * 获取《科学人》默认列表，取20个，我发现这样动态请求比果壳首页刷新的快……
     * resultObject.result是ArrayList[Article]
     *
     * @param offset 从第offset个开始取
     * @return ResponseObject
     */
    //TODO
    @Deprecated
    public static ResponseObject<ArrayList<Article>> getArticleListIndexPage(int offset) {
        String url = "http://www.guokr.com/apis/minisite/article.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("retrieve_type", "by_subject");
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return getArticleListFromJsonUrl(url, pairs);
    }

    /**
     * 按频道取《科学人》的文章，比如热点、前沿什么的
     * resultObject.result是ArrayList[Article]
     *
     * @param channelKey 频道key
     * @param offset     加载开始的index
     * @return ResponseObject
     */
    //TODO
    @Deprecated
    public static ResponseObject<ArrayList<Article>> getArticleListByChannel(String channelKey, int offset) {
        String url = "http://www.guokr.com/apis/minisite/article.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("retrieve_type", "by_channel");
        pairs.put("channel_key", channelKey);
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return getArticleListFromJsonUrl(url, pairs);
    }

    /**
     * 按学科取《科学人》的文章
     * resultObject.result是ArrayList[Article]
     *
     * @param subject_key 学科key
     * @param offset      从第几个开始加载
     * @return ResponseObject
     */
    //TODO
    @Deprecated
    public static ResponseObject<ArrayList<Article>> getArticleListBySubject(String subject_key, int offset) {
        String url = "http://www.guokr.com/apis/minisite/article.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("retrieve_type", "by_subject");
        pairs.put("subject_key", subject_key);
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return getArticleListFromJsonUrl(url, pairs);
    }

    /**
     * 根据上面几个方法生成的url去取文章列表
     * resultObject.result是ArrayList[Article]
     *
     * @param url jsonUrl
     * @return ResponseObject
     */
    //TODO
    @Deprecated
    private static ResponseObject<ArrayList<Article>> getArticleListFromJsonUrl(String url, HashMap<String, String> pairs) {
        ResponseObject<ArrayList<Article>> resultObject = new ResponseObject<>();
        try {
            String jString = HttpFetcher.get(url, pairs, false).toString();
            resultObject = parseArticleListJson(jString);
            if (resultObject.ok) {
                //请求成功则缓存之
                String key = null;
                if (pairs.size() == 4 && pairs.get("offset").equals("0")) {
                    String channel_key = pairs.get("channel_key");
                    String subject_key = pairs.get("subject_key");
                    key = "article." + (TextUtils.isEmpty(channel_key) ? subject_key : channel_key);
                } else if (pairs.size() == 3 && pairs.get("offset").equals("0")) {
                    key = "article";
                }
                if (key != null) {
                    RequestCache.getInstance().addStringToCacheForceUpdate(key, jString);
                }
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 获取缓存的文章列表
     * resultObject.result是ArrayList[Article]
     *
     * @param subItem SubItem
     * @return ResponseObject
     */
    //TODO
    public static ResponseObject<ArrayList<Article>> getCachedArticleList(SubItem subItem) {
        ResponseObject<ArrayList<Article>> resultObject = new ResponseObject<>();
        String key = null;
        if (subItem.getType() == SubItem.Type_Collections) {
            key = "article";
        } else if (subItem.getType() == SubItem.Type_Single_Channel) {
            key = "article." + subItem.getValue();
        } else if (subItem.getType() == SubItem.Type_Subject_Channel) {
            key = "article." + subItem.getValue();
        }
        if (key != null) {
            try {
                String jString = RequestCache.getInstance().getStringFromCache(key);
                if (jString != null) {
                    resultObject = parseArticleListJson(jString);
                }
            } catch (Exception e) {
                handleRequestException(e, resultObject);
            }
        }
        return resultObject;
    }

    /**
     * 获取缓存的文章列表
     * resultObject.result是ArrayList[Article]
     *
     * @param jString 要解析的json
     * @return ResponseObject
     */
    //TODO
    public static ResponseObject<ArrayList<Article>> parseArticleListJson(String jString) {
        ResponseObject<ArrayList<Article>> resultObject = new ResponseObject<>();
        try {
            ArrayList<Article> articleList = new ArrayList<>();
            if (jString != null) {
                JSONArray articles = APIBase.getUniversalJsonArray(jString, resultObject);
                if (articles != null) {
                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject jo = articles.getJSONObject(i);
                        articleList.add(Article.fromJsonSimple(jo));
                    }
                    resultObject.ok = true;
                    resultObject.result = articleList;
                }
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 根据文章id，解析页面获得文章内容
     * resultObject.result是Article
     *
     * @param id article ID
     * @return ResponseObject
     */
    //TODO
    @Deprecated
    public static ResponseObject<Article> getCachedArticleDetailByID(String id) {
        return getCachedArticleDetailByUrl("http://www.guokr.com/article/" + id + "/");
    }

    /**
     * 直接解析页面获得文章内容
     * resultObject.result是Article
     *
     * @param url article页面地址
     */
    //TODO
    @Deprecated
    public static ResponseObject<Article> getCachedArticleDetailByUrl(String url) {
        ResponseObject<Article> resultObject = new ResponseObject<>();
        try {
            String articleId = url.replaceAll("\\?.*$", "").replaceAll("\\D+", "");
            String html = RequestCache.getInstance().getStringFromCache(url);
            Article article = Article.fromHtmlDetail(articleId, html);
            resultObject.ok = true;
            resultObject.result = article;
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 根据文章id，解析页面获得文章内容
     * resultObject.result是Article
     *
     * @param id article ID
     * @return ResponseObject
     */
    //TODO
    @Deprecated
    public static ResponseObject<Article> getArticleDetailByID(String id) {
        return getArticleDetailByUrl("http://www.guokr.com/article/" + id + "/");
    }

    /**
     * 直接解析页面获得文章内容
     * resultObject.result是Article
     *
     * @param url article页面地址
     */
    //TODO
    @Deprecated
    public static ResponseObject<Article> getArticleDetailByUrl(String url) {
        ResponseObject<Article> resultObject = new ResponseObject<>();
        try {
            String aid = url.replaceAll("\\?.*$", "").replaceAll("\\D+", "");
            ResponseObject response = HttpFetcher.get(url);
            resultObject.statusCode = response.statusCode;
            if (resultObject.statusCode == 404) {
                return resultObject;
            }
            String html = response.toString();
            Article article = Article.fromHtmlDetail(aid, html);
            resultObject.ok = true;
            resultObject.result = article;
            RequestCache.getInstance().addStringToCacheForceUpdate(url, html);
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 根据文章id，解析页面获得文章内容
     * resultObject.result是Article
     *
     * @param id article ID
     * @return ResponseObject
     */
    public static RequestObject<Article> getArticleDetailByID(String id, CallBack<Article> callBack) {
        return getArticleDetailByUrl("http://www.guokr.com/article/" + id + "/", callBack);
    }

    /**
     * 直接解析页面获得文章内容
     * resultObject.result是Article
     *
     * @param url article页面地址
     */
    public static RequestObject<Article> getArticleDetailByUrl(String url, CallBack<Article> callBack) {
        final String aid = url.replaceAll("\\?.*$", "").replaceAll("\\D+", "");
        return new RequestBuilder<Article>()
                .setUrl(url)
                .setRequestCallBack(callBack)
                .get()
                .useCacheIfFailed(true)
                .setParser(new Parser<Article>() {
                    @Override
                    public Article parse(String str, ResponseObject<Article> responseObject) throws Exception {
                        Article article = Article.fromHtmlDetail(aid, str);
                        responseObject.ok = true;
                        return article;
                    }
                })
                .requestAsync();
    }

    /**
     * 解析html获得文章热门评论
     *
     * @param hotElement 热门评论元素
     * @param aid        article ID
     * @return ResponseObject
     * 暂时先不用ResponseObject返回
     * @deprecated
     */
    @Deprecated
    private static ArrayList<UComment> getArticleHotComments(Element hotElement, String aid) throws Exception {
        ArrayList<UComment> list = new ArrayList<>();
        Elements comments = hotElement.getElementsByTag("li");
        if (comments != null && comments.size() > 0) {
            for (int i = 0; i < comments.size(); i++) {
                Element element = comments.get(i);
                UComment comment = new UComment();
                String id = element.id().replace("reply", "");
                Element tmp = element.select(".cmt-img").select(".cmtImg").select(".pt-pic").get(0);
                Author author = new Author();
                author.setName(tmp.getElementsByTag("a").get(0).attr("title"));
                author.setId(tmp.getElementsByTag("a").get(0).attr("href").replaceAll("\\D+", ""));
                author.setAvatar(tmp.getElementsByTag("img").get(0).attr("src").replaceAll("\\?.*$", ""));
                String likeNum = element.getElementsByClass("cmt-do-num").get(0).text();
                String date = element.getElementsByClass("cmt-info").get(0).text();
                String content = element.select(".cmt-content").select(".gbbcode-content").select(".cmtContent").get(0).outerHtml();
                Elements tmpElements = element.getElementsByClass("cmt-auth");
                if (tmpElements != null && tmpElements.size() > 0) {
                    author.setTitle(element.getElementsByClass("cmt-auth").get(0).attr("title"));
                }

                comment.setID(id);
                comment.setLikeNum(Integer.valueOf(likeNum));
                comment.setAuthor(author);
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
     * resultObject.result是ArrayList[UComment]
     *
     * @param id     article ID
     * @param offset 从第几个开始加载
     * @return ResponseObject
     */
    //TODO
    @Deprecated
    public static ResponseObject<ArrayList<AceModel>> getArticleComments(String id, int offset, int limit) {
        ResponseObject<ArrayList<AceModel>> resultObject = new ResponseObject<>();
        try {
            ArrayList<AceModel> list = new ArrayList<>();
            String url = "http://apis.guokr.com/minisite/article_reply.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("article_id", id);
            pairs.put("limit", String.valueOf(limit));
            pairs.put("offset", String.valueOf(offset));
            String jString = HttpFetcher.get(url, pairs).toString();
            JSONArray articles = APIBase.getUniversalJsonArray(jString, resultObject);
            if (articles != null) {
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject jo = articles.getJSONObject(i);
                    UComment comment = UComment.fromArticleJson(id, "", jo);
                    comment.setFloor((offset + i + 1) + "楼");
                    list.add(comment);
                }
                resultObject.ok = true;
                resultObject.result = list;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 获取文章评论，json格式
     * resultObject.result是ArrayList[UComment]
     *
     * @param id     article ID
     * @param offset 从第几个开始加载
     * @return ResponseObject
     */
    public static RequestObject<ArrayList<UComment>> getArticleComments(final String id, final int offset, int limit, CallBack<ArrayList<UComment>> callBack) {
        String url = "http://apis.guokr.com/minisite/article_reply.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("article_id", id);
        pairs.put("limit", String.valueOf(limit));
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<UComment>>()
                .setUrl(url)
                .setRequestCallBack(callBack)
                .get()
                .setParams(pairs)
                .setParser(new Parser<ArrayList<UComment>>() {
                    @Override
                    public ArrayList<UComment> parse(String str, ResponseObject<ArrayList<UComment>> responseObject) throws Exception {
                        ArrayList<UComment> list = new ArrayList<>();
                        JSONArray articles = JsonHandler.getUniversalJsonArray(str, responseObject);
                        assert articles != null;
                        for (int i = 0; i < articles.length(); i++) {
                            JSONObject jo = articles.getJSONObject(i);
                            UComment comment = UComment.fromArticleJson(id, "", jo);
                            comment.setFloor((offset + i + 1) + "楼");
                            list.add(comment);
                        }
                        return list;
                    }
                })
                .requestAsync();
    }

    /**
     * 获取一条article的简介，也就是除了正文之外的一切，这里只需要两个，id和title
     *
     * @param article_id article_id
     * @return ResponseObject
     */
    //TODO
    @Deprecated
    private static ResponseObject<Article> getArticleSimpleByID(String article_id) {
        ResponseObject<Article> resultObject = new ResponseObject<>();
        String url = "http://apis.guokr.com/minisite/article.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("article_id", article_id);
        try {
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray articlesArray = getUniversalJsonArray(result, resultObject);
            if (articlesArray != null && articlesArray.length() == 1) {
                JSONObject articleObject = articlesArray.getJSONObject(0);
                Article article = Article.fromJsonSimple(articleObject);
                resultObject.ok = true;
                resultObject.result = article;
            }

        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 获取一条article的简介，也就是除了正文之外的一切，这里只需要两个，id和title
     *
     * @param article_id article_id
     * @return ResponseObject
     */
    private static RequestObject<Article> getArticleSimpleByID(String article_id, CallBack<Article> callBack) {
        String url = "http://apis.guokr.com/minisite/article.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("article_id", article_id);
        return new RequestBuilder<Article>()
                .setUrl(url)
                .setRequestCallBack(callBack)
                .get()
                .setParams(pairs)
                .setParser(new Parser<Article>() {
                    @Override
                    public Article parse(String str, ResponseObject<Article> responseObject) throws Exception {
                        JSONArray articlesArray = JsonHandler.getUniversalJsonArray(str, responseObject);
                        assert articlesArray != null;
                        JSONObject articleObject = articlesArray.getJSONObject(0);
                        return Article.fromJsonSimple(articleObject);
                    }
                })
                .requestAsync();
    }

    /**
     * 根据一条通知的id获取所有内容，蛋疼的需要跳转。
     * 先是：http://www.guokr.com/user/notice/8738252/
     * 跳到：http://www.guokr.com/article/reply/123456/
     * 这就走到了类似getSingleCommentFromRedirectUrl这一步
     * 两次跳转后可获得article_id，但是仍然无法获得title
     * 还需要另一个接口获取article的摘要。getArticleSimpleByID(article_id)
     *
     * @param notice_id 通知id
     * @return resultObject resultObject.result是UComment
     */
    //TODO
    public static ResponseObject<UComment> getSingleCommentByNoticeID(String notice_id) {
        ResponseObject<UComment> resultObject = new ResponseObject<>();
        String article_id;
        String reply_id;
        if (TextUtils.isEmpty(notice_id)) {
            return resultObject;
        }
        String notice_url = "http://www.guokr.com/user/notice/" + notice_id + "/";
        try {
            ResponseObject httpResult = HttpFetcher.get(notice_url);
            String replyRedirectResult = httpResult.toString();
            Document document = Jsoup.parse(replyRedirectResult);
            Elements elements = document.getElementsByTag("a");
            if (elements.size() == 1) {
                Matcher matcher = Pattern.compile("^/article/(\\d+)/.*#reply(\\d+)$").matcher(elements.get(0).text());
                if (matcher.find()) {
                    article_id = matcher.group(1);
                    reply_id = matcher.group(2);
                    ResponseObject<Article> articleResult = getArticleSimpleByID(article_id);
                    if (articleResult.ok) {
                        Article article = articleResult.result;
                        return getSingleCommentByID(reply_id, article.getId(), article.getTitle());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 根据一条评论的地址获取所有内容，蛋疼的需要跳转
     * http://www.guokr.com/article/reply/123456/
     * 一次跳转后可获得article_id，但是仍然无法获得title
     * 还需要另一个接口获取article的摘要。getArticleSimpleByID(article_id)
     * 多次跳转真让人想死啊。
     *
     * @param reply_url 评论地址
     * @return resultObject resultObject.result是UComment
     */
    //TODO
    public static ResponseObject<UComment> getSingleCommentFromRedirectUrl(String reply_url) {
        ResponseObject<UComment> resultObject = new ResponseObject<>();
        String article_id;
        String reply_id;
        try {
            reply_id = reply_url.replaceAll("\\D+", "");
            ResponseObject httpResult = HttpFetcher.get(reply_url);
            String replyRedirectResult = httpResult.toString();
            Document document = Jsoup.parse(replyRedirectResult);
            Elements elements = document.getElementsByTag("a");
            if (elements.size() == 1) {
                Matcher matcher = Pattern.compile("^/article/(\\d+)/.*#reply(\\d+)$").matcher(elements.get(0).text());
                if (matcher.find()) {
                    article_id = matcher.group(1);
                    ResponseObject<Article> articleResult = getArticleSimpleByID(article_id);
                    if (articleResult.ok) {
                        Article article = articleResult.result;
                        return getSingleCommentByID(reply_id, article.getId(), article.getTitle());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 根据一条评论的id获取评论内容，主要应用于消息通知
     * 无法取得此评论的文章的id和标题，无法取得楼层。
     * 太蛋疼了，只能不显示文章标题或者提前传入
     *
     * @param reply_id 评论id
     * @return resultObject resultObject.result是UComment
     */
    //TODO
    public static ResponseObject<UComment> getSingleCommentByID(String reply_id, String article_id, String article_title) {
        ResponseObject<UComment> resultObject = new ResponseObject<>();
        String url = "http://apis.guokr.com/minisite/article_reply.json";
        //url还有另一种形式，http://apis.guokr.com/minisite/article_reply/99999999.json;
        //这样后面就不必带reply_id参数了
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("reply_id", reply_id);
        try {
            String result = HttpFetcher.get(url, pairs).toString();
            JSONObject replyObject = getUniversalJsonObject(result, resultObject);
            assert replyObject != null;
            UComment comment = UComment.fromArticleJson(article_id, article_title, replyObject);
            resultObject.ok = true;
            resultObject.result = comment;
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 推荐文章
     *
     * @param articleID article ID
     * @param title     文章标题
     * @param summary   文章总结
     * @param comment   推荐评语
     * @return ResponseObject
     */
    public static RequestObject<Boolean> recommendArticle(String articleID, String title, String summary, String comment, CallBack<Boolean> callBack) {
        String articleUrl = "http://www.guokr.com/article/" + articleID + "/";
        return UserAPI.recommendLink(articleUrl, title, summary, comment, callBack);
    }

    /**
     * 赞一个文章评论
     *
     * @param id 文章id
     * @return ResponseObject
     */
    public static RequestObject<Boolean> likeComment(String id, CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/minisite/article_reply_liking.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("reply_id", id);
        return new RequestBuilder<Boolean>()
                .setUrl(url)
                .setParser(new BooleanParser())
                .setRequestCallBack(callBack)
                .setParams(pairs)
                .post()
                .requestAsync();
    }

    /**
     * 删除我的评论
     *
     * @param id 评论id
     * @return ResponseObject
     */
    public static RequestObject<Boolean> deleteMyComment(String id, CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/minisite/article_reply.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("reply_id", id);
        return new RequestBuilder<Boolean>()
                .setUrl(url)
                .setParser(new BooleanParser())
                .setRequestCallBack(callBack)
                .setParams(pairs)
                .delete()
                .requestAsync();
    }

    /**
     * 使用json请求回复文章
     *
     * @param id      文章id
     * @param content 回复内容
     * @return ResponseObject.result is the reply_id if ok;
     */
    public static RequestObject<String> replyArticle(String id, String content, CallBack<String> callBack) {
        String url = "http://apis.guokr.com/minisite/article_reply.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("article_id", id);
        pairs.put("content", content);
        return new RequestBuilder<String>()
                .setUrl(url)
                .setParser(new ContentValueForKeyParser("id"))
                .setRequestCallBack(callBack)
                .setParams(pairs)
                .post()
                .requestAsync();
    }
}
