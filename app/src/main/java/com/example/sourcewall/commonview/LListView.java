package com.example.sourcewall.commonview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ListView;

/**
 * Created by NashLegend on 2014/9/24 0024
 */
public class LListView extends ListView {
    public static final int State_Normal = 0;
    public static final int State_Pull_Down_To_Refresh = 1;
    public static final int State_Release_To_Refresh = 2;
    public static final int State_Refreshing = 3;
    public static final int State_Pull_Up_To_Load_More = 4;
    public static final int State_Release_To_Load_More = 5;
    public static final int State_Loading_More = 6;

    private boolean canPullToRefresh = false;
    private boolean canPullToLoadMore = false;

    public boolean canPullToRefresh() {
        return canPullToRefresh;
    }

    public void setCanPullToRefresh(boolean canPullToRefresh) {
        this.canPullToRefresh = canPullToRefresh;
    }

    public boolean canPullToLoadMore() {
        return canPullToLoadMore;
    }

    public void setCanPullToLoadMore(boolean canPullToLoadMore) {
        this.canPullToLoadMore = canPullToLoadMore;
    }

    private LListHeader headerView;
    private LListFooter footerView;
    int touchSlop = 32;// The last thing

    public LListView(Context context) {
        super(context);
        addViews(context);
    }

    public LListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addViews(context);
    }

    public LListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addViews(context);
    }

    public void doneOperation() {
        doneRefreshing();
        doneLoadingMore();
    }

    public void startRefreshing() {
        if (getState() == State_Loading_More) {
            doneLoadingMore();
        }
        if (getState() == State_Normal) {
            headerView.directlyStartRefresh();
        }
    }

    private void doneRefreshing() {
        headerView.doneRefreshing();
    }

    public void startLoadingMore() {
        if (getState() == State_Refreshing) {
            doneRefreshing();
        }
        if (getState() == State_Normal) {
            footerView.directlyStartLoading();
        }
    }

    private void doneLoadingMore() {
        footerView.doneLoading();
    }

    private void addViews(Context context) {
        touchSlop = (int) (ViewConfiguration.get(getContext()).getScaledTouchSlop() * 1.5);
        headerView = new LListHeader(context);
        footerView = new LListFooter(context);
        addHeaderView(headerView);
        addFooterView(footerView);
    }

    float touchDownY;
    float lastY = -1;
    float currentY;
    boolean dragging = false;
    boolean pullingDown = false;
    boolean pullingUp = false;
    float handMoveThreshold = 1.5f;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (listener != null) {
            float dist = 0f;
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchDownY = ev.getY();
                    currentY = touchDownY;
                    lastY = currentY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    currentY = ev.getY();
                    if (dragging) {
                        dist = currentY - lastY;
                        if (Math.abs(dist) > handMoveThreshold) {
                            if (!pullingUp && checkRefreshable()) {
                                if (headerView.handleMoveDistance(dist)) {
                                    pullingDown = true;
                                } else {
                                    pullingDown = false;
                                }
                            }
                            if (!pullingDown && checkLoadable()) {
                                if (footerView.handleMoveDistance(dist)) {
                                    pullingUp = true;
                                } else {
                                    pullingUp = false;
                                }
                            }
                            lastY = currentY;
                        }
                    } else {
                        if (Math.abs(currentY - touchDownY) > touchSlop) {
                            dragging = true;
                            lastY = currentY;
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (pullingDown) {
                        headerView.handleUpOperation();
                        pullingDown = false;
                    }
                    if (pullingUp) {
                        footerView.handleUpOperation();
                        pullingUp = false;
                    }
                    dragging = false;
                    break;
                default:
                    break;
            }
            if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                if (pullingDown || (pullingUp && dist > 0)) {
                    return true;
                }
                if (pullingUp && dist < 0) {
                    smoothScrollBy((int) (-dist), 0);
                    lastY = currentY;
                    return true;
                }
            }
        }
        return super.onTouchEvent(ev);
    }

    private int getState() {
        return headerView.getState() == State_Normal ? footerView.getState() : headerView.getState();
    }

    private boolean checkRefreshable() {
        return canPullToRefresh && getChildAt(0) instanceof LListHeader;
    }

    private boolean checkLoadable() {
        return canPullToLoadMore && this.getChildAt(getChildCount() - 1) instanceof LListFooter;
    }

    public static interface OnRefreshListener {
        public void onStartRefresh();

        public void onStartLoadMore();
    }

    OnRefreshListener listener;

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.listener = onRefreshListener;
        headerView.setOnRefreshListener(onRefreshListener);
        footerView.setOnRefreshListener(onRefreshListener);
    }
}
