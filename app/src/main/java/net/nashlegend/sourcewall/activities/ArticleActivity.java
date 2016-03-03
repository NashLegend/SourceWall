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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import net.nashlegend.sourcewall.adapters.ArticleDetailAdapter;
import net.nashlegend.sourcewall.commonview.AAsyncTask;
import net.nashlegend.sourcewall.commonview.IStackedAsyncTaskInterface;
import net.nashlegend.sourcewall.commonview.LListView;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.dialogs.FavorDialog;
import net.nashlegend.sourcewall.dialogs.InputDialog;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.api.ArticleAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.util.AutoHideUtil;
import net.nashlegend.sourcewall.util.AutoHideUtil.AutoHideListener;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.RegUtil;
import net.nashlegend.sourcewall.util.ShareUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;
import net.nashlegend.sourcewall.view.MediumListItemView;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class ArticleActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener, LoadingView.ReloadListener {

    private LListView listView;
    private ArticleDetailAdapter adapter;
    private Article article;
    private LoaderTask task;
    private LoadingView loadingView;
    private String notice_id;
    private AdapterView.OnItemClickListener onItemClickListener;
    private FloatingActionsMenu floatingActionsMenu;
    private ProgressBar progressBar;
    private boolean loadDesc = false;
    private Receiver receiver;
    private Menu menu;
    private AppBarLayout appbar;
    private int headerHeight = 112;
    /**
     * 是否倒序加载已经加载完成了所有的回帖
     */
    private boolean hasLoadAll = false;

    public ArticleActivity() {
        onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onReplyItemClick(view, position, id);
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        MobclickAgent.onEvent(this, Mob.Event_Open_Article);
        loadingView = (LoadingView) findViewById(R.id.article_progress_loading);
        loadingView.setReloadListener(this);
        progressBar = (ProgressBar) findViewById(R.id.article_loading);
        appbar = (AppBarLayout) findViewById(R.id.app_bar);
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
        article = getIntent().getParcelableExtra(Consts.Extra_Article);
        notice_id = getIntent().getStringExtra(Consts.Extra_Notice_Id);
        if (!TextUtils.isEmpty(article.getSubjectName())) {
            setTitle(article.getSubjectName() + " -- 科学人");
        }
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new ArticleDetailAdapter(this);
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

    /**
     * @param offset -1是指刷新
     */
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

    private void replyArticle() {
        replyArticle(null);
    }

    private void replyArticle(UComment comment) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            Intent intent = new Intent(this, ReplyActivity.class);
            intent.putExtra(Consts.Extra_Ace_Model, article);
            if (comment != null) {
                intent.putExtra(Consts.Extra_Simple_Comment, comment);
            }
            startActivityForResult(intent, Consts.Code_Reply_Article);
            overridePendingTransition(R.anim.slide_in_right, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Consts.Code_Reply_Article && resultCode == RESULT_OK && !loadDesc) {
            article.setCommentNum(article.getCommentNum() + 1);
            listView.startLoadingMore();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void recommend() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            MobclickAgent.onEvent(this, Mob.Event_Recommend_Article);
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
                        RecommendTask recommendTask = new RecommendTask(ArticleActivity.this);
                        recommendTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, article.getId(), article.getTitle(), article.getSummary(), text);
                    }
                }
            });
            InputDialog inputDialog = builder.create();
            inputDialog.show();
        }
    }

    private void favor() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            // basket dialog
            MobclickAgent.onEvent(this, Mob.Event_Favor_Article);
            new FavorDialog.Builder(this).setTitle(R.string.action_favor).create(article).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article, menu);
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
            case R.id.action_share_to_wechat_circle:
                MobclickAgent.onEvent(this, Mob.Event_Share_Article_To_Wechat_Circle);
                ShareUtil.shareToWeiXinCircle(this, article.getUrl(), article.getTitle(), article.getSummary(), null);
                break;
            case R.id.action_share_to_wechat_friends:
                MobclickAgent.onEvent(this, Mob.Event_Share_Article_To_Wechat_friend);
                ShareUtil.shareToWeiXinFriends(this, article.getUrl(), article.getTitle(), article.getSummary(), null);
                break;
            case R.id.action_share_to_weibo:
                MobclickAgent.onEvent(this, Mob.Event_Share_Article_To_Weibo);
                ShareUtil.shareToWeibo(this, article.getUrl(), article.getTitle(), article.getSummary(), null);
                break;
            case R.id.action_open_in_browser:
                if (!TextUtils.isEmpty(article.getUrl())) {
                    MobclickAgent.onEvent(this, Mob.Event_Open_Article_In_Browser);
                    UrlCheckUtil.openWithBrowser(article.getUrl());
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void replyComment(UComment comment) {
        replyArticle(comment);
    }

    private void likeComment(MediumListItemView mediumListItemView) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            if (mediumListItemView.getData().isHasLiked()) {
                toastSingleton(getString(R.string.has_liked_this));
            } else {
                LikeCommentTask likeCommentTask = new LikeCommentTask(ArticleActivity.this);
                likeCommentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediumListItemView);
            }
        }
    }

    private void deleteComment(UComment comment) {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            DeleteCommentTask deleteCommentTask = new DeleteCommentTask(this);
            deleteCommentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment);
        }
    }

    private void copyComment(UComment comment) {
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
            new AlertDialog.Builder(this).setTitle("").setItems(operations, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UComment comment = ((MediumListItemView) view).getData();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_reply:
                replyArticle();
                break;
            case R.id.button_recommend:
                recommend();
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

    private class RecommendTask extends AAsyncTask<String, Integer, ResponseObject> {

        public RecommendTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected ResponseObject doInBackground(String... params) {
            String articleID = params[0];
            String title = params[1];
            String summary = params[2];
            String comment = params[3];
            return ArticleAPI.recommendArticle(articleID, title, summary, comment);
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

    @Override
    public void onStartRefresh() {
        loadData(-1);
    }

    @Override
    public void onStartLoadMore() {
        loadData(adapter.getCount() - 1);
    }

    /**
     * 倒序查看
     */
    public void startLoadDesc() {
        MobclickAgent.onEvent(this, Mob.Event_Reverse_Read_Article);
        loadDesc = true;
        loadingView.startLoading();
        listView.setCanPullToLoadMore(false);
        setMenuVisibility();
        if (adapter.getCount() > 0 && adapter.getList().get(0) instanceof Article) {
            article = (Article) adapter.getList().get(0);
            article.setDesc(loadDesc);
            adapter.clear();
            adapter.add(article);
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
        MobclickAgent.onEvent(this, Mob.Event_Normal_Read_Article);
        loadDesc = false;
        loadingView.startLoading();
        listView.setCanPullToLoadMore(false);
        setMenuVisibility();
        if (adapter.getCount() > 0 && adapter.getList().get(0) instanceof Article) {
            article = (Article) adapter.getList().get(0);
            article.setDesc(loadDesc);
            adapter.clear();
            adapter.add(article);
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

    class LoaderTask extends AAsyncTask<Integer, ResponseObject<Article>, ResponseObject<ArrayList<AceModel>>> {
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
            int limit = 20;
            if (offset < 0) {
                offset = 0;
                ResponseObject<Article> articleResult = ArticleAPI.getArticleDetailByID(article.getId());//得不到回复数量
                if (articleResult.ok) {
                    publishProgress(articleResult);
                } else {
                    return new ResponseObject<>();
                }
            }
            if (loadDesc) {
                //因为无法保证获取回复的数据，所以只能采取一次全部加载的方式,但是又不能超过5000，这是服务器的限制
                if (article.getCommentNum() <= 0) {
                    limit = 4999;
                    offset = 0;
                    hasLoadAll = true;
                } else {
                    int tmpOffset = article.getCommentNum() - offset - 20;
                    if (tmpOffset <= 0) {
                        hasLoadAll = true;
                        limit = 20 + tmpOffset;
                        tmpOffset = 0;
                    } else {
                        hasLoadAll = false;
                    }
                    offset = tmpOffset;
                }
            }
            ResponseObject<ArrayList<AceModel>> resultObject = ArticleAPI.getArticleComments(article.getId(), offset, limit);
            if (!resultObject.ok && loadDesc) {
                hasLoadAll = false;
            }
            return resultObject;
        }

        @SafeVarargs
        @Override
        protected final void onProgressUpdate(ResponseObject<Article>... values) {
            //在这里取到正文，正文的结果一定是正确的
            progressBar.setVisibility(View.VISIBLE);
            floatingActionsMenu.setVisibility(View.VISIBLE);
            loadingView.onLoadSuccess();
            ResponseObject<Article> result = values[0];
            Article tmpArticle = result.result;
            tmpArticle.setUrl(article.getUrl());
            tmpArticle.setSummary(article.getSummary());
            tmpArticle.setCommentNum(article.getCommentNum());
            article = tmpArticle;
            adapter.add(0, article);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(ResponseObject<ArrayList<AceModel>> result) {
            progressBar.setVisibility(View.GONE);
            if (result.ok) {
                loadingView.onLoadSuccess();
                ArrayList<AceModel> ars = result.result;
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
                    loadingView.onLoadSuccess();
                }
            }
            if (adapter.getCount() > 0) {
                listView.setCanPullToLoadMore(true);
            } else {
                listView.setCanPullToLoadMore(false);
            }
            if (loadDesc && hasLoadAll) {
                article.setCommentNum(adapter.getCount() - 1);
                listView.setCanPullToLoadMore(false);
            }
            listView.doneOperation();
        }
    }

    class LikeCommentTask extends AAsyncTask<MediumListItemView, Integer, ResponseObject> {

        UComment comment;
        MediumListItemView mediumListItemView;

        public LikeCommentTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected ResponseObject doInBackground(MediumListItemView... params) {
            mediumListItemView = params[0];
            comment = mediumListItemView.getData();
            return ArticleAPI.likeComment(comment.getID());
        }

        @Override
        protected void onPostExecute(ResponseObject resultObject) {
            if (resultObject.ok) {
                comment.setHasLiked(true);
                comment.setLikeNum(comment.getLikeNum() + 1);
                if (mediumListItemView.getData() == comment) {
                    mediumListItemView.plusOneLike();
                }
            } else {
                //do nothing
            }
        }
    }

    class DeleteCommentTask extends AAsyncTask<UComment, Integer, ResponseObject> {

        UComment comment;

        public DeleteCommentTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected ResponseObject doInBackground(UComment... params) {
            comment = params[0];
            return ArticleAPI.deleteMyComment(comment.getID());
        }

        @Override
        protected void onPostExecute(ResponseObject resultObject) {
            if (resultObject.ok) {
                if (article.getCommentNum() > 0) {
                    article.setCommentNum(article.getCommentNum() - 1);
                }
                adapter.remove(comment);
                adapter.notifyDataSetChanged();
            } else {
                toastSingleton("删除失败~");
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
            if (isActive() && intent.getIntExtra(Consts.Extra_Activity_Hashcode, 0) == ArticleActivity.this.hashCode()) {
                if (Consts.Action_Start_Loading_Latest.equals(intent.getAction())) {
                    onStartLoadingLatest();
                } else if (Consts.Action_Finish_Loading_Latest.equals(intent.getAction())) {
                    onFinishLoadingLatest();
                }
            }

        }
    }
}
