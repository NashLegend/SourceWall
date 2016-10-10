package net.nashlegend.sourcewall.view.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import net.nashlegend.sourcewall.R;

/**
 * Created by NashLegend on 2014/12/26 0026
 */
public class LoadingView extends FrameLayout {
    View loadingView;
    View reloadView;
    View emptyView;
    ReloadListener reloadListener;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        int layout = R.layout.layout_loading;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
            int indexCount = a.getIndexCount();
            for (int i = 0; i < indexCount; i++) {
                int index = a.getIndex(i);
                if (index == R.styleable.LoadingView_layout) {
                    layout = a.getResourceId(index, R.layout.layout_loading);
                }
            }
            a.recycle();
        }
        initView(layout);
    }

    private void initView(int id) {
        initLayout(id);
    }

    public void initLayout(@LayoutRes int id) {
        removeAllViews();
        View.inflate(getContext(), id, this);
        emptyView = findViewById(R.id.empty_view);
        loadingView = findViewById(R.id.loading_view);
        reloadView = findViewById(R.id.reload_view);
        reloadView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reloadListener != null) {
                    onLoading();
                    reloadListener.reload();
                }
            }
        });
    }

    public void setReloadListener(ReloadListener listener) {
        this.reloadListener = listener;
    }

    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }

    public void onLoading() {
        loadingView.setVisibility(VISIBLE);
        reloadView.setVisibility(GONE);
        emptyView.setVisibility(GONE);
        setVisibility(VISIBLE);
    }

    public void onSuccess() {
        loadingView.setVisibility(VISIBLE);
        reloadView.setVisibility(GONE);
        emptyView.setVisibility(GONE);
        setVisibility(GONE);
    }

    public void onFailed() {
        loadingView.setVisibility(GONE);
        emptyView.setVisibility(GONE);
        reloadView.setVisibility(VISIBLE);
    }

    public void onEmpty() {
        loadingView.setVisibility(GONE);
        reloadView.setVisibility(GONE);
        emptyView.setVisibility(VISIBLE);
        setVisibility(VISIBLE);
    }

    public interface ReloadListener {
        void reload();
    }
}