package net.nashlegend.sourcewall.request.api;

import android.net.Uri;
import android.text.TextUtils;

import net.nashlegend.sourcewall.data.Mob;
import net.nashlegend.sourcewall.data.database.GroupHelper;
import net.nashlegend.sourcewall.data.database.gen.MyGroup;
import net.nashlegend.sourcewall.events.Emitter;
import net.nashlegend.sourcewall.events.GroupFetchedEvent;
import net.nashlegend.sourcewall.fragment.PostPagerFragment;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.PrepareData;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.ParamsMap;
import net.nashlegend.sourcewall.request.RequestBuilder;
import net.nashlegend.sourcewall.request.RequestObject.RequestCallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.parsers.BooleanParser;
import net.nashlegend.sourcewall.request.parsers.ContentValueForKeyParser;
import net.nashlegend.sourcewall.request.parsers.GroupListParser;
import net.nashlegend.sourcewall.request.parsers.Parser;
import net.nashlegend.sourcewall.request.parsers.PostCommentListParser;
import net.nashlegend.sourcewall.request.parsers.PostCommentParser;
import net.nashlegend.sourcewall.request.parsers.PostHtmlListParser;
import net.nashlegend.sourcewall.request.parsers.PostListParser;
import net.nashlegend.sourcewall.request.parsers.PostParser;
import net.nashlegend.sourcewall.request.parsers.PostPrepareDataParser;
import net.nashlegend.sourcewall.request.parsers.PublishPostParser;
import net.nashlegend.sourcewall.request.parsers.StringParser;
import net.nashlegend.sourcewall.simple.SimpleSubscriber;
import net.nashlegend.sourcewall.util.MDUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static net.nashlegend.sourcewall.data.Tail.getComplexReplyTail;
import static net.nashlegend.sourcewall.data.Tail.getDefaultComplexTail;

public class PostAPI extends APIBase {


    public static NetworkTask<String> getGroupNameById(String id, RequestCallBack<String> callBack) {
        String url = "http://m.guokr.com/group/" + id.trim() + "/";
        return new RequestBuilder<String>()
                .get()
                .url(url)
                .callback(callBack)
                .useCacheFirst(true, Integer.MAX_VALUE)
                .parser(new Parser<String>() {
                    @Override
                    public String parse(String response, ResponseObject<String> responseObject) throws Exception {
                        String name = Jsoup.parse(response).getElementsByClass("group-name").text().trim();
                        responseObject.ok = true;
                        return name;
                    }
                })
                .requestAsync();
    }

    public static NetworkTask<Boolean> reportPost(String postId, String reason, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/post/" + postId + "/";
        return UserAPI.report(url, reason, callBack);
    }

    public static NetworkTask<Boolean> reportReply(String replyId, String reason, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/post/reply/" + replyId + "/";
        return UserAPI.report(url, reason, callBack);
    }

    public static NetworkTask<ArrayList<Post>> getPostListByUser(String ukey, int offset, RequestCallBack<ArrayList<Post>> callBack) {
        String url = "http://apis.guokr.com/group/post.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("retrieve_type", "by_user");
        pairs.put("ukey", ukey);
        pairs.put("limit", "20");
        pairs.put("offset", offset);
        return new RequestBuilder<ArrayList<Post>>()
                .get()
                .url(url)
                .params(pairs)
                .callback(callBack)
                .useCacheIfFailed(true)
                .parser(new PostListParser())
                .requestAsync();
    }

