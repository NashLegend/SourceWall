package com.example.sourcewall.connection.api;

import android.text.TextUtils;

import com.example.sourcewall.connection.HttpFetcher;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.model.Question;
import com.example.sourcewall.model.QuestionAnswer;
import com.example.sourcewall.model.UComment;
import com.example.sourcewall.util.MDUtil;

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

public class QuestionAPI extends APIBase {
    static int maxImageWidth = 240;
    static String prefix = "<div class=\"ZoomBox\"><div class=\"content-zoom ZoomIn\">";
    static String suffix = "</div></div>";

    public QuestionAPI() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 根据tag获取相关问题，json格式
     * 比html还特么浪费流量，垃圾数据太多了
     * resultObject.result是ArrayList<Question>
     *
     * @param tag
     * @param offset
     * @return
     * @throws IOException
     */
    public static ResultObject getQuestionsByTagFromJsonUrl(String tag, int offset) {
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<Question> questions = new ArrayList<Question>();
            String url = "http://apis.guokr.com/ask/question.json?retrieve_type=by_tag&limit=20&tag_name="
                    + tag + "&offset=" + offset;
            String jString = HttpFetcher.get(url);
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray results = jss.getJSONArray("result");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject jsonObject = results.getJSONObject(i);
                    Question question = new Question();
                    question.setAnswerNum(getJsonInt(jsonObject, "answers_count"));
                    question.setAuthor(jsonObject.getJSONObject("author").getString("nickname"));
                    question.setAuthorID(jsonObject.getJSONObject("author").getString("url")
                            .replaceAll("\\D+", ""));
                    question.setAuthorAvatarUrl(jsonObject.getJSONObject("author")
                            .getJSONObject("avatar").getString("large").replaceAll("\\?\\S*$", ""));
                    question.setSummary(getJsonString(jsonObject, "summary"));
                    question.setDate(parseDate(getJsonString(jsonObject, "date_created")));
                    question.setFollowNum(getJsonInt(jsonObject, "followers_count"));
                    question.setId(getJsonString(jsonObject, "id"));
                    question.setTitle(getJsonString(jsonObject, "question"));
                    question.setUrl(getJsonString(jsonObject, "url"));
                    questions.add(question);
                }
                resultObject.ok = true;
                resultObject.result = questions;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 返回热门回答问题列表，解析html获得
     *
     * @param pageNo
     * @return
     * @throws IOException
     */
    public static ResultObject getHotQuestions(int pageNo) {
        String url = "http://m.guokr.com/ask/hottest/?page=" + pageNo;
        return getQuestionsFromMobileUrl(url);
    }

    /**
     * 返回精彩回答问题列表，解析html所得
     *
     * @param pageNo 页码
     * @return
     * @throws IOException
     */
    public static ResultObject getHighlightQuestions(int pageNo) {
        String url = "http://m.guokr.com/ask/highlight/?page=" + pageNo;
        return getQuestionsFromMobileUrl(url);
    }

