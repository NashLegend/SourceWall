package com.example.sourcewall;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.sourcewall.adapters.ArticleDetailAdapter;
import com.example.sourcewall.commonview.LListView;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.ArticleAPI;
import com.example.sourcewall.dialogs.InputDialog;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.model.SimpleComment;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.ToastUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


public class ArticleActivity extends BaseActivity implements LListView.OnRefreshListener {

    LListView listView;
    ArticleDetailAdapter adapter;
    Article article;
    LoaderTask task;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        article = (Article) getIntent().getSerializableExtra(Consts.Extra_Article);
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new ArticleDetailAdapter(this);
        listView.setAdapter(adapter);
        listView.setCanPullToRefresh(false);
        listView.setCanPullToLoadMore(true);
        listView.setOnRefreshListener(this);
        loadData(0);
    }

    private void loadData(int offset) {
        if (offset < 0) {
            offset = 0;
        }
        cancelPotentialTask();
        task = new LoaderTask();
        task.execute(offset);
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
            listView.doneOperation();
        }
    }

    private void startReplyActivity() {
        Intent intent = new Intent(this, ReplyArticleActivity.class);
        intent.putExtra(Consts.Extra_Article, article);
        startActivity(intent);
    }

    private void recommend() {
        InputDialog.Builder builder = new InputDialog.Builder(this);
        builder.setTitle("Recommend This Article");
        builder.setCancelable(true);
        builder.setCanceledOnTouchOutside(false);
        builder.setOnClickListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    InputDialog d = (InputDialog) dialog;
                    String text = d.InputString;
                    RecommendTask recommendTask = new RecommendTask();
                    recommendTask.execute(article.getId(), article.getTitle(), article.getSummary(), text);
                } else {
                    // cancel recommend
                }
            }
        });
        InputDialog inputDialog = builder.create();
        inputDialog.show();
    }

    private void favor() {
        // basket dialog
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_reply:
                startReplyActivity();
                break;
            case R.id.action_recommend:
                recommend();
                break;
            case R.id.action_favor:
                favor();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class FavorTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            String articleID = params[0];
            String title = params[1];
            String basketID = params[2];
            return ArticleAPI.collectArticle(articleID, title, basketID);
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                ToastUtil.toast("Favor OK");
            } else {
                ToastUtil.toast("Favor Failed");
            }
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
                ToastUtil.toast("Recommend OK");
            } else {
                ToastUtil.toast("Recommend Failed");
            }
        }
    }

    @Override
    public void onStartRefresh() {
        loadData(0);
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
                if (offset > 0) {
                    models.addAll(ArticleAPI.getArticleComments(article.getId(), offset));
                } else {
                    //同时取了热门回帖，但是在这里没有显示 TODO
                    Article detailArticle = ArticleAPI.getArticleDetailByID(article.getId());
                    ArrayList<SimpleComment> simpleComments = ArticleAPI.getArticleComments(article.getId(), 0);
                    article.setContent(detailArticle.getContent());
                    models.add(article);
                    models.addAll(simpleComments);
                }
                resultObject.result = models;
                resultObject.ok = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResultObject result) {
            if (!isCancelled()) {
                if (result.ok) {
                    ArrayList<AceModel> ars = (ArrayList<AceModel>) result.result;
                    if (offset > 0) {
                        //Load More
                        if (ars.size() > 0) {
                            adapter.addAll(ars);
                        } else {
                            //no data loaded
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        //Refresh
                        if (ars.size() > 0) {
                            adapter.setList(ars);
                        } else {
                            //no data loaded,不清除，保留旧数据
                        }
                        adapter.notifyDataSetInvalidated();
                    }
                } else {
                    // load error
                }
                listView.doneOperation();
            }
        }
    }
}
