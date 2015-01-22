package com.example.sourcewall.CommonView;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.sourcewall.util.UrlCheckUtil;

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
        setWebViewClient(client);//一旦设置了webViewClient，默认情况下链接就会在本页打开了……
        getSettings().setAllowFileAccess(true);
        getSettings().setAllowContentAccess(true);
        getSettings().setBlockNetworkImage(true);//暂时不加载图片，因为要延迟加载，只渲染文字还是比较快的
        getSettings().setDefaultTextEncodingName("UTF-8");
    }

    public void setExtWebViewClient(WebViewClient client) {
        extClient = client;
    }

    /**
     * 是否不加载图片
     *
     * @return
     */
    private boolean shouldInterceptImage() {
        return false;
    }

    WebViewClient extClient;
    WebViewClient client = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!shouldInterceptImage()) {
                //图片在此进行延迟加载
                getSettings().setBlockNetworkImage(false);
            }
            if (extClient != null) {
                extClient.onPageFinished(view, url);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            UrlCheckUtil.redirectRequest(url);
            if (extClient != null) {
                extClient.shouldOverrideUrlLoading(view, url);
            }
            return true;
        }
    };

}
