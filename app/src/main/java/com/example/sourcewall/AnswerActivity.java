package com.example.sourcewall;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sourcewall.CommonView.SScrollView;
import com.example.sourcewall.CommonView.WWebView;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.QuestionAPI;
import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.model.Question;
import com.example.sourcewall.model.QuestionAnswer;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.StyleChecker;
import com.example.sourcewall.util.ToastUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AnswerActivity extends SwipeActivity implements View.OnClickListener {

    View rootView;
    View authorLayout;
    Toolbar toolbar;
    View bottomLayout;
    SScrollView scrollView;
    View headerHolder;
    View footerHolder;
    LinearLayout webHolder;
    WWebView webView;
    ImageView avatar;
    TextView questionText;
    TextView authorName;
    TextView authorTitle;
    TextView supportText;
    View supportView;
    Question question;
    QuestionAnswer answer;
    FloatingActionButton replyButton;
    FloatingActionButton notAnButton;
    FloatingActionButton thankButton;
    Handler handler;
    int topBarHeight;
    int headerHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        handler = new Handler();
        rootView = findViewById(R.id.rootView);
        answer = (QuestionAnswer) getIntent().getSerializableExtra(Consts.Extra_Answer);
        question = (Question) getIntent().getSerializableExtra(Consts.Extra_Question);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        authorLayout = findViewById(R.id.layout_author);
        bottomLayout = findViewById(R.id.layout_operation);
        scrollView = (SScrollView) findViewById(R.id.scrollView);
        headerHolder = findViewById(R.id.headerHolder);
        footerHolder = findViewById(R.id.footerHolder);
        webHolder = (LinearLayout) findViewById(R.id.web_holder);
        webView = (WWebView) findViewById(R.id.web_content);
        questionText = (TextView) findViewById(R.id.text_title);
        avatar = (ImageView) findViewById(R.id.image_avatar);
        authorName = (TextView) findViewById(R.id.text_author);
        authorTitle = (TextView) findViewById(R.id.text_author_title);
        supportView = findViewById(R.id.layout_opinion);
        supportText = (TextView) findViewById(R.id.text_num_support);
        replyButton = (FloatingActionButton) findViewById(R.id.button_reply);
        notAnButton = (FloatingActionButton) findViewById(R.id.button_Bury);
        thankButton = (FloatingActionButton) findViewById(R.id.button_thank);

        questionText.setOnClickListener(this);
        supportView.setOnClickListener(this);
        replyButton.setOnClickListener(this);
        notAnButton.setOnClickListener(this);
        thankButton.setOnClickListener(this);

        questionText.setText(question.getTitle());
        supportText.setText(answer.getUpvoteNum() + "");
        authorName.setText(answer.getAuthor());
        authorTitle.setText(answer.getAuthorTitle());
        Picasso.with(this).load(answer.getAuthorAvatarUrl())
                .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen)
                .into(avatar);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (authorLayout.getHeight() > 0) {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    topBarHeight = toolbar.getHeight() + questionText.getHeight();
                    headerHeight = topBarHeight + authorLayout.getHeight();
                    ViewGroup.LayoutParams params = headerHolder.getLayoutParams();
                    params.height = headerHeight;
                    scrollView.applyAutoHide(AnswerActivity.this, topBarHeight, autoHideListener);
                    loadHtml();
                }
            }
        });
        webView.setExtWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                resize();
            }
        });
    }

    public void resize() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = webView.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                webView.setLayoutParams(params);
            }
        });
    }

    private void loadHtml() {
        String html = StyleChecker.getAnswerHtml(answer.getContent());
        webView.setBackgroundColor(0);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.loadDataWithBaseURL(Consts.Base_Url, html, "text/html", "charset=UTF-8", null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_answer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    SScrollView.AutoHideListener autoHideListener = new SScrollView.AutoHideListener() {
        AnimatorSet backAnimatorSet;

        @Override
        public void animateBack() {
            if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {
                hideAnimatorSet.cancel();
            }
            if (backFooterAnimatorSet != null && backFooterAnimatorSet.isRunning()) {
                backFooterAnimatorSet.cancel();
            }
            if (backAnimatorSet != null && backAnimatorSet.isRunning()) {

            } else {
                backAnimatorSet = new AnimatorSet();
                ObjectAnimator toolBarAnimator = ObjectAnimator.ofFloat(toolbar, "translationY", toolbar.getTranslationY(), 0f);
                ObjectAnimator titleAnimator = ObjectAnimator.ofFloat(questionText, "translationY", questionText.getTranslationY(), 0f);
                ObjectAnimator authorAnimator = ObjectAnimator.ofFloat(authorLayout, "translationY", authorLayout.getTranslationY(), 0f);
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(bottomLayout, "translationY", bottomLayout.getTranslationY(), 0f);
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(toolBarAnimator);
                animators.add(titleAnimator);
                animators.add(authorAnimator);
                animators.add(footerAnimator);
                backAnimatorSet.setDuration(300);
                backAnimatorSet.playTogether(animators);
                backAnimatorSet.start();
            }
        }

        AnimatorSet backFooterAnimatorSet;

        @Override
        public void animateBackFooter() {
            if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {
                hideAnimatorSet.cancel();
            }
            if (backAnimatorSet != null && backAnimatorSet.isRunning() || backFooterAnimatorSet != null && backFooterAnimatorSet.isRunning()) {
                //do nothing
            } else {
                backFooterAnimatorSet = new AnimatorSet();
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(bottomLayout, "translationY", bottomLayout.getTranslationY(), 0f);
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(footerAnimator);
                backFooterAnimatorSet.setDuration(300);
                backFooterAnimatorSet.playTogether(animators);
                backFooterAnimatorSet.start();
            }
        }

        AnimatorSet hideAnimatorSet;

        @Override
        public void animateHide() {
            if (backAnimatorSet != null && backAnimatorSet.isRunning()) {
                backAnimatorSet.cancel();
            }
            if (backFooterAnimatorSet != null && backFooterAnimatorSet.isRunning()) {
                backFooterAnimatorSet.cancel();
            }
            if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {

            } else {
                hideAnimatorSet = new AnimatorSet();
                ObjectAnimator toolBarAnimator = ObjectAnimator.ofFloat(toolbar, "translationY", toolbar.getTranslationY(), -toolbar.getBottom());
                ObjectAnimator titleAnimator = ObjectAnimator.ofFloat(questionText, "translationY", questionText.getTranslationY(), -questionText.getBottom());
                ObjectAnimator authorAnimator = ObjectAnimator.ofFloat(authorLayout, "translationY", authorLayout.getTranslationY(), -authorLayout.getTop());
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(bottomLayout, "translationY", bottomLayout.getTranslationY(), bottomLayout.getHeight());
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(toolBarAnimator);
                animators.add(titleAnimator);
                animators.add(authorAnimator);
                animators.add(footerAnimator);
                hideAnimatorSet.setDuration(300);
                hideAnimatorSet.playTogether(animators);
                hideAnimatorSet.start();
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_title:
                finish();
                break;
            case R.id.layout_opinion:
                invokeOpinionDialog();
                break;
            case R.id.button_reply:
                replyAnswer();
                break;
            case R.id.button_Bury:
                buryAnswer();
                break;
            case R.id.button_thank:
                thankAnswer();
                break;
        }
    }

    private void replyAnswer() {
        Intent intent = new Intent(this, SimpleReplyActivity.class);
        intent.putExtra(Consts.Extra_Ace_Model, answer);
        startActivity(intent);
    }

    private void invokeOpinionDialog() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            String[] operations = {getString(R.string.action_support), getString(R.string.action_oppose)};
            new AlertDialog.Builder(this).setTitle("").setItems(operations, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Boolean support = which == 0;
                    OpinionTask task = new OpinionTask();
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, support);
                }
            }).create().show();
        }
    }

    private void buryAnswer() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            BuryTask task = new BuryTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void thankAnswer() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            ThankTask task = new ThankTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    class OpinionTask extends AsyncTask<Boolean, Integer, ResultObject> {

        boolean isSupport;

        @Override
        protected ResultObject doInBackground(Boolean... params) {
            isSupport = params[0];
            if (isSupport) {
                return QuestionAPI.supportAnswer(answer.getID());
            }
            return QuestionAPI.opposeAnswer(answer.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                answer.setUpvoteNum(answer.getUpvoteNum() + 1);
                supportText.setText(answer.getUpvoteNum() + "");
                ToastUtil.toast((isSupport ? "赞同" : "反对") + "成功");
            } else {
                ToastUtil.toast((isSupport ? "赞同" : "反对") + "未遂");
            }
        }
    }

    class BuryTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(String... params) {
            return QuestionAPI.buryAnswer(answer.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                //TODO
                ToastUtil.toast("Bury 成功");
            } else {
                ToastUtil.toast("Bury 未遂");
            }

        }
    }

    class ThankTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(String... params) {
            return QuestionAPI.thankAnswer(answer.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                //TODO
                ToastUtil.toast("感谢成功");
            } else {
                ToastUtil.toast("感谢未遂");
            }
        }
    }
}
