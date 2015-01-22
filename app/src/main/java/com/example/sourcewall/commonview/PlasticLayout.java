package com.example.sourcewall.CommonView;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by NashLegend on 2015/1/22 0022
 */
public class PlasticLayout extends RelativeLayout {
    public PlasticLayout(Context context) {
        super(context);
    }

    public PlasticLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlasticLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlasticLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public float getYFraction() {
        final int height = getHeight();
        if (height == 0)
            return 0;
        return getTranslationY() / height;
    }

    public void setYFraction(float yFraction) {
        setTranslationY(yFraction * getHeight());
    }
}
