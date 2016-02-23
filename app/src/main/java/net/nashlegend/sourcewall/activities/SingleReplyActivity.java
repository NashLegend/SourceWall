package net.nashlegend.sourcewall.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.squareup.picasso.Picasso;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.commonview.AAsyncTask;
import net.nashlegend.sourcewall.commonview.IStackedAsyncTaskInterface;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.commonview.SScrollView;
import net.nashlegend.sourcewall.commonview.WWebView;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.request.api.ArticleAPI;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.ImageUtils;
import net.nashlegend.sourcewall.util.RoundTransformation;
import net.nashlegend.sourcewall.util.StyleChecker;

import java.util.ArrayList;
import java.util.List;

public class SingleReplyActivity extends SwipeActivity implements View.OnClickListener, LoadingView.ReloadListener {

    private View rootView;
    private View authorLayout;
    private Toolbar toolbar;
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
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
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
                        loadDataByUri();
                        break;
                    case "post":
                        hostSection = SubItem.Section_Post;
                        loadDataByUri();
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

    LoaderTask loaderTask;

    private void loadDataByUri() {
        if (loaderTask != null && loaderTask.getStatus() == AAsyncTask.Status.RUNNING) {
            loaderTask.cancel(true);
        }
        loaderTask = new LoaderTask(this);
        loaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, redirectUri);
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
            //TODO
            likeButton.setIcon(R.drawable.heart);
        } else {
            likeButton.setIcon(R.drawable.heart_outline);
        }
        if (Config.shouldLoadImage()) {
            ImageLoader.getInstance().displayImage(data.getAuthor().getAvatar(),avatar, ImageUtils.avatarOptions);
        } else {
            avatar.setImageResource(R.drawable.default_avatar);
        }
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (authorLayout.getHeight() > 0) {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    topBarHeight = toolbar.getHeight() + hostTitle.getHeight();
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
        Resources.Theme theme = getTheme();
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.color_webview_background});
        int colorBack = typedArray.getColor(0, 0);
        typedArray.recycle();
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
                ObjectAnimator toolBarAnimator = ObjectAnimator.ofFloat(toolbar, "translationY", toolbar.getTranslationY(), -toolbar.getBottom());
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
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, 0);
                break;
            case R.id.layout_opinion:
                likeThis();
                break;
            case R.id.button_reply:
                replyThis();
                break;
            case R.id.button_Delete:
                deleteThis();
                break;
            case R.id.button_thank:
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
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, 0);
    }

    private void likeThis() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            if (data.isHasLiked()) {
                toastSingleton(R.string.has_liked_this);
            } else {
                LikeTask task = new LikeTask(this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private void deleteThis() {
        if (!UserAPI.isLoggedIn()) {
            notifyNeedLog();
        } else {
            DeleteTask task = new DeleteTask(this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void reload() {
        loadDataByUri();
    }

    class LoaderTask extends AAsyncTask<Uri, Integer, ResponseObject<UComment>> {

        public LoaderTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected void onPreExecute() {
            floatingActionsMenu.setVisibility(View.GONE);
        }

        @Override
        protected ResponseObject<UComment> doInBackground(Uri... params) {
            ResponseObject<UComment> resultObject = new ResponseObject<>();
            if (TextUtils.isEmpty(notice_id)) {
                switch (hostSection) {
                    case SubItem.Section_Article:
                        resultObject = ArticleAPI.getSingleCommentFromRedirectUrl(redirectUri.toString());
                        break;
                    case SubItem.Section_Post:
                        resultObject = PostAPI.getSingleCommentFromRedirectUrl(redirectUri.toString());
                        break;
                }
            } else {
                switch (hostSection) {
                    case SubItem.Section_Article:
                        resultObject = ArticleAPI.getSingleCommentByNoticeID(notice_id);
                        break;
                    case SubItem.Section_Post:
                        resultObject = PostAPI.getSingleCommentByNoticeID(notice_id);
                        break;
                }
            }

            return resultObject;
        }

        @Override
        protected void onPostExecute(ResponseObject<UComment> result) {
            if (result.ok) {
                floatingActionsMenu.setVisibility(View.VISIBLE);
                loadingView.onLoadSuccess();
                data = result.result;
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
            } else {
                loadingView.onLoadFailed();
            }
        }
    }

    class LikeTask extends AAsyncTask<Void, Integer, ResponseObject> {

        public LikeTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected ResponseObject doInBackground(Void... params) {
            ResponseObject resultObject = new ResponseObject();
            switch (hostSection) {
                case SubItem.Section_Article:
                    resultObject = ArticleAPI.likeComment(data.getID());
                    break;
                case SubItem.Section_Post:
                    resultObject = PostAPI.likeComment(data.getID());
                    break;
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResponseObject resultObject) {
            if (resultObject.ok) {
                data.setHasLiked(true);
                data.setLikeNum(data.getLikeNum() + 1);
                supportText.setText(data.getLikeNum() + "");
                likeButton.setIcon(R.drawable.heart);
                toast("点赞成功");
            } else {
                toast("点赞未遂");
            }
        }
    }

    class DeleteTask extends AAsyncTask<Boolean, Integer, ResponseObject> {

        public DeleteTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected ResponseObject doInBackground(Boolean... params) {
            ResponseObject resultObject = new ResponseObject();
            switch (hostSection) {
                case SubItem.Section_Article:
                    resultObject = ArticleAPI.deleteMyComment(data.getID());
                    break;
                case SubItem.Section_Post:
                    resultObject = PostAPI.deleteMyComment(data.getID());
                    break;
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResponseObject resultObject) {
            if (resultObject.ok) {
                finish();
            } else {
                toastSingleton("操作失败");
            }

        }
    }
}
