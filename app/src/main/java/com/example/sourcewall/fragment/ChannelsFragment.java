package com.example.sourcewall.fragment;

import com.example.sourcewall.model.SubItem;

/**
 * Created by NashLegend on 2014/11/1 0001
 */
public abstract class ChannelsFragment extends BaseFragment {
    abstract public int getFragmentMenu();

    abstract public void resetData(SubItem subItem);

    abstract public void triggerRefresh();
}
