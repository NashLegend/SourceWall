package com.example.outerspace.commonview;

import com.example.outerspace.R;
import com.example.outerspace.commonview.LListView.OnRefreshListener;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

public class LListHeader extends FrameLayout {
    private int currentState = LListView.State_Normal;
    private int lastState = currentState;
    private MarginLayoutParams layoutParams;
    private OnRefreshListener onRefreshListener;
    private TextView tvHint;

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
        layoutParams = (MarginLayoutParams) getLayoutParams();
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
        tvHint.setText(R.string.refreshing);
        onRefreshListener.onRefresh();
    }

    private void normal2Pull() {
        tvHint.setText(R.string.pull_to_refresh);
    }

    private void pull2Normal() {
        tvHint.setText(R.string.idling);
    }

    private void pull2Release() {
        tvHint.setText(R.string.release_to_refresh);
    }

    private void release2Pull() {
        tvHint.setText(R.string.pull_to_refresh);
    }

    private void release2Normal() {
        tvHint.setText(R.string.idling);
    }

    private void release2Refreshing() {
        tvHint.setText(R.string.refreshing);
        onRefreshListener.onRefresh();
    }

    private void refreshing2Refreshing() {
        //TODO
    }

    private void refreshing2Normal() {
        //
    }

    /**
     * 处理运动过程中的变化
     * 好了问题来了：挖掘机技术到底哪家强？
     * 不对打错了，假如说我想让header的高度是变化的，那么如果使用margin来控制的话无疑增加了麻烦的计算。
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
        getHeight();
        layoutParams.height += getHeight() + dist;
        setLayoutParams(layoutParams);
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
        return getHeight() > 300;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        // TODO Auto-generated method stub
        super.onLayout(changed, left, top, right, bottom);
        layoutParams = (MarginLayoutParams) getLayoutParams();
    }

    public boolean isVisible() {
        return getHeight() > 0;
    }

    public int getState() {
        return currentState;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

}
