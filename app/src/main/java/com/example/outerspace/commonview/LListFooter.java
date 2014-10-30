package com.example.outerspace.commonview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class LListFooter extends FrameLayout {
    private ObjectAnimator heightAnimator;
    private int footerHeight = 200;
    private boolean layouted = false;

    public LListFooter(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public LListFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public LListFooter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public void show() {
        animateToHeight(footerHeight);
    }

    public void hide() {
        animateToHeight(0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!layouted) {
            layouted = true;
            setHeight(0);
        }
    }

    public void cancelPotentialHeightAnimator() {
        if (heightAnimator != null) {
            heightAnimator.cancel();
        }
    }

    public void handleMoveDistance(float dist) {

    }

    public void animateToHeight(int height) {
        int duration = 100;
        cancelPotentialHeightAnimator();
        heightAnimator = ObjectAnimator.ofInt(this, "height", getHeight(), height);
        heightAnimator.setDuration(duration);
        heightAnimator.start();
    }

    /**
     * @param height
     */
    public void setHeight(int height) {
        if (height <= 0) {
            setVisibility(View.GONE);
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = 1;
            setLayoutParams(params);
        } else {
            if (getVisibility() != View.VISIBLE) {
                setVisibility(View.VISIBLE);
            }
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = height;
            setLayoutParams(params);
        }
    }

}
