package net.nashlegend.sourcewall.request.api;

import android.net.Uri;
import android.text.TextUtils;

import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.PrepareData;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.HttpFetcher;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.RequestBuilder;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.RequestObject.CallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.parsers.BooleanParser;
import net.nashlegend.sourcewall.request.parsers.ContentValueForKeyParser;
import net.nashlegend.sourcewall.request.parsers.DirectlyStringParser;
import net.nashlegend.sourcewall.request.parsers.Parser;
import net.nashlegend.sourcewall.request.parsers.PostListParser;
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

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PostAPI extends APIBase {

    public static final String Key_Post_Hot_Posts = "post.hot.post";
    public static final String Key_Post_My_Recent_Replies = "post.my.recent.replies";

    public static Observable<ResponseObject<ArrayList<Post>>> getPostList(int type, String key, int offset, boolean useCache) {
        String url = "http://apis.guokr.com/group/post.json";
        HashMap<String, String> pairs = new HashMap<>();
        switch (type) {
            case SubItem.Type_Collections:
                pairs.put("retrieve_type", "hot_post");
                break;
            case SubItem.Type_Private_Channel:
                pairs.put("retrieve_type", "recent_replies");
                break;
            case SubItem.Type_Single_Channel:
                pairs.put("retrieve_type", "by_group");
                pairs.put("group_id", key);
                break;
        }
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<Post>>()
                .setUrl(url)
                .get()
                .setParams(pairs)
                .useCacheFirst(useCache)
                .cacheTimeOut(300000)
                .setParser(new PostListParser())
                .requestObservable();
    }

    /**
     * 加入小组
     *
     * @param id 小组id
     * @return resultObject
     */
    public static void joinGroup(String id, CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/member.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("group_id", id);
        new RequestBuilder<Boolean>()
                .setUrl(url)
                .post()
                .setParams(pairs)
                .setParser(new BooleanParser())
                .setRequestCallBack(callBack)
                .startRequest();
    }

    /**
     * 退出小组
     *
     * @param id 小组id
     * @return resultObject
     */
    public static void quitGroup(String id, CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/member.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("group_id", id);
        new RequestBuilder<Boolean>()
                .setUrl(url)
                .delete()
                .setParams(pairs)
                .setParser(new BooleanParser())
                .setRequestCallBack(callBack)
                .startRequest();
    }

    public static Observable<ResponseObject<ArrayList<SubItem>>> getAllMyGroups(String ukey) {
        String url = "http://apis.guokr.com/group/member.json";
        return new RequestBuilder<ArrayList<SubItem>>()
                .setUrl(url)
                .get()
                .addParam("retrieve_type", "by_user")
                .addParam("ukey", ukey)
                .addParam("limit", "999")
                .setParser(new Parser<ArrayList<SubItem>>() {
                    @Override
                    public ArrayList<SubItem> parse(String response, ResponseObject<ArrayList<SubItem>> responseObject) throws Exception {
                        ArrayList<SubItem> list = new ArrayList<>();
                        JSONArray subItems = JsonHandler.getUniversalJsonArray(response, responseObject);
                        assert subItems != null;
                        for (int i = 0; i < subItems.length(); i++) {
                            JSONObject jo = subItems.getJSONObject(i).optJSONObject("group");
                            SubItem subItem = new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, jo.optString("name"), jo.optString("id"));
                            list.add(subItem);
                        }
                        return list;
                    }
                })
                .requestObservable();
    }

    /**
     * 根据帖子id获取帖子内容，json格式
     *
     * @param id，帖子id
     * @return resultObject
     */
    public static Observable<ResponseObject<Post>> getPostDetailByID(String id) {
        String url = "http://apis.guokr.com/group/post/" + id + ".json";
        return new RequestBuilder<Post>()
                .setUrl(url)
                .useCacheIfFailed(true)
                .get()
                .setParser(new Parser<Post>() {
                    @Override
                    public Post parse(String response, ResponseObject<Post> responseObject) throws Exception {
                        JSONObject postResult = JsonHandler.getUniversalJsonObject(response, responseObject);
                        return Post.fromJson(postResult);
                    }
                })
                .requestObservable();
    }

    /**
     * 获取文章评论，json格式
     * resultObject.result是ArrayList[UComment]
     *
     * @param id     article ID
     * @param offset 从第几个开始加载
     * @param limit
     * @return ResponseObject
     */
    public static Observable<ResponseObject<ArrayList<UComment>>> getPostReplies(final String id, final int offset, int limit) {
        String url = "http://apis.guokr.com/group/post_reply.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("retrieve_type", "by_post");
        pairs.put("post_id", id);
        pairs.put("limit", limit + "");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<UComment>>()
                .setUrl(url)
                .get()
                .setParams(pairs)
                .useCacheIfFailed(offset == 0)
                .setParser(new Parser<ArrayList<UComment>>() {
                    @Override
                    public ArrayList<UComment> parse(String response, ResponseObject<ArrayList<UComment>> responseObject) throws Exception {
                        ArrayList<UComment> list = new ArrayList<>();
                        JSONArray comments = JsonHandler.getUniversalJsonArray(response, responseObject);
                        assert comments != null;
                        for (int i = 0; i < comments.length(); i++) {
                            JSONObject jo = comments.getJSONObject(i);
                            UComment comment = UComment.fromPostJson(jo);
                            list.add(comment);
                        }
                        return list;
                    }
                })
                .requestObservable();
    }

    /**
     * 赞一个帖子
     *
     * @param postID 帖子id
     * @return resultObject
     */
    public static RequestObject<Boolean> likePost(String postID, CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/post_liking.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("post_id", postID);
        return new RequestBuilder<Boolean>()
                .setUrl(url)
                .setParser(new BooleanParser())
                .post()
                .setRequestCallBack(callBack)
                .setParams(pairs)
                .startRequest();
    }

    /**
     * 回复一个帖子，使用json请求，所以格式简单
     * 回复一个评论不过是在回复帖子的时候@了这个人而已
     *
     * @param id      帖子id
     * @param content 回复内容
     * @return ResponseObject.result is the reply_id if ok;
     */
    public static RequestObject<String> replyPost(String id, String content, CallBack<String> callBack) {
        String url = "http://apis.guokr.com/group/post_reply.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("post_id", id);
        pairs.put("content", content);
        return new RequestBuilder<String>()
                .setUrl(url)
                .setParser(new ContentValueForKeyParser("id"))
                .setRequestCallBack(callBack)
                .setParams(pairs)
                .post()
                .startRequest();
    }

    /**
     * 匿名回复
     *
     * @param id       帖子id
     * @param content  回复内容
     * @param callBack
     * @return ResponseObject.result is the reply_id if ok;
     */
    public static RequestObject<String> replyPostAnonymous(String id, String content, CallBack<String> callBack) {
        // TODO: 16/4/27
        return null;
    }

    /**
     * 根据一条通知的id获取所有内容
     * 先是：http://www.guokr.com/user/notice/8738252/
     * 跳到：http://www.guokr.com/post/reply/654321/
     *
     * @param notice_id 通知id
     * @return resultObject resultObject.result是UComment
     */
    public static Observable<UComment> getSingleCommentByNotice(String notice_id) {
        String notice_url = "http://www.guokr.com/user/notice/" + notice_id + "/";
        return new RequestBuilder<String>()
                .setUrl(notice_url)
                .get()
                .setWithToken(false)
                .setParser(new DirectlyStringParser())
                .requestObservable()
                .flatMap(new Func1<ResponseObject<String>, Observable<String>>() {
                    @Override
                    public Observable<String> call(ResponseObject<String> response) {
                        Document document = Jsoup.parse(response.result);
                        Elements elements = document.getElementsByTag("a");
                        if (elements.size() == 1) {
                            Matcher matcher = Pattern.compile("^/post/(\\d+)/.*#(\\d+)$").matcher(elements.get(0).text());
                            if (matcher.find()) {
                                String reply_id = matcher.group(2);
                                return Observable.just(reply_id);
                            }
                        }
                        return Observable.error(new IllegalStateException("not a correct redirect content"));
                    }
                })
                .flatMap(new Func1<String, Observable<UComment>>() {
                    @Override
                    public Observable<UComment> call(String id) {
                        return getSingleCommentByID(id);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * 根据一条评论的地址获取所有内容，无需跳转，直接调用getSingleCommentByID即可
     *
     * @param url 评论id
     * @return resultObject resultObject.result是UComment
     */
    public static Observable<UComment> getSingleCommentFromRedirectUrl(String url) {
        //url sample：http://www.guokr.com/post/reply/6224695/
        //uri http://www.guokr.com/post/666281/reply/6224695/
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
     * @return resultObject resultObject.result是UComment
     */
    public static Observable<UComment> getSingleCommentByID(String id) {
//        String url = "http://www.guokr.com/apis/group/post_reply.json";
        String url = "http://www.guokr.com/apis/group/post_reply/" + id + ".json";
        //这样后面就不必带reply_id参数了
        return new RequestBuilder<UComment>()
                .setUrl(url)
                .get()
                .setParser(new Parser<UComment>() {
                    @Override
                    public UComment parse(String response, ResponseObject<UComment> responseObject) throws Exception {
                        JSONObject replyObject = JsonHandler.getUniversalJsonObject(response, responseObject);
                        return UComment.fromPostJson(replyObject);
                    }
                })
                .requestObservable()
                .flatMap(new Func1<ResponseObject<UComment>, Observable<UComment>>() {
                    @Override
                    public Observable<UComment> call(ResponseObject<UComment> responseObject) {
                        if (responseObject.ok) {
                            return Observable.just(responseObject.result);
                        } else {
                            return Observable.error(new IllegalStateException("Error Found"));
                        }
                    }
                });
    }

    /**
     * 删除我的评论
     *
     * @param id 评论id
     * @return resultObject
     */
    public static RequestObject<Boolean> deleteMyComment(String id, CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/post_reply.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("reply_id", id);
        pairs.put("reason", id);
        return new RequestBuilder<Boolean>()
                .setUrl(url)
                .setParser(new BooleanParser())
                .delete()
                .setRequestCallBack(callBack)
                .setParams(pairs)
                .startRequest();
    }

    /**
     * 赞一个评论
     *
     * @param id 评论id
     * @return resultObject
     */
    public static RequestObject<Boolean> likeComment(String id, CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/post_reply_liking.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("reply_id", id);
        return new RequestBuilder<Boolean>()
                .setUrl(url)
                .post()
                .setParser(new BooleanParser())
                .setRequestCallBack(callBack)
                .setParams(pairs)
                .startRequest();
    }

    /**
     * 获取发帖所需的csrf和topic列表
     * resultObject.result是PostPrepareData
     *
     * @param group_id 小组id
     * @return resultObject
     */
    public static RequestObject<PrepareData> getPostPrepareData(String group_id, CallBack<PrepareData> callBack) {
        String url = "http://www.guokr.com/group/" + group_id + "/post/edit/";
        return new RequestBuilder<PrepareData>()
                .setUrl(url)
                .get()
                .setParser(new Parser<PrepareData>() {
                    @Override
                    public PrepareData parse(String str, ResponseObject<PrepareData> responseObject) throws Exception {
                        Document doc = Jsoup.parse(str);
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
                        PrepareData prepareData = new PrepareData();
                        responseObject.ok = !TextUtils.isEmpty(csrf);
                        if (!TextUtils.isEmpty(csrf)) {
                            prepareData.setCsrf(csrf);
                            prepareData.setPairs(pairs);
                        }
                        return prepareData;
                    }
                })
                .setRequestCallBack(callBack)
                .startRequest();
    }

    /**
     * 发帖
     * TODO
     * 有Json方式的删贴，有空加上。
     * http://www.guokr.com/apis/group/post.json?reason={}&post_id={}&access_token={}  //
     * request method = delete/put
     *
     * @param group_id 小组id
     * @param csrf     csrf_token
     * @param title    标题
     * @param body     帖子内容   html格式
     * @param topic    帖子主题
     * @return resultObject
     */
    public static ResponseObject<String> publishPost(String group_id, String csrf, String title, String body, String topic) {
        ResponseObject<String> resultObject = new ResponseObject<>();
        String url = "http://www.guokr.com/group/" + group_id + "/post/edit/";
        try {
            ResponseObject<String> mdResult = MDUtil.parseMarkdownByGitHub(body);
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

            ResponseObject result = HttpFetcher.post(url, pairs, false);
            //这里已经将302手动设置为了200，所以
            if (result.statusCode == 200) {
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
            JsonHandler.handleRequestException(e, resultObject);
        }
        return resultObject;
    }

}
