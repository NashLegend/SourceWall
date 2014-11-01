package com.example.outerspace.fragment;

import com.example.outerspace.model.SubItem;

/**
 * Created by NashLegend on 2014/11/1 0001
 */
public abstract class ChannelsFragment extends BaseFragment {
    abstract public void resetData(SubItem subItem);

    abstract public void triggerRefresh();
}
