package com.example.sourcewall;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.sourcewall.adapters.ArticleDetailAdapter;
import com.example.sourcewall.commonview.LListView;
import com.example.sourcewall.commonview.LoadingView;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.ArticleAPI;
import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.dialogs.FavorDialog;
import com.example.sourcewall.dialogs.InputDialog;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.model.UComment;
import com.example.sourcewall.util.AutoHideUtil;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.RegUtil;
import com.example.sourcewall.util.ToastUtil;
import com.example.sourcewall.view.MediumListItemView;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class ArticleActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener {

    LListView listView;
    ArticleDetailAdapter adapter;
    Article article;
    LoaderTask task;
    Toolbar toolbar;
    View bottomLayout;
    FloatingActionButton replyButton;
    FloatingActionButton recomButton;
    FloatingActionButton favorButton;
    LoadingView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        loadingView = (LoadingView) findViewById(R.id.article_progress_loading);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        article = (Article) getIntent().getSerializableExtra(Consts.Extra_Article);
        bottomLayout = findViewById(R.id.layout_operation);
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new ArticleDetailAdapter(this);
        listView.setAdapter(adapter);

        AutoHideUtil.applyListViewAutoHide(this, listView, toolbar, bottomLayout, (int) getResources().getDimension(R.dimen.abc_action_bar_default_height_material));

        listView.setOnItemClickListener(onItemClickListener);
        listView.setCanPullToLoadMore(false);
        listView.setOnRefreshListener(this);

        replyButton = (FloatingActionButton) findViewById(R.id.button_reply);
        recomButton = (FloatingActionButton) findViewById(R.id.button_recommend);
        favorButton = (FloatingActionButton) findViewById(R.id.button_favor);

        replyButton.setOnClickListener(this);
        recomButton.setOnClickListener(this);
        favorButton.setOnClickListener(this);

        loadData(-1);
    }

    /**
     * @param offset -1是指刷新
     */
    private void loadData(int offset) {
        cancelPotentialTask();
        task = new LoaderTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, offset);
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
            listView.doneOperation();
        }
    }

    private void replyArticle() {
        replyArticle(null);
    }

    private void replyArticle(UComment comment) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
            return;
        }
        Intent intent = new Intent(this, ReplyActivity.class);
        intent.putExtra(Consts.Extra_Ace_Model, article);
        if (comment != null) {
            intent.putExtra(Consts.Extra_Simple_Comment, comment);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, 0);
    }

    private void recommend() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
            return;
        }
        InputDialog.Builder builder = new InputDialog.Builder(this);
        builder.setTitle(R.string.recommend_article);
        builder.setCancelable(true);
        builder.setCanceledOnTouchOutside(false);
        builder.setOnClickListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    InputDialog d = (InputDialog) dialog;
                    String text = d.InputString;
                    RecommendTask recommendTask = new RecommendTask();
                    recommendTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, article.getId(), article.getTitle(), article.getSummary(), text);
                } else {
                    // cancel recommend
                }
            }
        });
        InputDialog inputDialog = builder.create();
        inputDialog.show();
    }

    private void favor() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
            return;
        }
        // basket dialog
        new FavorDialog.Builder(this).setTitle(R.string.action_favor).create(article).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void replyComment(UComment comment) {
        replyArticle(comment);
    }

    private void likeComment(UComment comment) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
            return;
        }
        LikeCommentTask likeCommentTask = new LikeCommentTask();
        likeCommentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment);
    }

    private void copyComment(UComment comment) {
        //do nothing
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(null, RegUtil.html2PlainText(comment.getContent())));
    }

    private void onReplyItemClick(final View view, int position, long id) {
        String[] operations = {getString(R.string.action_reply), getString(R.string.action_like), getString(R.string.action_copy)};
        if (view instanceof MediumListItemView) {
            new AlertDialog.Builder(this).setTitle("").setItems(operations, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UComment comment = ((MediumListItemView) view).getData();
                    switch (which) {
                        case 0:
                            replyComment(comment);
                            break;
                        case 1:
                            likeComment(comment);
                            break;
                        case 2:
                            copyComment(comment);
                            break;
                    }
                }
            }).create().show();
        }
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            onReplyItemClick(view, position, id);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_reply:
                replyArticle();
                break;
            case R.id.button_recommend:
                recommend();
                break;
            case R.id.button_favor:
                favor();
                break;
        }
    }

    class RecommendTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            String articleID = params[0];
            String title = params[1];
            String summary = params[2];
            String comment = params[3];
            return ArticleAPI.recommendArticle(articleID, title, summary, comment);
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                ToastUtil.toast(R.string.recommend_ok);
            } else {
                ToastUtil.toast(R.string.recommend_failed);
            }
        }
    }

    @Override
    public void onStartRefresh() {
        loadData(-1);
    }

    @Override
    public void onStartLoadMore() {
        loadData(adapter.getCount() - 1);
    }

    class LoaderTask extends AsyncTask<Integer, Integer, ResultObject> {
        int offset;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(Integer... params) {
            offset = params[0];
            ArrayList<AceModel> models = new ArrayList<AceModel>();
            ResultObject resultObject = new ResultObject();
            try {
                if (offset < 0) {
                    //同时取了热门回帖，但是在这里没有显示 TODO
                    Article detailArticle = ArticleAPI.getArticleDetailByID(article.getId());
                    ArrayList<UComment> simpleComments = ArticleAPI.getArticleComments(article.getId(), 0);
                    article.setContent(detailArticle.getContent());
                    models.add(article);
                    models.addAll(simpleComments);
                } else {
                    models.addAll(ArticleAPI.getArticleComments(article.getId(), offset));
                }
                resultObject.result = models;
                resultObject.ok = true;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResultObject result) {
            loadingView.setVisibility(View.GONE);
            if (!isCancelled()) {
                if (result.ok) {
                    ArrayList<AceModel> ars = (ArrayList<AceModel>) result.result;
                    if (offset < 0) {
                        //Refresh
                        if (ars.size() > 0) {
                            adapter.setList(ars);
                            adapter.notifyDataSetInvalidated();
                        } else {
                            //no data loaded,不清除，保留旧数据
                        }
                    } else {
                        //Load More
                        if (ars.size() > 0) {
                            adapter.addAll(ars);
                            adapter.notifyDataSetChanged();
                        } else {
                            //no data loaded
                        }
                    }
                    if (adapter.getCount() > 0) {
                        //listView.setCanPullToLoadMore(ars.size() >= 20);
                        listView.setCanPullToLoadMore(true);
                    } else {
                        listView.setCanPullToLoadMore(false);
                    }
                } else {
                    // load error
                }
                listView.doneOperation();
            }
        }
    }

    class LikeCommentTask extends AsyncTask<UComment, Integer, ResultObject> {

        UComment comment;

        @Override
        protected ResultObject doInBackground(UComment... params) {
            comment = params[0];
            return ArticleAPI.likeComment(comment.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                comment.setLikeNum(comment.getLikeNum() + 1);
                adapter.notifyDataSetChanged();
            } else {
                //do nothing
            }
        }
    }
}
