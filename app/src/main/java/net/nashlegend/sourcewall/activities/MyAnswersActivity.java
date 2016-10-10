package net.nashlegend.sourcewall.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.AceAdapter;
import net.nashlegend.sourcewall.data.Mob;
import net.nashlegend.sourcewall.model.Answer;
import net.nashlegend.sourcewall.request.RequestObject.SimpleCallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.QuestionAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.AutoHideUtil;
import net.nashlegend.sourcewall.view.AnswerListItemView;
import net.nashlegend.sourcewall.view.common.LoadingView;
import net.nashlegend.sourcewall.view.common.listview.LListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyAnswersActivity extends BaseActivity {

    @BindView(R.id.list_answers)
    LListView listView;
    @BindView(R.id.answer_progress_loading)
    LoadingView loadingView;
    @BindView(R.id.action_bar)
    Toolbar toolbar;
    @BindView(R.id.app_bar)
    AppBarLayout appbar;

    private int headerHeight = 112;
    private AnswerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_answers);
        Mob.onEvent(Mob.Event_Open_My_Answers);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {

            boolean preparingToScrollToHead = false;

            @Override
            public void onClick(View v) {
                if (preparingToScrollToHead) {
                    listView.smoothScrollByOffset(-Integer.MAX_VALUE);
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
        adapter = new AnswerAdapter(this);
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
                if (view instanceof AnswerListItemView) {
                    Intent intent = new Intent();
                    intent.setClass(MyAnswersActivity.this, AnswerActivity.class);
                    Uri answerUri = Uri.parse("http://www.guokr.com/answer/" + ((AnswerListItemView) view).getData().getID() + "/redirect/");
                    intent.setData(answerUri);
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
        loadingView.onLoading();
    }

    private void loadOver() {
        loadData(0);
        loadingView.onLoading();
    }

    private void loadData(int offset) {
        loadAnswers(offset);
    }

    private void loadAnswers(final int offset) {
        QuestionAPI.getAnswerListByUser(UserAPI.getUkey(), offset, new SimpleCallBack<ArrayList<Answer>>() {
            @Override
            public void onFailure(@NonNull ResponseObject<ArrayList<Answer>> result) {
                toast(R.string.load_failed);
                loadingView.onFailed();
            }

            @Override
            public void onSuccess(@NonNull ArrayList<Answer> result) {
                if (isFinishing()) {
                    return;
                }
                listView.doneOperation();
                loadingView.onSuccess();
                for (Answer answer : result) {
                    answer.getAuthor().setName(UserAPI.getName());
                }
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

    public class AnswerAdapter extends AceAdapter<Answer> {
        public AnswerAdapter(Context context) {
            super(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new AnswerListItemView(getContext());
            }
            ((AnswerListItemView) convertView).setData(list.get(position));
            return convertView;
        }
    }

}
