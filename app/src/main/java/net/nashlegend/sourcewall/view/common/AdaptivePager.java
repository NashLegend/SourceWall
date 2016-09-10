package net.nashlegend.sourcewall.view.common;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import net.nashlegend.sourcewall.util.ErrorUtils;

/**
 * Created by NashLegend on 2015/10/16 0016.
 */
public class AdaptivePager extends ViewPager {
    public AdaptivePager(Context context) {
        super(context);
    }

    public AdaptivePager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ErrorUtils.onException(ex);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ErrorUtils.onException(ex);
        }
        return false;
    }
}
