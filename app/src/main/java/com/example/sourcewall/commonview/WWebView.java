package com.example.sourcewall.commonview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.sourcewall.AppApplication;

/**
 * Created by NashLegend on 2015/1/4 0004
 * 用于显示内容的
 */
public class WWebView extends WebView {

    public WWebView(Context context) {
        super(context);
        init();
    }

    public WWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setWebViewClient(webViewClient);//一旦设置了webViewClient，默认情况下链接就会在本页打开了……
        getSettings().setAllowFileAccess(true);
        getSettings().setAllowContentAccess(true);
        getSettings().setBlockNetworkImage(true);//暂时不加载图片，因为要延迟加载，只渲染文字还是比较快的
        getSettings().setDefaultTextEncodingName("UTF-8");
    }

    /**
     * 是否不加载图片
     *
     * @return
     */
    private boolean shouldInterceptImage() {
        return false;
    }

    /**
     * 是否拦截点击的链接
     *
     * @param url
     * @return
     */
    private boolean shouldRedirectRequest(String url) {
        return false;
    }

    WebViewClient webViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!shouldInterceptImage()) {
                //图片在此进行延迟加载
                getSettings().setBlockNetworkImage(false);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (shouldRedirectRequest(url)) {
                // TODO，跳转到界面，比如PostActivity等
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppApplication.getApplication().startActivity(intent);
            }
            return true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            //非UI线程执行
            return super.shouldInterceptRequest(view, url);
        }
    };

}
