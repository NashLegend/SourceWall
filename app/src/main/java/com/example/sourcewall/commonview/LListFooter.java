package com.example.sourcewall.commonview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.util.DisplayUtil;

public class LListFooter extends FrameLayout {
    private int currentState = LListView.State_Normal;
    private int lastState = currentState;
    private LListView.OnRefreshListener onRefreshListener;
    private TextView tvHint;
    private ObjectAnimator heightAnimator;
    private int Loading_Height_In_DP = 55;
    private int Release_Height_In_DP = 80;
    private int Loading_Height = 200;
    private int Release_Height = 300;
    private boolean layouted = false;

    //TODO 删掉，改成自动加载
    public LListFooter(Context context) {
        super(context);
        Release_Height = (int) (DisplayUtil.getPixelDensity(context) * Release_Height_In_DP);
        Loading_Height = (int) (DisplayUtil.getPixelDensity(context) * Loading_Height_In_DP);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_footer_view, this);
        tvHint = (TextView) findViewById(R.id.text_footer_hint);

    }

    protected boolean handleMoveDistance(float dist) {
        dist *= -1;
        if (dist < 0 && !isVisible()) {
            return false;
        }
        float rat = 1.5f;
        dist /= rat;
        handleMotion(dist);
        if (currentState != LListView.State_Loading_More) {
            // TODO 或许可以做更多
            if (isVisible()) {
                if (isOverReleaseThreshold()) {
                    currentState = LListView.State_Release_To_Load_More;
                } else {
                    currentState = LListView.State_Pull_Up_To_Load_More;
                }
            } else {
                currentState = LListView.State_Normal;
            }
        }

        //这段表示状态之间的转换，不涉及状态内动作
        if (lastState != currentState) {
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

    protected void handleUpOperation() {
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
            if (getHeight() > Release_Height) {
                loading2Loading();
            }
        }
        lastState = LListView.State_Normal;
    }

    private void normal2Loading() {
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
        animateToHeight(Loading_Height);
    }

    private void loading2Normal() {
        currentState = LListView.State_Normal;
        tvHint.setText(R.string.idling);
        animateToHeight(0);
    }

    protected void handleMotion(float dist) {
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

    protected void doneLoading() {
        loading2Normal();
    }

    protected void directlyStartLoading() {
        normal2Loading();
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

    private boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    protected int getState() {
        return currentState;
    }

    protected void setOnRefreshListener(LListView.OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    private void cancelPotentialHeightAnimator() {
        if (heightAnimator != null) {
            heightAnimator.cancel();
        }
    }

    private void animateToHeight(int height) {
        int duration = 300;
        cancelPotentialHeightAnimator();
        heightAnimator = ObjectAnimator.ofInt(this, "height", getHeight(), height);
        heightAnimator.setDuration(duration);
        heightAnimator.start();
    }

    public void setHeight(int height) {
        if (currentState == LListView.State_Loading_More && height < Loading_Height) {
            height = Loading_Height;
        }
        if (height <= 0) {
            setVisibility(View.GONE);
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = 1;
            setLayoutParams(params);
        } else {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = height;
            if (getVisibility() != View.VISIBLE) {
                setVisibility(View.VISIBLE);
            }
            setLayoutParams(params);
        }
    }

}
