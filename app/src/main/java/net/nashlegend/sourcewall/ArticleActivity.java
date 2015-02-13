package net.nashlegend.sourcewall;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import net.nashlegend.sourcewall.adapters.ArticleDetailAdapter;
import net.nashlegend.sourcewall.commonview.LListView;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.connection.ResultObject;
import net.nashlegend.sourcewall.connection.api.ArticleAPI;
import net.nashlegend.sourcewall.connection.api.UserAPI;
import net.nashlegend.sourcewall.dialogs.FavorDialog;
import net.nashlegend.sourcewall.dialogs.InputDialog;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.util.AutoHideUtil;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.RegUtil;
import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.view.MediumListItemView;

import java.util.ArrayList;

public class ArticleActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener, LoadingView.ReloadListener {

    private LListView listView;
    private ArticleDetailAdapter adapter;
    private Article article;
    private LoaderTask task;
    private LoadingView loadingView;
    private String notice_id;

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
        setContentView(net.nashlegend.sourcewall.R.layout.activity_article);
        loadingView = (LoadingView) findViewById(net.nashlegend.sourcewall.R.id.article_progress_loading);
        loadingView.setReloadListener(this);
        Toolbar toolbar = (Toolbar) findViewById(net.nashlegend.sourcewall.R.id.action_bar);
        setSupportActionBar(toolbar);
        article = (Article) getIntent().getSerializableExtra(Consts.Extra_Article);
        notice_id = getIntent().getStringExtra(Consts.Extra_Notice_Id);
        if (!TextUtils.isEmpty(article.getSubjectName())) {
            setTitle(article.getSubjectName() + " -- 科学人");
        }
        listView = (LListView) findViewById(net.nashlegend.sourcewall.R.id.list_detail);
        adapter = new ArticleDetailAdapter(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(onItemClickListener);
        listView.setCanPullToLoadMore(false);
        listView.setOnRefreshListener(this);

        FloatingActionsMenu floatingActionsMenu = (FloatingActionsMenu) findViewById(net.nashlegend.sourcewall.R.id.layout_operation);
        FloatingActionButton replyButton = (FloatingActionButton) findViewById(net.nashlegend.sourcewall.R.id.button_reply);
        FloatingActionButton recomButton = (FloatingActionButton) findViewById(net.nashlegend.sourcewall.R.id.button_recommend);
        FloatingActionButton favorButton = (FloatingActionButton) findViewById(net.nashlegend.sourcewall.R.id.button_favor);

        replyButton.setOnClickListener(this);
        recomButton.setOnClickListener(this);
        favorButton.setOnClickListener(this);

        AutoHideUtil.applyListViewAutoHide(this, listView, toolbar, floatingActionsMenu, (int) getResources().getDimension(net.nashlegend.sourcewall.R.dimen.abc_action_bar_default_height_material));

        loadData(-1);
    }