    public static Observable<ResponseObject<ArrayList<Post>>> getPostList(final int type, String key, int page, boolean useCache) {
        String url = "http://apis.guokr.com/group/post.json";
        ParamsMap pairs = new ParamsMap();
        long timeout = 600000;
        switch (type) {
            case SubItem.Type_Collections:
                pairs.put("retrieve_type", "hot_post");
                break;
            case SubItem.Type_Private_Channel:
                pairs.put("retrieve_type", "recent_replies");
                timeout = 60000;
                break;
            case SubItem.Type_Single_Channel:
                pairs.put("retrieve_type", "by_group");
                pairs.put("group_id", key);
                break;
        }
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(page * 20));
        return new RequestBuilder<ArrayList<Post>>()
                .get()
                .url(url)
                .params(pairs)
                .useCacheFirst(useCache)
                .cacheTimeOut(timeout)
                .parser(new Parser<ArrayList<Post>>() {
                    @Override
                    public ArrayList<Post> parse(String response, ResponseObject<ArrayList<Post>> responseObject) throws Exception {
                        PostListParser parser = new PostListParser();
                        ArrayList<Post> posts = parser.parse(response, responseObject);
                        if (type == SubItem.Type_Single_Channel) {
                            for (Post post : posts) {
                                post.setFeatured(true);
                            }
                        }
                        return posts;
                    }
                })
                .flatMap();
    }

    /**
     * 与getPostList相同,只是加载热门时使用Html加载
     *
     * @param type
     * @param key
     * @param page     从0开始
     * @param useCache
     * @return
     */
    public static Observable<ResponseObject<ArrayList<Post>>> getPostListHotHtml(final int type, String key, int page, boolean useCache) {
        String url = "http://apis.guokr.com/group/post.json";
        ParamsMap pairs = new ParamsMap();
        long timeout = 600000;
        switch (type) {
            case SubItem.Type_Collections:
                url = "http://m.guokr.com/group/hot_posts/";
                pairs.put("page", page + 1);
                break;
            case SubItem.Type_Private_Channel:
                pairs.put("retrieve_type", "recent_replies");
                pairs.put("retrieve_type", "hot_post");
                pairs.put("offset", String.valueOf(page * 20));
                timeout = 60000;
                break;
            case SubItem.Type_Single_Channel:
                pairs.put("retrieve_type", "by_group");
                pairs.put("group_id", key);
                pairs.put("limit", "20");
                pairs.put("offset", String.valueOf(page * 20));
                break;
        }
        return new RequestBuilder<ArrayList<Post>>()
                .get()
                .url(url)
                .params(pairs)
                .useCacheFirst(useCache)
                .cacheTimeOut(timeout)
                .parser(new Parser<ArrayList<Post>>() {
                    @Override
                    public ArrayList<Post> parse(String response, ResponseObject<ArrayList<Post>> responseObject) throws Exception {
                        Parser<ArrayList<Post>> parser = null;
                        if (type == SubItem.Type_Collections) {
                            parser = new PostHtmlListParser();
                        } else {
                            parser = new PostListParser();
                        }
                        ArrayList<Post> posts = parser.parse(response, responseObject);
                        if (type == SubItem.Type_Single_Channel) {
                            for (Post post : posts) {
                                post.setFeatured(true);
                            }
                        }
                        return posts;
                    }
                })
                .flatMap();
    }

    /**
     * 如果碰到热贴,先取html的,失败后才取json的,暂不用
     *
     * @param type
     * @param key
     * @param page
     * @param useCache
     * @return
     */
    public static Observable<ResponseObject<ArrayList<Post>>> getPostListMerge(int type, String key, int page, boolean useCache) {
        Observable<ResponseObject<ArrayList<Post>>> html = getPostListHotHtml(type, key, page, useCache);
        Observable<ResponseObject<ArrayList<Post>>> json = getPostList(type, key, page, useCache);
        return Observable.concat(html, json)
                .first(new Func1<ResponseObject<ArrayList<Post>>, Boolean>() {
                    @Override
                    public Boolean call(ResponseObject<ArrayList<Post>> arrayListResponseObject) {
                        return arrayListResponseObject.ok;
                    }
                });
    }


    /**
     * 加入小组
     *
     * @param id 小组id
     * @return resultObject
     */
    public static void joinGroup(String id, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/member.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("group_id", id);
        new RequestBuilder<Boolean>()
                .post()
                .url(url)
                .params(pairs)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 退出小组
     *
     * @param id 小组id
     * @return resultObject
     */
    public static void quitGroup(String id, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/member.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("group_id", id);
        new RequestBuilder<Boolean>()
                .delete()
                .url(url)
                .params(pairs)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    public static Observable<ResponseObject<ArrayList<SubItem>>> getAllMyGroups() {
        String url = "http://apis.guokr.com/group/member.json";
        return new RequestBuilder<ArrayList<SubItem>>()
                .get()
                .url(url)
                .addParam("retrieve_type", "by_user")
                .addParam("ukey", UserAPI.getUkey())
                .addParam("limit", "999")
                .parser(new GroupListParser())
                .flatMap();
    }

    public static Observable<ArrayList<MyGroup>> getAllMyGroupsAndMerge() {
        return PostAPI
                .getAllMyGroups()
                .flatMap(new Func1<ResponseObject<ArrayList<SubItem>>, Observable<ArrayList<SubItem>>>() {
                    @Override
                    public Observable<ArrayList<SubItem>> call(ResponseObject<ArrayList<SubItem>> result) {
                        if (result.ok) {
                            return Observable.just(result.result);
                        }
                        return Observable.error(new IllegalStateException("error occurred"));
                    }
                })
                .map(new Func1<ArrayList<SubItem>, ArrayList<SubItem>>() {
                    @Override
                    public ArrayList<SubItem> call(ArrayList<SubItem> subItems) {
                        Collections.sort(subItems, new Comparator<SubItem>() {
                            @Override
                            public int compare(SubItem o1, SubItem o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        return subItems;
                    }
                })
                .map(new Func1<ArrayList<SubItem>, ArrayList<MyGroup>>() {
                    @Override
                    public ArrayList<MyGroup> call(ArrayList<SubItem> subItems) {
                        ArrayList<MyGroup> myGroups = new ArrayList<>();
                        for (int i = 0; i < subItems.size(); i++) {
                            SubItem item = subItems.get(i);
                            MyGroup mygroup = new MyGroup();
                            mygroup.setName(item.getName());
                            mygroup.setValue(item.getValue());
                            mygroup.setType(item.getType());
                            mygroup.setSection(item.getSection());
                            mygroup.setOrder(i + 10086);
                            myGroups.add(mygroup);
                        }
                        return myGroups;
                    }
                })
                .map(new Func1<ArrayList<MyGroup>, ArrayList<MyGroup>>() {
                    @Override
                    public ArrayList<MyGroup> call(ArrayList<MyGroup> newGroups) {
                        List<MyGroup> original = GroupHelper.getAllMyGroups();
                        for (int i = 0; i < newGroups.size(); i++) {
                            MyGroup newGroup = newGroups.get(i);
                            for (int j = 0; j < original.size(); j++) {
                                if (original.get(j).getValue().equals(newGroup.getValue())) {
                                    newGroup.setOrder(j);
                                    break;
                                }
                            }
                        }
                        Collections.sort(newGroups, new Comparator<MyGroup>() {
                            @Override
                            public int compare(MyGroup lhs, MyGroup rhs) {
                                return lhs.getOrder() - rhs.getOrder();
                            }
                        });
                        for (int i = 0; i < newGroups.size(); i++) {
                            newGroups.get(i).setOrder(i);
                            newGroups.get(i).setSelected(true);
                        }
                        GroupHelper.putAllMyGroups(newGroups);
                        return newGroups;
                    }
                });
    }

    public static void getAllMyGroupsAndMergeAndNotify() {
        getAllMyGroupsAndMerge().subscribe(new SimpleSubscriber<ArrayList<MyGroup>>() {
            @Override
            public void onNext(ArrayList<MyGroup> myGroups) {
                PostPagerFragment.shouldNotifyDataSetChanged = true;
                Emitter.emit(new GroupFetchedEvent());
            }
        });
    }

    /**
     * 根据贴子id获取贴子内容，json格式
     *
     * @param id，贴子id
     * @return resultObject
     */
    public static Observable<ResponseObject<Post>> getPostDetailByID(String id) {
        String url = "http://apis.guokr.com/group/post/" + id + ".json";
        return new RequestBuilder<Post>()
                .get()
                .url(url)
                .useCacheIfFailed(true)
                .parser(new PostParser())
                .flatMap();
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
        ParamsMap pairs = new ParamsMap();
        pairs.put("retrieve_type", "by_post");
        pairs.put("post_id", id);
        pairs.put("limit", limit + "");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<UComment>>()
                .get()
                .url(url)
                .params(pairs)
                .useCacheIfFailed(offset == 0)
                .parser(new PostCommentListParser())
                .flatMap();
    }

    /**
     * 赞一个贴子
     *
     * @param postID 贴子id
     * @return resultObject
     */
    public static NetworkTask<Boolean> likePost(String postID, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/post_liking.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("post_id", postID);
        return new RequestBuilder<Boolean>()
                .post()
                .url(url)
                .params(pairs)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 回复一个贴子，使用json请求，所以格式简单
     * 回复一个评论不过是在回复贴子的时候@了这个人而已
     *
     * @param id      贴子id
     * @param content 回复内容
     * @return ResponseObject.result is the reply_id if ok;
     */
    public static NetworkTask<String> replyPost(String id, String content, RequestCallBack<String> callBack) {
        String url = "http://apis.guokr.com/group/post_reply.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("post_id", id);
        pairs.put("content", content);
        return new RequestBuilder<String>()
                .url(url)
                .parser(new ContentValueForKeyParser("id"))
                .callback(callBack)
                .params(pairs)
                .post()
                .requestAsync();
    }

    /**
     * 回复一个贴子，模拟网页请求回复
     *
     * @param id      贴子id
     * @param content 回复内容
     * @return ResponseObject.result is the reply_id if ok;
     */
    public static Subscription replyPostHtml(String id, final String content, final boolean is_anon, final RequestCallBack<Boolean> callBack) {
        final String url = "http://www.guokr.com/post/" + id + "/";
        return new RequestBuilder<String>()
                .get()
                .url(url)
                .parser(new Parser<String>() {
                    @Override
                    public String parse(String response, ResponseObject<String> responseObject) throws Exception {
                        String str = Jsoup.parse(response).getElementById("csrf_token").attr("value");
                        responseObject.ok = true;
                        return str;
                    }
                })
                .flatMap()
                .flatMap(new Func1<ResponseObject<String>, Observable<ResponseObject<Boolean>>>() {
                    @Override
                    public Observable<ResponseObject<Boolean>> call(ResponseObject<String> response) {
                        if (response.ok) {
                            final ParamsMap pairs = new ParamsMap();
                            pairs.put("csrf_token", response.result);
                            if (is_anon) {
                                Mob.onEvent(Mob.Event_Reply_Post_Anon);
                                pairs.put("is_anon", "y");
                            }
                            String tail = getComplexReplyTail();
                            if (is_anon && !TextUtils.isEmpty(tail)) {
                                tail = getDefaultComplexTail();
                            }
                            pairs.put("body", MDUtil.Markdown2Html(content) + tail);
                            pairs.put("captcha", "");
                            return new RequestBuilder<Boolean>()
                                    .post()
                                    .url(url)
                                    .params(pairs)
                                    .parser(new Parser<Boolean>() {
                                        @Override
                                        public Boolean parse(String response, ResponseObject<Boolean> responseObject) throws Exception {
                                            try {
                                                Document document = Jsoup.parse(response);
                                                String url = document.getElementsByTag("a").get(0).text();
                                                Matcher matcher = Pattern.compile("/post/(\\d+)/").matcher(url);
                                                responseObject.ok = matcher.find();
                                            } catch (Exception e) {
                                                if (response.contains("Redirecting")) {
                                                    responseObject.ok = true;
                                                } else {
                                                    throw e;
                                                }
                                            }
                                            return responseObject.ok;
                                        }
                                    })
                                    .flatMap();
                        } else {
                            ResponseObject<Boolean> fakeResponse = new ResponseObject<Boolean>();
                            fakeResponse.copyPartFrom(response);
                            return Observable.just(fakeResponse);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseObject<Boolean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        callBack.onFailure(e, new ResponseObject<Boolean>());
                    }

                    @Override
                    public void onNext(ResponseObject<Boolean> response) {
                        if (response.ok) {
                            callBack.onSuccess(true, response);
                        } else {
                            callBack.onFailure(response.throwable, response);
                        }
                    }
                });
    }

    /**
     * 根据一条通知的id获取所有内容
     * 先是：http://www.guokr.com/user/notice/8738252/
     * 跳到：http://www.guokr.com/post/reply/654321/
     *
     * @param notice_id 通知id
     * @return resultObject resultObject.result是UComment
     */
    public static Observable<ResponseObject<UComment>> getSingleCommentByNotice(String notice_id) {
        String notice_url = "http://www.guokr.com/user/notice/" + notice_id + "/";
        return new RequestBuilder<String>()
                .get()
                .url(notice_url)
                .withToken(false)
                .parser(new StringParser())
                .flatMap()
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
                .flatMap(new Func1<String, Observable<ResponseObject<UComment>>>() {
                    @Override
                    public Observable<ResponseObject<UComment>> call(String id) {
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
    public static Observable<ResponseObject<UComment>> getSingleCommentFromRedirectUrl(String url) {
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
    public static Observable<ResponseObject<UComment>> getSingleCommentByID(String id) {
//        String url = "http://www.guokr.com/apis/group/post_reply.json";
        String url = "http://www.guokr.com/apis/group/post_reply/" + id + ".json";
        //这样后面就不必带reply_id参数了
        return new RequestBuilder<UComment>()
                .get()
                .url(url)
                .parser(new PostCommentParser())
                .flatMap();
    }

    /**
     * 删除我的评论
     *
     * @param id 评论id
     * @return resultObject
     */
    public static NetworkTask<Boolean> deleteMyComment(String id, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/post_reply.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("reply_id", id);
        pairs.put("reason", id);
        return new RequestBuilder<Boolean>()
                .delete()
                .url(url)
                .params(pairs)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 赞一个评论
     *
     * @param id 评论id
     * @return resultObject
     */
    public static NetworkTask<Boolean> likeComment(String id, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/post_reply_liking.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("reply_id", id);
        return new RequestBuilder<Boolean>()
                .post()
                .url(url)
                .parser(new BooleanParser())
                .callback(callBack)
                .params(pairs)
                .requestAsync();
    }

    /**
     * 获取发贴所需的csrf和topic列表
     * resultObject.result是PostPrepareData
     *
     * @param group_id 小组id
     * @return resultObject
     */
    public static NetworkTask<PrepareData> getPostPrepareData(String group_id, RequestCallBack<PrepareData> callBack) {
        String url = "http://www.guokr.com/group/" + group_id + "/post/edit/";
        return new RequestBuilder<PrepareData>()
                .get()
                .url(url)
                .parser(new PostPrepareDataParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 发贴
     * 有Json方式的删贴，有空加上。
     * http://www.guokr.com/apis/group/post.json?reason={}&post_id={}&access_token={}  //
     * request method = delete/put
     *
     * @param group_id 小组id
     * @param csrf     csrf_token
     * @param title    标题
     * @param body     贴子内容   html格式
     * @param topic    贴子主题
     * @return resultObject
     */
    public static NetworkTask<String> publishPost(String group_id, String csrf, String title,
                                                  String body, String topic, boolean is_anon, RequestCallBack<String> callBack) {
        String url = "http://www.guokr.com/group/" + group_id + "/post/edit/";
        ParamsMap pairs = new ParamsMap();
        pairs.put("csrf_token", csrf);
        pairs.put("title", title);
        pairs.put("topic", topic);
        pairs.put("body", MDUtil.Markdown2Html(body) + getComplexReplyTail());
        pairs.put("share_opts", "activity");
        if (is_anon) {
            Mob.onEvent(Mob.Event_Publish_Post_Anon);
            pairs.put("is_anon", "y");
        }
        pairs.put("captcha", "");

        return new RequestBuilder<String>()
                .post()
                .url(url)
                .params(pairs)
                .parser(new PublishPostParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 删贴
     *
     * @param post_id
     * @param callBack
     * @return resultObject
     */
    public static NetworkTask<Boolean> deletePost(String post_id, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/group/post.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("reason", "");
        pairs.put("post_id", post_id);
        return new RequestBuilder<Boolean>()
                .delete()//or put?
                .url(url)
                .params(pairs)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

}
