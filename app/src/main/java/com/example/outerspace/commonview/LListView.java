package com.example.outerspace.commonview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Created by NashLegend on 2014/9/24 0024.
 */
public class LListView extends ListView implements OnScrollListener {

	public static final int State_Normal = 0;
	public static final int State_Pull_To_Refresh = 1;
	public static final int State_Release_To_Refresh = 2;
	public static final int State_Refreshing = 3;
	public static final int State_Loading_More = 4;

	private LListHeader headerView;
	private LListFooter footerView;
	int touchSlop = 16;// The last thing

	int headerHeight;

	private boolean refreshable = false;

	public LListView(Context context) {
		super(context);
	}

	public LListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		headerHeight = headerView.getHeight();
	}

	public LListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	float touchDownY;
	float lastY;
	float currentY;

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
				float dist = currentY - lastY;
				if (!headerView.handleMoveDistance(dist)) {
					return super.onTouchEvent(ev);
				}
				lastY = currentY;
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				headerView.handleOperationDone();
				break;

			default:
				break;
			}
			setPressed(false);
			setFocusable(false);
			setFocusableInTouchMode(false);
			// TODO 如果是return super.onTouchEvent呢
			return true;
		}
		return super.onTouchEvent(ev);
	}

	private int getState() {
		return headerView.getState();
	}

	private boolean checkRefreshable(MotionEvent ev) {
		View firstView = this.getChildAt(0);
		if (firstView != null) {
			int p = this.getFirstVisiblePosition();
			if (p == 0 && firstView.getTop() >= 0) {
				// TODO getTop有么有可能大于0，大于0的时候怎么办。
				refreshable = true;
			} else {
				refreshable = false;
			}
		} else {
			refreshable = true;
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
		public void onRefresh();

		public void onLoadMore();
	}

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		headerView.setOnRefreshListener(onRefreshListener);
	}
}
