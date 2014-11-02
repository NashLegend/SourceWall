package com.example.sourcewall.commonview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

/**
 * Created by NashLegend on 2014/9/24 0024
 */
public class LListView extends ListView implements OnScrollListener {

    public static final int State_Normal = 0;
    public static final int State_Pull_To_Refresh = 1;
    public static final int State_Release_To_Refresh = 2;
    public static final int State_Refreshing = 3;
    public static final int State_Loading_More = 4;

    private boolean can_pull_to_refresh = true;
    private boolean can_auto_load_more = false;

    public boolean isCan_pull_to_refresh() {
        return can_pull_to_refresh;
    }

    public void setCan_pull_to_refresh(boolean can_pull_to_refresh) {
        this.can_pull_to_refresh = can_pull_to_refresh;
    }

    public boolean isCan_auto_load_more() {
        return can_auto_load_more;
    }

    public void setCan_auto_load_more(boolean can_auto_load_more) {
        this.can_auto_load_more = can_auto_load_more;
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


    private boolean refreshable = false;

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

    public void cancelRefresh() {
        headerView.cancelRefresh();
    }

    public void doneRefreshing() {
        headerView.doneRefreshing();
    }

    public void startRefresh() {
        if (getState() == State_Loading_More) {
            //TODO
        }
        if (getState() == State_Normal) {
            headerView.directlyStartRefresh();
        }
    }

    public void doneLoadingMore() {

    }

    private void addViews(Context context) {
        touchSlop = (int) (ViewConfiguration.get(getContext()).getScaledTouchSlop() * 1.5);
        headerView = new LListHeader(context);
        addHeaderView(headerView);
    }

    float touchDownY;
    float lastY = -1;
    float currentY;
    boolean dragging = false;
    boolean pulling = false;
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
                                pulling = false;
                                return super.onTouchEvent(ev);
                            } else {
                                pulling = true;
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
                    pulling = false;
                    dragging = false;
                    break;
                default:
                    break;
            }
            if (ev.getAction() == MotionEvent.ACTION_MOVE && pulling) {
                return true;
            } else {
                return super.onTouchEvent(ev);
            }
        }
        return super.onTouchEvent(ev);
    }

    private int getState() {
        return headerView.getState();
    }

    private boolean checkRefreshable(MotionEvent ev) {
        if (can_pull_to_refresh) {
            View firstView = this.getChildAt(0);
            if (firstView != null) {
                int p = this.getFirstVisiblePosition();
                if (p == 0 && firstView.getTop() >= 0) {
                    refreshable = true;
                } else {
                    refreshable = false;
                }
            } else {
                refreshable = true;
            }
        } else {
            refreshable = false;
        }
        return refreshable;
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

        if (firstVisibleItem + visibleItemCount == totalItemCount) {
            if (footerView.isVisible()) {
                footerView.show();
            }
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
