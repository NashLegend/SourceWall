package com.example.sourcewall;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.example.sourcewall.adapters.ArticleDetailAdapter;
import com.example.sourcewall.commonview.LListView;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.ArticleAPI;
import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.dialogs.FavorDialog;
import com.example.sourcewall.dialogs.InputDialog;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.model.SimpleComment;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.ToastUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class ArticleActivity extends SwipeActivity implements LListView.OnRefreshListener, View.OnClickListener {

    LListView listView;
    ArticleDetailAdapter adapter;
    Article article;
    LoaderTask task;
    Toolbar toolbar;
    View header;
    View bottomLayout;
    FloatingActionButton replyButton;
    FloatingActionButton recomButton;
    FloatingActionButton favorButton;
    int touchSlop = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        touchSlop = (int) (ViewConfiguration.get(ArticleActivity.this).getScaledTouchSlop() * 0.9);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        article = (Article) getIntent().getSerializableExtra(Consts.Extra_Article);
        bottomLayout = findViewById(R.id.layout_operation);
        listView = (LListView) findViewById(R.id.list_detail);
        adapter = new ArticleDetailAdapter(this);
        listView.setAdapter(adapter);

        header = new View(ArticleActivity.this);
        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.abc_action_bar_default_height_material)));
        header.setBackgroundColor(Color.parseColor("#00000000"));
        listView.addHeaderView(header);

        listView.setOnScrollListener(onScrollListener);
        listView.setOnTouchListener(onTouchListener);
        listView.setCanPullToRefresh(false);
        listView.setCanPullToLoadMore(true);
        listView.setOnRefreshListener(this);

        replyButton = (FloatingActionButton) findViewById(R.id.button_reply);
        recomButton = (FloatingActionButton) findViewById(R.id.button_recommend);
        favorButton = (FloatingActionButton) findViewById(R.id.button_favor);

        replyButton.setOnClickListener(this);
        recomButton.setOnClickListener(this);
        favorButton.setOnClickListener(this);

        loadData(0);
    }

    private void loadData(int offset) {
        if (offset < 0) {
            offset = 0;
        }
        cancelPotentialTask();
        task = new LoaderTask();
        task.execute(offset);
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
            listView.doneOperation();
        }
    }

    private void startReplyActivity() {
        Intent intent = new Intent(this, ReplyArticleActivity.class);
        intent.putExtra(Consts.Extra_Article, article);
        startActivity(intent);
    }

    private void recommend() {
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
                    RecommendTask recommendTask = new RecommendTask();
                    recommendTask.execute(article.getId(), article.getTitle(), article.getSummary(), text);
                } else {
                    // cancel recommend
                }
            }
        });
        InputDialog inputDialog = builder.create();
        inputDialog.show();
    }

    private void favor() {
        // basket dialog
        new FavorDialog.Builder(this).setTitle(R.string.action_favor).create(article).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    AnimatorSet backAnimatorSet;

    private void animateBack() {
        if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {
            hideAnimatorSet.cancel();
        }
        if (backAnimatorSet != null && backAnimatorSet.isRunning()) {

        } else {
            backAnimatorSet = new AnimatorSet();
            ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(toolbar, "translationY", toolbar.getTranslationY(), 0f);
            ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(bottomLayout, "translationY", bottomLayout.getTranslationY(), 0f);
            ArrayList<Animator> animators = new ArrayList<>();
            animators.add(headerAnimator);
            animators.add(footerAnimator);
            backAnimatorSet.setDuration(300);
            backAnimatorSet.playTogether(animators);
            backAnimatorSet.start();
        }
    }

    AnimatorSet hideAnimatorSet;

    private void animateHide() {
        if (backAnimatorSet != null && backAnimatorSet.isRunning()) {
            backAnimatorSet.cancel();
        }
        if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {

        } else {
            hideAnimatorSet = new AnimatorSet();
            ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(toolbar, "translationY", toolbar.getTranslationY(), -toolbar.getHeight());
            ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(bottomLayout, "translationY", bottomLayout.getTranslationY(), bottomLayout.getHeight());
            ArrayList<Animator> animators = new ArrayList<>();
            animators.add(headerAnimator);
            animators.add(footerAnimator);
            hideAnimatorSet.setDuration(300);
            hideAnimatorSet.playTogether(animators);
            hideAnimatorSet.start();
        }
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        float lastY = 0f;
        float currentY = 0f;
        int lastDirection = 0;
        int currentDirection = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = event.getY();
                    currentY = event.getY();
                    currentDirection = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (listView.getFirstVisiblePosition() > 1) {
                        float tmpCurrentY = event.getY();
                        if (Math.abs(tmpCurrentY - lastY) > touchSlop) {
                            currentY = tmpCurrentY;
                            currentDirection = (int) (currentY - lastY);
                            if (lastDirection != currentDirection) {
                                if (currentDirection < 0) {
                                    animateHide();
                                } else {
                                    animateBack();
                                }
                            }
                            lastY = currentY;
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    currentDirection = 0;
                    break;
            }
            return false;
        }
    };


    AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        int lastPosition = 0;
        int state = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            state = scrollState;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0 || firstVisibleItem == 1) {
                animateBack();
            }
            if (firstVisibleItem > 1) {
                if (firstVisibleItem > lastPosition && state == SCROLL_STATE_FLING) {
                    animateHide();
                }
            }
            lastPosition = firstVisibleItem;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_reply:
                startReplyActivity();
                break;
            case R.id.button_recommend:
                recommend();
                break;
            case R.id.button_favor:
                favor();
                break;
        }
    }

    class RecommendTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            String articleID = params[0];
            String title = params[1];
            String summary = params[2];
            String comment = params[3];
            String articleUrl = "http://www.guokr.com/article/" + articleID + "/";
            return UserAPI.recommendLink(articleUrl, title, summary, comment);
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                ToastUtil.toast("Recommend OK");
            } else {
                ToastUtil.toast("Recommend Failed");
            }
        }
    }

    @Override
    public void onStartRefresh() {
        loadData(0);
    }

    @Override
    public void onStartLoadMore() {
        loadData(adapter.getCount() - 1);
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
            ArrayList<AceModel> models = new ArrayList<AceModel>();
            ResultObject resultObject = new ResultObject();
            try {
                if (offset > 0) {
                    models.addAll(ArticleAPI.getArticleComments(article.getId(), offset));
                } else {
                    //同时取了热门回帖，但是在这里没有显示 TODO
                    Article detailArticle = ArticleAPI.getArticleDetailByID(article.getId());
                    ArrayList<SimpleComment> simpleComments = ArticleAPI.getArticleComments(article.getId(), 0);
                    article.setContent(detailArticle.getContent());
                    models.add(article);
                    models.addAll(simpleComments);
                }
                resultObject.result = models;
                resultObject.ok = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResultObject result) {
            if (!isCancelled()) {
                if (result.ok) {
                    ArrayList<AceModel> ars = (ArrayList<AceModel>) result.result;
                    if (offset > 0) {
                        //Load More
                        if (ars.size() > 0) {
                            adapter.addAll(ars);
                        } else {
                            //no data loaded
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        //Refresh
                        if (ars.size() > 0) {
                            adapter.setList(ars);
                        } else {
                            //no data loaded,不清除，保留旧数据
                        }
                        adapter.notifyDataSetInvalidated();
                    }
                } else {
                    // load error
                }
                listView.doneOperation();
            }
        }
    }
}
