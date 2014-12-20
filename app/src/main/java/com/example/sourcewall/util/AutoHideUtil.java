package com.example.sourcewall.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/12/17 0017
 */
public class AutoHideUtil {
    public static void applyListViewAutoHide(Context context, ListView listView, View header, View footer, int headerHeight) {
        ListViewAutoHideTool tool = new ListViewAutoHideTool();
        tool.applyAutoHide(context, listView, header, footer, headerHeight);
    }

    private static class ListViewAutoHideTool {

        View header;
        View footer;
        ListView listView;
        int touchSlop = 10;

        public ListViewAutoHideTool() {

        }

        public void applyAutoHide(Context context, ListView listView, View header, View footer, int headerHeight) {
            touchSlop = (int) (ViewConfiguration.get(context).getScaledTouchSlop() * 0.9);
            this.listView = listView;
            this.header = header;
            this.footer = footer;

            header = new View(listView.getContext());
            header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, headerHeight));
            header.setBackgroundColor(Color.parseColor("#00000000"));
            listView.addHeaderView(header);

            listView.setOnScrollListener(onScrollListener);
            listView.setOnTouchListener(onTouchListener);
        }


        AnimatorSet backAnimatorSet;

        private void animateBack() {
            if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {
                hideAnimatorSet.cancel();
            }
            if (backAnimatorSet != null && backAnimatorSet.isRunning()) {

            } else {
                backAnimatorSet = new AnimatorSet();
                ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(header, "translationY", header.getTranslationY(), 0f);
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(footer, "translationY", footer.getTranslationY(), 0f);
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(headerAnimator);
                animators.add(footerAnimator);
                backAnimatorSet.setDuration(300);
                backAnimatorSet.playTogether(animators);
                backAnimatorSet.start();
            }
        }

        AnimatorSet hideAnimatorSet;

        private void animateHide() {
            if (backAnimatorSet != null && backAnimatorSet.isRunning()) {
                backAnimatorSet.cancel();
            }
            if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {

            } else {
                hideAnimatorSet = new AnimatorSet();
                ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(header, "translationY", header.getTranslationY(), -header.getHeight());
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(footer, "translationY", footer.getTranslationY(), footer.getHeight());
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(headerAnimator);
                animators.add(footerAnimator);
                hideAnimatorSet.setDuration(300);
                hideAnimatorSet.playTogether(animators);
                hideAnimatorSet.start();
            }
        }

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {

            float lastY = 0f;
            float currentY = 0f;
            int lastDirection = 0;
            int currentDirection = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastY = event.getY();
                        currentY = event.getY();
                        currentDirection = 0;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (listView.getFirstVisiblePosition() > 1) {
                            float tmpCurrentY = event.getY();
                            if (Math.abs(tmpCurrentY - lastY) > touchSlop) {
                                currentY = tmpCurrentY;
                                currentDirection = (int) (currentY - lastY);
                                if (lastDirection != currentDirection) {
                                    if (currentDirection < 0) {
                                        animateHide();
                                    } else {
                                        animateBack();
                                    }
                                }
                                lastY = currentY;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        currentDirection = 0;
                        break;
                }
                return false;
            }
        };

        AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
            int lastPosition = 0;
            int state = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                state = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0 || firstVisibleItem == 1) {
                    animateBack();
                }
                if (firstVisibleItem > 1) {
                    if (firstVisibleItem > lastPosition && state == SCROLL_STATE_FLING) {
                        animateHide();
                    }
                }
                lastPosition = firstVisibleItem;
            }
        };
    }
}
