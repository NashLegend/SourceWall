package com.example.sourcewall.commonview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.commonview.LListView.OnRefreshListener;

public class LListHeader extends FrameLayout {
    private int currentState = LListView.State_Normal;
    private int lastState = currentState;
    private OnRefreshListener onRefreshListener;
    private TextView tvHint;
    private ObjectAnimator heightAnimator;
    private int Refreshing_Height = 200;
    private int Release_Height = 300;
    private boolean layouted = false;

    public LListHeader(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_header_view, this);
        tvHint = (TextView) findViewById(R.id.text_header_hint);
    }

    private void checkStateChange() {
        if (lastState != currentState) {
            // 在move状态下只有这两种可能
            if (currentState == LListView.State_Pull_To_Refresh) {
                //有可能来自下拉从LListView.State_Normal变来也有可能来自上滑从State_Release_To_Refresh变来
            } else if (currentState == LListView.State_Release_To_Refresh) {
                //只有可能从State_Pull_To_Refresh变来
            }
        }
    }

    public boolean handleMoveDistance(float dist) {
        if (dist < 0 && !isVisible()) {
            return false;
        }
        float rat = 1.5f;
        dist /= rat;
        handleMotion(dist);
        if (currentState != LListView.State_Refreshing) {
            // 这时只有两种可能的状态State_Release_To_Refresh和State_Pull_To_Refresh
            // TODO 或许可以做更多
            if (isOverReleaseThreshold()) {
                currentState = LListView.State_Release_To_Refresh;
            } else {
                currentState = LListView.State_Pull_To_Refresh;
            }
        }

        //这段表示状态之间的转换，不涉及状态内动作
        if (lastState != currentState) {
            // 在move状态下只有这两种可能
            if (currentState == LListView.State_Pull_To_Refresh) {
                //有可能来自下拉从LListView.State_Normal变来也有可能来自上滑从State_Release_To_Refresh变来
                if (lastState == LListView.State_Normal) {
                    normal2Pull();
                } else if (lastState == LListView.State_Release_To_Refresh) {
                    release2Pull();
                }
            } else if (currentState == LListView.State_Release_To_Refresh) {
                //只有可能从State_Pull_To_Refresh变来
                pull2Release();
            }
        }
        lastState = currentState;
        return true;
    }

    public void handleOperationDone() {
        if (currentState == LListView.State_Release_To_Refresh) {
            // TODO start refresh
            if (onRefreshListener != null) {
                release2Refreshing();
            } else {
                release2Normal();
            }
        } else if (currentState == LListView.State_Pull_To_Refresh) {
            pull2Normal();
        } else if (currentState == LListView.State_Refreshing) {
            // TODO 这里的值应该是动画的正常高度，是在初始化时就确定的
            if (getHeight() > 300) {
                refreshing2Refreshing();
            }
        }
        checkStateChange();
    }

    private void normal2Refreshing() {
        animateToHeight(Refreshing_Height);
        tvHint.setText(R.string.refreshing);
        currentState = LListView.State_Refreshing;
        onRefreshListener.onStartRefresh();
    }

    private void normal2Pull() {
        tvHint.setText(R.string.pull_to_refresh);
    }

    private void pull2Normal() {
        currentState = LListView.State_Normal;
        tvHint.setText(R.string.idling);
        animateToHeight(0);
    }

    private void pull2Release() {
        currentState = LListView.State_Release_To_Refresh;
        tvHint.setText(R.string.release_to_refresh);
    }

    private void release2Pull() {
        currentState = LListView.State_Pull_To_Refresh;
        tvHint.setText(R.string.pull_to_refresh);
    }

    private void release2Normal() {
        currentState = LListView.State_Normal;
        tvHint.setText(R.string.idling);
        animateToHeight(0);
    }

    private void release2Refreshing() {
        currentState = LListView.State_Refreshing;
        animateToHeight(Refreshing_Height);
        tvHint.setText(R.string.refreshing);
        onRefreshListener.onStartRefresh();
    }

    private void refreshing2Refreshing() {
        //TODO
        animateToHeight(Refreshing_Height);
    }

    private void refreshing2Normal() {
        //
        currentState = LListView.State_Normal;
        tvHint.setText(R.string.idling);
        animateToHeight(0);
    }

    /**
     * 处理运动过程中的变化
     * 假如说我想让header的高度是变化的，那么如果使用margin来控制的话无疑增加了麻烦的计算。
     * 所以呢最好的方式是不用margin，而是使用直接改变高度的方式
     *
     * @param dist
     */
    public void handleMotion(float dist) {
        switch (currentState) {
            //TODO
            case LListView.State_Pull_To_Refresh:

                break;
            case LListView.State_Release_To_Refresh:

                break;
            case LListView.State_Refreshing:

                break;
        }
        setHeight((int) (getHeight() + dist));
    }

    public void cancelRefresh() {
        refreshing2Normal();
    }

    public void doneRefreshing() {
        refreshing2Normal();
    }

    /**
     * 直接开始刷新，当然前提是当前状态是State_Normal，状态的检测由LListView负责
     */
    public void directlyStartRefresh() {
        normal2Refreshing();
    }

    private boolean isOverReleaseThreshold() {
        //这里需要在初始化的时候就确定这个Threshold，TODO
        return getHeight() > Release_Height;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        // TODO Auto-generated method stub
        super.onLayout(changed, left, top, right, bottom);
        if (!layouted) {
            layouted = true;
            setHeight(0);
        }
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public int getState() {
        return currentState;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void cancelPotentialHeightAnimator() {
        if (heightAnimator != null) {
            heightAnimator.cancel();
        }
    }

    public void animateToHeight(int height) {
        int duration = 300;
        cancelPotentialHeightAnimator();
        heightAnimator = ObjectAnimator.ofInt(this, "height", getHeight(), height);
        heightAnimator.setDuration(duration);
        heightAnimator.start();
    }

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
