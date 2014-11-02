package com.example.sourcewall;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.sourcewall.adapters.ArticleDetailAdapter;
import com.example.sourcewall.commonview.LListView;
import com.example.sourcewall.connection.api.ArticleAPI;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.model.SimpleComment;
import com.example.sourcewall.util.Consts;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


public class ArticleActivity extends BaseActivity implements LListView.OnRefreshListener {

    LListView listView;
    ArticleDetailAdapter adapter;
    Article article;
    LoaderTask loaderTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        article = (Article) getIntent().getSerializableExtra(Consts.Extra_Article);
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new ArticleDetailAdapter(this);
        listView.setAdapter(adapter);
        loaderTask = new LoaderTask();
        RequestData data = new RequestData();
        data.isLoadMore = false;
        data.offset = 0;
        loaderTask.execute(data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStartRefresh() {
        //TODO
    }

    @Override
    public void onStartLoadMore() {
        //TODO
    }

    class LoaderTask extends AsyncTask<RequestData, Integer, Boolean> {
        RequestData data = null;
        ArrayList<AceModel> models = new ArrayList<AceModel>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(RequestData... params) {
            data = params[0];
            try {
                if (data.isLoadMore) {
                    models.addAll(ArticleAPI.getArticleComments(article.getId(), data.offset));
                } else {
                    Article detailArticle = ArticleAPI.getArticleDetailByID(article.getId());
                    ArrayList<SimpleComment> simpleComments = ArticleAPI.getArticleComments(article.getId(), 0);
                    article.setContent(detailArticle.getContent());
                    models.add(article);
                    models.addAll(simpleComments);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                if (data.isLoadMore) {
                    adapter.addAll(models);
                } else {
                    adapter.setList(models);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    class RequestData {
        boolean isLoadMore = false;
        int offset = 0;
    }
}
