package com.example.sourcewall.connection.api;

import com.example.sourcewall.connection.HttpFetcher;
import com.example.sourcewall.model.Post;
import com.example.sourcewall.model.SimpleComment;

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

public class PostAPI extends APIBase {

    public PostAPI() {
        // TODO Auto-generated constructor stub
    }

    public static ArrayList<Post> getMyGroupRecentPosts(int pageNo) throws IOException {
        //TODO
        ArrayList<Post> list = new ArrayList<Post>();
        String url = "http://m.guokr.com/group/user/recent_replies/";
        Document doc = Jsoup.connect(url).get();
        return null;
    }


    /**
     * 获得小组热贴（与登录无关）
     *
     * @param pageNo，要获取的页码
     * @return 帖子列表
     * @throws java.io.IOException
     */
    public static ArrayList<Post> getGroupHotPostListFromMobileUrl(int pageNo) throws IOException {
        ArrayList<Post> list = new ArrayList<Post>();
        String url = "http://m.guokr.com/group/hot_posts/?page=" + pageNo;
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
                item.setId(postUrl.replaceAll("\\?\\S*$", "").replaceAll("\\D+", ""));
                item.setTitleImageUrl(postImageUrl);
                item.setAuthor(postAuthor);
                item.setGroupName(postGroup);
                item.setLikeNum(postLike);
                item.setReplyNum(postComment);
                list.add(item);
            }
        }
        return list;
    }

    /**
     * 根据小组id获得帖子列表
     *
     * @param id     小组id
     * @param offset 从第几个帖子开始取
     * @return
     * @throws java.io.IOException
     */
    public static ArrayList<Post> getGroupPostListByJsonUrl(String id, int offset) throws IOException {
        String url = "http://apis.guokr.com/group/post.json?retrieve_type=by_group&group_id=" + id
                + "&limit=20&offset=" + offset;
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
                            .getString("large").replaceAll("\\?\\S*$", ""));
                    post.setDate(getJsonString(jo, "date_created"));
                    post.setReplyNum(getJsonInt(jo, "replies_count"));
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

    /**
     * 根据地址获取帖子列表，暂时没考虑个别小组，用于拦截链接点击，未解析热贴。
     *
     * @param url 浏览器里小组的地址。
     */
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
                            item.setReplyNum(postComment);
                        }
                    }
                    list.add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据帖子地址解析帖子详细内容
     *
     * @param id，帖子id
     * @return
     * @throws java.io.IOException
     */
    public static Post getPostDetailByIDFromMobileUrl(String id) throws IOException {
        return getPostDetailByPostMobileUrl("http://m.guokr.com/post/" + id + "/");
    }

    /**
     * 仅限第一页，根据帖子地址解析帖子详细内容
     *
     * @param url
     */
    public static Post getPostDetailByPostMobileUrl(String url) throws IOException {
        // 手机页面无法取得评论数，最好是从点击时带过来。TODO
        Post detail = new Post();
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
        return detail;
    }

    /**
     * 使用Json解析方式获得帖子评论列表
     *
     * @param id
     * @param offset
     * @return
     * @throws java.io.IOException
     * @throws org.json.JSONException
     */
    public static ArrayList<SimpleComment> getPostCommentsFromJsonUrl(String id, int offset) throws IOException, JSONException {
        ArrayList<SimpleComment> list = new ArrayList<SimpleComment>();
        String url = "http://apis.guokr.com/group/post_reply.json?retrieve_type=by_post&post_id="
                + id + "&limit=20&offset=" + offset;
        String jString = HttpFetcher.get(url);
        JSONObject jss = new JSONObject(jString);
        boolean ok = jss.getBoolean("ok");
        if (ok) {
            JSONArray comments = jss.getJSONArray("result");
            for (int i = 0; i < comments.length(); i++) {
                JSONObject jo = comments.getJSONObject(i);
                SimpleComment comment = new SimpleComment();
                comment.setID(getJsonString(jo, "id"));
                comment.setAuthor(getJsonObject(jo, "author").getString("nickname"));
                comment.setAuthorID(getJsonObject(jo, "author").getString("url")
                        .replaceAll("\\D+", ""));
                comment.setAuthorAvatarUrl(getJsonObject(jo, "author").getJSONObject("avatar")
                        .getString("large").replaceAll("\\?\\S*$", ""));
                comment.setDate(getJsonString(jo, "date_created"));
                comment.setLikeNum(getJsonInt(jo, "likings_count"));
                comment.setContent(getJsonString(jo, "html"));
                comment.setFloor((offset + i + 1) + "楼");
                comment.setHostID(jo.getJSONObject("post").getString("id"));
            }
        }
        return list;
    }

    /**
     * 使用Html解析方式获得帖子评论列表
     *
     * @param id
     * @param pageNo
     * @return
     */
    public static ArrayList<SimpleComment> getPostCommentsFromHtmlUrl(String id, int pageNo) {
        ArrayList<SimpleComment> list = new ArrayList<SimpleComment>();
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

    public static ArrayList<SimpleComment> extractPostComments(Element element, String postID) {
        ArrayList<SimpleComment> list = new ArrayList<SimpleComment>();
        Elements commentlist = element.getElementsByClass("comment");
        for (int i = 0; i < commentlist.size(); i++) {
            SimpleComment comment = new SimpleComment();
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
            comment.setHostID(postID);
            comment.setLikeNum(likes);
            list.add(comment);
        }
        return list;
    }

}
