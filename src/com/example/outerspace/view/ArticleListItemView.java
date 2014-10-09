package com.example.outerspace.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.outerspace.R;
import com.example.outerspace.model.AceModel;
import com.example.outerspace.model.Article;
import com.example.outerspace.util.DisplayUtil;
import com.example.outerspace.util.ImageFetcher.ImageFetcher;
import com.example.outerspace.util.ImageUtil.ImageLoader;

/**
 * Created by NashLegend on 2014/9/18 0018.
 */
public class ArticleListItemView extends AceView{

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
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
    public void setData(AceModel model) {
        article = (Article) model;
        titleView.setText(article.getTitle());
        contentView.setText(article.getSummary());
        authorView.setText(article.getAuthor());
        dateView.setText(article.getDate());
        replyView.setText(article.getCommentNum() + "");
        loadImage();
    }


    public void loadImage() {
        ViewGroup.LayoutParams params = titleImage.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        titleImage.setLayoutParams(params);
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
        }
        if (!TextUtils.isEmpty(article.getImageUrl())) {
            task = new LoaderTask();
            task.execute();
        }
    }

    LoaderTask task;

    class LoaderTask extends AsyncTask<Void, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            return ImageLoader.getBitmapForUrl(article.getImageUrl());
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                ViewGroup.LayoutParams params = titleImage.getLayoutParams();
                params.height = titleImage.getWidth() * bitmap.getHeight() / bitmap.getWidth();
                titleImage.setLayoutParams(params);
                titleImage.setImageBitmap(bitmap);
            } else {

            }
        }
    }
}
