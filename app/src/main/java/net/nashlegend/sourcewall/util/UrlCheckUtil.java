package net.nashlegend.sourcewall.util;

import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;

import net.nashlegend.sourcewall.AnswerActivity;
import net.nashlegend.sourcewall.AppApplication;
import net.nashlegend.sourcewall.ArticleActivity;
import net.nashlegend.sourcewall.PostActivity;
import net.nashlegend.sourcewall.QuestionActivity;
import net.nashlegend.sourcewall.SingleReplyActivity;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.Question;

import java.util.List;

/**
 * Created by NashLegend on 2015/1/14 0014
 */
public class UrlCheckUtil {

    /**
     * 是否拦截打开的链接
     *
     * @param url 要检查的链接
     */
    public static void redirectRequest(String url) {
        redirectRequest(Uri.parse(url));
    }

    /**
     * 是否拦截打开的链接
     *
     * @param uri 要检查的链接
     */
    public static void redirectRequest(Uri uri) {
        redirectRequest(uri, null);
    }

    /**
     * 是否拦截打开的链接
     *
     * @param url 要检查的链接
     */
    public static void redirectRequest(String url, String notice_id) {
        redirectRequest(Uri.parse(url), notice_id);
    }

    /**
     * 是否拦截打开的链接
     *
     * @param uri 要检查的链接
     */
    public static void redirectRequest(Uri uri, String notice_id) {
        String host = uri.getHost();
        List<String> segments = uri.getPathSegments();
        if ((host.equals("www.guokr.com") || host.equals("m.guokr.com")) && (segments != null && segments.size() >= 2)) {
            String section = segments.get(0);
            String secondSegment = segments.get(1);
            String thirdSegment = "";
            if (segments.size() == 3) {
                thirdSegment = segments.get(2);
            }
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Consts.Extra_Notice_Id, notice_id);
            switch (section) {
                case "article":
                    //http://www.guokr.com/article/reply/123456/
                    //http://www.guokr.com/article/654321/
                    if (segments.size() == 2) {
                        intent.setClass(AppApplication.getApplication(), ArticleActivity.class);
                        Article article = new Article();
                        article.setId(secondSegment);
                        intent.putExtra(Consts.Extra_Article, article);
                        AppApplication.getApplication().startActivity(intent);
                    } else if (segments.size() == 3) {
                        //跳转
                        intent.setClass(AppApplication.getApplication(), SingleReplyActivity.class);
                        intent.setData(uri);
                        AppApplication.getApplication().startActivity(intent);
                    }
                    break;
                case "post":
                    //http://www.guokr.com/post/123456/
                    //http://www.guokr.com/post/reply/654321/
                    if (segments.size() == 2) {
                        intent.setClass(AppApplication.getApplication(), PostActivity.class);
                        Post post = new Post();
                        post.setId(secondSegment);
                        intent.putExtra(Consts.Extra_Post, post);
                        AppApplication.getApplication().startActivity(intent);
                    } else if (segments.size() == 3) {
                        //跳转
                        intent.setClass(AppApplication.getApplication(), SingleReplyActivity.class);
                        intent.setData(uri);
                        AppApplication.getApplication().startActivity(intent);
                    }
                    break;
                case "question":
                    //http://www.guokr.com/question/123456
                    if (segments.size() == 2) {
                        intent.setClass(AppApplication.getApplication(), QuestionActivity.class);
                        Question question = new Question();
                        question.setId(secondSegment);
                        intent.putExtra(Consts.Extra_Question, question);
                        AppApplication.getApplication().startActivity(intent);
                    }
                case "answer":
                    //http://www.guokr.com/answer/654321/redirect/
                    if (segments.size() == 3) {
                        //跳转
                        //这个应该是跳转到AnswerActivity
                        intent.setClass(AppApplication.getApplication(), AnswerActivity.class);
                        intent.setData(uri);
                        AppApplication.getApplication().startActivity(intent);
                    }
                    break;
                default:
                    UrlCheckUtil.openWithBrowser(uri);
                    break;
            }
        } else {
            openWithBrowser(uri);
        }
    }

    public static void openWithBrowser(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, AppApplication.getApplication().getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AppApplication.getApplication().startActivity(intent);
    }
}
