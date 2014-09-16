package com.example.outerspace.connection;

import android.net.Uri;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.outerspace.model.Article;
import com.example.outerspace.model.Post;
import com.example.outerspace.model.PostComment;

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

    public static void getGroupPostListByJsonUrl(String id, int offset) {
        String url = "http://apis.guokr.com/group/post.json?retrieve_type=by_group&group_id="+id+"&limit=2&offset="+offset;
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
                    post.setGroupName(jo.getJSONObject("group").getString("name"));
                    post.setTitle(jo.getString("title"));
                    post.setUrl(jo.getString("url"));
                    post.setAuthor(jo.getJSONObject("author").getString("nickname"));
                    post.setCommentNum(jo.getInt("replies_count"));
                    //无法获取赞的数量
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

    public static ArrayList<PostComment> getPostComments(String id, int pageNo) {
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
            comment.setCommentAuthor(commentAuthor);
            comment.setCommentDate(commentDate);
            comment.setCommentID(commentID);
            comment.setContent(commentContent);
            comment.setFloor(commentFloor);
            comment.setPostID(postID);
            comment.setLikes(likes);
            list.add(comment);
        }
        return list;
    }

    public static String getArticleListByChannel(String channelKey, int offset) {
        String url = "http://www.guokr.com/apis/minisite/article.json?retrieve_type=by_channel&channel_key="
                + channelKey + "&limit=20&offset=" + offset;
        return url;
    }

    public static String getArticleListBySubject(String subject_key, int offset) {
        String url = "http://www.guokr.com/apis/minisite/article.json?retrieve_type=by_subject&subject_key="
                + subject_key + "&limit=20&offset=" + offset;
        return url;
    }

    public static void getArticleListFromJsonUrl(String url) {
        String jString = HttpFetcher.get(url);
        try {
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray articles = jss.getJSONArray("result");
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject jo = articles.getJSONObject(i);
                    Article article = new Article();
                    article.setId(jo.getString("id"));
                    article.setCommentNum(jo.getInt("replies_count"));
                    article.setAuthor(jo.getJSONObject("author").getString("nickname"));
                    article.setAuthorID(jo.getJSONObject("author").getString("url").replaceAll("\\D+", ""));
                    article.setDate(jo.getString("date_published"));
                    article.setSubjectName(jo.getJSONObject("subject").getString("name"));
                    article.setSubjectKey(jo.getJSONObject("subject").getString("key"));
                    article.setUrl(jo.getString("url"));
                    article.setImageUrl(jo.getString("small_image"));
                    article.setSummary(jo.getString("summary"));
                    article.setTitle(jo.getString("title"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void getArticleDetailByID(String id) {
        getPostDetailByUrl("http://www.guokr.com/article/" + id + "/");
    }

    /**
     * 仅限第一页
     *
     * @param url
     */
    public static Post getPostDetailByUrl(String url) {
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

    public static ArrayList<PostComment> getArticleComments(String id, int offset) {
        ArrayList<PostComment> list = new ArrayList<PostComment>();
        String url = "http://apis.guokr.com/minisite/article_reply.json?article_id=438938&limit=2&offset=0";
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
}
