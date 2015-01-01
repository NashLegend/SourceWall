package com.example.sourcewall.commonview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by NashLegend on 2015/1/1 0001
 * 用于管理那些要分页加载不能连在一起的列表
 */
public class PageController extends FrameLayout{
    public PageController(Context context) {
        super(context);
    }

    public PageController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PageController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
