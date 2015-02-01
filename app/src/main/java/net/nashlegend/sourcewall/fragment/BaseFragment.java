package net.nashlegend.sourcewall.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public abstract class BaseFragment extends Fragment {
    public View layoutView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (layoutView == null) {
            layoutView = onCreateLayoutView(inflater, container, savedInstanceState);
        } else {
            if (layoutView.getParent() != null) {
                ((ViewGroup) layoutView.getParent()).removeView(layoutView);
            }
            onCreateViewAgain(inflater, container, savedInstanceState);
        }
        return layoutView;
    }

    abstract public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    abstract public void onCreateViewAgain(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    abstract public void setTitle();


}
