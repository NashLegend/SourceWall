package com.example.sourcewall.CommonView;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * Created by NashLegend on 2014/12/20 0020
 */
public class SScrollView extends ScrollView {
    public SScrollView(Context context) {
        super(context);
    }

    public SScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    float lastY = 0f;
    float currentY = 0f;
    int lastDirection = 0;
    int currentDirection = 0;

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (getScrollY() > headerHeight) {
            float tmpCurrentY = getScrollY();
            if (Math.abs(tmpCurrentY - lastY) > touchSlop) {
                currentY = tmpCurrentY;
                currentDirection = (int) (currentY - lastY);
                if (lastDirection != currentDirection) {
                    if (currentDirection > 0) {
                        animateHide();
                    } else {
                        animateBackFooter();
                    }
                }
                lastY = currentY;
            }
        } else {
            animateBack();
        }
    }

    int touchSlop = 10;
    int headerHeight;
    AutoHideListener autoHideListener;

    public static interface AutoHideListener {
        void animateHide();

        void animateBack();

        void animateBackFooter();
    }

    public void applyAutoHide(Context context, int headerHeight, AutoHideListener autoHideListener) {
        touchSlop = (int) (ViewConfiguration.get(context).getScaledTouchSlop() * 0.9);
        this.autoHideListener = autoHideListener;
        this.headerHeight = headerHeight;
        this.setOnTouchListener(onTouchListener);
    }

    private void animateBack() {
        if (autoHideListener != null) {
            autoHideListener.animateBack();
        }
    }

    private void animateBackFooter() {
        if (autoHideListener != null) {
            autoHideListener.animateBackFooter();
        }
    }

    private void animateHide() {
        if (autoHideListener != null) {
            autoHideListener.animateHide();
        }
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = getScrollY();
                    currentY = getScrollY();
                    currentDirection = 0;
                    break;
                case MotionEvent.ACTION_MOVE:

                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    currentDirection = 0;
                    break;
            }
            return false;
        }
    };
}
