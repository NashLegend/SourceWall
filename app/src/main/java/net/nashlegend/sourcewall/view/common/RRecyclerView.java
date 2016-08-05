package net.nashlegend.sourcewall.view.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by NashLegend on 16/8/4.
 */

public class RRecyclerView extends RecyclerView {

    LinearLayoutManager layoutManager;
    private boolean shouldLoadMore = false;//是否具备加载更多的条件
    private boolean canLoadMore = false;//是否可以加载更多
    private boolean isLoading = false;//是否正在加载

    public RRecyclerView(Context context) {
        this(context, null);
    }

    public RRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        addItemDecoration(new ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, State state) {
                super.onDrawOver(c, parent, state);
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
                super.getItemOffsets(outRect, view, parent, state);
            }
        });
        layoutManager = new LinearLayoutManager(getContext());
        setHasFixedSize(true);
        setItemAnimator(new DefaultItemAnimator());
        setLayoutManager(layoutManager);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }
        });
    }

    private boolean isShouldLoadMore() {
        return !isLoading
                && layoutManager.findLastVisibleItemPosition() >= getAdapter().getItemCount() - 2;
    }
}
