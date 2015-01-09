package com.example.sourcewall.fragment;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.sourcewall.model.SubItem;

/**
 * Created by NashLegend on 2014/11/1 0001
 */
public abstract class ChannelsFragment extends BaseFragment {
    /**
     * 返回所需的menu id
     *
     * @return
     */
    abstract public int getFragmentMenu();

    /**
     * 接管Activity的Menu生成
     *
     * @param inflater
     * @param menu
     */
    abstract public void takeOverMenuInflate(MenuInflater inflater, Menu menu);

    abstract public boolean takeOverOptionsItemSelect(MenuItem item);

    abstract public void resetData(SubItem subItem);

    abstract public void triggerRefresh();
}
