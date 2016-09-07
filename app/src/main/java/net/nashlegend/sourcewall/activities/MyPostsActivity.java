package net.nashlegend.sourcewall.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.PostAdapter;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.request.RequestObject.SimpleCallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.AutoHideUtil;
import net.nashlegend.sourcewall.data.Consts.Extras;
import net.nashlegend.sourcewall.view.PostListItemView;
import net.nashlegend.sourcewall.view.common.LoadingView;
import net.nashlegend.sourcewall.view.common.listview.LListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyPostsActivity extends BaseActivity {

    @BindView(R.id.list_posts)
    LListView listView;
    @BindView(R.id.post_progress_loading)
    LoadingView loadingView;
    @BindView(R.id.action_bar)
    Toolbar toolbar;
    @BindView(R.id.app_bar)
    AppBarLayout appbar;

    private int headerHeight = 112;
    private PostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        ButterKnife.bind(this);
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        headerHeight = (int) getResources().getDimension(R.dimen.actionbar_height);
        AutoHideUtil.applyListViewAutoHide(this, listView, (int) getResources().getDimension(R.dimen.actionbar_height), autoHideListener);

        listView.setCanPullToRefresh(false);
        listView.setCanPullToLoadMore(false);
        adapter = new PostAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(new LListView.OnRefreshListener() {
            @Override
            public void onStartRefresh() {
                loadData(0);
            }

            @Override
            public void onStartLoadMore() {
                loadData(adapter.getCount());
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof PostListItemView) {
                    Intent intent = new Intent();
                    intent.setClass(MyPostsActivity.this, PostActivity.class);
                    intent.putExtra(Extras.Extra_Post, ((PostListItemView) view).getData());
                    startOneActivity(intent);
                }
            }
        });
        loadingView.setReloadListener(new LoadingView.ReloadListener() {
            @Override
            public void reload() {
                loadData(0);
            }
        });
        loadOver();
        loadingView.startLoading();
    }


    private void loadOver() {
        loadData(0);
        loadingView.startLoading();
    }

    private void loadData(int offset) {
        loadPosts(offset);
    }

    private void loadPosts(final int offset) {
        PostAPI.getPostListByUser(UserAPI.getUkey(), offset, new SimpleCallBack<ArrayList<Post>>() {
            @Override
            public void onFailure(@NonNull ResponseObject<ArrayList<Post>> result) {
                toast(R.string.load_failed);
                loadingView.onLoadFailed();
            }

            @Override
            public void onSuccess(@NonNull ArrayList<Post> result) {
                if (isFinishing()) {
                    return;
                }
                listView.doneOperation();
                loadingView.onLoadSuccess();
                if (offset > 0) {
                    adapter.addAll(result);
                } else {
                    adapter.setList(result);
                }
                adapter.notifyDataSetChanged();
                if (adapter.getCount() > 0) {
                    listView.setCanPullToLoadMore(true);
                    listView.setCanPullToRefresh(false);
                } else {
                    listView.setCanPullToLoadMore(false);
                    listView.setCanPullToRefresh(false);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private AutoHideUtil.AutoHideListener autoHideListener = new AutoHideUtil.AutoHideListener() {
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
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(headerAnimator);
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
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(headerAnimator);
                backAnimatorSet.setDuration(300);
                backAnimatorSet.playTogether(animators);
                backAnimatorSet.start();
            }
        }
    };
}
