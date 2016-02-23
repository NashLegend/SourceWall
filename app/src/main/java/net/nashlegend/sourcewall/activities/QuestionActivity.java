package net.nashlegend.sourcewall.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.QuestionDetailAdapter;
import net.nashlegend.sourcewall.commonview.AAsyncTask;
import net.nashlegend.sourcewall.commonview.IStackedAsyncTaskInterface;
import net.nashlegend.sourcewall.commonview.LListView;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.dialogs.FavorDialog;
import net.nashlegend.sourcewall.dialogs.InputDialog;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.request.api.QuestionAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.AutoHideUtil;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.ShareUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;
import net.nashlegend.sourcewall.view.AnswerListItemView;

import java.lang.reflect.Method;
import java.util.ArrayList;


public class QuestionActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener, LoadingView.ReloadListener {

    private LListView listView;
    private QuestionDetailAdapter adapter;
    private Question question;
    private LoaderTask task;
    private LoadingView loadingView;
    private String notice_id;
    private FloatingActionsMenu floatingActionsMenu;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        MobclickAgent.onEvent(this, Mob.Event_Open_Question);
        loadingView = (LoadingView) findViewById(R.id.question_progress_loading);
        loadingView.setReloadListener(this);
        progressBar = (ProgressBar) findViewById(R.id.question_loading);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {

            boolean preparingToScrollToHead = false;

            @Override
            public void onClick(View v) {
                if (preparingToScrollToHead) {
                    listView.setSelection(0);
                } else {
                    preparingToScrollToHead = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            preparingToScrollToHead = false;
                        }
                    }, 200);
                }
            }
        });
        View headView = findViewById(R.id.head_view);
        question = getIntent().getParcelableExtra(Consts.Extra_Question);
        notice_id = getIntent().getStringExtra(Consts.Extra_Notice_Id);
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new QuestionDetailAdapter(this);
        listView.setAdapter(adapter);

        listView.setCanPullToLoadMore(false);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(onItemClickListener);

        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.layout_operation);
        FloatingActionButton replyButton = (FloatingActionButton) findViewById(R.id.button_reply);
        FloatingActionButton recomButton = (FloatingActionButton) findViewById(R.id.button_recommend);
        FloatingActionButton favorButton = (FloatingActionButton) findViewById(R.id.button_favor);

        replyButton.setOnClickListener(this);
        recomButton.setOnClickListener(this);
        favorButton.setOnClickListener(this);

        AutoHideUtil.applyListViewAutoHide(this, listView, headView, floatingActionsMenu, (int) getResources().getDimension(R.dimen.abc_action_bar_default_height_material));
        floatingActionsMenu.setVisibility(View.GONE);
        loadData(-1);
    }

    private void loadData(int offset) {
        cancelPotentialTask();
        task = new LoaderTask(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, offset);
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AAsyncTask.Status.RUNNING) {
            task.cancel(true);
            listView.doneOperation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_question, menu);
        try {
            Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
            m.setAccessible(true);
            m.invoke(menu, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!UserAPI.isLoggedIn()) {
            menu.findItem(R.id.action_follow_question).setVisible(false);
            menu.findItem(R.id.action_unfollow_question).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_follow_question:
                followQuestion();
                break;
            case R.id.action_unfollow_question:
                unfollowQuestion();
                break;
            case R.id.action_open_in_browser:
                if (!TextUtils.isEmpty(question.getUrl())) {
                    MobclickAgent.onEvent(this, Mob.Event_Open_Question_In_Browser);
                    UrlCheckUtil.openWithBrowser(question.getUrl());
                }
                break;
            case R.id.action_share_to_wechat_circle:
                MobclickAgent.onEvent(this, Mob.Event_Share_Question_To_Wechat_Circle);
                ShareUtil.shareToWeiXinCircle(this, question.getUrl(), question.getTitle(), question.getSummary(), null);
                break;
            case R.id.action_share_to_wechat_friends:
                MobclickAgent.onEvent(this, Mob.Event_Share_Question_To_Wechat_friend);
                ShareUtil.shareToWeiXinFriends(this, question.getUrl(), question.getTitle(), question.getSummary(), null);
                break;
            case R.id.action_share_to_weibo:
                MobclickAgent.onEvent(this, Mob.Event_Share_Question_To_Weibo);
                ShareUtil.shareToWeibo(this, question.getUrl(), question.getTitle(), question.getSummary(), null);
                break;
        }
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
            onReplyItemClick(view);
        }
    };

    private void onReplyItemClick(final View view) {
        if (view instanceof AnswerListItemView) {
            Intent intent = new Intent(this, AnswerActivity.class);
            intent.putExtra(Consts.Extra_Answer, ((AnswerListItemView) view).getData());
            intent.putExtra(Consts.Extra_Question, question);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, 0);
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
            MobclickAgent.onEvent(this, Mob.Event_Favor_Question);
            new FavorDialog.Builder(this).setTitle(R.string.action_favor).create(question).show();
        }
    }

    private void recommend() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            MobclickAgent.onEvent(this, Mob.Event_Recommend_Question);
            InputDialog.Builder builder = new InputDialog.Builder(this);
            builder.setTitle(R.string.recommend_question);
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
            overridePendingTransition(R.anim.slide_in_right, 0);
        }
    }

    @Override
    public void reload() {
        loadData(-1);
    }

    class LoaderTask extends AAsyncTask<Integer, ResponseObject<Question>, ResponseObject<ArrayList<AceModel>>> {
        int offset;

        LoaderTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected ResponseObject<ArrayList<AceModel>> doInBackground(Integer... params) {
            if (!TextUtils.isEmpty(notice_id)) {
                UserAPI.ignoreOneNotice(notice_id);
                notice_id = null;
            }
            offset = params[0];
            if (offset < 0) {
                ResponseObject<Question> questionResult = QuestionAPI.getQuestionDetailByID(question.getId());
                if (questionResult.ok) {
                    publishProgress(questionResult);
                    return QuestionAPI.getQuestionAnswers(question.getId(), 0);
                } else {
                    return new ResponseObject<>();
                }
            } else {
                return QuestionAPI.getQuestionAnswers(question.getId(), offset);
            }
        }

        @SafeVarargs
        @Override
        protected final void onProgressUpdate(ResponseObject<Question>... values) {
            //在这里取到正文，正文的结果一定是正确的
            progressBar.setVisibility(View.VISIBLE);
            floatingActionsMenu.setVisibility(View.VISIBLE);
            loadingView.onLoadSuccess();
            ResponseObject<Question> result = values[0];
            question = result.result;
            adapter.add(0, question);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(ResponseObject<ArrayList<AceModel>> result) {
            progressBar.setVisibility(View.GONE);
            if (result.ok) {
                loadingView.onLoadSuccess();
                ArrayList<AceModel> ars = result.result;
                if (ars.size() > 0) {
                    adapter.addAll(ars);
                    adapter.notifyDataSetChanged();
                }
            } else {
                if (result.statusCode == 404) {
                    toastSingleton(R.string.page_404);
                    finish();
                } else {
                    toastSingleton(getString(R.string.load_failed));
                    loadingView.onLoadFailed();
                }
            }
            if (adapter.getCount() > 0) {
                listView.setCanPullToLoadMore(true);
            } else {
                listView.setCanPullToLoadMore(false);
            }
            listView.doneOperation();
        }
    }

    class RecommendTask extends AsyncTask<String, Integer, ResponseObject> {

        @Override
        protected ResponseObject doInBackground(String... params) {
            String questionID = params[0];
            String title = params[1];
            String summary = params[2];
            String comment = params[3];
            return QuestionAPI.recommendQuestion(questionID, title, summary, comment);
        }

        @Override
        protected void onPostExecute(ResponseObject resultObject) {
            if (resultObject.ok) {
                toast(R.string.recommend_ok);
            } else {
                toast(R.string.recommend_failed);
            }
        }
    }

    FollowTask followTask;
    UnfollowTask unfollowTask;

    private void followQuestion() {
        if (followTask != null && followTask.getStatus() == AsyncTask.Status.RUNNING) {
            followTask.cancel(true);
        }
        followTask = new FollowTask();
        followTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, question.getId());
    }

    private void unfollowQuestion() {
        if (unfollowTask != null && unfollowTask.getStatus() == AsyncTask.Status.RUNNING) {
            unfollowTask.cancel(true);
        }
        unfollowTask = new UnfollowTask();
        unfollowTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, question.getId());
    }

    class FollowTask extends AsyncTask<String, Integer, ResponseObject> {

        @Override
        protected void onPreExecute() {
            MobclickAgent.onEvent(QuestionActivity.this, Mob.Event_Follow_Question);
        }

        @Override
        protected ResponseObject doInBackground(String... params) {
            String questionID = params[0];
            return QuestionAPI.followQuestion(questionID);
        }

        @Override
        protected void onPostExecute(ResponseObject resultObject) {
            if (resultObject.ok) {
                toast(R.string.follow_ok);
            } else {
                toast(R.string.follow_failed);
            }
        }
    }

    class UnfollowTask extends AsyncTask<String, Integer, ResponseObject> {

        @Override
        protected void onPreExecute() {
            MobclickAgent.onEvent(QuestionActivity.this, Mob.Event_Unfollow_Question);
        }

        @Override
        protected ResponseObject doInBackground(String... params) {
            String questionID = params[0];
            return QuestionAPI.unfollowQuestion(questionID);
        }

        @Override
        protected void onPostExecute(ResponseObject resultObject) {
            if (resultObject.ok) {
                toast(R.string.unfollow_ok);
            } else {
                toast(R.string.unfollow_failed);
            }
        }
    }
}
