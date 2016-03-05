package net.nashlegend.sourcewall.request.api;

import android.text.TextUtils;

import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Author;
import net.nashlegend.sourcewall.model.PrepareData;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.model.QuestionAnswer;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.HttpFetcher;
import net.nashlegend.sourcewall.request.RequestCache;
import net.nashlegend.sourcewall.swrequest.RequestBuilder;
import net.nashlegend.sourcewall.swrequest.RequestObject;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.swrequest.parsers.BooleanParser;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.MDUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 单个答案地址。http://www.guokr.com/answer/782227/
 * 缓存key规则：
 * 热门问答的key是 question.hottest
 * 精彩回答的key是 question.highlight
 * 按tag加载的问题的key是 question.{tag}
 */
public class QuestionAPI extends APIBase {
    private static int maxImageWidth = 240;
    private static String prefix = "<div class=\"ZoomBox\"><div class=\"content-zoom ZoomIn\">";
    private static String suffix = "</div></div>";

    public static ResponseObject<ArrayList<Question>> getCachedQuestionList(SubItem subItem) {
        ResponseObject<ArrayList<Question>> cachedResponseObject = new ResponseObject<>();
        String key = "question." + subItem.getValue();
        String content = RequestCache.getInstance().getStringFromCache(key);
        if (!TextUtils.isEmpty(content)) {
            if (subItem.getType() == SubItem.Type_Collections) {
                cachedResponseObject = parseQuestionsHtml(content);
            } else {
                cachedResponseObject = parseQuestionsListJson(content);
            }
        }
        return cachedResponseObject;
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
            String firstPage = HttpFetcher.get(pageUrl).toString();
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
                    Document pageDoc = Jsoup.parse(HttpFetcher.get(url).toString());
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
            handleRequestException(e, resultObject);
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
    public static ResponseObject<ArrayList<Question>> getQuestionsByTagFromJsonUrl(String tag, int offset) {
        ResponseObject<ArrayList<Question>> resultObject = new ResponseObject<>();
        try {
            String url = "http://apis.guokr.com/ask/question.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("retrieve_type", "by_tag");
            pairs.put("tag_name", tag);
            pairs.put("limit", "20");
            pairs.put("offset", offset + "");
            String jString = HttpFetcher.get(url, pairs).toString();
            resultObject = parseQuestionsListJson(jString);

            if (resultObject.ok && offset == 0) {
                //请求成功则缓存之
                String key = "question." + URLDecoder.decode(tag, "utf-8");
                RequestCache.getInstance().addStringToCacheForceUpdate(key, jString);
            }

        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 解析QuestionList的json
     *
     * @param jString json
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<Question>> parseQuestionsListJson(String jString) {
        ResponseObject<ArrayList<Question>> resultObject = new ResponseObject<>();
        try {
            if (jString != null) {
                ArrayList<Question> questions = new ArrayList<>();
                JSONArray results = getUniversalJsonArray(jString, resultObject);
                if (results != null) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject jsonObject = results.getJSONObject(i);
                        Question question = Question.fromJson(jsonObject);
                        questions.add(question);
                    }
                    resultObject.ok = true;
                    resultObject.result = questions;
                }
            }

        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }


    /**
     * 返回热门回答问题列表，解析html获得
     *
     * @param pageNo 页码
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<Question>> getHotQuestions(int pageNo) {
        String url = "http://m.guokr.com/ask/hottest/?page=" + pageNo;
        ResponseObject<ArrayList<Question>> resultObject = new ResponseObject<>();
        try {
            String html = HttpFetcher.get(url).toString();
            resultObject = parseQuestionsHtml(html);
            if (resultObject.ok && pageNo == 1) {
                RequestCache.getInstance().addStringToCacheForceUpdate("question.hottest", html);
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 返回精彩回答问题列表，解析html所得
     *
     * @param pageNo 页码
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<Question>> getHighlightQuestions(int pageNo) {
        String url = "http://m.guokr.com/ask/highlight/?page=" + pageNo;
        ResponseObject<ArrayList<Question>> resultObject = new ResponseObject<>();
        try {
            String html = HttpFetcher.get(url).toString();
            resultObject = parseQuestionsHtml(html);
            if (resultObject.ok && pageNo == 1) {
                RequestCache.getInstance().addStringToCacheForceUpdate("question.highlight", html);
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 解析html页面获得问题列表
     *
     * @param html 页面内容
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<Question>> parseQuestionsHtml(String html) {
        ResponseObject<ArrayList<Question>> resultObject = new ResponseObject<>();
        try {
            ArrayList<Question> questions = Question.fromHtmlList(html);
            resultObject.ok = true;
            resultObject.result = questions;
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 返回问题内容,json格式
     *
     * @param id 问题ID
     * @return ResponseObject
     */
    public static ResponseObject<Question> getCachedQuestionDetailByID(String id) {
        String url = "http://apis.guokr.com/ask/question/" + id + ".json";
        return getCachedQuestionDetailFromJsonUrl(url);
    }


    /**
     * 返回问题内容
     * resultObject.result是Question
     *
     * @param url 返回问题内容,json格式
     * @return ResponseObject
     */
    public static ResponseObject<Question> getCachedQuestionDetailFromJsonUrl(String url) {
        ResponseObject<Question> resultObject = new ResponseObject<>();
        try {
            Question question;
            String jString = RequestCache.getInstance().getStringFromCache(url);
            JSONObject result = getUniversalJsonObject(jString, resultObject);
            if (result != null) {
                question = Question.fromJson(result);
                resultObject.ok = true;
                resultObject.result = question;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }

        return resultObject;
    }

    /**
     * 返回问题内容,json格式
     *
     * @param id 问题ID
     * @return ResponseObject
     */
    public static ResponseObject<Question> getQuestionDetailByID(String id) {
        String url = "http://apis.guokr.com/ask/question/" + id + ".json";
        return getQuestionDetailFromJsonUrl(url);
    }

    /**
     * 返回问题内容
     * resultObject.result是Question
     *
     * @param url 返回问题内容,json格式
     * @return ResponseObject
     */
    public static ResponseObject<Question> getQuestionDetailFromJsonUrl(String url) {
        ResponseObject<Question> resultObject = new ResponseObject<>();
        try {
            Question question;
            ResponseObject httpResult = HttpFetcher.get(url, null);
            resultObject.statusCode = httpResult.statusCode;
            if (resultObject.statusCode == 404) {
                return resultObject;
            }
            String jString = httpResult.toString();
            JSONObject result = getUniversalJsonObject(jString, resultObject);
            if (result != null) {
                question = Question.fromJson(result);
                resultObject.ok = true;
                resultObject.result = question;
                RequestCache.getInstance().addStringToCacheForceUpdate(url, jString);
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }

        return resultObject;
    }

    /**
     * 获取问题的答案，json格式
     * resultObject.result是ArrayList[QuestionAnswer]
     *
     * @param id     问题id
     * @param offset 从第几个开始加载
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<AceModel>> getQuestionAnswers(String id, int offset) {
        ResponseObject<ArrayList<AceModel>> resultObject = new ResponseObject<>();
        try {
            ArrayList<AceModel> answers = new ArrayList<>();
            String url = "http://apis.guokr.com/ask/answer.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("retrieve_type", "by_question");
            pairs.put("question_id", id);
            pairs.put("limit", "20");
            pairs.put("offset", offset + "");
            String jString = HttpFetcher.get(url, pairs).toString();
            JSONArray comments = getUniversalJsonArray(jString, resultObject);
            if (comments != null) {
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jo = comments.getJSONObject(i);
                    QuestionAnswer ans = QuestionAnswer.fromJson(jo);
                    answers.add(ans);
                }
                resultObject.ok = true;
                resultObject.result = answers;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 根据一条评论的id获取评论内容，主要应用于消息通知
     *
     * @param url 评论id
     * @return resultObject resultObject.result是UComment
     */
    public static ResponseObject<QuestionAnswer> getSingleAnswerFromRedirectUrl(String url) {
        //http://www.guokr.com/answer/654321/redirect/
        //http://www.guokr.com/answer/654321/
        return getSingleAnswerByID(url.replaceAll("\\D+", ""));
    }

    /**
     * 根据一条评论的id获取评论内容，主要应用于消息通知
     *
     * @param id 评论id
     * @return resultObject resultObject.result是UComment
     */
    public static ResponseObject<QuestionAnswer> getSingleAnswerByID(String id) {
        ResponseObject<QuestionAnswer> resultObject = new ResponseObject<>();
        String url = "http://apis.guokr.com/ask/answer.json";
        //url还有另一种形式，http://apis.guokr.com/ask/answer/999999.json
        //这样后面就不必带answer_id参数了
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("answer_id", id);
        try {
            String result = HttpFetcher.get(url, pairs).toString();
            JSONArray answerArray = getUniversalJsonArray(result, resultObject);
            if (answerArray != null && answerArray.length() > 0) {
                JSONObject answerObject = answerArray.getJSONObject(0);
                QuestionAnswer answer = QuestionAnswer.fromJson(answerObject);
                resultObject.ok = true;
                resultObject.result = answer;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 返回问题的评论，json格式
     * resultObject.result是ArrayList[UComment]
     *
     * @param id     问题id
     * @param offset 从第几个开始加载
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<UComment>> getQuestionComments(String id, int offset) {
        ResponseObject<ArrayList<UComment>> resultObject = new ResponseObject<>();
        try {
            ArrayList<UComment> list = new ArrayList<>();
            String url = "http://www.guokr.com/apis/ask/question_reply.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("retrieve_type", "by_question");
            pairs.put("question_id", id);
            pairs.put("limit", "20");
            pairs.put("offset", offset + "");
            String jString = HttpFetcher.get(url, pairs).toString();
            JSONArray comments = getUniversalJsonArray(jString, resultObject);
            if (comments != null) {
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jsonObject = comments.getJSONObject(i);
                    UComment comment = UComment.fromQuestionJson(jsonObject);
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
     * 返回答案的评论，json格式
     * resultObject.result是ArrayList[UComment]
     *
     * @param id     答案id
     * @param offset 从第几个开始加载
     * @return ResponseObject
     */
    public static ResponseObject<ArrayList<UComment>> getAnswerComments(String id, int offset) {
        ResponseObject<ArrayList<UComment>> resultObject = new ResponseObject<>();
        try {
            ArrayList<UComment> list = new ArrayList<>();
            String url = "http://www.guokr.com/apis/ask/answer_reply.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("retrieve_type", "by_answer");
            pairs.put("answer_id", id);
            pairs.put("limit", "20");
            pairs.put("offset", offset + "");
            String jString = HttpFetcher.get(url, pairs).toString();
            JSONArray comments = getUniversalJsonArray(jString, resultObject);
            if (comments != null) {
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jsonObject = comments.getJSONObject(i);
                    UComment comment = UComment.fromAnswerJson(jsonObject);
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
     * 回答问题，使用json请求
     *
     * @param id      问题id
     * @param content 答案内容
     * @return ResponseObject.result is the reply_id if ok;
     */
    public static ResponseObject<String> answerQuestion(String id, String content) {
        ResponseObject<String> resultObject = new ResponseObject<>();
        try {
            String url = "http://apis.guokr.com/ask/answer.json";
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("question_id", id);
            pairs.put("content", content);
            String result = HttpFetcher.post(url, pairs).toString();
            JSONObject resultJson = getUniversalJsonObject(result, resultObject);
            if (resultJson != null) {
                String replyID = resultJson.optString("id");
                resultObject.ok = true;
                resultObject.result = replyID;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 支持答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static ResponseObject supportAnswer(String id) {
        return supportOrOpposeAnswer(id, "support");
    }

    /**
     * 反对答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static ResponseObject opposeAnswer(String id) {
        return supportOrOpposeAnswer(id, "oppose");
    }

    /**
     * 支持或者反对答案
     *
     * @param id      答案id
     * @param opinion 反对或者赞同，参数
     * @return ResponseObject
     */
    private static ResponseObject supportOrOpposeAnswer(String id, String opinion) {
        String url = "http://www.guokr.com/apis/ask/answer_polling.json";
        ResponseObject resultObject = new ResponseObject();
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("answer_id", id);
            pairs.put("opinion", opinion);
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
     * 支持或者反对答案
     *
     * @param id      答案id
     * @param opinion 反对或者赞同，参数
     * @return ResponseObject
     */
    private static void supportOrOpposeAnswer(String id, String opinion, RequestObject.CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer_polling.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("answer_id", id);
        pairs.put("opinion", opinion);
        new RequestBuilder<Boolean>().setUrl(url).setParams(pairs).setParser(new BooleanParser())
                .setRequestCallBack(callBack).post().requestAsync();
    }

    /**
     * 感谢答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static ResponseObject thankAnswer(String id) {
        String url = "http://www.guokr.com/apis/ask/answer_thanking.json";
        ResponseObject resultObject = new ResponseObject();
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("v", System.currentTimeMillis() + "");
            pairs.put("answer_id", id);
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
     * 感谢答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static void thankAnswer(String id, RequestObject.CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer_thanking.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("v", System.currentTimeMillis() + "");
        pairs.put("answer_id", id);
        new RequestBuilder<Boolean>().setUrl(url).setParams(pairs).setParser(new BooleanParser())
                .setRequestCallBack(callBack).post().requestAsync();
    }

    /**
     * 不是答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static ResponseObject buryAnswer(String id) {
        String url = "http://www.guokr.com/apis/ask/answer_burying.json";
        ResponseObject resultObject = new ResponseObject();
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("v", System.currentTimeMillis() + "");
            pairs.put("answer_id", id);
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
     * 不是答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static void buryAnswer(String id, RequestObject.CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer_burying.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("v", System.currentTimeMillis() + "");
        pairs.put("answer_id", id);
        new RequestBuilder<Boolean>().setUrl(url).setParams(pairs).setParser(new BooleanParser())
                .setRequestCallBack(callBack).post().requestAsync();
    }

    /**
     * 取消不是答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static ResponseObject unBuryAnswer(String id) {
        String url = "http://www.guokr.com/apis/ask/answer_burying.json";
        ResponseObject resultObject = new ResponseObject();
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("answer_id", id);
            String result = HttpFetcher.delete(url, pairs).toString();
            if (getUniversalJsonSimpleBoolean(result, resultObject)) {
                resultObject.ok = true;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 取消不是答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static void unBuryAnswer(String id, RequestObject.CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer_burying.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("answer_id", id);
        new RequestBuilder<Boolean>().setUrl(url).setParams(pairs).setParser(new BooleanParser())
                .setRequestCallBack(callBack).delete().requestAsync();
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
    public static ResponseObject recommendQuestion(String questionID, String title, String summary, String comment) {
        String url = "http://www.guokr.com/question/" + questionID + "/";
        return UserAPI.recommendLink(url, title, summary, comment);
    }

    /**
     * 关注问题
     *
     * @param questionID 问题id
     * @return ResponseObject
     */
    public static ResponseObject followQuestion(String questionID) {
        ResponseObject resultObject = new ResponseObject();
        String url = "http://www.guokr.com/apis/ask/question_follower.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("question_id", questionID);
        pairs.put("retrieve_type", "by_question");
        try {
            String result = HttpFetcher.post(url, pairs).toString();
            if (getUniversalJsonSimpleBoolean(result, resultObject)) {
                resultObject.ok = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 关注问题
     *
     * @param questionID 问题id
     * @return ResponseObject
     */
    public static void followQuestion(String questionID, RequestObject.CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/question_follower.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("question_id", questionID);
        pairs.put("retrieve_type", "by_question");
        new RequestBuilder<Boolean>().setUrl(url).setParams(pairs).setParser(new BooleanParser())
                .setRequestCallBack(callBack).post().requestAsync();
    }

    /**
     * 取消关注问题
     *
     * @param questionID 问题id
     * @return ResponseObject
     */
    public static ResponseObject unfollowQuestion(String questionID) {
        ResponseObject resultObject = new ResponseObject();
        String url = "http://www.guokr.com/apis/ask/question_follower.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("question_id", questionID);
        pairs.put("retrieve_type", "by_question");
        try {
            String result = HttpFetcher.delete(url, pairs).toString();
            if (getUniversalJsonSimpleBoolean(result, resultObject)) {
                resultObject.ok = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 取消关注问题
     *
     * @param questionID 问题id
     * @return ResponseObject
     */
    public static void unfollowQuestion(String questionID, RequestObject.CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/question_follower.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("question_id", questionID);
        pairs.put("retrieve_type", "by_question");
        new RequestBuilder<Boolean>().setUrl(url).setParams(pairs).setRequestCallBack(callBack).setParser(new BooleanParser()).delete().requestAsync();
    }


    /**
     * 评论问题
     *
     * @param questionID 问题id
     * @param comment    评论内容
     * @return ResponseObject
     */
    public static ResponseObject<UComment> commentOnQuestion(String questionID, String comment) {
        String url = "http://www.guokr.com/apis/ask/question_reply.json";
        ResponseObject<UComment> resultObject = new ResponseObject<>();
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("question_id", questionID);
            pairs.put("content", comment);
            pairs.put("retrieve_type", "by_question");
            String result = HttpFetcher.post(url, pairs).toString();
            JSONObject jsonObject = getUniversalJsonObject(result, resultObject);
            if (jsonObject != null) {
                UComment uComment = new UComment();
                uComment.setAuthor(Author.fromJson(jsonObject.optJSONObject("author")));
                uComment.setContent(jsonObject.optString("text"));
                uComment.setDate(parseDate(jsonObject.optString("date_created")));
                uComment.setID(jsonObject.optString("id"));
                uComment.setHostID(jsonObject.optString("question_id"));
                resultObject.ok = true;
                resultObject.result = uComment;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 删除我的答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static ResponseObject deleteMyComment(String id) {
        ResponseObject resultObject = new ResponseObject();
        String url = "http://www.guokr.com/apis/ask/answer/" + id + ".json";
        HashMap<String, String> pairs = new HashMap<>();
        try {
            String result = HttpFetcher.delete(url, pairs).toString();
            resultObject.ok = getUniversalJsonSimpleBoolean(result, resultObject);
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
    }

    /**
     * 删除我的答案
     *
     * @param id 答案id
     * @return ResponseObject
     */
    public static void deleteMyComment(String id, RequestObject.CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/ask/answer/" + id + ".json";
        new RequestBuilder<Boolean>().setUrl(url).setRequestCallBack(callBack).setParser(new BooleanParser()).delete().requestAsync();
    }

    /**
     * 评论一个答案，resultObject.result 是一个UComment
     *
     * @param answerID 答案id
     * @param comment  评论内容
     * @return ResponseObject
     */
    public static ResponseObject<UComment> commentOnAnswer(String answerID, String comment) {
        String url = "http://www.guokr.com/apis/ask/answer_reply.json";
        ResponseObject<UComment> resultObject = new ResponseObject<>();
        try {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("answer_id", answerID);
            pairs.put("content", comment);
            pairs.put("retrieve_type", "by_answer");
            String result = HttpFetcher.post(url, pairs).toString();
            JSONObject jsonObject = getUniversalJsonObject(result, resultObject);
            if (jsonObject != null) {
                UComment uComment = new UComment();
                uComment.setAuthor(Author.fromJson(jsonObject.optJSONObject("author")));
                uComment.setContent(jsonObject.optString("text"));
                uComment.setDate(parseDate(jsonObject.optString("date_created")));
                uComment.setID(jsonObject.optString("id"));
                uComment.setHostID(jsonObject.optString("question_id"));
                resultObject.ok = true;
                resultObject.result = uComment;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
        }
        return resultObject;
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
            String html = HttpFetcher.get(url).toString();
            Document doc = Jsoup.parse(html);
            String csrf = doc.getElementById("csrf_token").attr("value");
            if (!TextUtils.isEmpty(csrf)) {
                PrepareData prepareData = new PrepareData();
                prepareData.setCsrf(csrf);
                resultObject.ok = true;
                resultObject.result = prepareData;
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
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
            ResponseObject<String> mdResult = MDUtil.parseMarkdownByGitHub(annotation);
            String htmlDesc;
            if (mdResult.ok) {
                htmlDesc = mdResult.result;
            } else {
                htmlDesc = MDUtil.Markdown2HtmlDumb(annotation);
            }
            htmlDesc += Config.getComplexReplyTail();
            HashMap<String, String> pairs = new HashMap<>();
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

            ResponseObject result = HttpFetcher.post(url, pairs, false);
            if (result.statusCode == 302 && testPublishResult(result.toString())) {
                resultObject.ok = true;
                resultObject.result = result.toString();
            }
        } catch (Exception e) {
            handleRequestException(e, resultObject);
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
