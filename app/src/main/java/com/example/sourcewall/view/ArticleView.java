package com.example.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.model.Article;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class ArticleView extends AceView<Article> {

    private TextView titleView;
    private WebView contentView;
    private TextView authorView;
    private TextView dateView;

    public Article getArticle() {
        return article;
    }

    private Article article;

    public ArticleView(Context context) {
        super(context);
        initViews();
    }

    public ArticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ArticleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    private void initViews(){
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_article_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        contentView = (WebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
    }

    @Override
    public void setData(Article model) {
        article = model;
        titleView.setText(article.getTitle());
        authorView.setText(article.getAuthor());
        dateView.setText(article.getDate());
        String html = "<html class=\"no-js screen-scroll\">\n" +
                " <head> \n" +
                "  <meta charset=\"UTF-8\" /> \n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge,chrome=1\" /> \n" +
                "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1,user-scalable=no\" /> \n" +
                "  <meta name=\"format-detection\" content=\"telephone=no\" /> \n" +
                "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/minisite/styles/3b737dd5.main.css\" type=\"text/css\"/> \n" +
                "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/minisite/styles/e8ff5a9c.gbbcode.css\" type=\"text/css\"/> \n" +
                "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/minisite/styles/edfe43e5.article.css\" type=\"text/css\"/> \n" +
                " </head> \n" +
                " <body> \n" +
                "  <div class=\"container article-page\"> \n" +
                "   <div class=\"main\"> \n" +
                "    <div class=\"content\"> \n" + article.getContent() +
                "    </div> \n" +
                "   </div> \n" +
                "  </div> \n" +
                " </body>\n" +
                "</html>";
        contentView.getSettings().setDefaultTextEncodingName("UTF-8");
        contentView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "charset=UTF-8", null);
    }

    @Override
    public Article getData() {
        return article;
    }
}
