package com.example.sourcewall.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.util.StyleChecker;

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

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_article_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        contentView = (WebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
    }

    @Override
    public void setData(Article model) {
        if (article == null) {
            article = model;
            titleView.setText(article.getTitle());
            authorView.setText(article.getAuthor());
            dateView.setText(article.getDate());
            String html = StyleChecker.getArticleHtml(article.getContent());
            contentView.setBackgroundColor(0);
            contentView.getSettings().setDefaultTextEncodingName("UTF-8");
            contentView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "charset=UTF-8", null);
        }
    }

    @Override
    public Article getData() {
        return article;
    }
}
