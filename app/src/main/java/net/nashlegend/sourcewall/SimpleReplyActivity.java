package net.nashlegend.sourcewall;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;

import net.nashlegend.sourcewall.adapters.SimpleCommentAdapter;
import net.nashlegend.sourcewall.commonview.LListView;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.connection.ResultObject;
import net.nashlegend.sourcewall.connection.api.QuestionAPI;
import net.nashlegend.sourcewall.connection.api.UserAPI;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.model.QuestionAnswer;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.view.SimpleCommentItemView;

import java.util.ArrayList;


public class SimpleReplyActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener, LoadingView.ReloadListener {

    private AceModel aceModel;
    LoaderTask task;
    LListView listView;
    SimpleCommentAdapter adapter;
    Toolbar toolbar;
    EditText textReply;
    ImageButton publishButton;
    ProgressDialog progressDialog;
    Menu mMenu;
    LoadingView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(net.nashlegend.sourcewall.R.layout.activity_simple_reply);
        loadingView = (LoadingView) findViewById(net.nashlegend.sourcewall.R.id.replies_progress_loading);
        loadingView.setReloadListener(this);
        toolbar = (Toolbar) findViewById(net.nashlegend.sourcewall.R.id.action_bar);
        setSupportActionBar(toolbar);
        textReply = (EditText) findViewById(net.nashlegend.sourcewall.R.id.text_simple_reply);
        publishButton = (ImageButton) findViewById(net.nashlegend.sourcewall.R.id.btn_publish);
        aceModel = (AceModel) getIntent().getSerializableExtra(Consts.Extra_Ace_Model);
        listView = (LListView) findViewById(net.nashlegend.sourcewall.R.id.list_detail);
        adapter = new SimpleCommentAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setCanPullToRefresh(false);
        listView.setCanPullToLoadMore(false);
        listView.setOnRefreshListener(this);
        publishButton.setOnClickListener(this);
        loadData(0);
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
        mMenu = menu;
        getMenuInflater().inflate(net.nashlegend.sourcewall.R.menu.menu_simple_reply, menu);
        mMenu.findItem(net.nashlegend.sourcewall.R.id.action_cancel_simple_reply).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == net.nashlegend.sourcewall.R.id.action_cancel_simple_reply) {
            mMenu.findItem(net.nashlegend.sourcewall.R.id.action_cancel_simple_reply).setVisible(false);
            textReply.setHint(net.nashlegend.sourcewall.R.string.hint_reply);
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
        if (view instanceof SimpleCommentItemView) {
            textReply.setHint("回复@" + ((SimpleCommentItemView) view).getData().getAuthor() + "：");
            mMenu.findItem(net.nashlegend.sourcewall.R.id.action_cancel_simple_reply).setVisible(true);
        }
    }

    /**
     * 是回复/问题，还是回复里面的评论
     *
     * @return
     */
    private boolean isCommentOnHost() {
        return !mMenu.findItem(net.nashlegend.sourcewall.R.id.action_cancel_simple_reply).isVisible();
    }

    @Override
    public void onStartRefresh() {
        loadData(0);
    }

    @Override
    public void onStartLoadMore() {
        loadData(adapter.getCount());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == net.nashlegend.sourcewall.R.id.btn_publish) {
            if (!UserAPI.isLoggedIn()) {
                notifyNeedLog();
                return;
            }
            if (!TextUtils.isEmpty(textReply.getText().toString().trim())) {
                String content = "";
                if (isCommentOnHost()) {
                    content = textReply.getText().toString();
                } else {
                    content = textReply.getHint().toString() + textReply.getText().toString();
                }
                ReplyTask replyTask = new ReplyTask();
                replyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, content);
            }
        }
    }

    private void hideInput(EditText editText) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && inputMethodManager.isActive(editText)) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void reload() {
        loadData(0);
    }

    class ReplyTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            addToStackedTasks(this);
            progressDialog = new ProgressDialog(SimpleReplyActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage(getString(net.nashlegend.sourcewall.R.string.message_replying));
            progressDialog.show();
        }

        @Override
        protected void onCancelled() {
            removeFromStackedTasks(this);
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            String content = params[0];
            ResultObject resultObject = new ResultObject();
            if (aceModel instanceof Question) {
                resultObject = QuestionAPI.commentOnQuestion(((Question) aceModel).getId(), content);
            } else if (aceModel instanceof QuestionAnswer) {
                resultObject = QuestionAPI.commentOnAnswer(((QuestionAnswer) aceModel).getID(), content);
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResultObject result) {
            removeFromStackedTasks(this);
            progressDialog.dismiss();
            if (result.ok) {
                mMenu.findItem(net.nashlegend.sourcewall.R.id.action_cancel_simple_reply).setVisible(false);
                textReply.setHint(net.nashlegend.sourcewall.R.string.hint_reply);
                textReply.setText("");
                hideInput(textReply);
                if (task == null || task.getStatus() != AsyncTask.Status.RUNNING) {
                    UComment uComment = (UComment) result.result;
                    adapter.add(0, uComment);
                    adapter.notifyDataSetChanged();
                }
                ToastUtil.toast("回复成功");
            } else {
                ToastUtil.toast("回复失败");
            }
        }
    }

    class LoaderTask extends AsyncTask<Integer, Integer, ResultObject> {
        int offset;

        @Override
        protected void onPreExecute() {
            addToStackedTasks(this);
        }

        @Override
        protected void onCancelled() {
            removeFromStackedTasks(this);
        }

        @Override
        protected ResultObject doInBackground(Integer... params) {
            offset = params[0];
            if (aceModel instanceof Question) {
                return QuestionAPI.getQuestionComments(((Question) aceModel).getId(), offset);
            } else if (aceModel instanceof QuestionAnswer) {
                return QuestionAPI.getAnswerComments(((QuestionAnswer) aceModel).getID(), offset);
            } else {
                //执行不到的
                return new ResultObject();
            }
        }

        @Override
        protected void onPostExecute(ResultObject result) {
            removeFromStackedTasks(this);
            if (result.ok) {
                loadingView.onLoadSuccess();
                ArrayList<UComment> ars = (ArrayList<UComment>) result.result;
                if (offset == 0) {
                    //Refresh
                    if (ars.size() > 0) {
                        adapter.setList(ars);
                        adapter.notifyDataSetInvalidated();
                    }
                } else {
                    //Load More
                    if (ars.size() > 0) {
                        adapter.addAll(ars);
                        adapter.notifyDataSetChanged();
                    }
                }
                if (adapter.getCount() > 0) {
                    listView.setCanPullToLoadMore(ars.size() >= 20);//请求下来20条说明已经后面可能还有数据
                } else {
                    listView.setCanPullToLoadMore(false);
                }
                listView.setCanPullToRefresh(true);
            } else {
                ToastUtil.toast(getString(net.nashlegend.sourcewall.R.string.load_failed));
                loadingView.onLoadFailed();
            }
            listView.doneOperation();
        }
    }
}
