package com.example.outerspace.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.outerspace.R;
import com.example.outerspace.model.AceModel;
import com.example.outerspace.model.Article;
import com.example.outerspace.util.ImageUtil.ImageLoader;

/**
 * Created by NashLegend on 2014/9/18 0018.
 */
public class ArticleListItemView extends AceView {

    TextView titleView;
    TextView summaryView;
    TextView authorView;
    TextView dateView;
    TextView replyView;
    ImageView titleImage;
    Article article;
    ImageLoader loader;

    public ArticleListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_article_item_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        summaryView = (TextView) findViewById(R.id.text_summary);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        replyView = (TextView) findViewById(R.id.text_replies_num);
        titleImage = (ImageView) findViewById(R.id.image_title);
    }

    public ArticleListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(AceModel model) {
        article = (Article) model;
        titleView.setText(article.getTitle());
        summaryView.setText(article.getSummary());
        authorView.setText(article.getAuthor());
        dateView.setText(article.getDate());
        replyView.setText(article.getCommentNum() + "");
        loader.loadImage(titleImage, article.getImageUrl());
    }
}
