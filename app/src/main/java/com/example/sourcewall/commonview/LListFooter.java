package com.example.sourcewall.commonview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.sourcewall.R;

public class LListFooter extends FrameLayout {
    private int currentState = LListView.State_Normal;
    private int lastState = currentState;
    private LListView.OnRefreshListener onRefreshListener;
    private TextView tvHint;
    private ObjectAnimator heightAnimator;
    private int Loading_Height = 200;
    private int Release_Height = 300;
    private boolean layouted = false;

    public LListFooter(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_footer_view, this);
        tvHint = (TextView) findViewById(R.id.text_footer_hint);
    }

    private void checkStateChange() {
        if (lastState != currentState) {
            // 在move状态下只有这两种可能
            if (currentState == LListView.State_Pull_Up_To_Load_More) {
                //有可能来自下拉从LListView.State_Normal变来也有可能来自上滑从State_Release_To_Load_More变来
            } else if (currentState == LListView.State_Release_To_Load_More) {
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
        if (currentState != LListView.State_Loading_More) {
            // 这时只有两种可能的状态State_Release_To_Load_More和State_Pull_To_Refresh
            // TODO 或许可以做更多
            if (isOverReleaseThreshold()) {
                currentState = LListView.State_Release_To_Load_More;
            } else {
                currentState = LListView.State_Pull_Up_To_Load_More;
            }
        }

        //这段表示状态之间的转换，不涉及状态内动作
        if (lastState != currentState) {
            // 在move状态下只有这两种可能
            if (currentState == LListView.State_Pull_Up_To_Load_More) {
                //有可能来自下拉从LListView.State_Normal变来也有可能来自上滑从State_Release_To_Load_More变来
                if (lastState == LListView.State_Normal) {
                    normal2Pull();
                } else if (lastState == LListView.State_Release_To_Load_More) {
                    release2Pull();
                }
            } else if (currentState == LListView.State_Release_To_Load_More) {
                //只有可能从State_Pull_Up_To_Load_More变来
                pull2Release();
            }
        }
        lastState = currentState;
        return true;
    }

    public void handleOperationDone() {
        if (currentState == LListView.State_Release_To_Load_More) {
            // TODO start loading more
            if (onRefreshListener != null) {
                release2Loading();
            } else {
                release2Normal();
            }
        } else if (currentState == LListView.State_Pull_Up_To_Load_More) {
            pull2Normal();
        } else if (currentState == LListView.State_Loading_More) {
            // TODO 这里的值应该是动画的正常高度，是在初始化时就确定的
            if (getHeight() > 300) {
                loading2Loading();
            }
        }
        checkStateChange();
    }

    private void normal2Refreshing() {
        animateToHeight(Loading_Height);
        tvHint.setText(R.string.loading);
        currentState = LListView.State_Loading_More;
        onRefreshListener.onStartLoadMore();
    }

    private void normal2Pull() {
        tvHint.setText(R.string.pull_up_to_load_more);
    }

    private void pull2Normal() {
        currentState = LListView.State_Normal;
        tvHint.setText(R.string.idling);
        animateToHeight(0);
    }

    private void pull2Release() {
        currentState = LListView.State_Release_To_Load_More;
        tvHint.setText(R.string.release_to_load_more);
    }

    private void release2Pull() {
        currentState = LListView.State_Pull_Up_To_Load_More;
        tvHint.setText(R.string.pull_up_to_load_more);
    }

    private void release2Normal() {
        currentState = LListView.State_Normal;
        tvHint.setText(R.string.idling);
        animateToHeight(0);
    }

    private void release2Loading() {
        currentState = LListView.State_Loading_More;
        animateToHeight(Loading_Height);
        tvHint.setText(R.string.loading);
        onRefreshListener.onStartLoadMore();
    }

    private void loading2Loading() {
        //TODO
        animateToHeight(Loading_Height);
    }

    private void loading2Normal() {
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
            case LListView.State_Pull_Up_To_Load_More:

                break;
            case LListView.State_Release_To_Load_More:

                break;
            case LListView.State_Loading_More:

                break;
        }
        setHeight((int) (getHeight() + dist));
    }

    public void cancelRefresh() {
        loading2Normal();
    }

    public void doneRefreshing() {
        loading2Normal();
    }

    /**
     * 直接开始刷新，当然前提是当前状态是State_Normal，状态的检测由LListView负责
     */
    public void directlyStartLoading() {
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

    public void setOnRefreshListener(LListView.OnRefreshListener onRefreshListener) {
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
