package com.example.outerspace.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.outerspace.R;
import com.example.outerspace.model.AceModel;
import com.example.outerspace.model.Article;
import com.example.outerspace.util.DisplayUtil;
import com.example.outerspace.util.ImageFetcher.ImageFetcher;
import com.squareup.picasso.Picasso;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class ArticleListItemView extends AceView<Article> {

    public Article getArticle() {
        return article;
    }

    private TextView titleView;
    private TextView contentView;
    private TextView authorView;
    private TextView dateView;
    private TextView replyView;
    private ImageView titleImage;
    private Article article;
    ImageFetcher titleImageFetcher;

    public ArticleListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_article_item_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        contentView = (TextView) findViewById(R.id.text_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        replyView = (TextView) findViewById(R.id.text_replies_num);
        titleImage = (ImageView) findViewById(R.id.image_title);
        titleImageFetcher = new ImageFetcher(getContext(), DisplayUtil.getScreenWidth(getContext()), 0);
    }

    public ArticleListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Article model) {
        article = model;
        titleView.setText(article.getTitle());
        contentView.setText(article.getSummary());
        authorView.setText(article.getAuthor());
        dateView.setText(article.getDate());
        replyView.setText(article.getCommentNum() + "");
        if (TextUtils.isEmpty(article.getImageUrl())) {
            titleImage.setImageResource(R.drawable.ic_launcher);
        } else {
            Picasso.with(getContext()).load(article.getImageUrl())
                    .resize(DisplayUtil.getScreenWidth(getContext()), -1)
                    .into(titleImage);
        }
    }
}
