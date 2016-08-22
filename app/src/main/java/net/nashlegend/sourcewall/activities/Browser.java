package net.nashlegend.sourcewall.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.util.ErrorUtils;
import net.nashlegend.sourcewall.util.UiUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Browser extends BaseActivity {

    private static final String WEB_PAGE_URL = "extra.web.page.url";
    private static final String WEB_PAGE_TITLE = "extra.web.page.title";

    @BindView(R.id.action_bar)
    Toolbar toolbar;
    @BindView(R.id.web_content)
    WebView webView;
    String url = "";
    String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        url = getIntent().getStringExtra(WEB_PAGE_URL);
        title = getIntent().getStringExtra(WEB_PAGE_TITLE);

        UiUtil.ensureWebView(webView);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setTitleText(view.getTitle());
            }
        });
        if (TextUtils.isEmpty(url) || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            finish();
        } else {
            webView.loadUrl(url);
            setTitle(title);
        }
    }


    @Override
    protected void onDestroy() {
        try {
            if (webView != null) {
                webView.stopLoading();
            }
        } catch (Exception e) {
            ErrorUtils.onException(e);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public static void open(String url, String title, Context context) {
        if (UiUtil.shouldThrottle()) {
            return;
        }
        if (context == null) {
            context = App.getApp();
        }
        Intent intent = new Intent(context, Browser.class);
        intent.putExtra(WEB_PAGE_TITLE, title);
        intent.putExtra(WEB_PAGE_URL, url);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public void setTitleText(String title) {
        if (getActionBar() != null) {
            getActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
