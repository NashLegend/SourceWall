package net.nashlegend.sourcewall;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.commonview.SScrollView;
import net.nashlegend.sourcewall.commonview.WWebView;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.model.QuestionAnswer;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.request.api.QuestionAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.RoundTransformation;
import net.nashlegend.sourcewall.util.StyleChecker;

import java.util.ArrayList;

public class AnswerActivity extends SwipeActivity implements View.OnClickListener, LoadingView.ReloadListener {

    private View rootView;
    private View authorLayout;
    private Toolbar toolbar;
    private SScrollView scrollView;
    private View headerHolder;
    private WWebView webView;
    private ImageView avatar;
    private TextView questionText;
    private TextView authorName;
    private TextView authorTitle;
    private TextView supportText;
    private View supportView;
    private Question question;
    private QuestionAnswer answer;
    private Uri redirectUri;
    private String notice_id;
    private LoadingView loadingView;
    private FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton replyButton;
    private FloatingActionButton notAnButton;
    private FloatingActionButton thankButton;
    private Handler handler;
    private int topBarHeight;
    private int headerHeight;
    private boolean fromHost;//是否由host页面跳转而来

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        MobclickAgent.onEvent(this, Mob.Event_Open_Answer);
        handler = new Handler();
        rootView = findViewById(R.id.rootView);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        authorLayout = findViewById(R.id.layout_author);
        scrollView = (SScrollView) findViewById(R.id.scrollView);
        headerHolder = findViewById(R.id.headerHolder);
        webView = (WWebView) findViewById(R.id.web_content);
        questionText = (TextView) findViewById(R.id.text_title);
        avatar = (ImageView) findViewById(R.id.image_avatar);
        authorName = (TextView) findViewById(R.id.text_author);
        authorTitle = (TextView) findViewById(R.id.text_author_title);
        supportView = findViewById(R.id.layout_opinion);
        supportText = (TextView) findViewById(R.id.text_num_support);
        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.layout_operation);
        loadingView = (LoadingView) findViewById(R.id.answer_progress_loading);

        replyButton = (FloatingActionButton) findViewById(R.id.button_reply);
        notAnButton = (FloatingActionButton) findViewById(R.id.button_Bury);
        thankButton = (FloatingActionButton) findViewById(R.id.button_thank);

        authorLayout.setOnClickListener(this);
        questionText.setOnClickListener(this);
        supportView.setOnClickListener(this);
        replyButton.setOnClickListener(this);
        notAnButton.setOnClickListener(this);
        thankButton.setOnClickListener(this);
        loadingView.setReloadListener(this);

        if (getIntent().hasExtra(Consts.Extra_Answer)) {
            fromHost = true;
            loadingView.setVisibility(View.GONE);
            answer = (QuestionAnswer) getIntent().getSerializableExtra(Consts.Extra_Answer);
            question = (Question) getIntent().getSerializableExtra(Consts.Extra_Question);
            initData();
        } else {
            //来自其他地方的跳转
            fromHost = false;
            redirectUri = getIntent().getData();
            notice_id = getIntent().getStringExtra(Consts.Extra_Notice_Id);
            if (redirectUri != null) {
                loadingView.setVisibility(View.VISIBLE);
                loadDataByUri();
            } else {
                finish();
            }
        }
    }

    LoaderTask loaderTask;

    private void loadDataByUri() {
        if (loaderTask != null && loaderTask.getStatus() == AsyncTask.Status.RUNNING) {
            loaderTask.cancel(true);
        }

        loaderTask = new LoaderTask();
        loaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void initData() {
        questionText.setText(question.getTitle());
        supportText.setText(answer.getUpvoteNum() + "");
        authorName.setText(answer.getAuthor());
        authorTitle.setText(answer.getAuthorTitle());
        if (answer.isHasBuried()) {
            notAnButton.setIcon(R.drawable.dustbin);
        } else {
            notAnButton.setIcon(R.drawable.dustbin_outline);
        }
        if (answer.isHasThanked()) {
            thankButton.setIcon(R.drawable.heart);
        } else {
            thankButton.setIcon(R.drawable.heart_outline);
        }
        if (Config.shouldLoadImage()) {
            Picasso.with(this).load(answer.getAuthorAvatarUrl()).placeholder(R.drawable.default_avatar)
                    .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen)
                    .transform(new RoundTransformation(Color.parseColor("#00000000"), 0, true)).into(avatar);
        } else {
            avatar.setImageResource(R.drawable.default_avatar);
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
        Resources.Theme theme = getTheme();
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.color_webview_background});
        int colorBack = typedArray.getColor(0, 0);
        typedArray.recycle();
        webView.setBackgroundColor(colorBack);

        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setPrimarySource(answer.getContent());
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
            if (backAnimatorSet == null || !backAnimatorSet.isRunning()) {
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
            if ((backAnimatorSet == null || !backAnimatorSet.isRunning()) && (backFooterAnimatorSet == null || !backFooterAnimatorSet.isRunning())) {
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
            if (hideAnimatorSet == null || !hideAnimatorSet.isRunning()) {
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
            case R.id.text_title:
                if (fromHost) {
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.setClass(this, QuestionActivity.class);
                    intent.putExtra(Consts.Extra_Question, (question));
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, 0);
                }
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
            case R.id.layout_author:
                if (authorLayout.getTranslationY() < 0) {
                    autoHideListener.animateBack();
                } else {
                    if (scrollView.getScrollY() > authorLayout.getTop()) {
                        autoHideListener.animateHide();
                    }
                }
                break;
        }
    }

    private void replyAnswer() {
        MobclickAgent.onEvent(this, Mob.Event_Open_Answer_Comment);
        Intent intent = new Intent(this, SimpleReplyActivity.class);
        intent.putExtra(Consts.Extra_Ace_Model, answer);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, 0);
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
                    if (answer.isHasUpVoted() && support) {
                        toastSingleton(R.string.has_supported);
                        return;
                    }
                    if (answer.isHasDownVoted() && !support) {
                        toastSingleton(R.string.has_opposed);
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
                toastSingleton("已经感谢过");
            } else {
                ThankTask task = new ThankTask();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void reload() {
        loadDataByUri();
    }

    class LoaderTask extends AsyncTask<Uri, Integer, ResultObject<QuestionAnswer>> {

        @Override
        protected void onPreExecute() {
            floatingActionsMenu.setVisibility(View.VISIBLE);
        }

        @Override
        protected ResultObject<QuestionAnswer> doInBackground(Uri... params) {
            UserAPI.ignoreOneNotice(notice_id);
            return QuestionAPI.getSingleAnswerFromRedirectUrl(redirectUri.toString());
        }

        @Override
        protected void onPostExecute(ResultObject<QuestionAnswer> result) {
            if (result.ok) {
                floatingActionsMenu.setVisibility(View.VISIBLE);
                loadingView.onLoadSuccess();
                answer = result.result;
                question = new Question();
                question.setTitle(answer.getQuestion());
                question.setId(answer.getQuestionID());
                initData();
            } else {
                loadingView.onLoadFailed();
            }
        }
    }

    class OpinionTask extends AsyncTask<Boolean, Integer, ResultObject> {

        boolean isSupport;

        @Override
        protected ResultObject doInBackground(Boolean... params) {
            isSupport = params[0];
            if (isSupport) {
                MobclickAgent.onEvent(AnswerActivity.this, Mob.Event_Support_Answer);
                return QuestionAPI.supportAnswer(answer.getID());
            } else {
                MobclickAgent.onEvent(AnswerActivity.this, Mob.Event_Oppose_Answer);
                return QuestionAPI.opposeAnswer(answer.getID());
            }
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                answer.setUpvoteNum(answer.getUpvoteNum() + 1);
                supportText.setText(answer.getUpvoteNum() + "");
                answer.setHasDownVoted(!isSupport);
                answer.setHasUpVoted(isSupport);
                toast((isSupport ? "赞同" : "反对") + "成功");
            } else {
                toast((isSupport ? "赞同" : "反对") + "未遂");
            }
        }
    }

    class BuryTask extends AsyncTask<Boolean, Integer, ResultObject> {
        boolean bury = true;

        @Override
        protected void onPreExecute() {
            MobclickAgent.onEvent(AnswerActivity.this, Mob.Event_Bury_Answer);
        }

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
                    toast("已标记为\"不是答案\"");
                    answer.setHasBuried(true);
                    notAnButton.setIcon(R.drawable.dustbin);
                } else {
                    toastSingleton("取消\"不是答案\"标记");
                    answer.setHasBuried(false);
                    notAnButton.setIcon(R.drawable.dustbin_outline);
                }
            } else {
                if (bury && resultObject.code == ResultObject.ResultCode.CODE_ALREADY_BURIED) {
                    toastSingleton("已经标记过了");
                } else {
                    toastSingleton("操作失败");
                }
            }

        }
    }

    class ThankTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            MobclickAgent.onEvent(AnswerActivity.this, Mob.Event_Thank_Answer);
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            return QuestionAPI.thankAnswer(answer.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                toast("感谢成功");
                answer.setHasThanked(true);
                thankButton.setIcon(R.drawable.heart);
            } else {
                if (resultObject.code == ResultObject.ResultCode.CODE_ALREADY_THANKED) {
                    toast("已经感谢过了");
                    answer.setHasThanked(true);
                    thankButton.setIcon(R.drawable.heart);
                } else {
                    toast("感谢未遂");
                }
            }
        }
    }
}
