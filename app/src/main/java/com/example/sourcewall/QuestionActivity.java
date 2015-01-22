package com.example.sourcewall;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.sourcewall.CommonView.LListView;
import com.example.sourcewall.CommonView.LoadingView;
import com.example.sourcewall.adapters.QuestionDetailAdapter;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.QuestionAPI;
import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.dialogs.FavorDialog;
import com.example.sourcewall.dialogs.InputDialog;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.model.Question;
import com.example.sourcewall.util.AutoHideUtil;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.ToastUtil;
import com.example.sourcewall.view.AnswerListItemView;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class QuestionActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener {

    LListView listView;
    QuestionDetailAdapter adapter;
    Question question;
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
        setContentView(R.layout.activity_question);
        loadingView = (LoadingView) findViewById(R.id.question_progress_loading);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        bottomLayout = findViewById(R.id.layout_operation);
        question = (Question) getIntent().getSerializableExtra(Consts.Extra_Question);
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new QuestionDetailAdapter(this);
        listView.setAdapter(adapter);

        AutoHideUtil.applyListViewAutoHide(this, listView, toolbar, bottomLayout, (int) getResources().getDimension(R.dimen.abc_action_bar_default_height_material));

        listView.setCanPullToLoadMore(false);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(onItemClickListener);

        replyButton = (FloatingActionButton) findViewById(R.id.button_reply);
        recomButton = (FloatingActionButton) findViewById(R.id.button_recommend);
        favorButton = (FloatingActionButton) findViewById(R.id.button_favor);

        replyButton.setOnClickListener(this);
        recomButton.setOnClickListener(this);
        favorButton.setOnClickListener(this);

        loadData(-1);
    }

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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStartRefresh() {
        loadData(-1);
    }

    @Override
    public void onStartLoadMore() {
        loadData(adapter.getCount() - 1);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            onReplyItemClick(view, position, id);
        }
    };

    private void onReplyItemClick(final View view, int position, long id) {
        if (view instanceof AnswerListItemView) {
            Intent intent = new Intent(this, AnswerActivity.class);
            intent.putExtra(Consts.Extra_Answer, ((AnswerListItemView) view).getData());
            intent.putExtra(Consts.Extra_Question, question);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_reply:
                answerQuestion();
                break;
            case R.id.button_recommend:
                recommend();
                break;
            case R.id.button_favor:
                favor();
                break;
        }
    }

    private void favor() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            new FavorDialog.Builder(this).setTitle(R.string.action_favor).create(question).show();
        }
    }

    private void recommend() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
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
                        recommendTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, question.getId(), question.getTitle(), question.getSummary(), text);
                    } else {
                        // cancel recommend
                    }
                }
            });
            InputDialog inputDialog = builder.create();
            inputDialog.show();
        }
    }

    private void answerQuestion() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            Intent intent = new Intent(this, ReplyActivity.class);
            intent.putExtra(Consts.Extra_Ace_Model, question);
            startActivity(intent);
        }
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
            if (offset < 0) {
                return QuestionAPI.getQuestionFirstPage(question.getId());
            } else {
                return QuestionAPI.getQuestionAnswers(question.getId(), offset);
            }
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

    class RecommendTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            String questionID = params[0];
            String title = params[1];
            String summary = params[2];
            String comment = params[3];
            return QuestionAPI.recommendQuestion(questionID, title, summary, comment);
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
}
