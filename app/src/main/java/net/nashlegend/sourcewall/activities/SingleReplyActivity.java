package net.nashlegend.sourcewall.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.nostra13.universalimageloader.core.ImageLoader;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.RequestObject.RequestCallBack;
import net.nashlegend.sourcewall.request.RequestObject.SimpleCallBack;
import net.nashlegend.sourcewall.request.api.ArticleAPI;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.ImageUtils;
import net.nashlegend.sourcewall.util.StyleChecker;
import net.nashlegend.sourcewall.view.common.LoadingView;
import net.nashlegend.sourcewall.view.common.SScrollView;
import net.nashlegend.sourcewall.view.common.WWebView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SingleReplyActivity extends BaseActivity implements View.OnClickListener, LoadingView.ReloadListener {

    private View rootView;
    private View authorLayout;
    private AppBarLayout appbar;
    private SScrollView scrollView;
    private View headerHolder;
    private WWebView webView;
    private ImageView avatar;
    private TextView hostTitle;
    private TextView authorName;
    private TextView authorTitle;
    private TextView supportText;
    private View likeView;
    private AceModel host;
    private UComment data;
    private Uri redirectUri;
    private String notice_id;
    private LoadingView loadingView;
    private FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton replyButton;
    private FloatingActionButton deleteButton;
    private FloatingActionButton likeButton;
    private Handler handler;
    private int topBarHeight;
    private int headerHeight;
    private int hostSection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_reply);
        handler = new Handler();
        rootView = findViewById(R.id.rootView);
        appbar = (AppBarLayout) findViewById(R.id.app_bar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        authorLayout = findViewById(R.id.layout_author);
        scrollView = (SScrollView) findViewById(R.id.scrollView);
        headerHolder = findViewById(R.id.headerHolder);
        webView = (WWebView) findViewById(R.id.web_content);
        hostTitle = (TextView) findViewById(R.id.text_title);
        avatar = (ImageView) findViewById(R.id.image_avatar);
        authorName = (TextView) findViewById(R.id.text_author);
        authorTitle = (TextView) findViewById(R.id.text_author_title);
        likeView = findViewById(R.id.layout_like);
        supportText = (TextView) findViewById(R.id.text_num_like);
        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.layout_operation);
        loadingView = (LoadingView) findViewById(R.id.answer_progress_loading);

        replyButton = (FloatingActionButton) findViewById(R.id.button_reply);
        deleteButton = (FloatingActionButton) findViewById(R.id.button_Delete);
        likeButton = (FloatingActionButton) findViewById(R.id.button_like);

        authorLayout.setOnClickListener(this);
        hostTitle.setOnClickListener(this);
        likeView.setOnClickListener(this);
        replyButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        likeButton.setOnClickListener(this);
        loadingView.setReloadListener(this);

        //来自其他地方的跳转
        redirectUri = getIntent().getData();
        notice_id = getIntent().getStringExtra(Consts.Extra_Notice_Id);
        if (redirectUri != null) {
            List<String> segments = redirectUri.getPathSegments();
            String hostString = redirectUri.getHost();
            if (("www.guokr.com".equals(hostString) || "m.guokr.com".equals(hostString)) && (segments != null && segments.size() >= 3)) {
                loadingView.setVisibility(View.VISIBLE);
                String sect = segments.get(0);
                switch (sect) {
                    case "article":
                        hostSection = SubItem.Section_Article;
                        load();
                        break;
                    case "post":
                        hostSection = SubItem.Section_Post;
                        load();
                        break;
                    default:
                        finish();
                        break;
                }
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void load() {
        Observable<UComment> observable = null;
        if (TextUtils.isEmpty(notice_id)) {
            switch (hostSection) {
                case SubItem.Section_Article:
                    observable = ArticleAPI.getSingleCommentFromRedirectUrl(redirectUri.toString());
                    break;
                case SubItem.Section_Post:
                    observable = PostAPI.getSingleCommentFromRedirectUrl(redirectUri.toString());
                    break;
            }
        } else {
            switch (hostSection) {
                case SubItem.Section_Article:
                    observable = ArticleAPI.getSingleCommentByNoticeID(notice_id);
                    break;
                case SubItem.Section_Post:
                    observable = PostAPI.getSingleCommentByNotice(notice_id);
                    break;
            }
        }

        if (observable == null) {
            return;
        }

        floatingActionsMenu.setVisibility(View.GONE);

        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UComment>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingView.onLoadFailed();
                    }

                    @Override
                    public void onNext(UComment uComment) {
                        floatingActionsMenu.setVisibility(View.VISIBLE);
                        loadingView.onLoadSuccess();
                        data = uComment;
                        if (UserAPI.getUserID().equals(data.getAuthor().getId())) {
                            deleteButton.setVisibility(View.VISIBLE);
                        } else {
                            deleteButton.setVisibility(View.GONE);
                        }
                        if (hostSection == SubItem.Section_Article) {
                            Article article = new Article();
                            article.setTitle(data.getHostTitle());
                            article.setId(data.getHostID());
                            host = article;
                        } else if (hostSection == SubItem.Section_Post) {
                            Post post = new Post();
                            post.setTitle(data.getHostTitle());
                            post.setId(data.getHostID());
                            host = post;
                        } else {
                            toast("Something Happened");
                        }
                        initData();
                    }
                });
    }

    private void initData() {
        if (host instanceof Post) {
            hostTitle.setText(((Post) host).getTitle());
        } else if (host instanceof Article) {
            hostTitle.setText(((Article) host).getTitle());
        }
        supportText.setText(String.valueOf(data.getLikeNum()));
        authorName.setText(data.getAuthor().getName());
        authorTitle.setText(data.getAuthor().getTitle());
        if (data.getAuthor().getId().equals(UserAPI.getUserID())) {
            deleteButton.setIcon(R.drawable.dustbin);
        } else {
            deleteButton.setIcon(R.drawable.dustbin_outline);
        }
        if (data.isHasLiked()) {
            likeButton.setIcon(R.drawable.heart);
        } else {
            likeButton.setIcon(R.drawable.heart_outline);
        }
        if (Config.shouldLoadImage()) {
            ImageLoader.getInstance().displayImage(data.getAuthor().getAvatar(), avatar, ImageUtils.avatarOptions);
        } else {
            avatar.setImageResource(R.drawable.default_avatar);
        }
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (authorLayout.getHeight() > 0) {
                    //noinspection deprecation
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    topBarHeight = appbar.getHeight() + hostTitle.getHeight();
                    headerHeight = topBarHeight + authorLayout.getHeight();
                    ViewGroup.LayoutParams params = headerHolder.getLayoutParams();
                    params.height = headerHeight;
                    scrollView.applyAutoHide(SingleReplyActivity.this, topBarHeight, autoHideListener);
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
        String html = StyleChecker.getAnswerHtml(data.getContent());
        int colorBack = getResources().getColor(R.color.webview_background);
        webView.setBackgroundColor(colorBack);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setPrimarySource(data.getContent());
        webView.loadDataWithBaseURL(Consts.Base_Url, html, "text/html", "charset=UTF-8", null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_answer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
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
                ObjectAnimator toolBarAnimator = ObjectAnimator.ofFloat(appbar, "translationY", appbar.getTranslationY(), 0f);
                ObjectAnimator titleAnimator = ObjectAnimator.ofFloat(hostTitle, "translationY", hostTitle.getTranslationY(), 0f);
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
                ObjectAnimator toolBarAnimator = ObjectAnimator.ofFloat(appbar, "translationY", appbar.getTranslationY(), -appbar.getBottom());
                ObjectAnimator titleAnimator = ObjectAnimator.ofFloat(hostTitle, "translationY", hostTitle.getTranslationY(), -hostTitle.getBottom());
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
                Intent intent = new Intent();
                if (host instanceof Article) {
                    intent.setClass(this, ArticleActivity.class);
                    intent.putExtra(Consts.Extra_Article, host);
                } else if (host instanceof Post) {
                    intent.setClass(this, PostActivity.class);
                    intent.putExtra(Consts.Extra_Post, host);
                }
                startOneActivity(intent);
                break;
            case R.id.layout_like:
                likeThis();
                break;
            case R.id.button_reply:
                replyThis();
                break;
            case R.id.button_Delete:
                deleteThis();
                break;
            case R.id.button_like:
                likeThis();
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

    private void replyThis() {
        Intent intent = new Intent(this, ReplyActivity.class);
        intent.putExtra(Consts.Extra_Ace_Model, host);
        if (data != null) {
            intent.putExtra(Consts.Extra_Simple_Comment, data);
        }
        startOneActivity(intent);
    }

    private void likeThis() {
        if (!UserAPI.isLoggedIn()) {
            gotoLogin();
            return;
        }
        if (data.isHasLiked()) {
            toastSingleton(R.string.has_liked_this);
            return;
        }
        RequestCallBack<Boolean> callBack = new SimpleCallBack<Boolean>() {
            @Override
            public void onFailure() {
                toast("点赞未遂");
            }

            @Override
            public void onSuccess() {
                data.setHasLiked(true);
                data.setLikeNum(data.getLikeNum() + 1);
                supportText.setText(String.valueOf(data.getLikeNum()));
                likeButton.setIcon(R.drawable.heart);
                toast("点赞成功");
            }
        };
        switch (hostSection) {
            case SubItem.Section_Article:
                ArticleAPI.likeComment(data.getID(), callBack);
                break;
            case SubItem.Section_Post:
                PostAPI.likeComment(data.getID(), callBack);
                break;
        }
    }

    private void deleteThis() {
        if (!UserAPI.isLoggedIn()) {
            gotoLogin();
            return;
        }
        RequestCallBack<Boolean> callBack = new SimpleCallBack<Boolean>() {
            @Override
            public void onFailure() {
                toastSingleton("未能删除");
            }

            @Override
            public void onSuccess() {
                finish();
            }
        };
        switch (hostSection) {
            case SubItem.Section_Article:
                ArticleAPI.deleteMyComment(data.getID(), callBack);
                break;
            case SubItem.Section_Post:
                PostAPI.deleteMyComment(data.getID(), callBack);
                break;
        }

    }

    @Override
    public void reload() {
        load();
    }
}
