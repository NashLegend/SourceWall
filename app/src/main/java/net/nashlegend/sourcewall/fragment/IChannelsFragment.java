package net.nashlegend.sourcewall.fragment;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.nashlegend.sourcewall.model.SubItem;

/**
 * Created by NashLegend on 16/5/5.
 */
public interface IChannelsFragment {
    /**
     * 返回所需的menu id
     *
     * @return
     */
    int getFragmentMenu();

    /**
     * 接管Activity的Menu生成
     *
     * @param inflater
     * @param menu
     */
    boolean takeOverMenuInflate(MenuInflater inflater, Menu menu);

    boolean takeOverOptionsItemSelect(MenuItem item);

    boolean takeOverBackPressed();

    void resetData(SubItem subItem);

    void triggerRefresh();

    void prepareLoading(SubItem subItem);

    void scrollToHead();
}
