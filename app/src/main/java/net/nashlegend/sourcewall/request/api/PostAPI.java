package net.nashlegend.sourcewall.request.api;

import android.net.Uri;
import android.text.TextUtils;

import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.PrepareData;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.HttpFetcher;
import net.nashlegend.sourcewall.request.RequestCache;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.MDUtil;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 单个回复地址。http://www.guokr.com/post/666281/reply/6224695/
 * 缓存key规则：
 * 我的小组的key是 Key_Post_My_Recent_Replies
 * 热门回帖的key是 Key_Post_Hot_Posts
 * 最近回复的key是 post.{id}
 */
public class PostAPI extends APIBase {

    public static final String Key_Post_Hot_Posts = "post.hot.post";
    public static final String Key_Post_My_Recent_Replies = "post.my.recent.replies";

    /**
     * 获取缓存的问题
     *
     * @param subItem 要取缓存的栏目
     */
    public static ResultObject<ArrayList<Post>> getCachedPostList(SubItem subItem) {
        ResultObject<ArrayList<Post>> cachedResultObject;
        if (subItem.getType() == SubItem.Type_Collections) {
            cachedResultObject = PostAPI.getCachedGroupHotPostListFromMobileUrl();
        } else if (subItem.getType() == SubItem.Type_Private_Channel) {
            cachedResultObject = PostAPI.getCachedMyGroupRecentRepliesPosts();
        } else {
            cachedResultObject = PostAPI.getCachedGroupPostListJson(subItem.getValue());// featured
        }
        return cachedResultObject;
    }

