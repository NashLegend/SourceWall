package net.nashlegend.sourcewall;

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

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.squareup.picasso.Picasso;

import net.nashlegend.sourcewall.commonview.SScrollView;
import net.nashlegend.sourcewall.commonview.WWebView;
import net.nashlegend.sourcewall.connection.ResultObject;
import net.nashlegend.sourcewall.connection.api.QuestionAPI;
import net.nashlegend.sourcewall.connection.api.UserAPI;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.model.QuestionAnswer;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.StyleChecker;
import net.nashlegend.sourcewall.util.ToastUtil;

import java.util.ArrayList;

public class AnswerActivity extends SwipeActivity implements View.OnClickListener {

    View rootView;
    View authorLayout;
    Toolbar toolbar;
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
    FloatingActionsMenu floatingActionsMenu;
    FloatingActionButton replyButton;
    FloatingActionButton notAnButton;
    FloatingActionButton thankButton;
    Handler handler;
    int topBarHeight;
    int headerHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(net.nashlegend.sourcewall.R.layout.activity_answer);
        handler = new Handler();
        rootView = findViewById(net.nashlegend.sourcewall.R.id.rootView);
        answer = (QuestionAnswer) getIntent().getSerializableExtra(Consts.Extra_Answer);
        question = (Question) getIntent().getSerializableExtra(Consts.Extra_Question);
        toolbar = (Toolbar) findViewById(net.nashlegend.sourcewall.R.id.action_bar);
        setSupportActionBar(toolbar);
        authorLayout = findViewById(net.nashlegend.sourcewall.R.id.layout_author);
        scrollView = (SScrollView) findViewById(net.nashlegend.sourcewall.R.id.scrollView);
        headerHolder = findViewById(net.nashlegend.sourcewall.R.id.headerHolder);
        footerHolder = findViewById(net.nashlegend.sourcewall.R.id.footerHolder);
        webHolder = (LinearLayout) findViewById(net.nashlegend.sourcewall.R.id.web_holder);
        webView = (WWebView) findViewById(net.nashlegend.sourcewall.R.id.web_content);
        questionText = (TextView) findViewById(net.nashlegend.sourcewall.R.id.text_title);
        avatar = (ImageView) findViewById(net.nashlegend.sourcewall.R.id.image_avatar);
        authorName = (TextView) findViewById(net.nashlegend.sourcewall.R.id.text_author);
        authorTitle = (TextView) findViewById(net.nashlegend.sourcewall.R.id.text_author_title);
        supportView = findViewById(net.nashlegend.sourcewall.R.id.layout_opinion);
        supportText = (TextView) findViewById(net.nashlegend.sourcewall.R.id.text_num_support);
        floatingActionsMenu = (FloatingActionsMenu) findViewById(net.nashlegend.sourcewall.R.id.layout_operation);

        replyButton = (FloatingActionButton) findViewById(net.nashlegend.sourcewall.R.id.button_reply);
        notAnButton = (FloatingActionButton) findViewById(net.nashlegend.sourcewall.R.id.button_Bury);
        thankButton = (FloatingActionButton) findViewById(net.nashlegend.sourcewall.R.id.button_thank);

        questionText.setOnClickListener(this);
        supportView.setOnClickListener(this);
        replyButton.setOnClickListener(this);
        notAnButton.setOnClickListener(this);
        thankButton.setOnClickListener(this);