    /**
     * @param offset -1是指刷新
     */
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
            startActivity(intent);
            overridePendingTransition(net.nashlegend.sourcewall.R.anim.slide_in_right, 0);
        }
    }

    private void recommend() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            InputDialog.Builder builder = new InputDialog.Builder(this);
            builder.setTitle(net.nashlegend.sourcewall.R.string.recommend_article);
            builder.setCancelable(true);
            builder.setCanceledOnTouchOutside(false);
            builder.setOnClickListener(new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        InputDialog d = (InputDialog) dialog;
                        String text = d.InputString;
                        RecommendTask recommendTask = new RecommendTask();
                        recommendTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, article.getId(), article.getTitle(), article.getSummary(), text);
                    } else {
                        // cancel recommend
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
            new FavorDialog.Builder(this).setTitle(net.nashlegend.sourcewall.R.string.action_favor).create(article).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(net.nashlegend.sourcewall.R.menu.menu_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
                ToastUtil.toastSingleton(getString(R.string.has_liked_this));
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
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(null, RegUtil.html2PlainText(comment.getContent())));
    }

    private void onReplyItemClick(final View view, int position, long id) {
        if (view instanceof MediumListItemView) {
            final UComment comment = ((MediumListItemView) view).getData();
            ArrayList<String> ops = new ArrayList<>();
            ops.add(getString(net.nashlegend.sourcewall.R.string.action_reply));
            ops.add(getString(net.nashlegend.sourcewall.R.string.action_copy));
            if (!comment.isHasLiked()) {
                ops.add(getString(net.nashlegend.sourcewall.R.string.action_like));
            }
            if (comment.getAuthorID().equals(UserAPI.getUserID())) {
                ops.add(getString(net.nashlegend.sourcewall.R.string.action_delete));
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

    private AdapterView.OnItemClickListener onItemClickListener;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case net.nashlegend.sourcewall.R.id.button_reply:
                replyArticle();
                break;
            case net.nashlegend.sourcewall.R.id.button_recommend:
                recommend();
                break;
            case net.nashlegend.sourcewall.R.id.button_favor:
                favor();
                break;
        }
    }

    @Override
    public void reload() {
        loadData(-1);
    }

    private class RecommendTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(String... params) {
            String articleID = params[0];
            String title = params[1];
            String summary = params[2];
            String comment = params[3];
            return ArticleAPI.recommendArticle(articleID, title, summary, comment);
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                ToastUtil.toast(net.nashlegend.sourcewall.R.string.recommend_ok);
            } else {
                ToastUtil.toast(net.nashlegend.sourcewall.R.string.recommend_failed);
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

    class LoaderTask extends AsyncTask<Integer, Integer, ResultObject> {
        int offset;

        @Override
        protected ResultObject doInBackground(Integer... params) {
            if (!TextUtils.isEmpty(notice_id)) {
                UserAPI.ignoreOneNotice(notice_id);
                notice_id = null;
            }
            offset = params[0];
            if (offset < 0) {
                //同时取了热门回帖，但是在这里没有显示 TODO
                return ArticleAPI.getArticleFirstPage(article);
            } else {
                return ArticleAPI.getArticleComments(article.getId(), offset);
            }
        }

        @Override
        protected void onPostExecute(ResultObject result) {
            if (result.ok) {
                loadingView.onLoadSuccess();
                ArrayList<AceModel> ars = (ArrayList<AceModel>) result.result;
                if (offset < 0) {
                    //Refresh
                    if (ars.size() > 0) {
                        adapter.setList(ars);
                        adapter.notifyDataSetInvalidated();
                    }//否则就是页面不存在，这种情况在ok的情况下不存在
                } else {
                    //Load More
                    if (ars.size() > 0) {
                        adapter.addAll(ars);
                        adapter.notifyDataSetChanged();
                    }
                }
                if (adapter.getCount() > 0) {
                    listView.setCanPullToLoadMore(true);
                } else {
                    listView.setCanPullToLoadMore(false);
                }
            } else {
                if (result.statusCode == 404) {
                    ToastUtil.toastSingleton(R.string.page_404);
                    finish();
                } else {
                    ToastUtil.toastSingleton(getString(net.nashlegend.sourcewall.R.string.load_failed));
                    loadingView.onLoadFailed();
                }
            }
            listView.doneOperation();
        }
    }

    class LikeCommentTask extends AsyncTask<MediumListItemView, Integer, ResultObject> {

        UComment comment;
        MediumListItemView mediumListItemView;

        @Override
        protected ResultObject doInBackground(MediumListItemView... params) {
            mediumListItemView = params[0];
            comment = mediumListItemView.getData();
            return ArticleAPI.likeComment(comment.getID());
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
                //do nothing
            }
        }
    }

    class DeleteCommentTask extends AsyncTask<UComment, Integer, ResultObject> {

        UComment comment;

        @Override
        protected ResultObject doInBackground(UComment... params) {
            comment = params[0];
            return ArticleAPI.deleteMyComment(comment.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                adapter.remove(comment);
                adapter.notifyDataSetChanged();
            } else {
                ToastUtil.toastSingleton("删除失败~");
            }
        }
    }
}
