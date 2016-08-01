package net.nashlegend.sourcewall.request.api;

import android.text.TextUtils;

import net.nashlegend.sourcewall.db.AskTagHelper;
import net.nashlegend.sourcewall.db.gen.AskTag;
import net.nashlegend.sourcewall.model.Answer;
import net.nashlegend.sourcewall.model.PrepareData;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.ParamsMap;
import net.nashlegend.sourcewall.request.RequestBuilder;
import net.nashlegend.sourcewall.request.RequestObject.RequestCallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.SimpleHttp;
import net.nashlegend.sourcewall.request.parsers.AnswerCommentListParser;
import net.nashlegend.sourcewall.request.parsers.AnswerCommentParser;
import net.nashlegend.sourcewall.request.parsers.AnswerParser;
import net.nashlegend.sourcewall.request.parsers.BooleanParser;
import net.nashlegend.sourcewall.request.parsers.ContentValueForKeyParser;
import net.nashlegend.sourcewall.request.parsers.QuestionAnswerListParser;
import net.nashlegend.sourcewall.request.parsers.QuestionCommentListParser;
import net.nashlegend.sourcewall.request.parsers.QuestionCommentParser;
import net.nashlegend.sourcewall.request.parsers.QuestionHtmlListParser;
import net.nashlegend.sourcewall.request.parsers.QuestionListParser;
import net.nashlegend.sourcewall.request.parsers.QuestionParser;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.MDUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class QuestionAPI extends APIBase {
    private static final String HOTTEST = "hottest";
    private static final String HIGHLIGHT = "highlight";
    private static int maxImageWidth = 240;
    private static String prefix = "<div class=\"ZoomBox\"><div class=\"content-zoom ZoomIn\">";
    private static String suffix = "</div></div>";

    public static Observable<ArrayList<AskTag>> getAllMyTagsAndMerge(){
        return Observable
                .create(new Observable.OnSubscribe<ResponseObject<ArrayList<SubItem>>>() {
                    @Override
                    public void call(Subscriber<? super ResponseObject<ArrayList<SubItem>>> subscriber) {
                        subscriber.onNext(QuestionAPI.getAllMyTags());
                    }
                })
                .flatMap(new Func1<ResponseObject<ArrayList<SubItem>>, Observable<ArrayList<SubItem>>>() {
                    @Override
                    public Observable<ArrayList<SubItem>> call(ResponseObject<ArrayList<SubItem>> result) {
                        if (result.ok) {
                            return Observable.just(result.result);
                        }
                        return Observable.error(new IllegalStateException("error occurred"));
                    }
                })
                .map(new Func1<ArrayList<SubItem>, ArrayList<AskTag>>() {
                    @Override
                    public ArrayList<AskTag> call(ArrayList<SubItem> subItems) {
                        ArrayList<AskTag> askTags = new ArrayList<>();
                        for (int i = 0; i < subItems.size(); i++) {
                            SubItem item = subItems.get(i);
                            AskTag tag = new AskTag();
                            tag.setName(item.getName());
                            tag.setValue(item.getValue());
                            tag.setType(item.getType());
                            tag.setSection(item.getSection());
                            tag.setOrder(i + 10086);
                            askTags.add(tag);
                        }
                        return askTags;
                    }
                })
                .map(new Func1<ArrayList<AskTag>, ArrayList<AskTag>>() {
                    @Override
                    public ArrayList<AskTag> call(ArrayList<AskTag> newTags) {
                        List<AskTag> original = AskTagHelper.getAllMyTags();
                        for (int i = 0; i < newTags.size(); i++) {
                            AskTag newGroup = newTags.get(i);
                            for (int j = 0; j < original.size(); j++) {
                                if (original.get(j).getValue().equals(newGroup.getValue())) {
                                    newGroup.setOrder(j);
                                    break;
                                }
                            }
                        }
                        Collections.sort(newTags, new Comparator<AskTag>() {
                            @Override
                            public int compare(AskTag lhs, AskTag rhs) {
                                return lhs.getOrder() - rhs.getOrder();
                            }
                        });
                        for (int i = 0; i < newTags.size(); i++) {
                            newTags.get(i).setOrder(i);
                            newTags.get(i).setSelected(true);
                        }
                        AskTagHelper.putAllMyTags(newTags);
                        return newTags;
                    }
                });
    }

    /**
     * 返回所有我感兴趣的标签
     *
     * @return ResponseObject，resultObject.result是ArrayList[SubItem]
     */
    public static ResponseObject<ArrayList<SubItem>> getAllMyTags() {
        ResponseObject<ArrayList<SubItem>> resultObject = new ResponseObject<>();
        String pageUrl = "http://www.guokr.com/ask/i/" + UserAPI.getUserID() + "/following_tags/";
        ArrayList<SubItem> subItems = new ArrayList<>();
        int numPages;
        try {
            String firstPage = SimpleHttp.get(pageUrl).result;
            Document doc1 = Jsoup.parse(firstPage);
            Elements as = doc1.getElementsByClass("gpages");
            if (as.size() == 0) {
                numPages = 1;
            } else {
                numPages = Integer.valueOf(as.get(0).getElementsByTag("a").last().attr("href").replaceAll("^\\S+?page=", ""));
            }
            Elements lis = doc1.getElementsByClass("join-list").get(0).getElementsByTag("li");
            //第一页
            for (int i = 0; i < lis.size(); i++) {
                Element element = lis.get(i).getElementsByClass("join-list-desc").get(0);
                String groupName = element.getElementsByTag("a").text();
                SubItem subItem = new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, groupName, groupName);
                subItems.add(subItem);
            }
            if (numPages > 1) {
                for (int j = 2; j <= numPages; j++) {
                    Thread.sleep(100);
                    String url = pageUrl + "?page=" + j;
                    Document pageDoc = Jsoup.parse(SimpleHttp.get(url).result);
                    Elements lis2 = pageDoc.getElementsByClass("join-list").get(0).getElementsByTag("li");
                    for (int i = 0; i < lis2.size(); i++) {
                        Element element = lis2.get(i).getElementsByClass("join-list-desc").get(0);
                        String groupName = element.getElementsByTag("a").text();
                        SubItem subItem = new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, groupName, groupName);
                        subItems.add(subItem);
                    }
                }
            }
            resultObject.ok = true;
            resultObject.result = subItems;
        } catch (Exception e) {
            JsonHandler.handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 根据tag获取相关问题，json格式
     * 比html还特么浪费流量，垃圾数据太多了
     * resultObject.result是ArrayList[Question]
     *
     * @param tag    标签名
     * @param offset 从第几个开始加载
     * @return ResponseObject
     */
    public static Observable<ResponseObject<ArrayList<Question>>> getQuestionsByTag(String tag, int offset, boolean useCache) {
        String url = "http://apis.guokr.com/ask/question.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("retrieve_type", "by_tag");
        pairs.put("tag_name", tag);
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<Question>>()
                .get()
                .url(url)
                .params(pairs)
                .cacheTimeOut(300000)
                .useCacheFirst(useCache)
                .parser(new QuestionListParser())
                .flatMap();
    }

    /**
     * 返回热门回答问题列表，解析json获得
     *
     * @param offset
     * @return ResponseObject
     */
    public static Observable<ResponseObject<ArrayList<Question>>> getHotQuestions(int offset, boolean useCache) {
        String url = "http://apis.guokr.com/ask/question.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("retrieve_type", "hot_question");
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<Question>>()
                .get()
                .url(url)
                .params(pairs)
                .cacheTimeOut(300000)
                .useCacheFirst(useCache)
                .parser(new QuestionListParser())
                .flatMap();
    }

    /**
     * 返回精彩回答问题列表，解析html所得
     *
     * @param pageNo 页码
     * @return ResponseObject
     */
    public static Observable<ResponseObject<ArrayList<Question>>> getHighlightQuestions(int pageNo, boolean useCache) {
        String url = "http://m.guokr.com/ask/highlight/?page=" + pageNo;
        return new RequestBuilder<ArrayList<Question>>()
                .get()
                .url(url)
                .cacheTimeOut(300000)
                .useCacheFirst(useCache)
                .parser(new QuestionHtmlListParser())
                .flatMap();
    }

    /**
     * 根据帖子id获取问题内容，json格式
     *
     * @param id，帖子id
     * @return resultObject
     */
    public static Observable<ResponseObject<Question>> getQuestionDetailByID(String id) {
        String url = "http://apis.guokr.com/ask/question/" + id + ".json";
        return new RequestBuilder<Question>()
                .get()
                .url(url)
                .useCacheIfFailed(true)
                .parser(new QuestionParser())
                .flatMap();
    }

    /**
     * 获取文章评论，json格式
     * resultObject.result是ArrayList[UComment]
     *
     * @param id     article ID
     * @param offset 从第几个开始加载
     * @return ResponseObject
     */
    public static Observable<ResponseObject<ArrayList<Answer>>> getQuestionAnswers(final String id, final int offset) {
        String url = "http://apis.guokr.com/ask/answer.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("retrieve_type", "by_question");
        pairs.put("question_id", id);
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<Answer>>()
                .get()
                .url(url)
                .params(pairs)
                .useCacheIfFailed(offset == 0)
                .parser(new QuestionAnswerListParser())
                .flatMap();
    }

    /**
     * 根据一条评论的id获取评论内容，主要应用于消息通知
     *
     * @param url 评论id
     * @return resultObject resultObject.result是UComment
     */
    public static NetworkTask<Answer> getSingleAnswerFromRedirectUrl(String url, RequestCallBack<Answer> callBack) {
        //http://www.guokr.com/answer/654321/redirect/
        //http://www.guokr.com/answer/654321/
        return getSingleAnswerByID(url.replaceAll("\\D+", ""), callBack);
    }

    /**
     * 根据一条评论的id获取评论内容，主要应用于消息通知
     *
     * @param id 评论id
     * @return resultObject resultObject.result是UComment
     */
    public static NetworkTask<Answer> getSingleAnswerByID(String id, RequestCallBack<Answer> callBack) {
        String url = "http://apis.guokr.com/ask/answer/" + id + ".json";
        return new RequestBuilder<Answer>()
                .get()
                .url(url)
                .parser(new AnswerParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 返回问题的评论，json格式
     * resultObject.result是ArrayList[UComment]
     *
     * @param id     问题id
     * @param offset 从第几个开始加载
     * @return ResponseObject
     */
    public static NetworkTask<ArrayList<UComment>> getQuestionComments(String id, int offset, RequestCallBack<ArrayList<UComment>> callBack) {
        String url = "http://www.guokr.com/apis/ask/question_reply.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("retrieve_type", "by_question");
        pairs.put("question_id", id);
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<UComment>>()
                .get()
                .url(url)
                .params(pairs)
                .parser(new QuestionCommentListParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 返回答案的评论，json格式
     * resultObject.result是ArrayList[UComment]
     *
     * @param id     问题id
     * @param offset 从第几个开始加载
     * @return ResponseObject
     */
    public static NetworkTask<ArrayList<UComment>> getAnswerComments(String id, int offset, RequestCallBack<ArrayList<UComment>> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer_reply.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("retrieve_type", "by_answer");
        pairs.put("answer_id", id);
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<UComment>>()
                .get()
                .url(url)
                .params(pairs)
                .parser(new AnswerCommentListParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 回答问题，使用json请求
     *
     * @param id      问题id
     * @param content 答案内容
     * @return ResponseObject.result is the reply_id if ok;
     */
    public static NetworkTask<String> answerQuestion(String id, String content, RequestCallBack<String> callBack) {
        String url = "http://apis.guokr.com/ask/answer.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("question_id", id);
        pairs.put("content", content);
        return new RequestBuilder<String>()
                .post()
                .url(url)
                .params(pairs)
                .parser(new ContentValueForKeyParser("id"))
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 支持答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static void supportAnswer(String id, RequestCallBack<Boolean> callBack) {
        supportOrOpposeAnswer(id, "support", callBack);
    }

    /**
     * 反对答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static void opposeAnswer(String id, RequestCallBack<Boolean> callBack) {
        supportOrOpposeAnswer(id, "oppose", callBack);
    }

    /**
     * 支持或者反对答案
     *
     * @param id      答案id
     * @param opinion 反对或者赞同，参数
     * @return ResponseObject
     */
    private static void supportOrOpposeAnswer(String id, String opinion, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer_polling.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("answer_id", id);
        pairs.put("opinion", opinion);
        new RequestBuilder<Boolean>()
                .post()
                .url(url)
                .params(pairs)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 感谢答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static void thankAnswer(String id, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer_thanking.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("v", System.currentTimeMillis() + "");
        pairs.put("answer_id", id);
        new RequestBuilder<Boolean>()
                .post()
                .url(url)
                .params(pairs)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 不是答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static void buryAnswer(String id, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer_burying.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("v", System.currentTimeMillis() + "");
        pairs.put("answer_id", id);
        new RequestBuilder<Boolean>()
                .post()
                .url(url)
                .params(pairs)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 取消不是答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static void unBuryAnswer(String id, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer_burying.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("answer_id", id);
        new RequestBuilder<Boolean>()
                .delete()
                .url(url)
                .params(pairs)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 推荐问题
     *
     * @param questionID 问题id
     * @param title      问题标题
     * @param summary    问题summary
     * @param comment    推荐评语
     * @return ResponseObject
     */
    public static NetworkTask<Boolean> recommendQuestion(String questionID, String title, String summary, String comment, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/question/" + questionID + "/";
        return UserAPI.recommendLink(url, title, summary, comment, callBack);
    }

    /**
     * 关注问题
     *
     * @param questionID 问题id
     * @return ResponseObject
     */
    public static void followQuestion(String questionID, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/question_follower.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("question_id", questionID);
        pairs.put("retrieve_type", "by_question");
        new RequestBuilder<Boolean>()
                .post()
                .url(url)
                .params(pairs)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 取消关注问题
     *
     * @param questionID 问题id
     * @return ResponseObject
     */
    public static void unfollowQuestion(String questionID, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/question_follower.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("question_id", questionID);
        pairs.put("retrieve_type", "by_question");
        new RequestBuilder<Boolean>()
                .delete()
                .url(url)
                .params(pairs)
                .callback(callBack)
                .parser(new BooleanParser())
                .requestAsync();
    }

    /**
     * 评论问题
     *
     * @param questionID 问题id
     * @param comment    评论内容
     * @return ResponseObject
     */
    public static NetworkTask<UComment> commentOnQuestion(String questionID, String comment, RequestCallBack<UComment> callBack) {
        String url = "http://www.guokr.com/apis/ask/question_reply.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("question_id", questionID);
        pairs.put("content", comment);
        pairs.put("retrieve_type", "by_question");
        return new RequestBuilder<UComment>()
                .post()
                .url(url)
                .params(pairs)
                .parser(new QuestionCommentParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 删除我的答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static void deleteMyComment(String id, RequestCallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer/" + id + ".json";
        new RequestBuilder<Boolean>()
                .delete()
                .url(url)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 评论一个答案，resultObject.result 是一个UComment
     *
     * @param answerID 答案id
     * @param comment  评论内容
     * @return ResponseObject
     */
    public static NetworkTask<UComment> commentOnAnswer(String answerID, String comment, RequestCallBack<UComment> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer_reply.json";
        ParamsMap pairs = new ParamsMap();
        pairs.put("answer_id", answerID);
        pairs.put("content", comment);
        pairs.put("retrieve_type", "by_answer");
        return new RequestBuilder<UComment>()
                .post()
                .url(url)
                .params(pairs)
                .parser(new AnswerCommentParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 获取提问所需的csrf_token
     * resultObject.result是PrepareData#csrf
     *
     * @return ResponseObject
     */
    public static ResponseObject<PrepareData> getQuestionPrepareData() {
        ResponseObject<PrepareData> resultObject = new ResponseObject<>();
        try {
            String url = "http://www.guokr.com/questions/new/";
            String html = SimpleHttp.get(url).result;
            Document doc = Jsoup.parse(html);
            String csrf = doc.getElementById("csrf_token").attr("value");
            if (!TextUtils.isEmpty(csrf)) {
                PrepareData prepareData = new PrepareData();
                prepareData.setCsrf(csrf);
                resultObject.ok = true;
                resultObject.result = prepareData;
            }
        } catch (Exception e) {
            JsonHandler.handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 提问，卧槽Cookie里面还需要两个值，给跪了_32382_access_token和_32382_ukey=
     * 由https://www.guokr.com/sso/ask/提供，妈蛋先不搞提问了
     *
     * @param csrf       csrf
     * @param question   标题
     * @param annotation 补充
     * @param tags       标签
     * @return ResponseObject
     * @deprecated
     */
    public static ResponseObject<String> publishQuestion(String csrf, String question, String annotation, String[] tags) {
        ResponseObject<String> resultObject = new ResponseObject<>();
        String url = "http://www.guokr.com/questions/new/";
        try {
            String htmlDesc = MDUtil.Markdown2Html(annotation);
            htmlDesc += Config.getComplexReplyTail();
            ParamsMap pairs = new ParamsMap();
            pairs.put("csrf_token", csrf);
            pairs.put("question", question);
            pairs.put("annotation", htmlDesc);
            for (String tag1 : tags) {
                String tag = tag1.trim();
                if (!TextUtils.isEmpty(tag)) {
                    pairs.put("tags", tag);
                }
            }
            pairs.put("captcha", "");
            ResponseObject result = SimpleHttp.post(url, pairs, false);
            if (result.statusCode == 302 && testPublishResult(result.toString())) {
                resultObject.ok = true;
                resultObject.result = result.toString();
            }
        } catch (Exception e) {
            JsonHandler.handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 解析页面结果，看看是不是发表成功了
     *
     * @param res 发表问题返回的结果
     * @return 是否成功
     */
    private static boolean testPublishResult(String res) {
        try {
            Document doc = Jsoup.parse(res);
            String href = doc.getElementsByTag("a").attr("href");
            return href.matches("/question/\\d+[/]?");
        } catch (Exception e) {
            return false;
        }
    }

}
