package com.example.sourcewall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.sourcewall.model.UComment;
import com.example.sourcewall.view.SimpleCommentItemView;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class SimpleCommentAdapter extends AceAdapter<UComment> {
    public SimpleCommentAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new SimpleCommentItemView(getContext());
        }
        ((SimpleCommentItemView) convertView).setData(list.get(position));
        return convertView;
    }
}
