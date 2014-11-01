package com.example.outerspace;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.outerspace.adapters.QuestionDetailAdapter;
import com.example.outerspace.commonview.LListView;
import com.example.outerspace.connection.api.QuestionAPI;
import com.example.outerspace.model.AceModel;
import com.example.outerspace.model.Question;
import com.example.outerspace.util.Consts;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


public class QuestionActivity extends BaseActivity implements LListView.OnRefreshListener {

    LListView listView;
    QuestionDetailAdapter adapter;
    Question mQuestion;
    LoaderTask loaderTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        setContentView(R.layout.activity_article);
        mQuestion = (Question) getIntent().getSerializableExtra(Consts.Extra_Question);
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new QuestionDetailAdapter(this);
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
        getMenuInflater().inflate(R.menu.question, menu);
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
                if (!data.isLoadMore) {
                    Question question = QuestionAPI.getQuestionDetailByID(mQuestion.getId());
                    mQuestion = question;
                    models.add(question);
                }
                models.addAll(QuestionAPI.getQuestionAnswers(mQuestion.getId(), data.offset));
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
                    if (models.size() > 0) {
                        adapter.setList(models);
                    }
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