        questionText.setText(question.getTitle());
        supportText.setText(answer.getUpvoteNum() + "");
        authorName.setText(answer.getAuthor());
        authorTitle.setText(answer.getAuthorTitle());
        if (answer.isHasBuried()) {
            notAnButton.setIcon(net.nashlegend.sourcewall.R.drawable.dustbin);
        } else {
            notAnButton.setIcon(net.nashlegend.sourcewall.R.drawable.dustbin_outline);
        }
        if (answer.isHasThanked()) {
            thankButton.setIcon(net.nashlegend.sourcewall.R.drawable.heart);
        } else {
            thankButton.setIcon(net.nashlegend.sourcewall.R.drawable.heart_outline);
        }
        if (Config.shouldLoadImage()) {
            Picasso.with(this).load(answer.getAuthorAvatarUrl())
                    .resizeDimen(net.nashlegend.sourcewall.R.dimen.list_standard_comment_avatar_dimen, net.nashlegend.sourcewall.R.dimen.list_standard_comment_avatar_dimen)
                    .into(avatar);
        } else {
            avatar.setImageResource(net.nashlegend.sourcewall.R.drawable.default_avatar);
        }
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
        getMenuInflater().inflate(net.nashlegend.sourcewall.R.menu.menu_answer, menu);
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
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(floatingActionsMenu, "translationY", floatingActionsMenu.getTranslationY(), 0f);
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
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(floatingActionsMenu, "translationY", floatingActionsMenu.getTranslationY(), 0f);
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
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(floatingActionsMenu, "translationY", floatingActionsMenu.getTranslationY(), floatingActionsMenu.getHeight());
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
            case net.nashlegend.sourcewall.R.id.text_title:
                finish();
                break;
            case net.nashlegend.sourcewall.R.id.layout_opinion:
                invokeOpinionDialog();
                break;
            case net.nashlegend.sourcewall.R.id.button_reply:
                replyAnswer();
                break;
            case net.nashlegend.sourcewall.R.id.button_Bury:
                buryAnswer();
                break;
            case net.nashlegend.sourcewall.R.id.button_thank:
                thankAnswer();
                break;
        }
    }

    private void replyAnswer() {
        Intent intent = new Intent(this, SimpleReplyActivity.class);
        intent.putExtra(Consts.Extra_Ace_Model, answer);
        startActivity(intent);
        overridePendingTransition(net.nashlegend.sourcewall.R.anim.slide_in_right, 0);
    }

    private void invokeOpinionDialog() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            String[] operations = {getString(net.nashlegend.sourcewall.R.string.action_support), getString(net.nashlegend.sourcewall.R.string.action_oppose)};
            new AlertDialog.Builder(this).setTitle("").setItems(operations, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Boolean support = which == 0;
                    if (answer.isHasUpVoted() && support) {
                        ToastUtil.toastSingleton(getString(R.string.has_supported));
                        return;
                    }
                    if (answer.isHasDownVoted() && !support) {
                        ToastUtil.toastSingleton(getString(R.string.has_opposed));
                        return;
                    }
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
            if (answer.isHasThanked()) {
                ToastUtil.toastSingleton("已经感谢过");
            } else {
                ThankTask task = new ThankTask();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    class OpinionTask extends AsyncTask<Boolean, Integer, ResultObject> {

        boolean isSupport;

        @Override
        protected void onCancelled(ResultObject resultObject) {
            super.onCancelled(resultObject);
        }

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
                answer.setHasDownVoted(!isSupport);
                answer.setHasUpVoted(isSupport);
                ToastUtil.toast((isSupport ? "赞同" : "反对") + "成功");
            } else {
                ToastUtil.toast((isSupport ? "赞同" : "反对") + "未遂");
            }
        }
    }

    class BuryTask extends AsyncTask<Boolean, Integer, ResultObject> {
        boolean bury = true;

        @Override
        protected ResultObject doInBackground(Boolean... params) {
            bury = !answer.isHasBuried();
            if (bury) {
                return QuestionAPI.buryAnswer(answer.getID());
            } else {
                return QuestionAPI.unBuryAnswer(answer.getID());
            }

        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                if (bury) {
                    ToastUtil.toast("已标记为\"不是答案\"");
                    answer.setHasBuried(true);
                    notAnButton.setIcon(net.nashlegend.sourcewall.R.drawable.dustbin);
                } else {
                    ToastUtil.toastSingleton("取消\"不是答案\"标记");
                    answer.setHasBuried(false);
                    notAnButton.setIcon(net.nashlegend.sourcewall.R.drawable.dustbin_outline);
                }
            } else {
                if (bury && resultObject.code == ResultObject.ResultCode.CODE_ALREADY_BURIED) {
                    ToastUtil.toastSingleton("已经标记过了");
                } else {
                    ToastUtil.toastSingleton("操作失败");
                }
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
                ToastUtil.toast("感谢成功");
                answer.setHasThanked(true);
                thankButton.setIcon(net.nashlegend.sourcewall.R.drawable.heart);
            } else {
                if (resultObject.code == ResultObject.ResultCode.CODE_ALREADY_THANKED) {
                    ToastUtil.toast("已经感谢过了");
                    answer.setHasThanked(true);
                    thankButton.setIcon(net.nashlegend.sourcewall.R.drawable.heart);
                } else {
                    ToastUtil.toast("感谢未遂");
                }
            }
        }
    }
}
