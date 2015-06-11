package net.nashlegend.sourcewall;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

import net.nashlegend.sourcewall.adapters.PostDetailAdapter;
import net.nashlegend.sourcewall.commonview.AAsyncTask;
import net.nashlegend.sourcewall.commonview.IStackedAsyncTaskInterface;
import net.nashlegend.sourcewall.commonview.LListView;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.dialogs.FavorDialog;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.AutoHideUtil;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.RegUtil;
import net.nashlegend.sourcewall.util.ShareUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;
import net.nashlegend.sourcewall.view.MediumListItemView;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class PostActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener, LoadingView.ReloadListener {
    private LListView listView;
    private final PostDetailAdapter adapter;
    private Post post;
    private LoaderTask task;
    private LoadingView loadingView;
    private AdapterView.OnItemClickListener onItemClickListener;
    private String notice_id;
    private FloatingActionsMenu floatingActionsMenu;
    private boolean loadDesc = false;
    private Menu menu;
    private Receiver receiver;
    /**
     * 是否倒序加载已经加载完成了所有的回帖
     */
    private boolean lastLoad = false;
    private ProgressBar progressBar;

    public PostActivity() {
        onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onReplyItemClick(view, position, id);
            }
        };
        adapter = new PostDetailAdapter(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        MobclickAgent.onEvent(this, Mob.Event_Open_Post);
        loadingView = (LoadingView) findViewById(R.id.post_progress_loading);
        loadingView.setReloadListener(this);
        progressBar = (ProgressBar) findViewById(R.id.post_loading);
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
        post = (Post) getIntent().getSerializableExtra(Consts.Extra_Post);
        notice_id = getIntent().getStringExtra(Consts.Extra_Notice_Id);
        if (!TextUtils.isEmpty(post.getGroupName())) {
            setTitle(post.getGroupName() + " -- 小组");
        }
        listView = (LListView) findViewById(R.id.list_detail);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(onItemClickListener);
        listView.setCanPullToLoadMore(false);
        listView.setOnRefreshListener(this);

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

        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Consts.Action_Start_Loading_Latest);
        filter.addAction(Consts.Action_Finish_Loading_Latest);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
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
        getMenuInflater().inflate(R.menu.menu_post, menu);
        try {
            Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
            m.setAccessible(true);
            m.invoke(menu, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.menu = menu;
        setMenuVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_load_acs:
                startLoadAcs();
                break;
            case R.id.action_load_desc:
                startLoadDesc();
                break;
            case R.id.action_open_in_browser:
                if (!TextUtils.isEmpty(post.getUrl())) {
                    MobclickAgent.onEvent(this, Mob.Event_Open_Post_In_Browser);
                    UrlCheckUtil.openWithBrowser(post.getUrl());
                }
                break;
            case R.id.action_share_to_wechat_circle:
                MobclickAgent.onEvent(this, Mob.Event_Share_Post_To_Wechat_Circle);
                ShareUtil.shareToWeiXinCircle(this, post.getUrl(), post.getTitle(), post.getTitle(), null);
                break;
            case R.id.action_share_to_wechat_friends:
                MobclickAgent.onEvent(this, Mob.Event_Share_Post_To_Wechat_friend);
                ShareUtil.shareToWeiXinFriends(this, post.getUrl(), post.getTitle(), post.getTitle(), null);
                break;
            case R.id.action_share_to_weibo:
                MobclickAgent.onEvent(this, Mob.Event_Share_Post_To_Weibo);
                ShareUtil.shareToWeibo(this, post.getUrl(), post.getTitle(), post.getTitle(), null);
                break;
        }
        return true;
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
            MobclickAgent.onEvent(this, Mob.Event_Like_Post);
            LikePostTask likePostTask = new LikePostTask();
            likePostTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, post);
        }
    }

    private void favor() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            MobclickAgent.onEvent(this, Mob.Event_Favor_Post);
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

    @Override
    public void reload() {
        loadData(-1);
    }

    /**
     * 倒序查看
     */
    public void startLoadDesc() {
        MobclickAgent.onEvent(this, Mob.Event_Reverse_Read_Post);
        loadDesc = true;
        loadingView.startLoading();
        listView.setCanPullToLoadMore(false);
        setMenuVisibility();
        if (adapter.getCount() > 0 && adapter.getList().get(0) instanceof Post) {
            post = (Post) adapter.getList().get(0);
            post.setDesc(loadDesc);
            adapter.clear();
            adapter.add(post);
            loadData(0);
        } else {
            adapter.clear();
            loadData(-1);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 正序查看
     */
    private void startLoadAcs() {
        MobclickAgent.onEvent(this, Mob.Event_Normal_Read_Post);
        loadDesc = false;
        loadingView.startLoading();
        listView.setCanPullToLoadMore(false);
        setMenuVisibility();
        if (adapter.getCount() > 0 && adapter.getList().get(0) instanceof Post) {
            post = (Post) adapter.getList().get(0);
            post.setDesc(loadDesc);
            adapter.clear();
            adapter.add(post);
            loadData(0);
        } else {
            adapter.clear();
            loadData(-1);
        }
        adapter.notifyDataSetChanged();
    }

    private void setMenuVisibility() {
        if (menu != null) {
            if (loadDesc) {
                menu.findItem(R.id.action_load_acs).setVisible(true);
                menu.findItem(R.id.action_load_desc).setVisible(false);
            } else {
                menu.findItem(R.id.action_load_acs).setVisible(false);
                menu.findItem(R.id.action_load_desc).setVisible(true);
            }
        }
    }

    class LoaderTask extends AAsyncTask<Integer, ResultObject, ResultObject> {

        int offset;

        LoaderTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected ResultObject doInBackground(Integer... params) {
            if (!TextUtils.isEmpty(notice_id)) {
                UserAPI.ignoreOneNotice(notice_id);
                notice_id = null;
            }
            offset = params[0];
            int limit = 20;
            if (offset < 0) {
                offset = 0;
                ResultObject postResult = PostAPI.getPostDetailByIDFromJsonUrl(post.getId());//得不到回复数量
                if (postResult.ok) {
                    publishProgress(postResult);
                } else {
                    return postResult;
                }
            }
            if (loadDesc) {
                int tmpOffset = post.getReplyNum() - offset - 20;
                if (tmpOffset <= 0) {
                    lastLoad = true;
                    limit = 20 + tmpOffset;
                    tmpOffset = 0;
                } else {
                    lastLoad = false;
                }
                offset = tmpOffset;
            }
            return PostAPI.getPostCommentsFromJsonUrl(post.getId(), offset, limit);
        }

        @Override
        protected void onProgressUpdate(ResultObject... values) {
            //在这里取到正文，正文的结果一定是正确的
            progressBar.setVisibility(View.VISIBLE);
            floatingActionsMenu.setVisibility(View.VISIBLE);
            loadingView.onLoadSuccess();
            ResultObject resultObject = values[0];
            post = (Post) resultObject.result;
            post.setDesc(loadDesc);
            adapter.add(0, post);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(ResultObject result) {
            progressBar.setVisibility(View.GONE);
            if (result.ok) {
                loadingView.onLoadSuccess();
                ArrayList<AceModel> ars = (ArrayList<AceModel>) result.result;
                if (ars.size() > 0) {
                    if (loadDesc) {
                        adapter.addAllReversely(ars);
                    } else {
                        adapter.addAll(ars);
                    }
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
            if (loadDesc && lastLoad) {
                listView.setCanPullToLoadMore(false);
            }
            listView.doneOperation();
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
            startActivityForResult(intent, Consts.Code_Reply_Post);
            overridePendingTransition(R.anim.slide_in_right, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Consts.Code_Reply_Post && resultCode == RESULT_OK && !loadDesc) {
            post.setReplyNum(post.getReplyNum() + 1);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void replyComment(UComment comment) {
        replyPost(comment);
    }

    private void likeComment(MediumListItemView mediumListItemView) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            if (mediumListItemView.getData().isHasLiked()) {
                toastSingleton("已经赞过了");
            } else {
                LikeCommentTask likeCommentTask = new LikeCommentTask();
                likeCommentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediumListItemView);
            }
        }
    }

    private void deleteComment(UComment comment) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            DeleteCommentTask deleteCommentTask = new DeleteCommentTask();
            deleteCommentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment);
        }
    }

    private void copyComment(UComment comment) {
        //do nothing
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(null, RegUtil.html2PlainText(comment.getContent())));
    }

    private void onReplyItemClick(final View view, int position, long id) {
        if (view instanceof MediumListItemView) {
            final UComment comment = ((MediumListItemView) view).getData();
            ArrayList<String> ops = new ArrayList<>();
            ops.add(getString(R.string.action_reply));
            ops.add(getString(R.string.action_copy));
            if (!comment.isHasLiked()) {
                ops.add(getString(R.string.action_like));
            }
            if (comment.getAuthorID().equals(UserAPI.getUserID())) {
                ops.add(getString(R.string.action_delete));
            }
            String[] operations = new String[ops.size()];
            for (int i = 0; i < ops.size(); i++) {
                operations[i] = ops.get(i);
            }
            new AlertDialog.Builder(this).setItems(operations, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            replyComment(comment);
                            break;
                        case 1:
                            copyComment(comment);
                            break;
                        case 2:
                            likeComment((MediumListItemView) view);
                            break;
                        case 3:
                            deleteComment(comment);
                            break;
                    }
                }
            }).create().show();
        }
    }


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
                toastSingleton("已赞");
            }
        }
    }

    class LikeCommentTask extends AsyncTask<MediumListItemView, Integer, ResultObject> {

        UComment comment;
        MediumListItemView mediumListItemView;

        @Override
        protected ResultObject doInBackground(MediumListItemView... params) {
            mediumListItemView = params[0];
            comment = mediumListItemView.getData();
            return PostAPI.likeComment(comment.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                comment.setHasLiked(true);
                comment.setLikeNum(comment.getLikeNum() + 1);
                if (mediumListItemView.getData() == comment) {
                    mediumListItemView.plusOneLike();
                }
            } else {
                if (resultObject.code == ResultObject.ResultCode.CODE_ALREADY_LIKED) {
                    comment.setHasLiked(true);
                }
            }
        }
    }

    class DeleteCommentTask extends AsyncTask<UComment, Integer, ResultObject> {

        UComment comment;

        @Override
        protected ResultObject doInBackground(UComment... params) {
            comment = params[0];
            return PostAPI.deleteMyComment(comment.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                adapter.remove(comment);
                adapter.notifyDataSetChanged();
            } else {
                toastSingleton(getString(R.string.delete_failed));
            }
        }
    }

    private void onStartLoadingLatest() {
        cancelPotentialTask();
        listView.setCanPullToLoadMore(false);
        menu.findItem(R.id.action_load_acs).setVisible(false);
        menu.findItem(R.id.action_load_desc).setVisible(false);
    }

    private void onFinishLoadingLatest() {
        if (adapter.getCount() > 0) {
            listView.setCanPullToLoadMore(true);
        } else {
            listView.setCanPullToLoadMore(false);
        }
        if (loadDesc && lastLoad) {
            listView.setCanPullToLoadMore(false);
        }
        setMenuVisibility();
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Consts.Action_Start_Loading_Latest.equals(intent.getAction())) {
                onStartLoadingLatest();
            } else if (Consts.Action_Finish_Loading_Latest.equals(intent.getAction())) {
                onFinishLoadingLatest();
            }
        }
    }
}
