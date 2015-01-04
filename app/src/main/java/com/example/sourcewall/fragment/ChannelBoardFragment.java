package com.example.sourcewall.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by NashLegend on 2014/9/18 0018.
 */
public class ChannelBoardFragment extends BaseFragment {
    OnChannelSelectedListener onChannelSelectedListener;

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onCreateViewAgain(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    }

    @Override
    public void setTitle() {

    }

    public void setOnChannelSelectedListener(OnChannelSelectedListener onChannelSelectedListener) {
        this.onChannelSelectedListener = onChannelSelectedListener;
    }

    public static interface OnChannelSelectedListener {
        void onChannelSelected(String key);

        void onSubjectSelected(String key);
    }
}
