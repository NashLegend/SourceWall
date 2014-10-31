package com.example.outerspace.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.example.outerspace.model.SubItem;
import com.example.outerspace.view.GroupItemView;
import com.example.outerspace.view.SubItemView;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/10/31 0031
 */
public class ChannelsAdapter extends BaseExpandableListAdapter {

    private ArrayList<SubItem> groupList = new ArrayList<SubItem>();
    private ArrayList<ArrayList<SubItem>> subLists = new ArrayList<ArrayList<SubItem>>();
    private Context mContext;

    public ChannelsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return subLists.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return subLists.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupList.get(groupPosition).getName().hashCode();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (groupList.get(groupPosition).getName() + "_/_" + subLists.get(groupPosition).get(childPosition).getName()).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new GroupItemView(mContext);
        }
        ((GroupItemView) convertView).setData(groupList.get(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new SubItemView(mContext);
        }
        ((SubItemView) convertView).setData(subLists.get(groupPosition).get(childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