    /**
     * 加入小组
     *
     * @param id 小组id
     *
     * @return resultObject
     */
    public static ResultObject joinGroup(String id) {
        ResultObject resultObject = new ResultObject();
        String url = "http://www.guokr.com/apis/group/member.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("group_id", id);
        try {
            String result = HttpFetcher.post(url, pairs).toString();
            resultObject.ok = getUniversalJsonSimpleBoolean(result, resultObject);
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 退出小组
     *
     * @param id 小组id
     *
     * @return resultObject
     */
    public static ResultObject quitGroup(String id) {
        ResultObject resultObject = new ResultObject();
        String url = "http://www.guokr.com/apis/group/member.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("group_id", id);
        try {
            String result = HttpFetcher.delete(url, pairs).toString();
            resultObject.ok = getUniversalJsonSimpleBoolean(result, resultObject);
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 返回所有我加入的小组
     *
     * @return ResultObject，resultObject.result是ArrayList[SubItem]
     */
    public static ResultObject<ArrayList<SubItem>> getAllMyGroups() {
        ResultObject<ArrayList<SubItem>> resultObject = new ResultObject<>();
        if (TextUtils.isEmpty(UserAPI.getUserID())) {
            resultObject.error_message = "无法获得用户id";
            resultObject.code = ResultObject.ResultCode.CODE_NO_USER_ID;
            return resultObject;
        }
        String pageUrl = "http://m.guokr.com/group/i/" + UserAPI.getUserID() + "/joined/";
        ArrayList<SubItem> subItems = new ArrayList<>();
        int numPages;
        try {
            String firstPage = HttpFetcher.get(pageUrl).toString();
            Document doc1 = Jsoup.parse(firstPage);
            Elements lis = doc1.getElementsByClass("group-list").get(0).getElementsByTag("li");
            numPages = Integer.valueOf(doc1.getElementsByClass("page-num").text().replaceAll("1/", ""));
            //第一页
            for (int i = 0; i < lis.size(); i++) {
                Element element = lis.get(i).getElementsByTag("a").get(0);
                String groupUrl = element.attr("href");//
                String groupID = groupUrl.replaceAll("^\\D+(\\d+)\\D*", "$1");
                String groupName = element.getElementsByTag("b").text();
                SubItem subItem = new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, groupName, groupID);
                subItems.add(subItem);
            }
            if (numPages > 1) {
                for (int j = 2; j <= numPages; j++) {
                    Thread.sleep(100);
                    String url = pageUrl + "?page=" + j;
                    Document pageDoc = Jsoup.parse(HttpFetcher.get(url).toString());
                    Elements pageLis = pageDoc.getElementsByClass("group-list").get(0).getElementsByTag("li");
                    for (int i = 0; i < pageLis.size(); i++) {
                        Element element = pageLis.get(i).getElementsByTag("a").get(0);
                        String groupUrl = element.attr("href");//
                        String groupID = groupUrl.replaceAll("^\\D+(\\d+)\\D*", "$1");
                        String groupName = element.getElementsByTag("b").text();
                        SubItem subItem = new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, groupName, groupID);
                        subItems.add(subItem);
                    }
                }
            }
            resultObject.ok = true;
            resultObject.result = subItems;
        } catch (Exception e) {
            resultObject = retryGetGroups();
        }
        return resultObject;
    }

    public static ResultObject<ArrayList<SubItem>> retryGetGroups() {
        ResultObject<ArrayList<SubItem>> resultObject = new ResultObject<>();
        if (TextUtils.isEmpty(UserAPI.getUserID())) {
            resultObject.error_message = "无法获得用户id";
            resultObject.code = ResultObject.ResultCode.CODE_NO_USER_ID;
            return resultObject;
        }
        String pageUrl = "http://m.guokr.com/group/i/" + UserAPI.getUserID() + "/joined/";
        ArrayList<SubItem> subItems = new ArrayList<>();
        try {
            String firstPage = HttpFetcher.get(pageUrl).toString();
            Document doc1 = Jsoup.parse(firstPage);
            Elements lis = doc1.getElementsByClass("group-list").get(0).getElementsByTag("li");
            //第一页
            for (int i = 0; i < lis.size(); i++) {
                Element element = lis.get(i).getElementsByTag("a").get(0);
                String groupUrl = element.attr("href");//
                String groupID = groupUrl.replaceAll("^\\D+(\\d+)\\D*", "$1");
                String groupName = element.getElementsByTag("b").text();
                SubItem subItem = new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, groupName, groupID);
                subItems.add(subItem);
            }
            resultObject.ok = true;
            resultObject.result = subItems;
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 缓存的最近回复帖子
     *
     * @return resultObject
     */
    private static ResultObject<ArrayList<Post>> getCachedMyGroupRecentRepliesPosts() {
        ResultObject<ArrayList<Post>> resultObject = new ResultObject<>();
        try {
            String html = RequestCache.getInstance().getStringFromCache(Key_Post_My_Recent_Replies);
            if (html != null) {
                resultObject = parseMyGroupPostList(html);
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }


    /**
     * @param html 解析我的小组的帖子
     */
    private static ResultObject<ArrayList<Post>> parseMyGroupPostList(String html) {
        ResultObject<ArrayList<Post>> resultObject = new ResultObject<>();
        try {
            ArrayList<Post> list = new ArrayList<>();
            Document doc = Jsoup.parse(html);
            Elements elements = doc.getElementsByClass("post-list");
            if (elements.size() == 1) {
                Elements postlist = elements.get(0).getElementsByTag("li");
                for (Element aPostlist : postlist) {
                    Post item = new Post();
                    Element link = aPostlist.getElementsByClass("post").get(0);
                    String postTitle = link.getElementsByTag("h4").get(0).text();
                    String postUrl = link.attr("href");
                    String postImageUrl = "";
                    String postAuthor = "";//没有Author名……
                    String postGroup = aPostlist.getElementsByClass("post-author").get(0).text();//没错，post-author是小组名……
                    Elements children = aPostlist.getElementsByClass("post-info-right").get(0).children();
                    int postLike = Integer.valueOf(children.get(0).text().replaceAll("\\D*", ""));
                    int postComm = Integer.valueOf(children.get(1).text().replaceAll("\\D*", ""));
                    item.setTitle(postTitle);
                    item.setUrl(postUrl);
                    item.setId(postUrl.replaceAll("\\?.*$", "").replaceAll("\\D+", ""));
                    item.setTitleImageUrl(postImageUrl);
                    item.setAuthor(postAuthor);
                    item.setGroupName(postGroup);
                    item.setLikeNum(postLike);
                    item.setReplyNum(postComm);
                    item.setFeatured(false);
                    list.add(item);
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
     * 返回《我的小组》最新回复的主题列表，解析html获得
     *
     * @param offset offset
     *
     * @return resultObject
     */
    public static ResultObject<ArrayList<Post>> getMyGroupRecentRepliesPostsByJson(int offset) {
        String url = "http://apis.guokr.com/group/post.json";
        ResultObject<ArrayList<Post>> resultObject = new ResultObject<>();
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("retrieve_type", "recent_replies");
            pairs.put("limit", "20");
            pairs.put("offset", offset + "");
            String html = HttpFetcher.get(url, pairs, true).toString();
            resultObject = parsePostListJson(html);
            if (resultObject.ok && offset == 0) {
                RequestCache.getInstance().addStringToCacheForceUpdate(Key_Post_My_Recent_Replies, html);
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 获得小组热贴，json
     *
     * @param offset，要获取的页码
     *
     * @return 帖子列表
     */
    public static ResultObject<ArrayList<Post>> getGroupHotPostListByJson(int offset) {
        ResultObject<ArrayList<Post>> resultObject = new ResultObject<>();
        try {
            String url = "http://apis.guokr.com/group/post.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("retrieve_type", "hot_post");
            pairs.put("limit", "20");
            pairs.put("offset", offset + "");
            String json = HttpFetcher.get(url, pairs, true).toString();
            resultObject = parsePostListJson(json);
            if (resultObject.ok && offset == 0) {
                RequestCache.getInstance().addStringToCacheForceUpdate(Key_Post_Hot_Posts, json);
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 获得缓存的小组热贴
     *
     * @return 帖子列表
     */
    public static ResultObject<ArrayList<Post>> getCachedGroupHotPostListFromMobileUrl() {
        ResultObject<ArrayList<Post>> resultObject = new ResultObject<>();
        try {
            String html = RequestCache.getInstance().getStringFromCache(Key_Post_Hot_Posts);
            if (html != null) {
                resultObject = parsePostListJson(html);
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 根据小组id获得帖子列表，json格式
     *
     * @param id     小组id
     * @param offset 从第几个帖子开始取
     *
     * @return resultObject
     */
    public static ResultObject<ArrayList<Post>> getGroupPostListByJsonUrl(String id, int offset) {
        ResultObject<ArrayList<Post>> resultObject = new ResultObject<>();
        try {
            String url = "http://apis.guokr.com/group/post.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("retrieve_type", "by_group");
            pairs.put("group_id", id);
            pairs.put("limit", "20");
            pairs.put("offset", offset + "");
            String jString = HttpFetcher.get(url, pairs).toString();
            resultObject = parsePostListJson(jString);
            if (resultObject.ok && offset == 0) {
                //请求成功则缓存之
                String key = "post." + id;
                RequestCache.getInstance().addStringToCacheForceUpdate(key, jString);
            }

        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 根据小组id获得缓存的帖子列表，json格式
     *
     * @param id 小组id
     *
     * @return resultObject
     */
    public static ResultObject<ArrayList<Post>> getCachedGroupPostListJson(String id) {
        ResultObject<ArrayList<Post>> resultObject = new ResultObject<>();
        try {
            String jString = RequestCache.getInstance().getStringFromCache("post." + id);
            if (jString != null) {
                resultObject = parsePostListJson(jString);
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    private static ResultObject<ArrayList<Post>> parsePostListJson(String jString) {
        ResultObject<ArrayList<Post>> resultObject = new ResultObject<>();
        try {
            ArrayList<Post> list = new ArrayList<>();
            JSONArray articles = APIBase.getUniversalJsonArray(jString, resultObject);
            if (articles != null) {
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject jo = articles.getJSONObject(i);
                    Post post = new Post();
                    post.setId(getJsonString(jo, "id"));
                    post.setGroupName(getJsonString(jo.getJSONObject("group"), "name"));
                    post.setTitle(getJsonString(jo, "title"));
                    post.setUrl(getJsonString(jo, "url"));
                    JSONObject authorObject = getJsonObject(jo, "author");
                    boolean exists = getJsonBoolean(authorObject, "is_exists");
                    post.setAuthorExists(exists);
                    if (exists) {
                        post.setAuthor(getJsonString(authorObject, "nickname"));
                        post.setAuthorID(getJsonString(authorObject, "url").replaceAll("\\D+", ""));
                        post.setAuthorAvatarUrl(getJsonObject(authorObject, "avatar").getString("large").replaceAll("\\?.*$", ""));
                    } else {
                        post.setAuthor("此用户不存在");
                    }
                    post.setDate(parseDate(getJsonString(jo, "date_created")));
                    post.setReplyNum(getJsonInt(jo, "replies_count"));
                    post.setLikeNum(getJsonInt(jo, "recommends_count"));
                    post.setContent(getJsonString(jo, "html"));
                    post.setFeatured(true);
                    // 无法获取titleImageUrl，也用不着，太TMD费流量了
                    list.add(post);
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
     * 根据帖子id获取帖子内容，json格式
     *
     * @param id，帖子id
     *
     * @return resultObject
     */
    public static ResultObject<Post> getPostDetailByIDFromJsonUrl(String id) {
        ResultObject<Post> resultObject = new ResultObject<>();
        String url = "http://apis.guokr.com/group/post/" + id + ".json";
        try {
            Post detail = new Post();
            ResultObject response = HttpFetcher.get(url);
            resultObject.statusCode = response.statusCode;
            if (resultObject.statusCode == 404) {
                return resultObject;
            }
            String json = response.toString();
            JSONObject postResult = getUniversalJsonObject(json, resultObject);
            if (postResult != null) {
                String postID = getJsonString(postResult, "id");
                String title = getJsonString(postResult, "title");
                String date = getJsonString(postResult, "date_created");
                String content = "<div id=\"postContent\" class=\"html-text-mixin gbbcode-content\">" + getJsonString(postResult, "html") + "</div>";
                //int likeNum = getJsonInt(postResult, "");//取不到like数量
                int recommendNum = getJsonInt(postResult, "recommends_count");
                int reply_num = getJsonInt(postResult, "replies_count");

                JSONObject authorObject = getJsonObject(postResult, "author");
                String authorAvatarUrl = getJsonObject(authorObject, "avatar").getString("large").replaceAll("\\?.*$", "");
                String author = getJsonString(authorObject, "nickname");
                String authorID = getJsonString(authorObject, "url").replaceAll("\\D+", "");

                JSONObject groupObject = getJsonObject(postResult, "group");
                String groupName = getJsonString(groupObject, "name");
                String groupID = getJsonString(postResult, "group_id");

                detail.setGroupID(groupID);
                detail.setGroupName(groupName);
                detail.setAuthor(author);
                detail.setAuthorAvatarUrl(authorAvatarUrl);
                detail.setAuthorID(authorID);
                detail.setId(postID);
                detail.setTitle(title);
                detail.setDate(parseDate(date));
                detail.setContent(content);
                detail.setReplyNum(reply_num);
                resultObject.ok = true;
                resultObject.result = detail;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 使用Json解析方式获得帖子评论列表
     * resultObject.result是ArrayList[UComment]
     *
     * @param id     帖子id
     * @param offset 从第offset个开始加载
     * @param limit  要加载多少个
     *
     * @return resultObject
     */
    public static ResultObject<ArrayList<AceModel>> getPostCommentsFromJsonUrl(String id, int offset, int limit) {
        ResultObject<ArrayList<AceModel>> resultObject = new ResultObject<>();
        try {
            ArrayList<AceModel> list = new ArrayList<>();
            String url = "http://apis.guokr.com/group/post_reply.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("retrieve_type", "by_post");
            pairs.put("post_id", id);
            pairs.put("limit", limit + "");
            pairs.put("offset", offset + "");
            String jString = HttpFetcher.get(url, pairs).toString();
            JSONArray comments = getUniversalJsonArray(jString, resultObject);
            if (comments != null) {
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jo = comments.getJSONObject(i);
                    UComment comment = new UComment();
                    comment.setID(getJsonString(jo, "id"));
                    comment.setHasLiked(jo.getBoolean("current_user_has_liked"));//始终是false，不知怎么搞了，猜不出
                    JSONObject authorObject = getJsonObject(jo, "author");
                    boolean exists = getJsonBoolean(authorObject, "is_exists");
                    comment.setAuthorExists(exists);
                    if (exists) {
                        comment.setAuthor(getJsonString(authorObject, "nickname"));
                        comment.setAuthorID(getJsonString(authorObject, "url").replaceAll("\\D+", ""));
                        comment.setAuthorAvatarUrl(getJsonObject(authorObject, "avatar").getString("large").replaceAll("\\?.*$", ""));
                    } else {
                        comment.setAuthor("此用户不存在");
                    }
                    comment.setDate(parseDate(getJsonString(jo, "date_created")));
                    comment.setLikeNum(getJsonInt(jo, "likings_count"));
                    comment.setContent(getJsonString(jo, "html"));
                    //                    comment.setFloor((offset + i + 1) + "楼");
                    comment.setFloor(getJsonInt(jo, "level") + "楼");
                    comment.setHostID(jo.getJSONObject("post").getString("id"));
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
     * 赞一个帖子
     *
     * @param postID 帖子id
     *
     * @return resultObject
     */
    public static ResultObject likePost(String postID) {
        String url = "http://www.guokr.com/apis/group/post_liking.json";
        ResultObject resultObject = new ResultObject();
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("post_id", postID);
            String result = HttpFetcher.post(url, pairs).toString();
            if (getUniversalJsonSimpleBoolean(result, resultObject)) {
                resultObject.ok = true;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 回复一个帖子，使用json请求，所以格式简单
     * 回复一个评论不过是在回复帖子的时候@了这个人而已
     *
     * @param id      帖子id
     * @param content 回复内容
     *
     * @return ResultObject.result is the reply_id if ok;
     */
    public static ResultObject<String> replyPost(String id, String content) {
        ResultObject<String> resultObject = new ResultObject<>();
        try {
            String url = "http://apis.guokr.com/group/post_reply.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("post_id", id);
            pairs.put("content", content);
            String result = HttpFetcher.post(url, pairs).toString();
            JSONObject resultJson = getUniversalJsonObject(result, resultObject);
            if (resultJson != null) {
                String replyID = getJsonString(resultJson, "id");
                resultObject.ok = true;
                resultObject.result = replyID;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 根据一条通知的id获取所有内容
     * 先是：http://www.guokr.com/user/notice/8738252/
     * 跳到：http://www.guokr.com/post/reply/654321/
     *
     * @param notice_id 通知id
     *
     * @return resultObject resultObject.result是UComment
     */
    public static ResultObject<UComment> getSingleCommentByNoticeID(String notice_id) {
        ResultObject<UComment> resultObject = new ResultObject<>();
        String reply_id;
        if (TextUtils.isEmpty(notice_id)) {
            return resultObject;
        }
        String notice_url = "http://www.guokr.com/user/notice/" + notice_id + "/";
        try {
            ResultObject httpResult = HttpFetcher.get(notice_url);
            String replyRedirectResult = httpResult.toString();
            Document document = Jsoup.parse(replyRedirectResult);
            Elements elements = document.getElementsByTag("a");
            if (elements.size() == 1) {
                ///post/662450/?page=2#6150472
                ///post/662632/#6148664
                Matcher matcher = Pattern.compile("^/post/(\\d+)/.*#(\\d+)$").matcher(elements.get(0).text());
                if (matcher.find()) {
                    reply_id = matcher.group(2);
                    return getSingleCommentByID(reply_id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 根据一条评论的地址获取所有内容，无需跳转，直接调用getSingleCommentByID即可
     *
     * @param url 评论id
     *
     * @return resultObject resultObject.result是UComment
     */
    public static ResultObject<UComment> getSingleCommentFromRedirectUrl(String url) {
        //url sample：http://www.guokr.com/post/reply/6224695/
        //uri http://www.guokr.com/post/666281/reply/6224695/
        //TODO
        Uri uri = Uri.parse(url);
        List<String> segments = uri.getPathSegments();
        String id = "-1";
        if (segments.size() > 0) {
            id = segments.get(segments.size() - 1);
        }
        return getSingleCommentByID(id);
    }

    /**
     * 根据一条评论的id获取评论内容，主要应用于消息通知
     *
     * @param id 评论id
     *
     * @return resultObject resultObject.result是UComment
     */
    public static ResultObject<UComment> getSingleCommentByID(String id) {
        ResultObject<UComment> resultObject = new ResultObject<>();
        String url = "http://www.guokr.com/apis/group/post_reply.json";
        //url还有另一种形式，http://www.guokr.com/apis/group/post_reply/99999999.json
        //这样后面就不必带reply_id参数了
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("reply_id", id);
        try {
            UComment comment = new UComment();

            String result = HttpFetcher.get(url, pairs).toString();
            JSONObject replyObject = getUniversalJsonObject(result, resultObject);
            JSONObject postObject = getJsonObject(replyObject, "post");
            String hostTitle = getJsonString(postObject, "title");
            String hostID = getJsonString(postObject, "id");
            boolean hasLiked = getJsonBoolean(replyObject, "current_user_has_liked");
            String floor = getJsonString(replyObject, "level");
            String date = parseDate(getJsonString(replyObject, "date_created"));
            int likeNum = getJsonInt(replyObject, "likings_count");
            String content = getJsonString(replyObject, "html");

            JSONObject authorObject = getJsonObject(replyObject, "author");
            boolean is_exists = getJsonBoolean(authorObject, "is_exists");
            if (is_exists) {
                String author = getJsonString(authorObject, "nickname");
                String authorID = getJsonString(authorObject, "url").replaceAll("\\D+", "");
                String authorTitle = getJsonString(authorObject, "title");
                JSONObject avatarObject = getJsonObject(authorObject, "avatar");
                String avatarUrl = getJsonString(avatarObject, "large").replaceAll("\\?.*$", "");

                comment.setAuthor(author);
                comment.setAuthorTitle(authorTitle);
                comment.setAuthorID(authorID);
                comment.setAuthorAvatarUrl(avatarUrl);
            } else {
                comment.setAuthor("此用户不存在");
            }

            comment.setHostTitle(hostTitle);
            comment.setHostID(hostID);
            comment.setHasLiked(hasLiked);
            comment.setFloor(floor + "楼");
            comment.setDate(date);
            comment.setLikeNum(likeNum);
            comment.setContent(content);
            comment.setID(id);
            resultObject.ok = true;
            resultObject.result = comment;
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 删除我的评论
     *
     * @param id 评论id
     *
     * @return resultObject
     */
    public static ResultObject deleteMyComment(String id) {
        ResultObject resultObject = new ResultObject();
        String url = "http://www.guokr.com/apis/group/post_reply.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("reply_id", id);
        pairs.put("reason", id);
        try {
            String result = HttpFetcher.delete(url, pairs).toString();
            resultObject.ok = getUniversalJsonSimpleBoolean(result, resultObject);
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 赞一个评论
     *
     * @param id 评论id
     *
     * @return resultObject
     */
    public static ResultObject likeComment(String id) {
        String url = "http://www.guokr.com/apis/group/post_reply_liking.json";
        ResultObject resultObject = new ResultObject();
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("reply_id", id);
            String result = HttpFetcher.post(url, pairs).toString();
            if (getUniversalJsonSimpleBoolean(result, resultObject)) {
                resultObject.ok = true;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 获取发帖所需的csrf和topic列表
     * resultObject.result是PostPrepareData
     *
     * @param group_id 小组id
     *
     * @return resultObject
     */
    public static ResultObject<PrepareData> getPostPrepareData(String group_id) {
        ResultObject<PrepareData> resultObject = new ResultObject<>();
        try {
            String url = "http://www.guokr.com/group/" + group_id + "/post/edit/";
            ResultObject response = HttpFetcher.get(url);
            resultObject.statusCode = response.statusCode;
            String html = response.toString();
            Document doc = Jsoup.parse(html);
            Element selects = doc.getElementById("topic");
            ArrayList<BasicNameValuePair> pairs = new ArrayList<>();
            String csrf = doc.getElementById("csrf_token").attr("value");
            if (selects != null) {
                Elements elements = selects.getElementsByTag("option");
                if (elements != null && elements.size() > 0) {
                    for (int i = 0; i < elements.size(); i++) {
                        Element topic = elements.get(i);
                        String name = topic.text();
                        String value = topic.attr("value");
                        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                            pairs.add(new BasicNameValuePair(name, value));
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(csrf)) {
                PrepareData prepareData = new PrepareData();
                prepareData.setCsrf(csrf);
                prepareData.setPairs(pairs);
                resultObject.ok = true;
                resultObject.result = prepareData;
            }

        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 发帖
     *
     * @param group_id 小组id
     * @param csrf     csrf_token
     * @param title    标题
     * @param body     帖子内容   html格式
     * @param topic    帖子主题
     *
     * @return resultObject
     */
    public static ResultObject<String> publishPost(String group_id, String csrf, String title, String body, String topic) {
        ResultObject<String> resultObject = new ResultObject<>();
        String url = "http://www.guokr.com/group/" + group_id + "/post/edit/";
        try {
            ResultObject<String> mdResult = MDUtil.parseMarkdownByGitHub(body);
            String htmlBody = "";
            if (mdResult.ok) {
                //使用github接口转换成html
                htmlBody = mdResult.result;
            } else {
                htmlBody = MDUtil.Markdown2HtmlDumb(body);
            }
            htmlBody += Config.getComplexReplyTail();
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("csrf_token", csrf);
            pairs.put("title", title);
            pairs.put("topic", topic);
            pairs.put("body", htmlBody);
            pairs.put("captcha", "");
            pairs.put("share_opts", "activity");

            ResultObject result = HttpFetcher.post(url, pairs, false);
            if (result.statusCode == 302) {
                try {
                    String replyRedirectResult = result.toString();
                    Document document = Jsoup.parse(replyRedirectResult);
                    Elements elements = document.getElementsByTag("a");
                    if (elements.size() == 1) {
                        Matcher matcher = Pattern.compile("^/post/(\\d+)/$").matcher(elements.get(0).text());
                        if (matcher.find()) {
                            String post_id = matcher.group(1);
                            resultObject.ok = true;
                            resultObject.result = post_id;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    private static boolean testPublishResult(String res) {
        try {
            Document doc = Jsoup.parse(res);
            String href = doc.getElementsByTag("a").attr("href");
            return href.matches("/post/\\d+[/]?");
        } catch (Exception e) {
            return false;
        }
    }

}
