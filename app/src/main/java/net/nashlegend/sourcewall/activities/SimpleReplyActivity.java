package net.nashlegend.sourcewall.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.SimpleCommentAdapter;
import net.nashlegend.sourcewall.commonview.AAsyncTask;
import net.nashlegend.sourcewall.commonview.IStackedAsyncTaskInterface;
import net.nashlegend.sourcewall.commonview.LListView;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.model.QuestionAnswer;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.api.APIBase;
import net.nashlegend.sourcewall.request.api.QuestionAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.swrequest.RequestObject;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.util.CommonUtil;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.view.SimpleCommentItemView;

import java.util.ArrayList;


public class SimpleReplyActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener, LoadingView.ReloadListener {

    private AceModel aceModel;
    private LoaderTask task;
    private LListView listView;
    private SimpleCommentAdapter adapter;
    private Toolbar toolbar;
    private EditText textReply;
    private ImageButton publishButton;
    private ProgressDialog progressDialog;
    private Menu mMenu;
    private LoadingView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_reply);
        loadingView = (LoadingView) findViewById(R.id.replies_progress_loading);
        loadingView.setReloadListener(this);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
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
        textReply = (EditText) findViewById(R.id.text_simple_reply);
        publishButton = (ImageButton) findViewById(R.id.btn_publish);
        aceModel = getIntent().getParcelableExtra(Consts.Extra_Ace_Model);
        listView = (LListView) findViewById(R.id.list_detail);
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
     * @param offset 从第几个开始加载
     */
    private void loadData(int offset) {
        if (offset < 0) {
            offset = 0;
        }
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
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_simple_reply, menu);
        mMenu.findItem(R.id.action_cancel_simple_reply).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cancel_simple_reply) {
            mMenu.findItem(R.id.action_cancel_simple_reply).setVisible(false);
            textReply.setHint(R.string.hint_reply);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            onReplyItemClick(view);
        }
    };

    private void onReplyItemClick(final View view) {
        if (view instanceof SimpleCommentItemView) {
            textReply.setHint("回复@" + ((SimpleCommentItemView) view).getData().getAuthor().getName() + "：");
            mMenu.findItem(R.id.action_cancel_simple_reply).setVisible(true);
        }
    }

    /**
     * 是回复/问题，还是回复里面的评论
     *
     * @return boolean
     */
    private boolean isCommentOnHost() {
        return !mMenu.findItem(R.id.action_cancel_simple_reply).isVisible();
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
        if (v.getId() == R.id.btn_publish) {
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

                if (aceModel instanceof Question) {
                    MobclickAgent.onEvent(SimpleReplyActivity.this, Mob.Event_Comment_On_Question);
                } else if (aceModel instanceof QuestionAnswer) {
                    MobclickAgent.onEvent(SimpleReplyActivity.this, Mob.Event_Comment_On_Answer);
                }

                RequestObject.CallBack<UComment> callBack = new RequestObject.CallBack<UComment>() {
                    @Override
                    public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<UComment> result) {
                        CommonUtil.dismissDialog(progressDialog);
                        toast("回复失败");
                    }

                    @Override
                    public void onResponse(@NonNull ResponseObject<UComment> result) {
                        CommonUtil.dismissDialog(progressDialog);
                        if (result.ok) {
                            mMenu.findItem(R.id.action_cancel_simple_reply).setVisible(false);
                            textReply.setHint(R.string.hint_reply);
                            textReply.setText("");
                            hideInput();
                            if (task == null || task.getStatus() != AAsyncTask.Status.RUNNING) {
                                UComment uComment = result.result;
                                adapter.add(0, uComment);
                                adapter.notifyDataSetChanged();
                            }
                            toast("回复成功");
                        } else {
                            toast("回复失败");
                        }
                    }
                };

                RequestObject<UComment> requestObject = null;
                if (aceModel instanceof Question) {
                    requestObject = QuestionAPI.commentOnQuestion(((Question) aceModel).getId(), content, callBack);
                } else if (aceModel instanceof QuestionAnswer) {
                    requestObject = QuestionAPI.commentOnAnswer(((QuestionAnswer) aceModel).getID(), content, callBack);
                }

                if (requestObject != null) {
                    progressDialog = new ProgressDialog(SimpleReplyActivity.this);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage(getString(R.string.message_wait_a_minute));
                    final RequestObject<UComment> finalRequestObject = requestObject;
                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finalRequestObject.softCancel();
                        }
                    });
                    progressDialog.show();
                }
            }
        }
    }

    //    private void hideInput(EditText editText) {
    //        try {
    //            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
    //                    INPUT_METHOD_SERVICE);
    //            if (inputMethodManager != null && inputMethodManager.isActive(editText)) {
    //                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    //            }
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    }

    private void hideInput() {
        try {
            if (getCurrentFocus() != null) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {
        loadData(0);
    }

    class LoaderTask extends AAsyncTask<Integer, Integer, ResponseObject<ArrayList<UComment>>> {
        int offset;

        LoaderTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected ResponseObject<ArrayList<UComment>> doInBackground(Integer... params) {
            ResponseObject<ArrayList<UComment>> resultObject = new ResponseObject<>();
            offset = params[0];
            if (aceModel instanceof Question) {
                resultObject = QuestionAPI.getQuestionComments(((Question) aceModel).getId(), offset);
            } else if (aceModel instanceof QuestionAnswer) {
                resultObject = QuestionAPI.getAnswerComments(((QuestionAnswer) aceModel).getID(), offset);
            }
            return resultObject;

        }

        @Override
        protected void onPostExecute(ResponseObject<ArrayList<UComment>> result) {
            if (result.ok) {
                loadingView.onLoadSuccess();
                ArrayList<UComment> ars = result.result;
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
                toast(R.string.load_failed);
                loadingView.onLoadFailed();
            }
            listView.doneOperation();
        }
    }
}
