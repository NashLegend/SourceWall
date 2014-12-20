package com.example.sourcewall;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.sourcewall.adapters.SimpleCommentAdapter;
import com.example.sourcewall.commonview.LListView;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.QuestionAPI;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.model.Question;
import com.example.sourcewall.model.QuestionAnswer;
import com.example.sourcewall.model.UniversalComment;
import com.example.sourcewall.util.Consts;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


public class SimpleReplyActivity extends SwipeActivity implements LListView.OnRefreshListener {

    private AceModel aceModel;
    LoaderTask task;
    LListView listView;
    SimpleCommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_reply);
        aceModel = (AceModel) getIntent().getSerializableExtra(Consts.Extra_Ace_Model);
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new SimpleCommentAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setCanPullToRefresh(false);
        listView.setCanPullToLoadMore(false);
        listView.setOnRefreshListener(this);
//        loadData(0);
    }

    /**
     * offset=0是指刷新
     *
     * @param offset
     */
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_simple_reply, menu);
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

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            onReplyItemClick(view, position, id);
        }
    };

    private void onReplyItemClick(final View view, int position, long id) {

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
        protected ResultObject doInBackground(Integer... params) {
            offset = params[0];
            ArrayList<UniversalComment> models = new ArrayList<>();
            ResultObject resultObject = new ResultObject();
            try {
                if (aceModel instanceof Question) {
                    models = QuestionAPI.getQuestionComments(((Question) aceModel).getId(), offset);
                } else if (aceModel instanceof QuestionAnswer) {
                    models = QuestionAPI.getAnswerComments(((QuestionAnswer) aceModel).getID(), offset);
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
                    ArrayList<UniversalComment> ars = (ArrayList<UniversalComment>) result.result;
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
                } else {
                    // load error
                }
                if (adapter.getCount() > 0) {
                    listView.setCanPullToLoadMore(true);
                    listView.setCanPullToRefresh(false);
                } else {
                    listView.setCanPullToLoadMore(false);
                    listView.setCanPullToRefresh(true);
                }
                listView.doneOperation();
            }
        }
    }
}
