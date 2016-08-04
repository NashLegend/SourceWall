package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.view.SimpleCommentItemView;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class SimpleCommentAdapter extends AceAdapter<UComment> {
    public SimpleCommentAdapter(Context context) {
        super(context);
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
