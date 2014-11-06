package com.example.sourcewall.commonview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

/**
 * Created by NashLegend on 2014/9/24 0024
 */
public class LListView extends ListView implements OnScrollListener {

    public static final int State_Normal = 0;
    public static final int State_Pull_Down_To_Refresh = 1;
    public static final int State_Release_To_Refresh = 2;
    public static final int State_Refreshing = 3;
    public static final int State_Pull_Up_To_Load_More = 4;
    public static final int State_Release_To_Load_More = 5;
    public static final int State_Loading_More = 6;

    private boolean refreshable = true;
    private boolean loadable = false;

    public boolean isRefreshable() {
        return refreshable;
    }

    public void setRefreshable(boolean refreshable) {
        this.refreshable = refreshable;
    }

    public boolean isLoadable() {
        return loadable;
    }

    public void setLoadable(boolean loadable) {
        this.loadable = loadable;
    }

    private LListHeader headerView;
    private LListFooter footerView;
    int touchSlop = 32;// The last thing

    private int loadingMoreThreshold = 10;//低于此自动不显示

    public int getLoadingMoreThreshold() {
        return loadingMoreThreshold;
    }

    public void setLoadingMoreThreshold(int loadingMoreThreshold) {
        this.loadingMoreThreshold = loadingMoreThreshold;
    }

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

    private void cancelRefreshing() {
        headerView.cancelRefresh();
    }

    private void doneRefreshing() {
        headerView.doneRefreshing();
    }

    public void startRefreshing() {
        if (getState() == State_Loading_More) {
            cancelLoadingMore();
        }
        if (getState() == State_Normal) {
            headerView.directlyStartRefresh();
        }
    }

    private void cancelLoadingMore() {

    }

    private void doneLoadingMore() {

    }

    public void startLoadingMore() {
        if (getState() == State_Refreshing) {
            cancelRefreshing();
        }
        if (getState() == State_Normal) {
            footerView.directlyStartLoading();
        }
    }

    private void addViews(Context context) {
        touchSlop = (int) (ViewConfiguration.get(getContext()).getScaledTouchSlop() * 1.5);
        headerView = new LListHeader(context);
        addHeaderView(headerView);
        footerView = new LListFooter(context);
        addFooterView(footerView);
    }

    float touchDownY;
    float lastY = -1;
    float currentY;
    boolean dragging = false;
    boolean pullingDown = false;
    float handMoveThreshold = 1.5f;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (checkRefreshable(ev)) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchDownY = ev.getY();
                    currentY = touchDownY;
                    lastY = currentY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    currentY = ev.getY();
                    if (dragging) {
                        float dist = currentY - lastY;
                        if (Math.abs(dist) > handMoveThreshold) {
                            if (!headerView.handleMoveDistance(dist)) {
                                pullingDown = false;
                                return super.onTouchEvent(ev);
                            } else {
                                pullingDown = true;
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
                    headerView.handleOperationDone();
                    pullingDown = false;
                    dragging = false;
                    break;
                default:
                    break;
            }
            if (ev.getAction() == MotionEvent.ACTION_MOVE && pullingDown) {
                return true;
            } else {
                return super.onTouchEvent(ev);
            }
        }
        return super.onTouchEvent(ev);
    }

    private void handleHeaderMotion(MotionEvent ev) {

    }

    private int getState() {
        return headerView.getState() == State_Normal ? footerView.getState() : headerView.getState();
    }

    private boolean checkRefreshable(MotionEvent ev) {
        return refreshable && getChildAt(0) instanceof LListHeader;
    }

    private boolean checkLoadable(MotionEvent ev) {
        return loadable && this.getChildAt(getChildCount() - 1) instanceof LListFooter;
    }

    private OnScrollListener outerListener;

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        outerListener = l;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (outerListener != null) {
            outerListener.onScrollStateChanged(view, scrollState);
        }
        if (scrollState == SCROLL_STATE_IDLE) {
            //TODO if last
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (outerListener != null) {
            outerListener.onScroll(view, firstVisibleItem, visibleItemCount,
                    totalItemCount);
        }
    }

    public static interface OnRefreshListener {
        public void onStartRefresh();

        public void onStartLoadMore();
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        headerView.setOnRefreshListener(onRefreshListener);
    }
}
