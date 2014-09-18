package com.example.outerspace.connection;

import com.example.outerspace.model.Article;
import com.example.outerspace.model.Post;
import com.example.outerspace.model.PostComment;
import com.example.outerspace.model.SimpleComment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class JsoupUtil {

    public JsoupUtil() {
        // TODO Auto-generated constructor stub
    }

    public static void getGroupHotPostListFromMobileUrl(int pageNo) {
        ArrayList<Post> list = new ArrayList<Post>();
        String url = "http://m.guokr.com/group/hot_posts/?page=" + pageNo;
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.getElementsByClass("post-index-list");
            if (elements.size() == 1) {
                Elements postlist = elements.get(0).getElementsByTag("li");
                for (Iterator<Element> iterator = postlist.iterator(); iterator.hasNext(); ) {
                    Post item = new Post();
                    Element element = (Element) iterator.next();
                    Element link = element.getElementsByClass("post").get(0);
                    String postTitle = link.text();
                    String postUrl = link.attr("href");
                    String postImageUrl = "";
                    if (link.getElementsByClass("post-img").size() > 0) {
                        String bgimg = link.getElementsByClass("post-img").get(0).attr("style")
                                .replace("background-image:url(", "");
                        int idx = bgimg.indexOf("?");
                        if (idx == -1) {
                            idx = bgimg.length();
                        }
                        postImageUrl = bgimg.substring(0, idx);
                    }
                    String postAuthor = "";
                    String postGroup = "";
                    String[] ang = element.getElementsByClass("post-info-content").get(0).text()
                            .split(" 发表于 ");
                    postAuthor = ang[0];
                    postGroup = ang[1];
                    int postLike = Integer.valueOf(element.getElementsByClass("like-num").get(0)
                            .text());
                    int postComment = Integer.valueOf(element.getElementsByClass("post-reply-num")
                            .get(0).text().replaceAll(" 回应$", ""));
                    item.setTitle(postTitle);
                    item.setUrl(postUrl);
                    item.setTitleImageUrl(postImageUrl);
                    item.setAuthor(postAuthor);
                    item.setGroupName(postGroup);
                    item.setLikeNum(postLike);
                    item.setCommentNum(postComment);
                    list.add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Post> getGroupPostListByJsonUrl(String id, int offset) {
        String url = "http://apis.guokr.com/group/post.json?retrieve_type=by_group&group_id=" + id
                + "&limit=2&offset=" + offset;
        ArrayList<Post> list = new ArrayList<Post>();
        String jString = HttpFetcher.get(url);
        try {
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray articles = jss.getJSONArray("result");
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject jo = articles.getJSONObject(i);
                    Post post = new Post();
                    post.setId(getJsonString(jo, "id"));
                    post.setGroupName(jo.getJSONObject("group").getString("name"));
                    post.setTitle(getJsonString(jo, "title"));
                    post.setUrl(getJsonString(jo, "url"));
                    post.setAuthor(jo.getJSONObject("author").getString("nickname"));
                    post.setAuthorID(jo.getJSONObject("author").getString("url")
                            .replaceAll("\\D+", ""));
                    post.setAuthorAvatarUrl(jo.getJSONObject("author").getJSONObject("avatar")
                            .getString("small").replaceAll("\\?\\S*$", ""));
                    post.setDate(getJsonString(jo, "date_created"));
                    post.setCommentNum(getJsonInt(jo, "replies_count"));
                    post.setContent(getJsonString(jo, "html"));
                    // 无法获取赞的数量
                    // 无法获取titleImageUrl
                    list.add(post);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void getGroupPostListByHtmlUrl(String url) {
        ArrayList<Post> list = new ArrayList<Post>();
        try {
            Document doc = Jsoup.connect(url).get();
            String postGroup = doc.getElementsByClass("group-name").text();
            Elements elements = doc.getElementsByClass("post-list");
            if (elements.size() == 1) {
                Elements postlist = elements.get(0).getElementsByTag("li");
                for (Iterator<Element> iterator = postlist.iterator(); iterator.hasNext(); ) {
                    Post item = new Post();
                    item.setGroupName(postGroup);
                    Element element = (Element) iterator.next();
                    Elements links = element.getElementsByTag("a");
                    if (links.size() > 0) {
                        Element link = links.get(0);
                        Elements tagElements = link.getElementsByTag("h4").get(0)
                                .getElementsByTag("span");
                        String tagPreffix = "";
                        String tag = "";
                        for (int i = 0; i < tagElements.size(); i++) {
                            Element element2 = tagElements.get(i);
                            tagPreffix += element2.text() + " ";
                            tag += element2.text() + ((i == tagElements.size() - 1) ? "" : "|");
                        }
                        String postTitle = link.text().replaceAll("^" + tagPreffix, "");
                        String postUrl = link.attr("href");
                        item.setTitle(postTitle);
                        item.setUrl(postUrl);
                        item.setTag(tag);
                        if (links.size() == 2) {
                            String postAuthor = links.get(1).text();
                            Elements ups = element.getElementsByClass("post-info").get(0)
                                    .getElementsByTag("span");
                            int postLike = Integer.valueOf(ups.get(1).text().replace("赞", ""));
                            int postComment = Integer.valueOf(ups.get(2).text().replace("回应", ""));
                            item.setAuthor(postAuthor);
                            item.setLikeNum(postLike);
                            item.setCommentNum(postComment);
                        }
                    }
                    list.add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getPostDetailByIDFromMobileUrl(String id) {
        getPostDetailByPostMobileUrl("http://m.guokr.com/post/" + id + "/");
    }

    /**
     * 仅限第一页
     *
     * @param url
     */
    public static Post getPostDetailByPostMobileUrl(String url) {
        // 手机页面无法取得评论数，最好是从点击时带过来。TODO
        Post detail = new Post();
        try {
            Document doc = Jsoup.connect(url).get();
            String postID = url.replaceAll("\\?\\S*$", "").replaceAll("\\D+", "");
            String groupID = doc.getElementsByClass("crumbs").get(0).getElementsByTag("a")
                    .attr("href").replaceAll("\\D+", "");
            String groupName = doc.getElementsByClass("crumbs").get(0).getElementsByTag("a").text();
            Element mainElement = doc.getElementById("contentMain");
            String authorAvatarUrl = mainElement.getElementsByClass("author-avatar").get(0)
                    .getElementsByTag("img").attr("src").replaceAll("\\?\\S*$", "");
            String title = mainElement.getElementsByClass("title").text();
            String author = mainElement.select(".author").select(".gfl").text();
            String authorID = mainElement.select(".author").select(".gfl").attr("href")
                    .replaceAll("\\D+", "");
            String date = mainElement.getElementsByTag("time").text();
            String content = mainElement.getElementById("postContent").outerHtml();
            int likeNum = Integer.valueOf(mainElement.getElementsByClass("like-num").get(0).text());
            detail.setGroupID(groupID);
            detail.setGroupName(groupName);
            detail.setAuthor(author);
            detail.setAuthorAvatarUrl(authorAvatarUrl);
            detail.setAuthorID(authorID);
            detail.setId(postID);
            detail.setTitle(title);
            detail.setDate(date);
            detail.setContent(content);
            detail.setLikeNum(likeNum);
            Elements elements = doc.getElementsByClass("group-comments");
            if (elements.size() == 1) {
                detail.setComments(extractPostComments(elements.get(0), postID));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return detail;
    }

    public static ArrayList<PostComment> getPostCommentsFromJsonUrl(String id, int offset) {
        ArrayList<PostComment> list = new ArrayList<PostComment>();
        String url = "http://apis.guokr.com/group/post_reply.json?retrieve_type=by_post&post_id="
                + id + "&limit=10&offset=" + offset;
        String jString = HttpFetcher.get(url);
        try {
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray comments = jss.getJSONArray("result");
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jo = comments.getJSONObject(i);
                    PostComment comment = new PostComment();
                    comment.setID(getJsonString(jo, "id"));
                    comment.setAuthor(getJsonObject(jo, "author").getString("nickname"));
                    comment.setAuthorID(getJsonObject(jo, "author").getString("url")
                            .replaceAll("\\D+", ""));
                    comment.setAuthorAvatarUrl(getJsonObject(jo, "author").getJSONObject("avatar")
                            .getString("small").replaceAll("\\?\\S*$", ""));
                    comment.setDate(getJsonString(jo, "date_created"));
                    comment.setLikes(getJsonInt(jo, "likings_count"));
                    comment.setContent(getJsonString(jo, "html"));
                    comment.setFloor((offset + i + 1) + "楼");
                    comment.setPostID(jo.getJSONObject("post").getString("id"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<PostComment> getPostCommentsFromHtmlUrl(String id, int pageNo) {
        ArrayList<PostComment> list = new ArrayList<PostComment>();
        String url = "http://m.guokr.com/post/" + id + "/?page=" + pageNo;
        try {
            Document doc = Jsoup.connect(url).get();
            String postID = url.replaceAll("\\?\\S*$", "").replaceAll("\\D+", "");
            Elements elements = doc.getElementsByClass("group-comments");
            if (elements.size() == 1) {
                return extractPostComments(elements.get(0), postID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<PostComment> extractPostComments(Element element, String postID) {
        ArrayList<PostComment> list = new ArrayList<PostComment>();
        Elements commentlist = element.getElementsByClass("comment");
        for (int i = 0; i < commentlist.size(); i++) {
            PostComment comment = new PostComment();
            Element liElement = commentlist.get(i);
            String commentID = liElement.id();
            String commentAuthorAvatarUrl = liElement.getElementsByClass("cmt-author-img")
                    .get(0).getElementsByTag("img").attr("src").replaceAll("\\?\\S*$", "");
            String commentAuthor = liElement.getElementsByClass("cmt-author").text();
            String commentAuthorID = liElement.getElementsByClass("cmt-author-img")
                    .attr("href").replaceAll("\\D+", "");
            String commentDate = liElement.getElementsByClass("cmt-time").text();
            String commentFloor = liElement.getElementsByClass("cmt-info-txt-left").text()
                    .replaceAll("^" + commentAuthor, "").replaceAll(commentDate + "$", "")
                    .replaceAll(" ", "");
            String commentContent = liElement.getElementsByClass("cmt-main").outerHtml();
            int likes = Integer.valueOf(liElement.getElementsByClass("cmt-like").text());
            comment.setAuthorAvatarUrl(commentAuthorAvatarUrl);
            comment.setAuthorID(commentAuthorID);
            comment.setAuthor(commentAuthor);
            comment.setDate(commentDate);
            comment.setID(commentID);
            comment.setContent(commentContent);
            comment.setFloor(commentFloor);
            comment.setPostID(postID);
            comment.setLikes(likes);
            list.add(comment);
        }
        return list;
    }

    public static ArrayList<Article> getArticleListByChannel(String channelKey, int offset) {
        String url = "http://www.guokr.com/apis/minisite/article.json?retrieve_type=by_channel&channel_key="
                + channelKey + "&limit=20&offset=" + offset;
        return getArticleListFromJsonUrl(url);
    }

    public static ArrayList<Article> getArticleListBySubject(String subject_key, int offset) {
        String url = "http://www.guokr.com/apis/minisite/article.json?retrieve_type=by_subject&subject_key="
                + subject_key + "&limit=20&offset=" + offset;
        return getArticleListFromJsonUrl(url);
    }

    private static ArrayList<Article> getArticleListFromJsonUrl(String url) {
        ArrayList<Article> articleList = new ArrayList<Article>();
        String jString = HttpFetcher.get(url);
        try {
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
                            .getString("large"));
                    article.setDate(getJsonString(jo, "date_published"));
                    article.setSubjectName(getJsonString(getJsonObject(jo, "subject"), "name"));
                    article.setSubjectKey(getJsonString(getJsonObject(jo, "subject"), "key"));
                    article.setUrl(getJsonString(jo, "url"));
                    article.setImageUrl(getJsonString(jo, "small_image"));
                    article.setSummary(getJsonString(jo, "summary"));
                    article.setTitle(getJsonString(jo, "title"));
                    articleList.add(article);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return articleList;
    }

    public static void getArticleDetailByID(String id) {
        getArticleDetailByUrl("http://www.guokr.com/article/" + id + "/");
    }

    /**
     * 仅限第一页
     *
     * @param url
     */
    public static Article getArticleDetailByUrl(String url) {
        Article article = new Article();
        String aid = url.replaceAll("\\D+", "").replaceAll("\\?\\S*$", "");
        try {
            Document doc = Jsoup.connect(url).get();
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
        } catch (Exception e) {
            e.printStackTrace();
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

    public static ArrayList<SimpleComment> getArticleComments(String id, int offset) {
        ArrayList<SimpleComment> list = new ArrayList<SimpleComment>();
        String url = "http://apis.guokr.com/minisite/article_reply.json?article_id=" + id
                + "&limit=2&offset=" + offset;
        try {
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
                            .getString("large"));
                    comment.setAuthorTitle(getJsonString(getJsonObject(jo, "author"), "title"));
                    comment.setDate(getJsonString(jo, "date_created"));
                    comment.setFloor((offset + i + 1) + "楼");
                    comment.setContent(getJsonString(jo, "html"));
                    comment.setHostID(id);
                    list.add(comment);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int getJsonInt(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getInt(key);
        } else {
            return 0;
        }
    }

    public static boolean getJsonBoolean(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getBoolean(key);
        } else {
            return false;
        }
    }

    public static String getJsonString(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getString(key);
        } else {
            return "";
        }
    }

    public static JSONObject getJsonObject(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getJSONObject(key);
        } else {
            return new JSONObject();
        }
    }
}
