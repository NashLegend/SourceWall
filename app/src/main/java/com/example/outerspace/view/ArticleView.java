package com.example.outerspace.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.TextView;

import com.example.outerspace.R;
import com.example.outerspace.model.AceModel;
import com.example.outerspace.model.Article;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class ArticleView extends AceView {

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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_article_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        contentView = (WebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
    }

    public ArticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(AceModel model) {
        if (model instanceof Article) {
            article = (Article) model;
            titleView.setText(article.getTitle());
            authorView.setText(article.getAuthor());
            dateView.setText(article.getDate());
            String html = "<html class=\"no-js screen-scroll\"><head>\n" +
                    "<link rel=\"stylesheet\" href=\"http://static.guokr.com/apps/minisite/styles/3b737dd5.main.css\">\n" +
                    "<link rel=\"stylesheet\" href=\"http://static.guokr.com/apps/minisite/styles/e8ff5a9c.gbbcode.css\">\n" +
                    "<link rel=\"stylesheet\" href=\"http://static.guokr.com/apps/minisite/styles/edfe43e5.article.css\">\n" +
                    "<style>[touch-action=\"none\"]{ -ms-touch-action: none; touch-action: none; }[touch-action=\"pan-x\"]" +
                    "{ -ms-touch-action: pan-x; touch-action: pan-x; }" +
                    "[touch-action=\"pan-y\"]{ -ms-touch-action: pan-y; touch-action: pan-y; }" +
                    "[touch-action=\"scroll\"],[touch-action=\"pan-x pan-y\"]," +
                    "[touch-action=\"pan-y pan-x\"]{ -ms-touch-action: pan-x pan-y; touch-action: pan-x pan-y; }" +
                    "</style><style id=\"style-1-cropbar-clipper\">/* Copyright 2014 Evernote Corporation. All rights reserved. */\n" +
                    ".en-markup-crop-options {\n" +
                    "    top: 18px !important;\n" +
                    "    left: 50% !important;\n" +
                    "    margin-left: -90px !important;\n" +
                    "    width: 180px !important;\n" +
                    "    border: 2px rgba(255,255,255,.38) solid !important;\n" +
                    "    border-radius: 4px !important;\n" +
                    "}\n" +
                    ".en-markup-crop-options div div:first-of-type {\n" +
                    "    margin-left: 0px !important;\n" +
                    "}\n" +
                    "</style></head>\n" +
                    "    <body>\n" +
                    "<div class=\"article-page\">\n" +
                    "    <div class=\"main\">\n" +
                    "        <div class=\"content\"> <div itemprop=\"http://rdfs.org/sioc/ns#content\"" +
                    " class=\"content-txt\" id=\"articleContent\"> " + article.getContent() + "\n" +
                    "        </div>\n" + "</div>" +
                    "    </div>\n" +
                    " </div>\n" +
                    "</body></html>";
            contentView.getSettings().setDefaultTextEncodingName("UTF-8");
            contentView.loadData(html, "text/html; charset=UTF-8", null);
        }
    }
}
