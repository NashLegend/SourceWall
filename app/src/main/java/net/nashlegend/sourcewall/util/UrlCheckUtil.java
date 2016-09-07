package net.nashlegend.sourcewall.util;

import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextUtils;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.AnswerActivity;
import net.nashlegend.sourcewall.activities.ArticleActivity;
import net.nashlegend.sourcewall.activities.PostActivity;
import net.nashlegend.sourcewall.activities.QuestionActivity;
import net.nashlegend.sourcewall.activities.SingleReplyActivity;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.data.Consts.Extras;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NashLegend on 2015/1/14 0014
 */
public class UrlCheckUtil {

    /**
     * 是否拦截打开的链接
     *
     * @param url 要检查的链接
     */
    public static boolean redirectRequest(String url) {
        return !TextUtils.isEmpty(url) && redirectRequest(Uri.parse(url));
    }

    /**
     * 是否拦截打开的链接
     *
     * @param uri 要检查的链接
     */
    public static boolean redirectRequest(Uri uri) {
        return uri != null && redirectRequest(uri, null);
    }

    /**
     * 是否拦截打开的链接
     *
     * @param url 要检查的链接
     */
    public static boolean redirectRequest(String url, String notice_id) {
        return !TextUtils.isEmpty(url) && redirectRequest(Uri.parse(url), notice_id);
    }

