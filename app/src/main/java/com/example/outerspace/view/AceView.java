package com.example.outerspace.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.example.outerspace.model.AceModel;

/**
 * Created by NashLegend on 2014/9/19 0019.
 */
public abstract class AceView extends FrameLayout {

    public AceView(Context context) {
        super(context);
    }

    public AceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public abstract void setData(AceModel model);
}
