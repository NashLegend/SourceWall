package com.example.sourcewall;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.sourcewall.adapters.PostDetailAdapter;
import com.example.sourcewall.commonview.LListView;
import com.example.sourcewall.commonview.LoadingView;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.PostAPI;
import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.dialogs.FavorDialog;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.model.Post;
import com.example.sourcewall.model.UComment;
import com.example.sourcewall.util.AutoHideUtil;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.RegUtil;
import com.example.sourcewall.util.ToastUtil;
import com.example.sourcewall.view.MediumListItemView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;

public class PostActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener {
    LListView listView;
    PostDetailAdapter adapter;
    Post post;
    LoaderTask task;
    Toolbar toolbar;
    FloatingActionsMenu floatingActionsMenu;
    FloatingActionButton replyButton;
    FloatingActionButton recomButton;
    FloatingActionButton favorButton;
    LoadingView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        loadingView = (LoadingView) findViewById(R.id.post_progress_loading);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        post = (Post) getIntent().getSerializableExtra(Consts.Extra_Post);
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new PostDetailAdapter(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(onItemClickListener);
        listView.setCanPullToLoadMore(false);
        listView.setOnRefreshListener(this);

        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.layout_operation);
        replyButton = (FloatingActionButton) findViewById(R.id.button_reply);
        recomButton = (FloatingActionButton) findViewById(R.id.button_recommend);
        favorButton = (FloatingActionButton) findViewById(R.id.button_favor);

        replyButton.setOnClickListener(this);
        recomButton.setOnClickListener(this);
        favorButton.setOnClickListener(this);

        AutoHideUtil.applyListViewAutoHide(this, listView, toolbar, floatingActionsMenu, (int) getResources().getDimension(R.dimen.abc_action_bar_default_height_material));

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
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    private void likePost() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            LikePostTask likePostTask = new LikePostTask();
            likePostTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, post);
        }
    }

    private void favor() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            new FavorDialog.Builder(this).setTitle(R.string.action_favor).create(post).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_reply:
                replyPost();
                break;
            case R.id.button_recommend:
                likePost();
                break;
            case R.id.button_favor:
                favor();
                break;
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
                return PostAPI.getPostFirstPage(post.getId());
            } else {
                return PostAPI.getPostCommentsFromJsonUrl(post.getId(), offset);
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

    private void replyPost() {
        replyPost(null);
    }

    private void replyPost(UComment comment) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            Intent intent = new Intent(this, ReplyActivity.class);
            intent.putExtra(Consts.Extra_Ace_Model, post);
            if (comment != null) {
                intent.putExtra(Consts.Extra_Simple_Comment, comment);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, 0);
        }
    }

    private void replyComment(UComment comment) {
        replyPost(comment);
    }

    private void likeComment(UComment comment) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            if (comment.isHasLiked()) {
                ToastUtil.toastSingleton("已经赞过了");
            } else {
                LikeCommentTask likeCommentTask = new LikeCommentTask();
                likeCommentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment);
            }
        }
    }

    private void copyComment(UComment comment) {
        //do nothing
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(null, RegUtil.html2PlainText(comment.getContent())));
    }

    private void onReplyItemClick(final View view, int position, long id) {
        if (view instanceof MediumListItemView) {
            String[] operations;
            final UComment comment = ((MediumListItemView) view).getData();
            if (comment.isHasLiked()) {
                operations = new String[]{getString(R.string.action_reply), getString(R.string.action_copy)};
            } else {
                operations = new String[]{getString(R.string.action_reply), getString(R.string.action_like), getString(R.string.action_copy)};
            }
            new AlertDialog.Builder(this).setTitle("").setItems(operations, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            replyComment(comment);
                            break;
                        case 1:
                            if (comment.isHasLiked()) {
                                copyComment(comment);
                            } else {
                                likeComment(comment);
                            }
                            break;
                        case 2:
                            copyComment(comment);
                            break;
                    }
                }
            }).create().show();
        }
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            onReplyItemClick(view, position, id);
        }
    };

    class LikePostTask extends AsyncTask<Post, Integer, ResultObject> {
        Post post;

        @Override
        protected ResultObject doInBackground(Post... params) {
            post = params[0];
            return PostAPI.likePost(post.getId());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                post.setLikeNum(post.getLikeNum() + 1);
                adapter.notifyDataSetChanged();
            } else {
                //do nothing
            }
        }
    }

    class LikeCommentTask extends AsyncTask<UComment, Integer, ResultObject> {

        UComment comment;

        @Override
        protected ResultObject doInBackground(UComment... params) {
            comment = params[0];
            return PostAPI.likeComment(comment.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                comment.setHasLiked(true);
                comment.setLikeNum(comment.getLikeNum() + 1);
                adapter.notifyDataSetChanged();
            } else {
                //do nothing
            }
        }
    }
}
