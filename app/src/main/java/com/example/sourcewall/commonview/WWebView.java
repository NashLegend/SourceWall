package com.example.sourcewall.commonview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by NashLegend on 2015/1/4 0004
 * 用于显示内容的
 */
public class WWebView extends WebView {

    public WWebView(Context context) {
        super(context);
    }

    public WWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

}
