package net.nashlegend.sourcewall.util;

import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;

import net.nashlegend.sourcewall.AppApplication;
import net.nashlegend.sourcewall.ArticleActivity;
import net.nashlegend.sourcewall.PostActivity;
import net.nashlegend.sourcewall.QuestionActivity;
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
        String host = uri.getHost();
        List<String> segments = uri.getPathSegments();
        if ((host.equals("www.guokr.com") || host.equals("m.guokr.com")) && (segments != null && segments.size() == 2)) {
            String section = segments.get(0);
            String id = segments.get(1);
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            switch (section) {
                case "article":
                    intent.setClass(AppApplication.getApplication(), ArticleActivity.class);
                    Article article = new Article();
                    article.setId(id);
                    intent.putExtra(Consts.Extra_Article, article);
                    AppApplication.getApplication().startActivity(intent);
                    break;
                case "post":
                    intent.setClass(AppApplication.getApplication(), PostActivity.class);
                    Post post = new Post();
                    post.setId(id);
                    intent.putExtra(Consts.Extra_Post, post);
                    AppApplication.getApplication().startActivity(intent);
                    break;
                case "question":
                    intent.setClass(AppApplication.getApplication(), QuestionActivity.class);
                    Question question = new Question();
                    question.setId(id);
                    intent.putExtra(Consts.Extra_Question, question);
                    AppApplication.getApplication().startActivity(intent);
                    break;
                default:
                    openWithBrowser(uri);
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