    /**
     * 解析html页面获得问题列表
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static ResultObject getQuestionsFromMobileUrl(String url) {
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<Question> questions = new ArrayList<Question>();
            String html = HttpFetcher.get(url);
            Document doc = Jsoup.parse(html);
            Elements elements = doc.getElementsByClass("ask-list");
            if (elements.size() == 1) {
                Elements questioList = elements.get(0).getElementsByTag("li");
                for (int i = 0; i < questioList.size(); i++) {
                    Question item = new Question();
                    Element element = questioList.get(i);
                    Element link = element.getElementsByTag("a").get(0);
                    String title = link.getElementsByTag("h4").text();
                    String id = link.attr("href").replaceAll("\\D+", "");
                    String summary = link.getElementsByTag("p").text();
                    String l = link.getElementsByClass("ask-descrip").text().replaceAll("\\D+", "");
                    if (!TextUtils.isEmpty(l)) {
                        item.setRecommendNum(Integer.valueOf(l));
                    }
                    item.setTitle(title);
                    item.setId(id);
                    item.setSummary(summary);
                    item.setFeatured(true);
                    questions.add(item);
                }
                resultObject.ok = true;
                resultObject.result = questions;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultObject;
    }

    /**
     * 返回问题内容,json格式
     *
     * @param id 问题ID
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static ResultObject getQuestionDetailByID(String id) {
        String url = "http://apis.guokr.com/ask/question/" + id + ".json";
        return getQuestionDetailFromJsonUrl(url);
    }

    /**
     * 返回问题内容
     * resultObject.result是Question
     *
     * @param url 返回问题内容,json格式
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static ResultObject getQuestionDetailFromJsonUrl(String url) {
        ResultObject resultObject = new ResultObject();
        try {
            Question question = null;
            String jString = HttpFetcher.get(url);
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONObject result = getJsonObject(jss, "result");
                question = new Question();
                question.setAnswerable(getJsonBoolean(result, "is_answerable"));
                question.setAnswerNum(getJsonInt(result, "answers_count"));
                question.setCommentNum(getJsonInt(result, "replies_count"));
                question.setAuthor(result.getJSONObject("author").getString("nickname"));
                question.setAuthorID(result.getJSONObject("author").getString("url")
                        .replaceAll("\\D+", ""));
                question.setAuthorAvatarUrl(result.getJSONObject("author").getJSONObject("avatar")
                        .getString("large").replaceAll("\\?\\S*$", ""));
                question.setContent(getJsonString(result, "annotation_html").replaceAll("<img .*?/>", prefix + "$0" + suffix).replaceAll("style=\"max-width: \\d+px\"", "style=\"max-width: " + maxImageWidth + "px\""));
                question.setDate(parseDate(getJsonString(result, "date_created")));
                question.setFollowNum(getJsonInt(result, "followers_count"));
                question.setId(getJsonString(result, "id"));
                question.setRecommendNum(getJsonInt(result, "recommends_count"));
                question.setTitle(getJsonString(result, "question"));
                question.setUrl(getJsonString(result, "url"));
                resultObject.ok = true;
                resultObject.result = question;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultObject;
    }

    /**
     * 获取问题的答案，json格式
     * resultObject.result是ArrayList<QuestionAnswer>
     *
     * @param id
     * @param offset
     * @return
     * @throws IOException
     */
    public static ResultObject getQuestionAnswers(String id, int offset) {
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<QuestionAnswer> answers = new ArrayList<QuestionAnswer>();
            String url = "http://apis.guokr.com/ask/answer.json?retrieve_type=by_question&limit=20&question_id="
                    + id + "&offset=" + offset;
            String jString = HttpFetcher.get(url);
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray comments = jss.getJSONArray("result");
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jo = comments.getJSONObject(i);
                    QuestionAnswer ans = new QuestionAnswer();
                    ans.setAuthor(getJsonString(getJsonObject(jo, "author"), "nickname"));
                    ans.setAuthorID(getJsonString(getJsonObject(jo, "author"), "url")
                            .replaceAll("\\D+", ""));
                    ans.setAuthorAvatarUrl(jo.getJSONObject("author").getJSONObject("avatar")
                            .getString("large").replaceAll("\\?\\S*$", ""));
                    ans.setAuthorTitle(getJsonString(getJsonObject(jo, "author"), "title"));
                    ans.setCommentNum(getJsonInt(jo, "replies_count"));
                    ans.setContent(getJsonString(jo, "html").replaceAll("<img .*?/>", prefix + "$0" + suffix).replaceAll("style=\"max-width: \\d+px\"", "style=\"max-width: " + maxImageWidth + "px\""));
                    ans.setDate_created(parseDate(getJsonString(jo, "date_created")));
                    ans.setDate_modified(parseDate(getJsonString(jo, "date_modified")));
                    ans.setHasDownVoted(getJsonBoolean(jo, "current_user_has_buried"));
                    ans.setHasUpvoted(getJsonBoolean(jo, "current_user_has_supported"));
                    ans.setHasThanked(getJsonBoolean(jo, "current_user_has_thanked"));
                    ans.setID(getJsonString(jo, "id"));
                    ans.setQuestionID(getJsonString(jo, "question_id"));
                    ans.setUpvoteNum(getJsonInt(jo, "supportings_count"));
                    answers.add(ans);
                }
                resultObject.ok = true;
                resultObject.result = answers;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 返回第一页数据，包括Post与第一页的评论列表
     * resultObject.result是ArrayList<AceModel>
     *
     * @param id
     * @return
     */
    public static ResultObject getQuestionFirstPage(String id) {
        ResultObject resultObject = new ResultObject();
        ArrayList<AceModel> aceModels = new ArrayList<>();
        ResultObject articleResult = getQuestionDetailByID(id);
        if (articleResult.ok) {
            ResultObject commentsResult = getQuestionAnswers(id, 0);
            if (commentsResult.ok) {
                Question post = (Question) articleResult.result;
                ArrayList<UComment> simpleComments = (ArrayList<UComment>) commentsResult.result;
                aceModels.add(post);
                aceModels.addAll(simpleComments);
                resultObject.ok = true;
                resultObject.result = aceModels;
            }
        }
        return resultObject;
    }

