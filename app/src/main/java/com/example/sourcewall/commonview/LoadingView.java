package com.example.sourcewall.commonview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.sourcewall.R;

/**
 * Created by NashLegend on 2014/12/26 0026
 */
public class LoadingView extends FrameLayout {
    ProgressBar progressBar;
    ImageButton loadingButton;
    ReloadListener reloadListener;

    public LoadingView(Context context) {
        super(context);
        initView(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_loading, this);
        progressBar = (ProgressBar) findViewById(R.id.progress_loading);
        loadingButton = (ImageButton) findViewById(R.id.button_reload);
        loadingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reloadListener != null) {
                    progressBar.setVisibility(VISIBLE);
                    loadingButton.setVisibility(GONE);
                    reloadListener.reload();
                }
            }
        });
    }

    public void setReloadListener(ReloadListener listener) {
        this.reloadListener = listener;
    }

    public void startLoading() {
        progressBar.setVisibility(VISIBLE);
        loadingButton.setVisibility(GONE);
        setVisibility(VISIBLE);
    }

    public void onLoadFailed() {
        progressBar.setVisibility(GONE);
        loadingButton.setVisibility(VISIBLE);
    }

    public void onLoadSuccess() {
        progressBar.setVisibility(VISIBLE);
        loadingButton.setVisibility(GONE);
        setVisibility(GONE);
    }

    public static interface ReloadListener {
        void reload();
    }
}
