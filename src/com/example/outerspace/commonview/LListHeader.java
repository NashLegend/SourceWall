package com.example.outerspace.commonview;

import com.example.outerspace.commonview.LListView.OnRefreshListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;

public class LListHeader extends FrameLayout {

	private int currentState = LListView.State_Normal;
	private int lastState = currentState;
	private MarginLayoutParams layoutParams;
	private OnRefreshListener onRefreshListener;

	public LListHeader(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public LListHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public LListHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void switch2Refreshing() {
		// TODO
	}

	public void switch2PullToRefresh() {
		// TODO
	}

	public void switch2ReleaseToRefresh() {
		// TODO
	}

	private void checkStateChange() {
		if (lastState != currentState) {
			// 或许不应该这样，或许应该把motion状态算进来，明天再说……
		}
	}

	public boolean handleMoveDistance(float dist) {
		layoutParams = (MarginLayoutParams) getLayoutParams();
		if (dist < 0 && !isVisible()) {
			return false;
		}
		layoutParams.topMargin += layoutParams.topMargin + dist;
		setLayoutParams(layoutParams);
		if (currentState != LListView.State_Refreshing) {
			// 这时只有两种可能的状态State_Release_To_Refresh和State_Pull_To_Refresh
			// TODO 或许可以做更多
			if (layoutParams.topMargin > 0) {
				currentState = LListView.State_Release_To_Refresh;
			} else {
				currentState = LListView.State_Pull_To_Refresh;
			}
		}
		checkStateChange();
		return true;
	}

	public void handleOperationDone() {
		layoutParams = (MarginLayoutParams) getLayoutParams();
		if (currentState == LListView.State_Release_To_Refresh) {
			// TODO start refresh
			if (onRefreshListener != null) {
				animateToRefreshing();
				onRefreshListener.onRefresh();
			} else {
				animateToNormal();
			}
		} else if (currentState == LListView.State_Pull_To_Refresh) {
			animateToNormal();
		} else if (currentState == LListView.State_Refreshing) {
			if (layoutParams.topMargin >= 0) {
				animateToRefreshing();
			}
		}
		checkStateChange();
	}

	private void animateToRefreshing() {
		// TODO Auto-generated method stub

	}

	private void animateToNormal() {
		// TODO Auto-generated method stub

	}

	public void handleMotion() {

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
	}

	public boolean isVisible() {
		layoutParams = (MarginLayoutParams) getLayoutParams();
		return layoutParams.topMargin <= getHeight();
	}

	public int getState() {
		return currentState;
	}

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
	}

}