    /**
     * 返回问题的评论，json格式
     * resultObject.result是ArrayList<UComment>
     *
     * @param id
     * @param offset
     * @return
     */
    public static ResultObject getQuestionComments(String id, int offset) {
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<UComment> list = new ArrayList<UComment>();
            String url = "http://www.guokr.com/apis/ask/question_reply.json?retrieve_type=by_question&question_id="
                    + id + "&offset=" + offset;
            String jString = HttpFetcher.get(url);
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray comments = jss.getJSONArray("result");
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jsonObject = comments.getJSONObject(i);
                    UComment comment = new UComment();
                    comment.setAuthor(jsonObject.getJSONObject("author").getString("nickname"));
                    comment.setAuthorID(jsonObject.getJSONObject("author").getString("url")
                            .replaceAll("\\D+", ""));
                    comment.setAuthorAvatarUrl(jsonObject.getJSONObject("author")
                            .getJSONObject("avatar").getString("large").replaceAll("\\?\\S*$", ""));
                    comment.setContent(getJsonString(jsonObject, "text"));
                    comment.setDate(parseDate(getJsonString(jsonObject, "date_created")));
                    comment.setID(getJsonString(jsonObject, "id"));
                    comment.setHostID(getJsonString(jsonObject, "question_id"));
                    list.add(comment);
                }
                resultObject.ok = true;
                resultObject.result = list;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 返回答案的评论，json格式
     * resultObject.result是ArrayList<UComment>
     *
     * @param id
     * @param offset
     * @return
     * @throws IOException
     */
    public static ResultObject getAnswerComments(String id, int offset) {
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<UComment> list = new ArrayList<UComment>();
            String url = "http://www.guokr.com/apis/ask/answer_reply.json?retrieve_type=by_answer&limit=20&answer_id="
                    + id + "&offset=" + offset;
            String jString = HttpFetcher.get(url);
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray comments = jss.getJSONArray("result");
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jsonObject = comments.getJSONObject(i);
                    UComment comment = new UComment();
                    comment.setAuthor(jsonObject.getJSONObject("author").getString("nickname"));
                    comment.setAuthorID(jsonObject.getJSONObject("author").getString("url")
                            .replaceAll("\\D+", ""));
                    comment.setAuthorAvatarUrl(jsonObject.getJSONObject("author")
                            .getJSONObject("avatar").getString("large").replaceAll("\\?\\S*$", ""));
                    comment.setContent(getJsonString(jsonObject, "text"));
                    comment.setDate(parseDate(getJsonString(jsonObject, "date_created")));
                    comment.setID(getJsonString(jsonObject, "id"));
                    comment.setHostID(getJsonString(jsonObject, "question_id"));
                    list.add(comment);
                }
                resultObject.ok = true;
                resultObject.result = list;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultObject;
    }

    /**
     * 回答问题，使用json请求
     *
     * @param id
     * @param content
     * @return ResultObject.result is the reply_id if ok;
     */
    public static ResultObject answerQuestion(String id, String content) {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://apis.guokr.com/ask/answer.json";
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("question_id", id));
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
     * 支持答案
     *
     * @param id
     * @return
     */
    public static ResultObject supportAnswer(String id) {
        return supportOrOpposeAnswer(id, "support");
    }

    /**
     * 反对答案
     *
     * @param id
     * @return
     */
    public static ResultObject opposeAnswer(String id) {
        return supportOrOpposeAnswer(id, "oppose");
    }

    /**
     * 支持或者反对答案
     *
     * @param id
     * @param opinion
     * @return
     */
    private static ResultObject supportOrOpposeAnswer(String id, String opinion) {
        String url = "http://www.guokr.com/apis/ask/answer_polling.json";
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("answer_id", id));
            pairs.add(new BasicNameValuePair("opinion", opinion));
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
     * 感谢答案
     *
     * @param id
     * @return
     */
    public static ResultObject thankAnswer(String id) {
        String url = "http://www.guokr.com/apis/ask/answer_thanking.json?v=" + System.currentTimeMillis();
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("answer_id", id));
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
     * 不是答案
     *
     * @param id
     * @return
     */
    public static ResultObject buryAnswer(String id) {
        String url = "http://www.guokr.com/apis/ask/answer_burying.json?v=" + System.currentTimeMillis();
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("answer_id", id));
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
     * 推荐问题
     *
     * @param questionID
     * @param title
     * @param summary
     * @param comment
     * @return
     */
    public static ResultObject recommendQuestion(String questionID, String title, String summary, String comment) {
        String url = "http://www.guokr.com/question/" + questionID + "/";
        return UserAPI.recommendLink(url, title, summary, comment);
    }

    /**
     * 评论问题
     *
     * @param questionID
     * @param comment
     * @return
     */
    public static ResultObject commentOnQuestion(String questionID, String comment) {
        String url = "http://www.guokr.com/apis/ask/question_reply.json";
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("question_id", questionID));
            pairs.add(new BasicNameValuePair("content", comment));
            pairs.add(new BasicNameValuePair("retrieve_type", "by_question"));
            pairs.add(new BasicNameValuePair("access_token", UserAPI.getToken()));
            String result = HttpFetcher.post(url, pairs);
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                JSONObject jsonObject = object.getJSONObject("result");
                UComment uComment = new UComment();
                uComment.setAuthor(jsonObject.getJSONObject("author").getString("nickname"));
                uComment.setAuthorID(jsonObject.getJSONObject("author").getString("url")
                        .replaceAll("\\D+", ""));
                uComment.setAuthorAvatarUrl(jsonObject.getJSONObject("author")
                        .getJSONObject("avatar").getString("large").replaceAll("\\?\\S*$", ""));
                uComment.setContent(getJsonString(jsonObject, "text"));
                uComment.setDate(parseDate(getJsonString(jsonObject, "date_created")));
                uComment.setID(getJsonString(jsonObject, "id"));
                uComment.setHostID(getJsonString(jsonObject, "question_id"));
                resultObject.ok = true;
                resultObject.result = uComment;
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 评论一个答案，resultObject.result 是一个UComment
     *
     * @param answerID
     * @param comment
     * @return
     */
    public static ResultObject commentOnAnswer(String answerID, String comment) {
        String url = "http://www.guokr.com/apis/ask/answer_reply.json";
        ResultObject resultObject = new ResultObject();
        try {
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("answer_id", answerID));
            pairs.add(new BasicNameValuePair("content", comment));
            pairs.add(new BasicNameValuePair("retrieve_type", "by_answer"));
            pairs.add(new BasicNameValuePair("access_token", UserAPI.getToken()));
            String result = HttpFetcher.post(url, pairs);
            JSONObject object = new JSONObject(result);
            if (getJsonBoolean(object, "ok")) {
                JSONObject jsonObject = object.getJSONObject("result");
                UComment uComment = new UComment();
                uComment.setAuthor(jsonObject.getJSONObject("author").getString("nickname"));
                uComment.setAuthorID(jsonObject.getJSONObject("author").getString("url")
                        .replaceAll("\\D+", ""));
                uComment.setAuthorAvatarUrl(jsonObject.getJSONObject("author")
                        .getJSONObject("avatar").getString("large").replaceAll("\\?\\S*$", ""));
                uComment.setContent(getJsonString(jsonObject, "text"));
                uComment.setDate(parseDate(getJsonString(jsonObject, "date_created")));
                uComment.setID(getJsonString(jsonObject, "id"));
                uComment.setHostID(getJsonString(jsonObject, "question_id"));
                resultObject.ok = true;
                resultObject.result = uComment;
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 获取提问所需的csrf_token
     * resultObject.result是String#csrf
     *
     * @return
     */
    public static ResultObject getQuestionPrepareData() {
        ResultObject resultObject = new ResultObject();
        try {
            String url = "http://www.guokr.com/questions/new/";
            String html = HttpFetcher.get(url);
            Document doc = Jsoup.parse(html);
            Element selects = doc.getElementById("topic");
            ArrayList<BasicNameValuePair> pairs = new ArrayList<>();
            String csrf = doc.getElementById("csrf_token").attr("value");
            if (!TextUtils.isEmpty(csrf)) {
                resultObject.ok = true;
                resultObject.result = csrf;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /**
     * 提问
     *
     * @param csrf
     * @param askTitle
     * @param askDesc
     * @param tagContent
     * @return
     */
    public static ResultObject publishQuestion(String csrf, String askTitle, String askDesc, String tagContent) {
        ResultObject resultObject = new ResultObject();
        String url = "http://www.guokr.com/questions/new/";
        try {
            ResultObject mdResult = MDUtil.parseMarkdownByGitHub(askDesc);
            if (mdResult.ok) {
                String htmlDesc = (String) mdResult.result;
                ArrayList<NameValuePair> pairs = new ArrayList<>();
                pairs.add(new BasicNameValuePair("csrf_token", csrf));
                pairs.add(new BasicNameValuePair("askTitle", askTitle));
                pairs.add(new BasicNameValuePair("askDesc", htmlDesc));
                pairs.add(new BasicNameValuePair("tagContent", tagContent));
                pairs.add(new BasicNameValuePair("captcha", ""));
                String result = HttpFetcher.post(url, pairs);
                resultObject.ok = true;
                resultObject.result = result;
            } else {
                //转换失败……
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

}
