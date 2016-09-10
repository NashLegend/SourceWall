package net.nashlegend.sourcewall.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.SimpleCommentAdapter;
import net.nashlegend.sourcewall.data.Consts.Extras;
import net.nashlegend.sourcewall.data.Mob;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Answer;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.RequestObject.RequestCallBack;
import net.nashlegend.sourcewall.request.RequestObject.SimpleCallBack;
import net.nashlegend.sourcewall.request.api.QuestionAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.view.SimpleCommentItemView;
import net.nashlegend.sourcewall.view.common.LoadingView;
import net.nashlegend.sourcewall.view.common.LoadingView.ReloadListener;
import net.nashlegend.sourcewall.view.common.listview.LListView;
import net.nashlegend.sourcewall.view.common.listview.LListView.OnRefreshListener;

import java.util.ArrayList;

public class SimpleReplyActivity extends BaseActivity implements OnRefreshListener, OnClickListener, ReloadListener {

    private AceModel aceModel;
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setOnClickListener(new OnClickListener() {

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
        aceModel = getIntent().getParcelableExtra(Extras.Extra_Ace_Model);
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
        loadComments(offset);
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
        if (item.getItemId() == android.R.id.home) {
            finish();
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
                gotoLogin();
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
                } else if (aceModel instanceof Answer) {
                    MobclickAgent.onEvent(SimpleReplyActivity.this, Mob.Event_Comment_On_Answer);
                }

                RequestCallBack<UComment> callBack = new SimpleCallBack<UComment>() {
                    @Override
                    public void onFailure() {
                        UiUtil.dismissDialog(progressDialog);
                        toast("回复失败");
                    }

                    @Override
                    public void onSuccess(@NonNull UComment result) {
                        UiUtil.dismissDialog(progressDialog);
                        mMenu.findItem(R.id.action_cancel_simple_reply).setVisible(false);
                        textReply.setHint(R.string.hint_reply);
                        textReply.setText("");
                        hideInput();
                        adapter.add(0, result);
                        adapter.notifyDataSetChanged();
                        toast("回复成功");
                    }
                };

                NetworkTask<UComment> networkTask = null;
                if (aceModel instanceof Question) {
                    networkTask = QuestionAPI.commentOnQuestion(((Question) aceModel).getId(), content, callBack);
                } else if (aceModel instanceof Answer) {
                    networkTask = QuestionAPI.commentOnAnswer(((Answer) aceModel).getID(), content, callBack);
                }

                if (networkTask != null) {
                    progressDialog = new ProgressDialog(SimpleReplyActivity.this);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage(getString(R.string.message_wait_a_minute));
                    final NetworkTask<UComment> finalRequestObject = networkTask;
                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finalRequestObject.dismiss();
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

    private void loadComments(final int offset) {
        RequestCallBack<ArrayList<UComment>> callBack = new SimpleCallBack<ArrayList<UComment>>() {
            @Override
            public void onFailure() {
                toast(R.string.load_failed);
                loadingView.onLoadFailed();
                listView.doneOperation();
            }

            @Override
            public void onSuccess(@NonNull ArrayList<UComment> result) {
                loadingView.onLoadSuccess();
                if (offset == 0) {
                    //Refresh
                    if (result.size() > 0) {
                        adapter.setList(result);
                        adapter.notifyDataSetInvalidated();
                    }
                } else {
                    //Load More
                    if (result.size() > 0) {
                        adapter.addAll(result);
                        adapter.notifyDataSetChanged();
                    }
                }
                if (adapter.getCount() > 0) {
                    listView.setCanPullToLoadMore(result.size() >= 20);//请求下来20条说明已经后面可能还有数据
                } else {
                    listView.setCanPullToLoadMore(false);
                }
                listView.setCanPullToRefresh(true);
                listView.doneOperation();
            }
        };

        if (aceModel instanceof Question) {
            QuestionAPI.getQuestionComments(((Question) aceModel).getId(), offset, callBack);
        } else if (aceModel instanceof Answer) {
            QuestionAPI.getAnswerComments(((Answer) aceModel).getID(), offset, callBack);
        }
    }
}
