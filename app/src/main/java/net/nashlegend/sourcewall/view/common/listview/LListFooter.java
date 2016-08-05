package net.nashlegend.sourcewall.view.common.listview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.util.DisplayUtil;

public class LListFooter extends FrameLayout {
    private int currentState = LListView.State_Normal;
    private int lastState = currentState;
    private LListView.OnRefreshListener onRefreshListener;
    private TextView tvHint;
    private LListView listView;
    private ObjectAnimator heightAnimator;
    private int Loading_Height_In_DP = 55;
    private int Release_Height_In_DP = 80;
    private int Loading_Height = 200;
    private int Release_Height = 300;
    private boolean layouted = false;

    public LListFooter(Context context, LListView listView) {
        super(context);
        this.listView = listView;
        Release_Height = (int) (DisplayUtil.getPixelDensity(context) * Release_Height_In_DP);
        Loading_Height = (int) (DisplayUtil.getPixelDensity(context) * Loading_Height_In_DP);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_footer_view, this);
        tvHint = (TextView) findViewById(R.id.text_footer_hint);
        setClickable(true);
    }

    protected boolean handleMoveDistance(float dist) {
        dist *= -1;
        if (dist < 0 && !isVisible()) {
            return false;
        }
        if (currentState != LListView.State_Loading_More) {
            // 或许可以做更多
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
        float rat = 1.5f;
        dist /= rat;
        handleMotion(dist);
        lastState = currentState;
        return true;
    }

    protected void handleUpOperation() {
        if (currentState == LListView.State_Release_To_Load_More) {
            // start loading more
            if (onRefreshListener != null) {
                release2Loading();
            } else {
                release2Normal();
            }
        } else if (currentState == LListView.State_Loading_More) {
            if (getActualHeight() > Release_Height) {
                loading2Loading();
            }
        } else {
            pull2Normal();
        }
        lastState = LListView.State_Normal;
    }

    private void setTopPadding() {
        int[] listPos = {0, 0};
        int[] footPos = {0, 0};
        listView.getLocationOnScreen(listPos);
        this.getLocationOnScreen(footPos);
        int mt = listPos[1] + listView.getHeight() - footPos[1] - 1;
        setPadding(0, mt, 0, 0);
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
        if (getHeight() < getPaddingTop()) {
            setHeight((int) (getHeight() + dist));
        } else {
            setHeight((int) (getHeight() + dist - getPaddingTop()));
        }
    }

    protected void doneLoading() {
        loading2Normal();
    }

    protected void directlyStartLoading() {
        normal2Loading();
    }

    private boolean isOverReleaseThreshold() {
        return getActualHeight() > Release_Height;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!layouted) {
            layouted = true;
            setHeight(0);
        }
    }

    private boolean isVisible() {
        return getVisibility() == View.VISIBLE || getHeight() < 1;
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
        heightAnimator = ObjectAnimator.ofInt(this, "height", getActualHeight(), height);
        heightAnimator.setDuration(duration);
        heightAnimator.start();
    }

    public int getActualHeight() {
        if (getHeight() < getPaddingTop()) {
            return getHeight();
        } else {
            return getHeight() - getPaddingTop();
        }
    }

    public void setHeight(int height) {
        if (currentState == LListView.State_Loading_More && height < Loading_Height) {
            height = Loading_Height;
        }
        if (height > 0) {
            height += getPaddingTop();
        }
        if (height < 1) {
            setVisibility(View.GONE);
            tvHint.setVisibility(GONE);
            ViewGroup.LayoutParams params = getLayoutParams();
            if (params != null) {
                params.height = 0;
                setLayoutParams(params);
            }
            setPadding(0, 0, 0, 0);
        } else {
            ViewGroup.LayoutParams params = getLayoutParams();
            if (params != null) {
                params.height = height;
                setLayoutParams(params);
            }
            if (getVisibility() != View.VISIBLE) {
                setVisibility(View.VISIBLE);
                tvHint.setVisibility(VISIBLE);
                setTopPadding();
            }
        }
    }

}
