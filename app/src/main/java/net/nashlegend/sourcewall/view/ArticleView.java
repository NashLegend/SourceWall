package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.ArticleDetailAdapter;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.ArticleAPI;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.PrefsUtil;
import net.nashlegend.sourcewall.util.StyleChecker;
import net.nashlegend.sourcewall.view.common.WWebView;

import java.util.ArrayList;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class ArticleView extends AceView<Article> {

    private TextView titleView;
    private WWebView contentView;
    private TextView authorView;
    private TextView dateView;
    private View loadDesc;
    private ArticleDetailAdapter adapter;

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
        contentView = (WWebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        loadDesc = findViewById(R.id.view_load_latest);

        Resources.Theme theme = getContext().getTheme();
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.cardBackgroundColor});
        int colorBack = typedArray.getColor(0, 0);
        typedArray.recycle();
        contentView.setBackgroundColor(colorBack);
    }

    @Override
    public void setData(Article model) {
        if (article == null) {
            article = model;
            titleView.setText(article.getTitle());
            authorView.setText(article.getAuthor().getName());
            dateView.setText(article.getDate());
            String html = StyleChecker.getArticleHtml(article.getContent());
            contentView.setPrimarySource(article.getContent());
            contentView.loadDataWithBaseURL(Consts.Base_Url, html, "text/html", "charset=UTF-8", null);
        } else {
            article = model;
        }

        if (article.isDesc()) {
            loadDesc.setVisibility(VISIBLE);
            contentView.setVisibility(GONE);
        } else {
            loadDesc.setVisibility(GONE);
            contentView.setVisibility(VISIBLE);
        }

        loadDesc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLatest();
            }
        });
    }


    private void loadLatest() {
        Intent intent = new Intent();
        intent.setAction(Consts.Action_Start_Loading_Latest);
        App.getApp().sendBroadcast(intent);
        loadDesc.findViewById(R.id.text_header_load_hint).setVisibility(View.INVISIBLE);
        loadDesc.findViewById(R.id.progress_header_loading).setVisibility(View.VISIBLE);

        ArticleAPI
                .getArticleReplies(article.getId(), article.getCommentNum(), 4999)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseObject<ArrayList<UComment>>>() {
                    @Override
                    public void call(ResponseObject<ArrayList<UComment>> result) {
                        loadDesc.findViewById(R.id.text_header_load_hint).setVisibility(View.VISIBLE);
                        loadDesc.findViewById(R.id.progress_header_loading).setVisibility(View.INVISIBLE);
                        if (result.ok) {
                            ArrayList<UComment> ars = result.result;
                            if (ars.size() > 0) {
                                adapter.addAllReversely(ars, 1);
                                adapter.notifyDataSetChanged();
                            }
                            article.setCommentNum(article.getCommentNum() + ars.size());
                        }
                        Intent intent = new Intent();
                        intent.setAction(Consts.Action_Finish_Loading_Latest);
                        intent.putExtra(Consts.Extra_Activity_Hashcode, getContext().hashCode());
                        App.getApp().sendBroadcast(intent);
                    }
                });
    }

    public void setAdapter(ArticleDetailAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Article getData() {
        return article;
    }
}
