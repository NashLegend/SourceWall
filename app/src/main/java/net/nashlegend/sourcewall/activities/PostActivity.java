package net.nashlegend.sourcewall.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
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
import net.nashlegend.sourcewall.adapters.PostDetailAdapter;
import net.nashlegend.sourcewall.dialogs.FavorDialog;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.ResponseCode;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.MessageAPI;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.AutoHideUtil;
import net.nashlegend.sourcewall.util.AutoHideUtil.AutoHideListener;
import net.nashlegend.sourcewall.util.CommonUtil;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.RegUtil;
import net.nashlegend.sourcewall.util.ShareUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;
import net.nashlegend.sourcewall.view.MediumListItemView;
import net.nashlegend.sourcewall.view.common.LListView;
import net.nashlegend.sourcewall.view.common.LoadingView;

import java.lang.reflect.Method;
import java.util.ArrayList;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

public class PostActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener, LoadingView.ReloadListener {
    private LListView listView;
    private final PostDetailAdapter adapter;
    private Post post;
    private LoadingView loadingView;
    private AdapterView.OnItemClickListener onItemClickListener;
    private String notice_id;
    private FloatingActionsMenu floatingActionsMenu;
    private boolean loadDesc = false;
    private Menu menu;
    private Receiver receiver;
    private AppBarLayout appbar;
    private int headerHeight = 112;
    /**
     * 是否倒序加载已经加载完成了所有的回帖
     */
    private boolean hasLoadAll = false;
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
        appbar = (AppBarLayout) findViewById(R.id.app_bar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
        post = getIntent().getParcelableExtra(Consts.Extra_Post);
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

        headerHeight = (int) getResources().getDimension(R.dimen.actionbar_height);
        AutoHideUtil.applyListViewAutoHide(this, listView, (int) getResources().getDimension(R.dimen.actionbar_height), autoHideListener);
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
        if (offset < 0) {
            loadFromPost();
        } else {
            loadReplies(offset);
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
            case android.R.id.home:
                finish();
                break;
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
            PostAPI.likePost(post.getId(), new RequestObject.CallBack<Boolean>() {
                @Override
                public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<Boolean> result) {

                }

                @Override
                public void onSuccess(@NonNull Boolean result, @NonNull ResponseObject<Boolean> detailed) {
                    post.setLikeNum(post.getLikeNum() + 1);
                    adapter.notifyDataSetChanged();
                    toastSingleton("已赞");
                }
            });
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
        if (CommonUtil.shouldThrottle()) {
            return;
        }
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
        adapter.clear();
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
            listView.startLoadingMore();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void replyComment(UComment comment) {
        replyPost(comment);
    }

    private void likeComment(final MediumListItemView mediumListItemView) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
            return;
        }
        if (mediumListItemView.getData().isHasLiked()) {
            toastSingleton("已经赞过了");
            return;
        }
        final UComment comment = mediumListItemView.getData();
        PostAPI.likeComment(comment.getID(), new RequestObject.CallBack<Boolean>() {
            @Override
            public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<Boolean> result) {
                if (result.code == ResponseCode.CODE_ALREADY_LIKED) {
                    comment.setHasLiked(true);
                }
            }

            @Override
            public void onSuccess(@NonNull Boolean result, @NonNull ResponseObject<Boolean> detailed) {
                comment.setHasLiked(true);
                comment.setLikeNum(comment.getLikeNum() + 1);
                if (mediumListItemView.getData() == comment) {
                    mediumListItemView.plusOneLike();
                }
            }
        });
    }

    private void deleteComment(final UComment comment) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            PostAPI.deleteMyComment(comment.getID(), new RequestObject.CallBack<Boolean>() {
                @Override
                public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<Boolean> result) {
                    toastSingleton(getString(R.string.delete_failed));
                }

                @Override
                public void onSuccess(@NonNull Boolean result, @NonNull ResponseObject<Boolean> detailed) {
                    if (post.getReplyNum() > 0) {
                        post.setReplyNum(post.getReplyNum() - 1);
                    }
                    adapter.remove(comment);
                    adapter.notifyDataSetChanged();
                }
            });
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
            if (comment.getAuthor().getId().equals(UserAPI.getUserID())) {
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

    private void onStartLoadingLatest() {
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
        if (loadDesc && hasLoadAll) {
            listView.setCanPullToLoadMore(false);
        }
        setMenuVisibility();
    }

    private AutoHideListener autoHideListener = new AutoHideListener() {
        AnimatorSet backAnimatorSet;
        AnimatorSet hideAnimatorSet;

        @Override
        public void animateHide() {
            if (backAnimatorSet != null && backAnimatorSet.isRunning()) {
                backAnimatorSet.cancel();
            }
            if (hideAnimatorSet == null || !hideAnimatorSet.isRunning()) {
                hideAnimatorSet = new AnimatorSet();
                ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(appbar, "translationY", appbar.getTranslationY(), -headerHeight);
                ObjectAnimator header2Animator = ObjectAnimator.ofFloat(progressBar, "translationY", progressBar.getTranslationY(), -headerHeight);
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(floatingActionsMenu, "translationY", floatingActionsMenu.getTranslationY(), floatingActionsMenu.getHeight());
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(headerAnimator);
                animators.add(header2Animator);
                animators.add(footerAnimator);
                hideAnimatorSet.setDuration(300);
                hideAnimatorSet.playTogether(animators);
                hideAnimatorSet.start();
            }
        }

        @Override
        public void animateBack() {
            if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {
                hideAnimatorSet.cancel();
            }
            if (backAnimatorSet == null || !backAnimatorSet.isRunning()) {
                backAnimatorSet = new AnimatorSet();
                ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(appbar, "translationY", appbar.getTranslationY(), 0f);
                ObjectAnimator header2Animator = ObjectAnimator.ofFloat(progressBar, "translationY", progressBar.getTranslationY(), 0f);
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(floatingActionsMenu, "translationY", floatingActionsMenu.getTranslationY(), 0f);
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(headerAnimator);
                animators.add(header2Animator);
                animators.add(footerAnimator);
                backAnimatorSet.setDuration(300);
                backAnimatorSet.playTogether(animators);
                backAnimatorSet.start();
            }
        }
    };

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isActive() && intent.getIntExtra(Consts.Extra_Activity_Hashcode, 0) == PostActivity.this.hashCode()) {
                if (Consts.Action_Start_Loading_Latest.equals(intent.getAction())) {
                    onStartLoadingLatest();
                } else if (Consts.Action_Finish_Loading_Latest.equals(intent.getAction())) {
                    onFinishLoadingLatest();
                }
            }

        }
    }

    private void loadFromPost() {
        if (!TextUtils.isEmpty(notice_id)) {
            MessageAPI.ignoreOneNotice(notice_id);
            notice_id = null;
        }
        PostAPI
                .getPostDetailByID(post.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseObject<Post>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseObject<Post> result) {
                        if (result.ok) {
                            progressBar.setVisibility(View.VISIBLE);
                            floatingActionsMenu.setVisibility(View.VISIBLE);
                            loadingView.onLoadSuccess();
                            post = result.result;
                            post.setDesc(loadDesc);
                            adapter.add(0, post);
                            adapter.notifyDataSetChanged();
                            loadReplies(0);
                        } else {
                            loadingView.onLoadFailed();
                        }
                    }
                });
    }

    private void loadReplies(int offset) {
        int limit = 20;
        if (loadDesc) {
            int tmpOffset = post.getReplyNum() - offset - 20;
            if (tmpOffset <= 0) {
                hasLoadAll = true;
                limit = 20 + tmpOffset;
                tmpOffset = 0;
            } else {
                hasLoadAll = false;
            }
            offset = tmpOffset;
        }
        PostAPI
                .getPostReplies(post.getId(), offset, limit)
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        loadingView.onLoadSuccess();
                        listView.doneOperation();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseObject<ArrayList<UComment>>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseObject<ArrayList<UComment>> result) {
                        progressBar.setVisibility(View.GONE);
                        if (result.ok) {
                            loadingView.onLoadSuccess();
                            ArrayList<UComment> ars = result.result;
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
                        if (loadDesc && hasLoadAll) {
                            listView.setCanPullToLoadMore(false);
                        }
                        listView.doneOperation();
                    }
                });
    }
}
