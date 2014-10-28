package com.example.outerspace;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.outerspace.adapters.ArticleDetailAdapter;
import com.example.outerspace.adapters.PostDetailAdapter;
import com.example.outerspace.commonview.LListView;
import com.example.outerspace.connection.api.PostAPI;
import com.example.outerspace.model.AceModel;
import com.example.outerspace.model.Article;
import com.example.outerspace.model.Post;
import com.example.outerspace.model.SimpleComment;
import com.example.outerspace.util.Consts;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


public class PostActivity extends Activity {
    LListView listView;
    PostDetailAdapter adapter;
    Post mPost;
    LoaderTask loaderTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        setContentView(R.layout.activity_article);
        mPost= (Post) getIntent().getSerializableExtra(Consts.Extra_Post);
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new PostDetailAdapter(this);
        listView.setAdapter(adapter);
        loaderTask = new LoaderTask();
        RequestData data = new RequestData();
        data.isLoadMore = false;
        data.offset = 0;
        loaderTask.execute(data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    models.addAll(PostAPI.getPostCommentsFromJsonUrl(mPost.getId(), data.offset));
                } else {
                    Post post = PostAPI.getPostDetailByIDFromMobileUrl(mPost.getId());
                    mPost.setContent(post.getContent());
                    models.add(mPost);
                    models.addAll(post.getComments());
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