    /**
     * 是否拦截打开的链接
     *
     * @param uri 要检查的链接
     */
    public static boolean redirectRequest(Uri uri, String notice_id) {
        if (uri == null) {
            return false;
        }
        boolean flag = true;
        String host = uri.getHost();
        String url = uri.toString();
        List<String> segments = uri.getPathSegments();
        if ((host.equals("www.guokr.com") || host.equals("m.guokr.com")) && (segments != null && segments.size() >= 2)) {
            String section = segments.get(0);
            String secondSegment = segments.get(1);
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Extras.Extra_Notice_Id, notice_id);
            switch (section) {
                case "article":
                    if (segments.size() == 2) {
                        if (url.matches("^http://(www|m).guokr.com/article/(\\d+)/.*reply(\\d+)$")) {
                            //http://www.guokr.com/article/439937/#reply2903572
                            //http://www.guokr.com/article/439937/?page=2#reply2904015
                            Matcher matcher = Pattern.compile("^http://(www|m).guokr.com/article/(\\d+)/.*reply(\\d+)$").matcher(url);
                            if (matcher.find()) {
                                String reply_id = matcher.group(3);
                                //http://www.guokr.com/article/reply/2907293/
                                Uri replyUri = Uri.parse("http://www.guokr.com/article/reply/" + reply_id + "/");
                                intent.setClass(App.getApp(), SingleReplyActivity.class);
                                intent.setData(replyUri);
                                App.getApp().startActivity(intent);
                            }
                        } else if (url.matches("^http://(www|m).guokr.com/article/\\d+.*")) {
                            //url.matches("^http://(www|m).guokr.com/article/\\d+[/]?$",http://www.guokr.com/article/123456/
                            //url.matches("^http://(www|m).guokr.com/article/\\d+.*"),http://www.guokr.com/article/438683/#comments
                            intent.setClass(App.getApp(), ArticleActivity.class);
                            Article article = new Article();
                            article.setId(secondSegment);
                            intent.putExtra(Extras.Extra_Article, article);
                            App.getApp().startActivity(intent);
                        }
                    } else if (segments.size() == 3) {
                        //跳转.http://www.guokr.com/article/reply/123456
                        if (url.matches("^http://(www|m).guokr.com/article/reply/\\d+[/]?$")) {
                            intent.setClass(App.getApp(), SingleReplyActivity.class);
                            intent.setData(uri);
                            App.getApp().startActivity(intent);
                        }
                    }
                    break;
                case "post":
                    if (segments.size() == 2) {
                        if (url.matches("^http://(www|m).guokr.com/post/(\\d+)/.*#(\\d+)$")) {
                            //http://www.guokr.com/post/662632/#6155334
                            Matcher matcher = Pattern.compile("^http://(www|m).guokr.com/post/(\\d+)/.*#(\\d+)$").matcher(url);
                            if (matcher.find()) {
                                String reply_id = matcher.group(3);
                                Uri replyUri = Uri.parse("http://www.guokr.com/post/reply/" + reply_id + "/");
                                intent.setClass(App.getApp(), SingleReplyActivity.class);
                                intent.setData(replyUri);
                                App.getApp().startActivity(intent);
                            }
                        } else if (url.matches("^http://(www|m).guokr.com/post/\\d+.*")) {
                            //http://www.guokr.com/post/123456/
                            intent.setClass(App.getApp(), PostActivity.class);
                            Post post = new Post();
                            post.setId(secondSegment);
                            intent.putExtra(Extras.Extra_Post, post);
                            App.getApp().startActivity(intent);
                        }
                    } else if (segments.size() == 3) {
                        //跳转
                        if (url.matches("^http://(www|m).guokr.com/post/reply/\\d+[/]?$")) {
                            intent.setClass(App.getApp(), SingleReplyActivity.class);
                            intent.setData(uri);
                            App.getApp().startActivity(intent);
                        }
                    } else if (segments.size() == 4) {
                        //跳转
                        //http://www.guokr.com/post/666281/reply/6224695/
                        if (url.matches("^http://(www|m).guokr.com/post/\\d+/reply/\\d+/?$")) {
                            intent.setClass(App.getApp(), SingleReplyActivity.class);
                            intent.setData(uri);
                            App.getApp().startActivity(intent);
                        }
                    }
                    break;
                case "question":
                    if (segments.size() == 2) {
                        if (url.matches("^http://(www|m).guokr.com/question/(\\d+)/.*answer(\\d+)$")) {
                            //http://www.guokr.com/question/123456/#answer654321
                            Matcher matcher = Pattern.compile("^http://(www|m).guokr.com/question/(\\d+)/.*answer(\\d+)$").matcher(url);
                            if (matcher.find()) {
                                String answer_id = matcher.group(3);
                                Uri answerUri = Uri.parse("http://www.guokr.com/answer/" + answer_id + "/redirect/");
                                intent.setClass(App.getApp(), AnswerActivity.class);
                                intent.setData(answerUri);
                                App.getApp().startActivity(intent);
                            }
                        } else if (url.matches("^http://(www|m).guokr.com/question/\\d+.*$")) {
                            //http://www.guokr.com/question/123456
                            intent.setClass(App.getApp(), QuestionActivity.class);
                            Question question = new Question();
                            question.setId(secondSegment);
                            intent.putExtra(Extras.Extra_Question, question);
                            App.getApp().startActivity(intent);
                        }
                    }
                    break;
                case "answer":
                    //http://www.guokr.com/answer/654321/redirect/
                    if (segments.size() == 3) {
                        //跳转
                        //这个应该是跳转到AnswerActivity
                        if (url.matches("^http://(www|m).guokr.com/answer/\\d+/redirect[/]?$")) {
                            intent.setClass(App.getApp(), AnswerActivity.class);
                            intent.setData(uri);
                            App.getApp().startActivity(intent);
                        }
                    } else if (segments.size() == 2) {
                        //http://www.guokr.com/answer/654321/
                        if (url.matches("^http://(www|m).guokr.com/answer/\\d+[/]?$")) {
                            intent.setClass(App.getApp(), AnswerActivity.class);
                            intent.setData(uri);
                            App.getApp().startActivity(intent);
                        }
                    }
                    break;
                default:
                    flag = false;
                    UrlCheckUtil.openWithBrowser(uri);
                    break;
            }
        } else {
            flag = false;
            openWithBrowser(uri);
        }
        return flag;
    }

    public static void openWithBrowser(String url) {
        openWithBrowser(Uri.parse(url));
    }

    public static void openWithBrowser(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, App.getApp().getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getApp().startActivity(intent);
        } catch (Exception e) {
            ToastUtil.toastSingleton(App.getApp().getString(R.string.maybe_you_have_no_browsers));
        }
    }
}
