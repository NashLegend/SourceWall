package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.nashlegend.sourcewall.AppApplication;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.ArticleDetailAdapter;
import net.nashlegend.sourcewall.commonview.AAsyncTask;
import net.nashlegend.sourcewall.commonview.WWebView;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.request.api.ArticleAPI;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;
import net.nashlegend.sourcewall.util.StyleChecker;

import java.util.ArrayList;

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
    private LoaderTask task;

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
        if (SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }

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
            authorView.setText(article.getAuthor());
            dateView.setText(article.getDate());
            String html = StyleChecker.getArticleHtml(article.getContent());
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
        if (loadDesc.findViewById(R.id.text_header_load_hint).getVisibility() == View.VISIBLE) {
            cancelPotentialTask();
            task = new LoaderTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AAsyncTask.Status.RUNNING) {
            task.cancel(true);
            loadDesc.findViewById(R.id.text_header_load_hint).setVisibility(View.VISIBLE);
            loadDesc.findViewById(R.id.progress_header_loading).setVisibility(View.INVISIBLE);
        }
    }

    public void setAdapter(ArticleDetailAdapter adapter) {
        this.adapter = adapter;
    }

    class LoaderTask extends AAsyncTask<Integer, ResultObject, ResultObject> {

        @Override
        protected void onPreExecute() {
            Intent intent = new Intent();
            intent.setAction(Consts.Action_Start_Loading_Latest);
            AppApplication.getApplication().sendBroadcast(intent);
            loadDesc.findViewById(R.id.text_header_load_hint).setVisibility(View.INVISIBLE);
            loadDesc.findViewById(R.id.progress_header_loading).setVisibility(View.VISIBLE);
        }

        @Override
        protected ResultObject doInBackground(Integer... params) {
            return ArticleAPI.getArticleComments(article.getId(), adapter.getCount() - 1, 4999);//1000足够了
        }

        @Override
        protected void onPostExecute(ResultObject result) {
            loadDesc.findViewById(R.id.text_header_load_hint).setVisibility(View.VISIBLE);
            loadDesc.findViewById(R.id.progress_header_loading).setVisibility(View.INVISIBLE);
            if (result.ok) {
                ArrayList<AceModel> ars = (ArrayList<AceModel>) result.result;
                if (ars.size() > 0) {
                    adapter.addAllReversely(ars, 1);
                    adapter.notifyDataSetChanged();
                }
                article.setCommentNum(adapter.getCount() - 1);
            }
            Intent intent = new Intent();
            intent.setAction(Consts.Action_Finish_Loading_Latest);
            intent.putExtra(Consts.Extra_Activity_Hashcode, getContext().hashCode());
            AppApplication.getApplication().sendBroadcast(intent);
        }
    }

    @Override
    public Article getData() {
        return article;
    }
}
